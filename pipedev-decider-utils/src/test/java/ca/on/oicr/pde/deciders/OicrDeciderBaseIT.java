package ca.on.oicr.pde.deciders;

import ca.on.oicr.gsi.provenance.DefaultProvenanceClient;
import ca.on.oicr.gsi.provenance.PineryProvenanceProvider;
import ca.on.oicr.gsi.provenance.SeqwareMetadataAnalysisProvenanceProvider;
import ca.on.oicr.pde.client.MetadataBackedSeqwareClient;
import ca.on.oicr.pde.testing.metadata.SeqwareTestEnvironment;
import ca.on.oicr.pinery.client.HttpResponseException;
import ca.on.oicr.pinery.client.PineryClient;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import javax.naming.NamingException;
import net.sourceforge.seqware.common.model.IUS;
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.common.module.FileMetadata;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.joda.time.DateTime;
import org.mortbay.log.Log;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import static org.testng.Assert.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author mlaszloffy
 */
public class OicrDeciderBaseIT extends OicrDeciderBase {

    private SeqwareTestEnvironment seqwareTestEnvironment;
    private Server server;

    public OicrDeciderBaseIT() {
        Logger.getRootLogger().setLevel(Level.WARN);
    }

    @BeforeMethod(groups = "setup")
    public void setupPinery() throws Exception {
        assertNotNull(System.getProperty("pineryWar"), "pineryWar is null");
        File pineryWar = FileUtils.getFile(System.getProperty("pineryWar"));
        assertTrue(pineryWar.exists(), "pineryWar [ " + System.getProperty("pineryWar") + "] does not exist");

        WebAppContext appContext = new WebAppContext(pineryWar.getAbsolutePath(), "/");

        //use the test's classloader, otherwise class cast exceptions (eg, Test's classloader DispatcherServlet != Pinery's classloader DispatcherServlet)
        appContext.setParentLoaderPriority(true);

        //the mock profile searches the classpath for a config file named "mock-config.xml" (this is defined in pinery-ws WEB-INF/spring-servlet.xml)
        appContext.setInitParameter("spring.profiles.active", "mock");

        //setup the jetty server
        server = new Server(9999);
        server.setHandler(appContext);
        server.start();

        //get the spring servlet and context
        DispatcherServlet dispatcherServlet = (DispatcherServlet) appContext.getServletHandler().getServlet("spring").getServletInstance();
        assertNotNull(dispatcherServlet, "Unable to get spring servlet");

        WebApplicationContext webApplicationContext = dispatcherServlet.getWebApplicationContext();
        Log.debug("pinery beans = " + Arrays.toString(webApplicationContext.getBeanDefinitionNames()));

        //get the Lims mock (defined in src/test/resources/mock-config.xml)
        limsMock = webApplicationContext.getBean(ca.on.oicr.pinery.api.Lims.class);
        assertNotNull(limsMock, "Pinery Lims mock is null");

        pineryClient = new PineryClient("http://localhost:9999");
    }

    @BeforeMethod(groups = "setup")
    public void setupSeqware() throws NamingException, HttpResponseException {
        String dbHost = System.getProperty("dbHost");
        String dbPort = System.getProperty("dbPort");
        String dbUser = System.getProperty("dbUser");
        String dbPassword = System.getProperty("dbPassword");

        assertNotNull(dbHost, "dbHost is not set");
        assertNotNull(dbPort, "dbPort is not set");
        assertNotNull(dbUser, "dbUser is not set");

        assertNotNull(System.getProperty("seqwareWar"), "seqwareWar is null");
        File seqwareWar = FileUtils.getFile(System.getProperty("seqwareWar"));
        assertTrue(seqwareWar.exists(), "seqwareWar [ " + System.getProperty("seqwareWar") + "] does not exist");

        seqwareTestEnvironment = new SeqwareTestEnvironment(dbHost, dbPort, dbUser, dbPassword, seqwareWar);
        config = seqwareTestEnvironment.getSeqwareConfig();
        metadata = seqwareTestEnvironment.getMetadata();
        seqwareClient = new MetadataBackedSeqwareClient(metadata, config);
    }

    @BeforeMethod(dependsOnMethods = {"setupSeqware", "setupPinery"}, groups = "setup")
    public void setupProvenance() {
        PineryProvenanceProvider pineryProvenanceProvider = new PineryProvenanceProvider(pineryClient);
        DefaultProvenanceClient dpc = new DefaultProvenanceClient();
        dpc.registerAnalysisProvenanceProvider("seqware", new SeqwareMetadataAnalysisProvenanceProvider(metadata));
        dpc.registerSampleProvenanceProvider("pinery", pineryProvenanceProvider);
        dpc.registerLaneProvenanceProvider("pinery", pineryProvenanceProvider);
        provenanceClient = dpc;
    }

    @Test
    public void testDeciderPineryUrlOption() {

        //check preconditions
        assertEquals(provenanceClient.getSampleProvenance().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 0);

        //create a file in seqware
        Workflow upstreamWorkflow = seqwareClient.createWorkflow("UpstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        IUS ius;
        FileMetadata file;
        ius = seqwareClient.addLims("seqware", "1_1_1", "1", new DateTime());
        file = new FileMetadata();
        file.setDescription("description");
        file.setMd5sum("md5sum");
        file.setFilePath("/tmp/file.bam");
        file.setMetaType("text/plain");
        file.setType("type?");
        file.setSize(1L);
        seqwareClient.createWorkflowRun(upstreamWorkflow, Sets.newHashSet(ius), Collections.EMPTY_LIST, Arrays.asList(file));
        assertEquals(provenanceClient.getSampleProvenance().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 1);

        //setup downstream workflow and decider
        Workflow downstreamWorkflow = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        OicrDecider decider = new OicrDecider();
        decider.setWorkflowAccession(downstreamWorkflow.getSwAccession().toString());
        decider.setMetaType(Arrays.asList("text/plain"));

        //test pinery-url decider option
        run(decider, Arrays.asList("--all", "--ignore-previous-runs", "--pinery-url", "http://localhost:9999"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 2);
    }

    @AfterMethod(alwaysRun = true)
    public void destroyProvenance() {

    }

    @AfterMethod(alwaysRun = true)
    public void destroyPinery() throws Exception {
        if (server != null) {
            server.stop();
            server.join();
        }
    }

    @AfterMethod(alwaysRun = true)
    public void destroySeqware() {
        if (seqwareTestEnvironment != null) {
            seqwareTestEnvironment.shutdown();
        }
    }
}
