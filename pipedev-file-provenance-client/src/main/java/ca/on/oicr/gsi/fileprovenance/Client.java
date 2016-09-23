package ca.on.oicr.gsi.fileprovenance;

import ca.on.oicr.gsi.common.transformation.FunctionBuilder;
import ca.on.oicr.gsi.common.transformation.StringSanitizerBuilder;
import ca.on.oicr.gsi.provenance.AnalysisProvenanceProvider;
import ca.on.oicr.gsi.provenance.DefaultProvenanceClient;
import ca.on.oicr.gsi.provenance.LaneProvenanceProvider;
import ca.on.oicr.gsi.provenance.MultiThreadedDefaultProvenanceClient;
import ca.on.oicr.gsi.provenance.ProviderLoader;
import ca.on.oicr.gsi.provenance.SampleProvenanceProvider;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import ca.on.oicr.gsi.provenance.model.FileProvenanceParam;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.joda.time.DateTimeZone;

/**
 *
 * @author mlaszloffy
 */
public class Client {

    private final DefaultProvenanceClient dpc;
    private final Logger log = LogManager.getLogger(Client.class);

    public Client(String providerSettings) {
        ProviderLoader providerLoader;
        try {
            providerLoader = new ProviderLoader(providerSettings);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        dpc = new MultiThreadedDefaultProvenanceClient();
        for (Entry<String, AnalysisProvenanceProvider> e : providerLoader.getAnalysisProvenanceProviders().entrySet()) {
            dpc.registerAnalysisProvenanceProvider(e.getKey(), e.getValue());
        }
        for (Entry<String, LaneProvenanceProvider> e : providerLoader.getLaneProvenanceProviders().entrySet()) {
            dpc.registerLaneProvenanceProvider(e.getKey(), e.getValue());
        }
        for (Entry<String, SampleProvenanceProvider> e : providerLoader.getSampleProvenanceProviders().entrySet()) {
            dpc.registerSampleProvenanceProvider(e.getKey(), e.getValue());
        }
    }

    public void getFileProvenanceReport(String outputFilePath, Map<String, Set<String>> filters) throws IOException {
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
                        "Status", "Status Reason"
                );
        CSVPrinter cp;
        Stopwatch sw = Stopwatch.createStarted();
        log.info("Starting download of file provenance");
        Collection<FileProvenance> fps = dpc.getFileProvenance(filters);
        log.info("Completed download of " + fps.size() + " file provenance records in " + sw.stop());

        try (BufferedWriter fw = Files.newBufferedWriter(Paths.get(outputFilePath), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
            cp = new CSVPrinter(fw, cf);
            Joiner j = Joiner.on(delimiter).skipNulls();
            sw = Stopwatch.createStarted();
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

                cp.printRecord(cs);
            }
            fw.flush();
        }
        cp.close();
        log.info("File provenance report formatted and written to tsv in " + sw.stop());
    }

    public static Map<String, Set<String>> getDefaultFilters() {
        Map<String, Set<String>> filters = new HashMap<>();
        filters.put(FileProvenanceParam.processing_status.toString(), Sets.newHashSet("success"));
        filters.put(FileProvenanceParam.workflow_run_status.toString(), Sets.newHashSet("completed"));
        filters.put(FileProvenanceParam.skip.toString(), Sets.newHashSet("false"));
        return filters;
    }

    public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        OptionSpec helpOpt = parser.accepts("help").forHelp();
        OptionSpec<String> providerSettingFileOpt = parser.accepts("settings", "Provider settings json file (default: ~/.provenance/settings.json)").withRequiredArg();
        OptionSpec<String> outOpt = parser.accepts("out", "File provenance report TSV output file path").withRequiredArg().required();
        OptionSpec allOpt = parser.accepts("all",
                "Get all records rather than only the records that pass the default filters: [processing status = success, workflow run status = completed, skip = false]");

        Map<String, OptionSpec<String>> filterOpts = new HashMap<>();
        for (FileProvenanceParam fpp : FileProvenanceParam.values()) {
            String fileProvenanceParamString = fpp.toString();
            OptionSpec<String> opts = parser.accepts(fileProvenanceParamString, "Filter/select file provenance records by " + fpp.name()).withRequiredArg();
            filterOpts.put(fileProvenanceParamString, opts);
        }

        //parse args provided on the command line
        OptionSet options = parser.parse(args);

        if (options.has(helpOpt)) {
            parser.printHelpOn(System.out);
            System.exit(0);
        }

        Path providerSettingFile = Paths.get(System.getProperty("user.home"), ".provenance", "settings.json");
        if (options.has(providerSettingFileOpt)) {
            providerSettingFile = Paths.get(options.valueOf(providerSettingFileOpt));
        }
        if (!Files.exists(providerSettingFile) || !Files.isReadable(providerSettingFile) || !Files.isRegularFile(providerSettingFile)) {
            throw new RuntimeException("Provider settings file [" + providerSettingFile.toString() + "] is not accessible");
        }

        Path outputFilePath = Paths.get(options.valueOf(outOpt));
        if (Files.exists(outputFilePath)) {
            throw new RuntimeException("Output file [" + outputFilePath.toString() + "] already exists");
        }

        Map<String, Set<String>> filterArgs = new HashMap<>();
        for (Entry<String, OptionSpec<String>> e : filterOpts.entrySet()) {
            if (options.has(e.getValue())) {
                filterArgs.put(e.getKey(), Sets.newHashSet(options.valuesOf(e.getValue())));
            }
        }
        if (filterArgs.isEmpty() && !options.has(allOpt)) {
            filterArgs.putAll(Client.getDefaultFilters());
        }

        Client client = new Client(FileUtils.readFileToString(providerSettingFile.toFile()));
        client.getFileProvenanceReport(outputFilePath.toString(), filterArgs);
    }

}
