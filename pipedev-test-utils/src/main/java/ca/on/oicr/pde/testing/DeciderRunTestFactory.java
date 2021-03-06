package ca.on.oicr.pde.testing;

import ca.on.oicr.gsi.provenance.AnalysisProvenanceProvider;
import ca.on.oicr.gsi.provenance.DefaultProvenanceClient;
import ca.on.oicr.gsi.provenance.LaneProvenanceProvider;
import ca.on.oicr.gsi.provenance.MultiThreadedDefaultProvenanceClient;
import ca.on.oicr.gsi.provenance.ProviderLoader;
import ca.on.oicr.gsi.provenance.SampleProvenanceProvider;
import ca.on.oicr.pde.client.MetadataBackedSeqwareClient;
import ca.on.oicr.pde.testing.decider.RunTest;
import static ca.on.oicr.pde.utilities.Helpers.*;
import ca.on.oicr.pde.dao.executor.ThreadedSeqwareExecutor;
import ca.on.oicr.pde.testing.decider.RunTestSuiteDefinition;
import ca.on.oicr.pde.testing.decider.RunTestDefinition;
import com.jcabi.manifests.Manifests;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.metadata.MetadataFactory;
import net.sourceforge.seqware.common.util.maptools.MapTools;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;
import ca.on.oicr.pde.client.SeqwareClient;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import net.sourceforge.seqware.common.model.Workflow;

public class DeciderRunTestFactory {

    private final static Logger log = LogManager.getLogger(DeciderRunTestFactory.class);

    private final File seqwareDistribution;
    private final File bundledWorkflow;
    private final String deciderName;
    private final String deciderVersion;
    private final String deciderClass;
    private final File workingDirectory;
    private final String schedulingSystem;
    private final String webserviceUrl;
    private final String webserviceUser;
    private final String webservicePassword;
    private final String schedulingHost;

    private final File deciderJar;

    private String dbFile = null;
    private String dbPatchDir = null;
    private String dbHost = null;
    private String dbPort = null;
    private String dbUser = null;
    private String dbPass = null;
    private String seqwareWebserviceDir = null;

    private Path provenanceSettingsPath = null;

    public DeciderRunTestFactory() {

        seqwareDistribution = getRequiredSystemPropertyAsFile("seqwareDistribution");
        deciderName = getRequiredSystemPropertyAsString("deciderName");
        deciderVersion = getRequiredSystemPropertyAsString("deciderVersion");
        deciderClass = getRequiredSystemPropertyAsString("deciderClass");
        workingDirectory = getRequiredSystemPropertyAsFile("workingDirectory");
        schedulingSystem = getRequiredSystemPropertyAsString("schedulingSystem");
        schedulingHost = getRequiredSystemPropertyAsString("schedulingHost");
        webserviceUrl = getRequiredSystemPropertyAsString("webserviceUrl");
        webserviceUser = getRequiredSystemPropertyAsString("webserviceUser");
        webservicePassword = getRequiredSystemPropertyAsString("webservicePassword");
        deciderJar = getRequiredSystemPropertyAsFile("deciderJar");

        dbFile = System.getProperty("dbFile");
        dbPatchDir = System.getProperty("dbPatchDir");
        dbHost = System.getProperty("dbHost");
        dbPort = System.getProperty("dbPort");
        dbUser = System.getProperty("dbUser");
        dbPass = System.getProperty("dbPass");
        seqwareWebserviceDir = System.getProperty("seqwareWebserviceDir");

        if (System.getProperty("provenanceSettingsPath") != null && !System.getProperty("provenanceSettingsPath").isEmpty()) {
            provenanceSettingsPath = Paths.get(System.getProperty("provenanceSettingsPath"));
        }

        if (System.getProperty("bundledWorkflow") != null && !System.getProperty("bundledWorkflow").isEmpty()) {
            bundledWorkflow = getRequiredSystemPropertyAsFile("bundledWorkflow");
        } else if (Manifests.exists("Workflow-Bundle-Path") && Manifests.read("Workflow-Bundle-Path") != null && !Manifests.read("Workflow-Bundle-Path").isEmpty()) {
            bundledWorkflow = new File(Manifests.read("Workflow-Bundle-Path"));
        } else {
            bundledWorkflow = null;
        }

    }

    @Parameters({"testDefinition"})
    @Factory
    public Object[] createTests(String testDefinitionFilePath) throws IOException {
        if (System.getProperty("testDefinition") != null) {
            testDefinitionFilePath = System.getProperty("testDefinition");
            log.printf(Level.WARN, "test defintion has been overridden by jvm argument");
        }

        log.printf(Level.INFO, "test definition path [%s]", testDefinitionFilePath);

        //TODO: below is temporary workaround to allow the user to override the test definition via the command line (eg, mvn -DtestDefinition=/tmp/a.json ...)
        // refactor create tests into a separate function, create command line tool as a different way to execute test?
        InputStream tdStream = null;
        try {
            if (FileUtils.getFile(testDefinitionFilePath).exists()) {
                tdStream = FileUtils.openInputStream(FileUtils.getFile(testDefinitionFilePath));
            } else {
                tdStream = getClass().getResourceAsStream(testDefinitionFilePath);
            }
        } catch (IOException ioe) {
            log.error("Error locating the test definition file:", ioe);
            throw new RuntimeException(ioe);
        }

        String tdString;
        try {
            tdString = IOUtils.toString(tdStream);
        } catch (IOException ioe) {
            log.error("Error reading test definition stream:", ioe);
            throw new RuntimeException(ioe);
        }

        RunTestSuiteDefinition td;
        try {
            td = RunTestSuiteDefinition.buildFromJson(tdString);
        } catch (IOException ioe) {
            log.error("Error deserializing test definition:", ioe);
            throw new RuntimeException(ioe);
        }

        List<RunTest> tests = new ArrayList<>();
        int count = 0;

        //Setup a shared thread pool for all tests to use
        ExecutorService sharedPool = Executors.newFixedThreadPool(50);

        for (RunTestDefinition t : td.getTests()) {

            //Build test name
            StringBuilder b = new StringBuilder();
            b.append("DeciderRunTest_");
            b.append(deciderName).append("-").append(deciderVersion);
            if (!t.getId().trim().isEmpty()) {
                //camel case the id
                b.append("_").append(WordUtils.capitalizeFully(t.getId().trim()).replaceAll("[^A-Za-z0-9]", ""));
            }

            String testName = b.toString();
            String prefix = new SimpleDateFormat("yyMMdd_HHmm").format(new Date());
            String testId = (count++) + "_" + UUID.randomUUID().toString().substring(0, 7);

            File testWorkingDir = generateTestWorkingDirectory(workingDirectory, prefix, testName, testId);
            File seqwareSettings = generateSeqwareSettings(testWorkingDir, webserviceUrl, webserviceUser, webservicePassword, schedulingSystem, schedulingHost);

            Map<String, String> config = new HashMap<>();
            MapTools.ini2Map(seqwareSettings.getAbsolutePath(), config, true);
            Metadata metadata = MetadataFactory.get(config);

            SeqwareClient seqwareClient = new MetadataBackedSeqwareClient(metadata, config);

            ProviderLoader providerLoader = new ProviderLoader(provenanceSettingsPath);
            DefaultProvenanceClient provenanceClient = new MultiThreadedDefaultProvenanceClient();
            for (Entry<String, AnalysisProvenanceProvider> e : providerLoader.getAnalysisProvenanceProviders().entrySet()) {
                provenanceClient.registerAnalysisProvenanceProvider(e.getKey(), e.getValue());
            }
            for (Entry<String, SampleProvenanceProvider> e : providerLoader.getSampleProvenanceProviders().entrySet()) {
                provenanceClient.registerSampleProvenanceProvider(e.getKey(), e.getValue());
            }
            for (Entry<String, LaneProvenanceProvider> e : providerLoader.getLaneProvenanceProviders().entrySet()) {
                provenanceClient.registerLaneProvenanceProvider(e.getKey(), e.getValue());
            }

            Workflow workflow = new Workflow();
            if (bundledWorkflow != null) {
                workflow.setCwd(bundledWorkflow.getAbsolutePath());
            } else if (t.getWorkflowName() != null && t.getWorkflowVersion() != null) {
                workflow.setName(t.getWorkflowName());
                workflow.setVersion(t.getWorkflowVersion());
            } else {
                System.out.println("fail");
            }

            RunTest test = new RunTest(seqwareClient, provenanceClient, seqwareDistribution, seqwareSettings, testWorkingDir, testName, deciderJar, workflow, deciderClass, t);
            test.setSeqwareExecutor(new ThreadedSeqwareExecutor(testName, seqwareDistribution, seqwareSettings, testWorkingDir, sharedPool, seqwareClient));
            test.setProvenanceSettings(provenanceSettingsPath);
            tests.add(test);
        }

        return tests.toArray();

    }

}
