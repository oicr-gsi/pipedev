package ca.on.oicr.gsi.fileprovenance;

import ca.on.oicr.gsi.provenance.AnalysisProvenanceProvider;
import ca.on.oicr.gsi.provenance.DefaultProvenanceClient;
import ca.on.oicr.gsi.provenance.LaneProvenanceProvider;
import ca.on.oicr.gsi.provenance.MultiThreadedDefaultProvenanceClient;
import ca.on.oicr.gsi.provenance.ProviderLoader;
import ca.on.oicr.gsi.provenance.SampleProvenanceProvider;
import ca.on.oicr.gsi.provenance.model.FileProvenance;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import ca.on.oicr.gsi.provenance.FileProvenanceFilter;
import joptsimple.BuiltinHelpFormatter;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

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

    public static Map<FileProvenanceFilter, Set<String>> getDefaultFilters() {
        Map<FileProvenanceFilter, Set<String>> filters = new HashMap<>();
        filters.put(FileProvenanceFilter.processing_status, Sets.newHashSet("success"));
        filters.put(FileProvenanceFilter.workflow_run_status, Sets.newHashSet("completed"));
        filters.put(FileProvenanceFilter.skip, Sets.newHashSet("false"));
        return filters;
    }

    public Collection<FileProvenance> getFileProvenance(Map<FileProvenanceFilter, Set<String>> filters) {
        Stopwatch sw = Stopwatch.createStarted();
        log.info("Starting download of file provenance");
        Collection<FileProvenance> fps = dpc.getFileProvenance(filters);
        log.info("Completed download of " + fps.size() + " file provenance records in " + sw.stop());
        return fps;
    }

    public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        OptionSpec helpOpt = parser.accepts("help").forHelp();
        OptionSpec<String> providerSettingFileOpt = parser.accepts("settings", "Provider settings json file (default: ~/.provenance/settings.json)").withRequiredArg();
        OptionSpec<String> outOpt = parser.accepts("out", "Path to write the output file to").withRequiredArg().required();
        OptionSpec allOpt = parser.accepts("all",
                "Get all records rather than only the records that pass the default filters: [processing status = success, workflow run status = completed, skip = false]");
        OptionSpec<Boolean> outputJsonOpt = parser.accepts("json", "Output report as json (default: tsv)").withOptionalArg().ofType(Boolean.class).defaultsTo(false);

        Map<String, OptionSpec<String>> filterOpts = new HashMap<>();
        for (FileProvenanceFilter fpp : FileProvenanceFilter.values()) {
            String fileProvenanceParamString = fpp.toString();
            OptionSpec<String> opts = parser.accepts(fileProvenanceParamString, "Filter/select file provenance records by " + fpp.name()).withRequiredArg();
            filterOpts.put(fileProvenanceParamString, opts);
        }

        //parse args provided on the command line
        OptionSet options = parser.parse(args);

        if (options.has(helpOpt)) {
            parser.formatHelpWith(new BuiltinHelpFormatter(200, 5));
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

        Map<FileProvenanceFilter, Set<String>> filters = new HashMap<>();
        for (Entry<String, OptionSpec<String>> e : filterOpts.entrySet()) {
            if (options.has(e.getValue())) {
                filters.put(FileProvenanceFilter.fromString(e.getKey()), Sets.newHashSet(options.valuesOf(e.getValue())));
            }
        }
        if (filters.isEmpty() && !options.has(allOpt)) {
            filters.putAll(Client.getDefaultFilters());
        }

        ReportBuilder reportBuilder;
        if (options.has(outputJsonOpt)) {
            reportBuilder = new JsonReportBuilder();
        } else {
            reportBuilder = new TsvReportBuilder();
        }

        Client client = new Client(FileUtils.readFileToString(providerSettingFile.toFile()));
        reportBuilder.writeReport(client.getFileProvenance(filters), outputFilePath);
    }

}
