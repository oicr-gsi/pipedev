package ca.on.oicr.pde.testing;

import ca.on.oicr.pde.dao.SeqwareService;
import ca.on.oicr.pde.dao.SeqwareWebserviceImpl;
import ca.on.oicr.pde.testing.decider.DeciderRunTest;
import ca.on.oicr.pde.testing.decider.TestDefinition;
import static ca.on.oicr.pde.utilities.Helpers.*;
import ca.on.oicr.pde.utilities.ThreadedSeqwareExecutor;
import ca.on.oicr.pde.utilities.Timer;
import com.jcabi.manifests.Manifests;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;

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
    private final String schedulingHost;

    private final File deciderJar;

    private String dbFile = null;
    private String dbPatchDir = null;
    private String dbHost = null;
    private String dbPort = null;
    private String dbUser = null;
    private String dbPass = null;
    private String seqwareWebserviceDir = null;

    public DeciderRunTestFactory() {

        seqwareDistribution = getRequiredSystemPropertyAsFile("seqwareDistribution");
        deciderName = getRequiredSystemPropertyAsString("deciderName");
        deciderVersion = getRequiredSystemPropertyAsString("deciderVersion");
        deciderClass = getRequiredSystemPropertyAsString("deciderClass");
        workingDirectory = getRequiredSystemPropertyAsFile("workingDirectory");
        schedulingSystem = getRequiredSystemPropertyAsString("schedulingSystem");
        schedulingHost = getRequiredSystemPropertyAsString("schedulingHost");
        webserviceUrl = getRequiredSystemPropertyAsString("webserviceUrl");
        deciderJar = getRequiredSystemPropertyAsFile("deciderJar");

        dbFile = System.getProperty("dbFile");
        dbPatchDir = System.getProperty("dbPatchDir");
        dbHost = System.getProperty("dbHost");
        dbPort = System.getProperty("dbPort");
        dbUser = System.getProperty("dbUser");
        dbPass = System.getProperty("dbPass");
        seqwareWebserviceDir = System.getProperty("seqwareWebserviceDir");

        if (System.getProperty("bundledWorkflow") != null && !System.getProperty("bundledWorkflow").isEmpty()) {
            bundledWorkflow = getRequiredSystemPropertyAsFile("bundledWorkflow");
        } else if (Manifests.read("Workflow-Bundle-Path") != null && !Manifests.read("Workflow-Bundle-Path").isEmpty()) {
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

        TestDefinition td;
        try {
            td = TestDefinition.buildFromJson(tdString);
        } catch (IOException ioe) {
            log.error("Error deserializing test definition:", ioe);
            throw new RuntimeException(ioe);
        }

        //TODO: provide user a way to specify impl
        Timer timer = Timer.start();
        SeqwareService seqwareService = new SeqwareWebserviceImpl(webserviceUrl, "admin@admin.com", "admin");
        seqwareService.update();
        log.printf(Level.INFO, "Completed loading of seqware metadata in %s", timer.stop());

        List<DeciderRunTest> tests = new ArrayList<DeciderRunTest>();
        int count = 0;

        //Setup a shared thread pool for all tests to use
        ExecutorService sharedPool = Executors.newFixedThreadPool(50);

        for (TestDefinition.Test t : td.tests) {

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
            File seqwareSettings = generateSeqwareSettings(testWorkingDir, webserviceUrl, schedulingSystem, schedulingHost);

            DeciderRunTest test = new DeciderRunTest(seqwareService, seqwareDistribution, seqwareSettings, testWorkingDir, testName, deciderJar, bundledWorkflow, deciderClass, t);
            test.setSeqwareExecutor(new ThreadedSeqwareExecutor(testName, seqwareDistribution, seqwareSettings, testWorkingDir, sharedPool, seqwareService));

            tests.add(test);

        }

        return tests.toArray();

    }

}
