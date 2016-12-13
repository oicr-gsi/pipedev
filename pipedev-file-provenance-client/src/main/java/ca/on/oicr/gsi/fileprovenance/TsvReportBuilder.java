package ca.on.oicr.gsi.fileprovenance;

import ca.on.oicr.gsi.common.transformation.FunctionBuilder;
import ca.on.oicr.gsi.common.transformation.StringSanitizerBuilder;
import ca.on.oicr.gsi.provenance.model.FileProvenance;
import ca.on.oicr.gsi.provenance.model.IusLimsKey;
import ca.on.oicr.gsi.provenance.model.LimsKey;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.joda.time.DateTimeZone;

/**
 *
 * @author mlaszloffy
 */
public class TsvReportBuilder implements ReportBuilder {

    @Override
    public void writeReport(Collection<FileProvenance> fps, Path outputFilePath) throws IOException {
        StringSanitizerBuilder ssbForFields = new StringSanitizerBuilder();
        ssbForFields.add("\t", "\u2300");
        ssbForFields.add(";", "\u2300");
        ssbForFields.add("=", "\u2300");
        ssbForFields.add("&", "\u2300");
        ssbForFields.add(" ", "_");
        Function<String, String> stringSanitizer = ssbForFields.build();

        StringSanitizerBuilder ssbForAttributes = new StringSanitizerBuilder();
        ssbForAttributes.add("\t", " ");
        ssbForAttributes.add(";", "\u2300");
        ssbForAttributes.add("=", "\u2300");
        ssbForAttributes.add("&", "\u2300");
        FunctionBuilder fb = new FunctionBuilder(ssbForAttributes.build());
        Function mapOfSetsToString = fb.getFunction();

        String nullString = " ";
        Character delimiter = ';';

        CSVFormat cf = CSVFormat.newFormat('\t')
                .withNullString(nullString)
                .withRecordSeparator('\n')
                .withHeader("Last Modified",
                        "Study Title", "Study SWID", "Study Attributes",
                        "Experiment Name", "Experiment SWID", "Experiment Attributes",
                        "Root Sample Name", "Root Sample SWID",
                        "Parent Sample Name", "Parent Sample SWID", "Parent Sample Organism IDs", "Parent Sample Attributes",
                        "Sample Name", "Sample SWID", "Sample Organism ID", "Sample Organism Code", "Sample Attributes",
                        "Sequencer Run Name", "Sequencer Run SWID", "Sequencer Run Attributes", "Sequencer Run Platform ID", "Sequencer Run Platform Name",
                        "Lane Name", "Lane Number", "Lane SWID", "Lane Attributes",
                        "IUS Tag", "IUS SWID", "IUS Attributes",
                        "Workflow Name", "Workflow Version", "Workflow SWID", "Workflow Attributes",
                        "Workflow Run Name", "Workflow Run Status", "Workflow Run SWID", "Workflow Run Attributes",
                        "Workflow Run Input File SWAs",
                        "Processing Algorithm", "Processing SWID", "Processing Attributes", "Processing Status",
                        "File Meta-Type", "File SWID", "File Attributes", "File Path", "File Md5sum", "File Size", "File Description",
                        "Path Skip", "Skip",
                        "Status", "Status Reason",
                        "LIMS IUS SWID", "LIMS Provider", "LIMS ID", "LIMS Version", "LIMS Last Modified"
                );

        try (BufferedWriter fw = Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW); 
                CSVPrinter cp = new CSVPrinter(fw, cf)) {
            Joiner j = Joiner.on(delimiter).skipNulls();
            for (FileProvenance fp : fps) {

                List cs = new ArrayList<>();

                cs.add(fp.getLastModified().withZone(DateTimeZone.forID("America/Toronto")).toString("YYYY-MM-dd HH:mm:ss.SSS"));

                cs.add(j.join(Iterables.transform(fp.getStudyTitles(), stringSanitizer)));
                cs.add(nullString); //study swids not available
                cs.add(j.join(Iterables.transform(fp.getStudyAttributes().entrySet(), mapOfSetsToString)));

                cs.add(nullString);
                cs.add(nullString); //experiment swids not available
                cs.add(nullString);

                cs.add(j.join(Iterables.transform(fp.getRootSampleNames(), stringSanitizer)));
                cs.add(nullString); //root sample swids not available

                cs.add(j.join(Iterables.transform(fp.getParentSampleNames(), stringSanitizer)));
                cs.add(nullString); //parent sample swids not available
                cs.add(j.join(fp.getParentSampleOrganismIDs()));
                cs.add(j.join(Iterables.transform(fp.getParentSampleAttributes().entrySet(), mapOfSetsToString)));

                cs.add(j.join(Iterables.transform(fp.getSampleNames(), stringSanitizer)));
                cs.add(nullString); //sample swids not available
                cs.add(j.join(Iterables.transform(fp.getSampleOrganismIDs(), stringSanitizer)));
                cs.add(j.join(Iterables.transform(fp.getSampleOrganismCodes(), stringSanitizer)));
                cs.add(j.join(Iterables.transform(fp.getSampleAttributes().entrySet(), mapOfSetsToString)));

                cs.add(j.join(Iterables.transform(fp.getSequencerRunNames(), stringSanitizer)));
                cs.add(nullString); //sequencer run swids not available
                cs.add(j.join(Iterables.transform(fp.getSequencerRunAttributes().entrySet(), mapOfSetsToString)));
                cs.add(j.join(Iterables.transform(fp.getSequencerRunPlatformIDs(), stringSanitizer)));
                cs.add(j.join(Iterables.transform(fp.getSequencerRunPlatformNames(), stringSanitizer)));

                cs.add(j.join(Iterables.transform(fp.getLaneNames(), stringSanitizer)));
                cs.add(j.join(Iterables.transform(fp.getLaneNumbers(), stringSanitizer)));
                cs.add(nullString); //lane swids not available
                cs.add(j.join(Iterables.transform(fp.getLaneAttributes().entrySet(), mapOfSetsToString)));

                cs.add(j.join(Iterables.transform(fp.getIusTags(), stringSanitizer)));
                cs.add(j.join(Iterables.transform(fp.getIusSWIDs(), stringSanitizer)));
                cs.add(j.join(Iterables.transform(fp.getIusAttributes().entrySet(), mapOfSetsToString)));

                cs.add(stringSanitizer.apply(fp.getWorkflowName()));
                cs.add(stringSanitizer.apply(fp.getWorkflowVersion()));
                cs.add(fp.getWorkflowSWID());
                cs.add(j.join(Iterables.transform(fp.getWorkflowAttributes().entrySet(), mapOfSetsToString)));

                cs.add(stringSanitizer.apply(fp.getWorkflowRunName()));
                cs.add(stringSanitizer.apply(fp.getWorkflowRunStatus()));
                cs.add(fp.getWorkflowRunSWID());
                cs.add(j.join(Iterables.transform(fp.getWorkflowRunAttributes().entrySet(), mapOfSetsToString)));

                cs.add(j.join(fp.getWorkflowRunInputFileSWIDs()));

                cs.add(stringSanitizer.apply(fp.getProcessingAlgorithm()));
                cs.add(fp.getProcessingSWID());
                cs.add(j.join(Iterables.transform(fp.getProcessingAttributes().entrySet(), mapOfSetsToString)));
                cs.add(stringSanitizer.apply(fp.getProcessingStatus()));

                cs.add(stringSanitizer.apply(fp.getFileMetaType()));
                cs.add(fp.getFileSWID());
                cs.add(j.join(Iterables.transform(fp.getFileAttributes().entrySet(), mapOfSetsToString)));
                cs.add(stringSanitizer.apply(fp.getFilePath()));
                cs.add(stringSanitizer.apply(fp.getFileMd5sum()));
                cs.add(stringSanitizer.apply(fp.getFileSize()));
                cs.add(stringSanitizer.apply(fp.getFileDescription()));

                cs.add(stringSanitizer.apply(fp.getSkip())); //path skip
                cs.add(stringSanitizer.apply(fp.getSkip()));

                cs.add(fp.getStatus().toString());
                cs.add(fp.getStatusReason());

                IusLimsKey ilk = Iterables.getOnlyElement(fp.getIusLimsKeys());
                LimsKey lk = ilk.getLimsKey();
                cs.add(ilk.getIusSWID().toString());
                cs.add(lk.getProvider());
                cs.add(lk.getId());
                cs.add(lk.getVersion());
                cs.add(lk.getLastModified().toString());

                cp.printRecord(cs);
            }
            fw.flush();
        }
    }

}
