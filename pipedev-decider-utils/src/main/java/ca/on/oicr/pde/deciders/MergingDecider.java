package ca.on.oicr.pde.deciders;

import ca.on.oicr.gsi.provenance.ExtendedProvenanceClient;
import ca.on.oicr.pde.deciders.GroupableFileFactory.GroupableFile;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.seqware.common.hibernate.FindAllTheFiles;
import net.sourceforge.seqware.common.module.FileMetadata;
import net.sourceforge.seqware.common.module.ReturnValue;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author mlaszloffy
 */
public abstract class MergingDecider extends OicrDecider {

    public final Logger log;
    public final GroupableFileFactory groupableFileFactory;
    public final Map<String, GroupableFileFactory.GroupableFile> fileSwaToFile = new HashMap<>();

    public MergingDecider(Logger logger) {
        this.log = logger;
        this.groupableFileFactory = new GroupableFileFactory();
    }

    public MergingDecider(Logger logger, ExtendedProvenanceClient provenanceClient) {
        this(logger);
        this.provenanceClient = provenanceClient;
    }
    
    /**
     *
     * @param fileAttributes
     * @return
     */
    protected abstract boolean checkFilePassesFilterBeforeGrouping(FileAttributes fileAttributes);

    /**
     * This method is extended in the GATK decider so that only the most recent
     * file for each sequencer run, lane, barcode and filetype is kept.
     *
     * @param vals
     * @param groupBy
     *
     * @return candidate groups of files to scheduled workflow runs on
     */
    @Override
    public Map<String, List<ReturnValue>> separateFiles(List<ReturnValue> vals, String groupBy) {
        Map<String, ReturnValue> iusDeetsToRV = new HashMap<>();

        //Iterate through the potential files
        for (ReturnValue currentRV : vals) {

            FileAttributes fileAttributes = new FileAttributes(currentRV, Iterables.getOnlyElement(currentRV.getFiles()));
            if (!checkFilePassesFilterBeforeGrouping(fileAttributes)) {
                continue;
            }

            GroupableFile currentFile = groupableFileFactory.getGroupableFile(currentRV);
            fileSwaToFile.put(currentRV.getAttribute(FindAllTheFiles.Header.FILE_SWA.getTitle()), currentFile);

            //make sure you only have the most recent single file for each
            //sequencer run + lane + barcode + meta-type
            String fileDeets = currentFile.getIusDetails();
            Date currentDate = currentFile.getDate();

            //if there is no entry yet, add it
            if (iusDeetsToRV.get(fileDeets) == null) {
                log.debug("Adding file " + fileDeets + " -> \n\t" + currentFile.getPath());
                iusDeetsToRV.put(fileDeets, currentRV);
            } //if there is an entry, compare the current value to the 'old' one in
            //the groupedFiles. if the current date is newer than the 'old' date, replace
            //it in the groupedFiles
            else {
                ReturnValue oldRV = iusDeetsToRV.get(fileDeets);
                GroupableFile oldFile = fileSwaToFile.get(oldRV.getAttribute(FindAllTheFiles.Header.FILE_SWA.getTitle()));
                Date oldDate = oldFile.getDate();
                if (currentDate.after(oldDate)) {
                    log.debug("Adding file " + fileDeets + " -> \n\t" + currentFile.getDate()
                            + "\n\t instead of file "
                            + "\n\t" + oldFile.getDate());
                    iusDeetsToRV.put(fileDeets, currentRV);
                } else {
                    log.debug("Disregarding file " + fileDeets + " -> \n\t" + currentFile.getDate()
                            + "\n\tas older than duplicate sequencer run/lane/barcode in favour of "
                            + "\n\t" + oldFile.getDate());
                    log.debug(currentDate + " is before " + oldDate);
                }
            }
        }

        //only use those files that entered into the iusDeetsToRV
        //since it's a map, only the most recent values
        Map<String, List<ReturnValue>> groupedFiles;
        if (options.hasArgument("group-by") && getHeadersToGroupBy() != null) {
            groupedFiles = super.separateFiles(new ArrayList<>(iusDeetsToRV.values()), getHeadersToGroupBy());
        } else {
            //use the default grouping
            ListMultimap<String, ReturnValue> hm = ArrayListMultimap.create();
            for (ReturnValue rv : iusDeetsToRV.values()) {
                hm.put(fileSwaToFile.get(rv.getAttribute(FindAllTheFiles.Header.FILE_SWA.getTitle())).getGroupByAttribute(), rv);
            }
            groupedFiles = Multimaps.asMap(hm);
        }

        if (options.has("verbose")) {
            for (Map.Entry<String, List<ReturnValue>> e : groupedFiles.entrySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append("group = ");
                sb.append(e.getKey());
                sb.append("\n");

                List<String> fileInfos = new ArrayList<>();
                for (ReturnValue rv : e.getValue()) {
                    StringBuilder fileInfo = new StringBuilder();
                    fileInfo.append(fileSwaToFile.get(rv.getAttribute(FindAllTheFiles.Header.FILE_SWA.getTitle())).getGroupByAttribute());
                    fileInfo.append(" -> ");
                    fileInfo.append(Iterables.getOnlyElement(rv.getFiles()).getFilePath());
                    fileInfos.add(fileInfo.toString());
                }
                sb.append(Joiner.on("\n").join(fileInfos));
                log.debug(sb.toString());
            }
        }

        int groupedFilesCount = 0;
        for (List<ReturnValue> rvs : groupedFiles.values()) {
            groupedFilesCount += rvs.size();
        }

        log.info("Grouping summary: group by = [{}], input file count = [{}], group count = [{}], grouped file count = [{}]",
                groupBy, vals.size(), groupedFiles.size(), groupedFilesCount);

        return groupedFiles;
    }

    /**
     *
     * @param fileAttributes
     * @return
     */
    protected abstract boolean checkFilePassesFilterAfterGrouping(FileAttributes fileAttributes);

    @Override
    protected final boolean checkFileDetails(ReturnValue returnValue, FileMetadata fm) {
        return super.checkFileDetails(returnValue, fm);
    }

    @Override
    protected final boolean checkFileDetails(FileAttributes attributes) {
        return checkFilePassesFilterAfterGrouping(attributes);
    }

    /**
     *
     * @param run
     * @return
     */
    protected abstract ReturnValue customizeWorkflowRun(WorkflowRun run);

    @Override
    public final ReturnValue customizeRun(WorkflowRun run) {
        return new ReturnValue();
    }

    @Override
    public final ReturnValue doFinalCheck(String commaSeparatedFilePaths, String commaSeparatedParentAccessions) {
        ReturnValue r;

        r = super.doFinalCheck(commaSeparatedFilePaths, commaSeparatedParentAccessions);
        if (r.getExitStatus() != ReturnValue.SUCCESS) {
            return r;
        }

        //modifies currentWorkflowRun + calls BasicDecider modifyIniFile
        super.modifyIniFile(commaSeparatedFilePaths, commaSeparatedParentAccessions);

        //allow implementing decider opportunity to customize the workflow run
        if (options.has("verbose")) {
            Set<String> t = new HashSet<>();
            for (FileAttributes fa : currentWorkflowRun.getFiles()) {
                GroupableFileFactory.GroupableFile file = fileSwaToFile.get(fa.getOtherAttribute(FindAllTheFiles.Header.FILE_SWA.getTitle()));
                t.add(file.getGroupByAttribute());
            }
            log.debug("Workflow run for group = [" + Iterables.getOnlyElement(t));
        }
        r = customizeWorkflowRun(currentWorkflowRun);

        return r;
    }

    @Override
    protected final Map<String, String> modifyIniFile(String commaSeparatedFilePaths, String commaSeparatedParentAccessions) {
        return currentWorkflowRun.getIniFile();
    }

}
