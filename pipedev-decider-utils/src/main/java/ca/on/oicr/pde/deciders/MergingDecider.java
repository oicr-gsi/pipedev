package ca.on.oicr.pde.deciders;

import ca.on.oicr.gsi.provenance.ExtendedProvenanceClient;
import ca.on.oicr.pde.deciders.GroupableFileFactory.GroupableFile;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.sourceforge.seqware.common.hibernate.FindAllTheFiles;
import net.sourceforge.seqware.common.module.FileMetadata;
import net.sourceforge.seqware.common.module.ReturnValue;
import org.apache.logging.log4j.Logger;

/**
 * Base decider logic for grouping files together and scheduling merging workflow runs.
 * <p>
 * The decider uses the default grouping key of:
 * DONOR + TISSUE_ORIGIN + LIBRARY_TEMPLATE_TYPE + WORKFLOW_NAME + TISSUE_TYPE + TISSUE_PREP + TISSUE_REGION + GROUP_ID + TARGETED_RESEQUENCING
 * <p>
 * Grouping behaviour can be modified by setting "group-by" parameter.
 *
 * @author mlaszloffy
 */
public abstract class MergingDecider extends OicrDecider {

    public final Logger log;
    public final GroupableFileFactory groupableFileFactory;
    public final Map<String, GroupableFile> fileSwaToFile = new HashMap<>();
    public final Map<String, String> fileSwaToGroupName = new HashMap<>();

    public MergingDecider(Logger logger) {
        this.log = logger;
        this.groupableFileFactory = new GroupableFileFactory();
    }

    public MergingDecider(Logger logger, ExtendedProvenanceClient provenanceClient) {
        this(logger);
        this.provenanceClient = provenanceClient;
    }

    /**
     * Before grouping, check if the file should be included or excluded.
     *
     * @param fileAttributes the file attributes for the current ungrouped file
     *
     * @return true if the file should be included (and grouped), false if the file should be excluded
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

        //create a map of file swid to group name
        for (Entry<String, List<ReturnValue>> e : groupedFiles.entrySet()) {
            String groupName = e.getKey();
            for (ReturnValue rv : e.getValue()) {
                String fileSwa = rv.getAttribute(FindAllTheFiles.Header.FILE_SWA.getTitle());
                String previousGroupName = fileSwaToGroupName.put(fileSwa, groupName);
                if (previousGroupName != null && !previousGroupName.equals(groupName)) {
                    throw new UnsupportedOperationException(
                            MessageFormat.format("File with SWID = [{0}] belongs to multiple groups = [previous = {1}, new = {2}].",
                                    fileSwa, previousGroupName, groupName));
                }
            }
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
     * After grouping files, check if files within the group are valid.
     * <p>
     * If any file within a group is not valid, the whole group is invalid.
     *
     * @param fileAttributes the file attributes for the current grouped file
     *
     * @return true if the file is valid, false if the file and the group is invalid
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
     * Validate and modify workflow run parameters/ini.
     * <p>
     * The ReturnValue is used to signal if the workflow run is valid or not.
     * <p>
     * NOTE:
     * <p>
     * This method is called BEFORE getSwidsToLinkWorkflowRunTo(). If modification of ini properties that rely or modify swids to link
     * workflow run needs to be done, implement the customizeWorkflowRunAfterCreatingSwidsToLinkWorkflowRunTo().
     *
     * @param run the workflow run object that will be used to create a workflow run
     *
     * @return a ReturnValue indicating SUCCESS if the workflow run is valid or anything other than SUCCESS if invalid
     */
    protected abstract ReturnValue customizeWorkflowRun(WorkflowRun run);

    /**
     * Customize the workflow run parameter/ini after creating swids to link the workflow run to.
     * <p>
     * If an ini property depends on swids to link the workflow run to, this method needs to be implemented.
     *
     * @param run the workflow run object with swids to link the workflow run to
     *
     * @return a ReturnValue indicating SUCCESS if the workflow run customization was valid or anything other than SUCCESS if not
     */
    protected ReturnValue customizeWorkflowRunAfterCreatingSwidsToLinkWorkflowRunTo(WorkflowRun run) {
        return new ReturnValue();
    }

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

        //use the group name as the workflow run's descriptive name
        Set<String> t = new HashSet<>();
        for (FileAttributes fa : currentWorkflowRun.getFiles()) {
            t.add(fileSwaToGroupName.get(fa.getOtherAttribute(FindAllTheFiles.Header.FILE_SWA.getTitle())));
        }
        currentWorkflowRun.setName(Iterables.getOnlyElement(t));

        if (options.has("verbose")) {
            log.debug("Workflow run for group = " + currentWorkflowRun.getName());
        }

        //call to subclass to customize the workflow run
        r = customizeWorkflowRun(currentWorkflowRun);

        return r;
    }

    @Override
    protected final Map<String, String> modifyIniFile(String commaSeparatedFilePaths, String commaSeparatedParentAccessions) {

        //call to subclass to customize the workflow run
        ReturnValue r = customizeWorkflowRunAfterCreatingSwidsToLinkWorkflowRunTo(currentWorkflowRun);

        if (r.getExitStatus() != ReturnValue.SUCCESS) {
            log.error("Failed to generate a valid workflow run for = " + currentWorkflowRun.getName());
            abortSchedulingOfCurrentWorkflowRun();
        }

        return currentWorkflowRun.getIniFile();
    }

}
