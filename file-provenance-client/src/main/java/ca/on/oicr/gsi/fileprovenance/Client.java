package ca.on.oicr.gsi.fileprovenance;


import ca.on.oicr.gsi.provenance.AnalysisProvenanceProvider;
import ca.on.oicr.gsi.provenance.DefaultProvenanceClient;
import ca.on.oicr.gsi.provenance.SeqwareMetadataAnalysisProvenanceProvider;
import ca.on.oicr.gsi.provenance.SeqwareMetadataLimsMetadataProvenanceProvider;
import ca.on.oicr.gsi.provenance.model.FileProvenance;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.metadata.MetadataFactory;
import net.sourceforge.seqware.common.model.FileProvenanceParam;
import net.sourceforge.seqware.common.util.configtools.ConfigTools;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;

/**
 *
 * @author mlaszloffy
 */
public class Client {

    private Map<String, String> config;
    private Metadata metadata;
    private DefaultProvenanceClient dpc;

    public Client() {
        setupConfig();
        setupMetadata();
        AnalysisProvenanceProvider analysisProvenanceProvider = new SeqwareMetadataAnalysisProvenanceProvider(metadata);
        SeqwareMetadataLimsMetadataProvenanceProvider seqwareLimsMetadataProvider = new SeqwareMetadataLimsMetadataProvenanceProvider(metadata);
        dpc = new DefaultProvenanceClient(analysisProvenanceProvider, seqwareLimsMetadataProvider, seqwareLimsMetadataProvider);
    }

    private class StringSanitizerBuilder {

        private final List<String> searchList;
        private final List<String> replacementList;

        public StringSanitizerBuilder() {
            searchList = new ArrayList<>();
            replacementList = new ArrayList<>();
        }

        public void add(String searchString, String replacementString) {
            searchList.add(searchString);
            replacementList.add(replacementString);
        }

        public Function<String, String> build() {
            final String[] searchArr = searchList.toArray(new String[0]);
            final String[] replacementArr = replacementList.toArray(new String[0]);
            return new Function<String, String>() {
                @Override
                public String apply(String s) {
                    return StringUtils.replaceEach(s, searchArr, replacementArr);
                }
            };
        }
    }

    private class FunctionBuilder {

        Function<String, String> transformer;

        public FunctionBuilder(Function<String, String> transformer) {
            this.transformer = transformer;
        }

        public Function getFunction() {
            return new Function<Map.Entry<String, Set<String>>, String>() {
                @Override
                public String apply(Map.Entry<String, Set<String>> s) {
                    String key = transformer.apply(s.getKey());
                    return key + "=" + Joiner.on("&").join(Iterables.transform(s.getValue(), transformer));
                }
            };
        }
    }

    public void getFileProvenanceReport(String outputFilePath, Map<String, Set<String>> filters) throws IOException {
        System.out.println("starting");

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
                        "Path Skip", "Skip"
                );
        CSVPrinter cp;
        Stopwatch sw;
        try (BufferedWriter fw = Files.newBufferedWriter(Paths.get(outputFilePath), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
            cp = new CSVPrinter(fw, cf);
            //
            sw = Stopwatch.createStarted();
            Collection<FileProvenance> fps = dpc.getFileProvenance(filters);
            System.out.println(fps.size() + " file provenance records retrieved in " + sw.stop());
            //
            Joiner jjj = Joiner.on(delimiter).skipNulls();
            sw = Stopwatch.createStarted();
            for (FileProvenance fp : fps) {
                List cs = new ArrayList<>();

                cs.add(fp.getLastModified().withZone(DateTimeZone.forID("EST")).toString("YYYY-MM-dd HH:mm:ss.SSS"));

                cs.add(jjj.join(Iterables.transform(fp.getStudyTitles(), stringSanitizer)));
                cs.add(nullString); //study swids not available
                cs.add(jjj.join(Iterables.transform(fp.getStudyAttributes().entrySet(), mapOfSetsToString)));

                cs.add(jjj.join(Iterables.transform(fp.getExperimentNames(), stringSanitizer)));
                cs.add(nullString); //experiment swids not available
                cs.add(jjj.join(Iterables.transform(fp.getExperimentAttributes().entrySet(), mapOfSetsToString)));

                cs.add(jjj.join(Iterables.transform(fp.getRootSampleNames(), stringSanitizer)));
                cs.add(nullString); //root sample swids not available

                cs.add(jjj.join(Iterables.transform(fp.getParentSampleNames(), stringSanitizer)));
                cs.add(nullString); //parent sample swids not available
                cs.add(jjj.join(fp.getParentSampleOrganismIDs()));
                cs.add(jjj.join(Iterables.transform(fp.getParentSampleAttributes().entrySet(), mapOfSetsToString)));

                cs.add(jjj.join(Iterables.transform(fp.getSampleNames(), stringSanitizer)));
                cs.add(nullString); //sample swids not available
                cs.add(jjj.join(Iterables.transform(fp.getSampleOrganismIDs(), stringSanitizer)));
                cs.add(jjj.join(Iterables.transform(fp.getSampleOrganismCodes(), stringSanitizer)));
                cs.add(jjj.join(Iterables.transform(fp.getSampleAttributes().entrySet(), mapOfSetsToString)));

                cs.add(jjj.join(Iterables.transform(fp.getSequencerRunNames(), stringSanitizer)));
                cs.add(nullString); //sequencer run swids not available
                cs.add(jjj.join(Iterables.transform(fp.getSequencerRunAttributes().entrySet(), mapOfSetsToString)));
                cs.add(jjj.join(Iterables.transform(fp.getSequencerRunPlatformIDs(), stringSanitizer)));
                cs.add(jjj.join(Iterables.transform(fp.getSequencerRunPlatformNames(), stringSanitizer)));

                cs.add(jjj.join(Iterables.transform(fp.getLaneNames(), stringSanitizer)));
                cs.add(jjj.join(Iterables.transform(fp.getLaneNumbers(), stringSanitizer)));
                cs.add(nullString); //lane swids not available
                cs.add(jjj.join(Iterables.transform(fp.getLaneAttributes().entrySet(), mapOfSetsToString)));

                cs.add(jjj.join(Iterables.transform(fp.getIusTags(), stringSanitizer)));
                cs.add(jjj.join(Iterables.transform(fp.getIusSWIDs(), stringSanitizer)));
                cs.add(jjj.join(Iterables.transform(fp.getIusAttributes().entrySet(), mapOfSetsToString)));

                cs.add(stringSanitizer.apply(fp.getWorkflowName()));
                cs.add(stringSanitizer.apply(fp.getWorkflowVersion()));
                cs.add(fp.getWorkflowSWID());
                cs.add(jjj.join(Iterables.transform(fp.getWorkflowAttributes().entrySet(), mapOfSetsToString)));

                cs.add(stringSanitizer.apply(fp.getWorkflowRunName()));
                cs.add(stringSanitizer.apply(fp.getWorkflowRunStatus()));
                cs.add(fp.getWorkflowRunSWID());
                cs.add(jjj.join(Iterables.transform(fp.getWorkflowRunAttributes().entrySet(), mapOfSetsToString)));

                cs.add(jjj.join(fp.getWorkflowRunInputFileSWIDs()));

                cs.add(stringSanitizer.apply(fp.getProcessingAlgorithm()));
                cs.add(fp.getProcessingSWID());
                cs.add(jjj.join(Iterables.transform(fp.getProcessingAttributes().entrySet(), mapOfSetsToString)));
                cs.add(stringSanitizer.apply(fp.getProcessingStatus()));

                cs.add(stringSanitizer.apply(fp.getFileMetaType()));
                cs.add(fp.getFileSWID());
                cs.add(jjj.join(Iterables.transform(fp.getFileAttributes().entrySet(), mapOfSetsToString)));
                cs.add(stringSanitizer.apply(fp.getFilePath()));
                cs.add(stringSanitizer.apply(fp.getFileMd5sum()));
                cs.add(stringSanitizer.apply(fp.getFileSize()));
                cs.add(stringSanitizer.apply(fp.getFileDescription()));

                cs.add(stringSanitizer.apply(fp.getSkip())); //path skip
                cs.add(stringSanitizer.apply(fp.getSkip()));

                cp.printRecord(cs);
            }
            fw.flush();
        }
        cp.close();
        System.out.println("File provenance report formatted and writen to tsv in " + sw.stop());
    }

    private void setupConfig() {
        if (this.config != null) {
            return;
        }
        try {
            this.config = ConfigTools.getSettings();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Set<String>> getDefaultFilters() {
        Map<String, Set<String>> filters = new HashMap<>();
        filters.put(FileProvenanceParam.processing_status.toString(), Sets.newHashSet("success"));
        filters.put(FileProvenanceParam.workflow_run_status.toString(), Sets.newHashSet("completed"));
        filters.put(FileProvenanceParam.skip.toString(), Sets.newHashSet("false"));
        return filters;
    }

//    public void setConfig(Map<String, String> config) {
//        this.config = config;
//    }
    private void setupMetadata() {
        this.metadata = MetadataFactory.get(config);
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.getFileProvenanceReport("/tmp/fpr.tsv", Client.getDefaultFilters());
    }

}
