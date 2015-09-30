package ca.on.oicr.pde.deciders;

import ca.on.oicr.pde.dao.executor.SeqwareExecutor;
import ca.on.oicr.pde.dao.reader.SeqwareReadService;
import ca.on.oicr.pde.dao.writer.SeqwareWriteService;
import ca.on.oicr.pde.model.Ius;
import ca.on.oicr.pde.model.SeqwareObject;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.testing.decider.DeciderRunTestReport;
import ca.on.oicr.pde.testing.metadata.RegressionTestStudy;
import com.google.common.collect.Iterables;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.seqware.common.metadata.MetadataFactory;
import net.sourceforge.seqware.common.util.configtools.ConfigTools;
import static org.testng.Assert.*;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 *
 * @author mlaszloffy
 */
public class OicrDeciderIT {

    RegressionTestStudy r;
    File bundledWorkflow;
    SeqwareExecutor ses;
    SeqwareReadService srs;
    SeqwareWriteService sws;
    Map<String, SeqwareObject> sos;

    Workflow upstreamWorkflow;

    public OicrDeciderIT() {
    }

    @BeforeSuite
    public void setupMetadb() {

        //get the workflow bundle associated with the decider
        //bundledWorkflow = Helpers.getBundledWorkflow();
        //Assert.assertNotNull(bundledWorkflow, "Unable to locate the workflow bundle.");
        //Assert.assertTrue(bundledWorkflow.exists(), "The workflow bundle [" + bundledWorkflow + "] does not exist.");
        //get the database settings
        String dbHost = System.getProperty("dbHost");
        String dbPort = System.getProperty("dbPort");
        String dbUser = System.getProperty("dbUser");
        String dbPassword = System.getProperty("dbPassword");
        assertNotNull(dbHost, "Set dbHost to a testing Postgres database host name");
        assertNotNull(dbPort, "Set dbPort to a testing Postgres database port");
        assertNotNull(dbUser, "Set dbUser to a testing Postgres database user name");
        //assertNotNull(dbPassword, "Set dbPassword to a testing Postgres database password");

        //get the seqware webservice war
        String seqwareWarPath = System.getProperty("seqwareWar");
        assertNotNull(seqwareWarPath, "seqwareWar is not set.");
        File seqwareWar = new File(seqwareWarPath);
        assertTrue(seqwareWar.exists(), "seqware war is not accessible.");

        //get the regression test study and PDE's service objects
        r = new RegressionTestStudy(dbHost, dbPort, dbUser, dbPassword, seqwareWar);
        ses = r.getSeqwareExecutor();
        srs = r.getSeqwareReadService();
        sws = r.getSeqwareWriteService();
        sos = r.getSeqwareObjects();

        //set the seqware settings path (needed by Seqware's plugin runner)
        System.setProperty("SEQWARE_SETTINGS", r.getSeqwareSettings().getAbsolutePath());

        upstreamWorkflow = sws.createWorkflow("UpstreamWorkflow", "0.0", "");

        //get all ius objects and link two fastqs
        for (Ius ius : Iterables.filter(sos.values(), Ius.class)) {
            sws.createWorkflowRun(upstreamWorkflow, Arrays.asList(ius),
                    Arrays.asList(new SeqwareWriteService.FileInfo("type", "text/plain", "/tmp/" + ius.getSwid() + ".txt")));
        }

    }

    @Test
    public void test() {

        sws.updateFileReport();

        Map<String, String> defaultParams = new HashMap<>();
        defaultParams.put("test_key", "test_value");
        Workflow downstreamWorkflow = sws.createWorkflow("DownstreamWorkflow", "0.0", "", defaultParams);

        OicrDecider decider = new OicrDecider();
        decider.setWorkflowAccession(downstreamWorkflow.getSwid());
        decider.setMetaType(Arrays.asList("text/plain"));

        List<String> params = new ArrayList<>();
        params.add("--all");

        assertEquals(run(decider, params).getWorkflowRunCount().intValue(), 16);

        assertEquals(run(decider, params).getWorkflowRunCount().intValue(), 16); //no new workflow runs should be scheduled

        //Test for SEQWARE-2017
        params.add("--ignore-previous-runs");
        assertEquals(run(decider, params).getWorkflowRunCount().intValue(), 32);
    }

    @Test
    public void rootSampleNameFilterTest() {

        sws.updateFileReport();

        Map<String, String> defaultParams = new HashMap<>();
        defaultParams.put("test_key", "test_value");
        Workflow downstreamWorkflow = sws.createWorkflow("DownstreamWorkflow", "0.0", "", defaultParams);

        OicrDecider decider = new OicrDecider();
        decider.setWorkflowAccession(downstreamWorkflow.getSwid());
        decider.setMetaType(Arrays.asList("text/plain"));

        List<String> params = new ArrayList<>();
        params.add("--root-sample-name");
        params.add("TEST_0001_does_not_exist");
        //assertEquals(run(d, params).getWorkflowRunCount().intValue(), 0);

        params.clear();
        params.add("--root-sample-name");
        params.add("TEST_0001");
        assertEquals(run(decider, params).getWorkflowRunCount().intValue(), 12);
    }

    @Test
    public void sampleNameFilterTest() {

        sws.updateFileReport();

        Map<String, String> defaultParams = new HashMap<>();
        defaultParams.put("test_key", "test_value");
        Workflow downstreamWorkflow = sws.createWorkflow("DownstreamWorkflow", "0.0", "", defaultParams);

        OicrDecider decider = new OicrDecider();
        decider.setWorkflowAccession(downstreamWorkflow.getSwid());
        decider.setMetaType(Arrays.asList("text/plain"));

        List<String> params = new ArrayList<>();
        params.add("--sample-name");
        params.add("TEST_0001_Ly_R_does_not_exist");
        //assertEquals(run(d, params).getWorkflowRunCount().intValue(), 0);

        params.clear();
        params.add("--sample-name");
        params.add("TEST_0001_Ly_R%");
        assertEquals(run(decider, params).getWorkflowRunCount().intValue(), 4);
    }

    @Test
    public void sampleNameFilterTest2() {

        sws.updateFileReport();

        Map<String, String> defaultParams = new HashMap<>();
        defaultParams.put("test_key", "test_value");
        Workflow downstreamWorkflow = sws.createWorkflow("DownstreamWorkflow", "0.0", "", defaultParams);

        OicrDecider decider = new OicrDecider();
        decider.setWorkflowAccession(downstreamWorkflow.getSwid());
        decider.setMetaType(Arrays.asList("text/plain"));

        List<String> params = new ArrayList<>();
        params.add("--sample-name");
        params.add("TEST_0001_Ly_R_PE_500_WG_does_not_exist");
        //assertEquals(run(d, params).getWorkflowRunCount().intValue(), 0);

        params.clear();
        params.add("--sample-name");
        params.add("TEST_0001_Ly_R_PE_500_WG");
        assertEquals(run(decider, params).getWorkflowRunCount().intValue(), 2);
    }

    @Test
    public void studyNameFilterTest() {

        sws.updateFileReport();

        Map<String, String> defaultParams = new HashMap<>();
        defaultParams.put("test_key", "test_value");
        Workflow downstreamWorkflow = sws.createWorkflow("DownstreamWorkflow", "0.0", "", defaultParams);

        OicrDecider decider = new OicrDecider();
        decider.setWorkflowAccession(downstreamWorkflow.getSwid());
        decider.setMetaType(Arrays.asList("text/plain"));

        List<String> params = new ArrayList<>();
        params.add("--study-name");
        params.add("PDE_TEST_does_not_exist");
        //assertEquals(run(d, params).getWorkflowRunCount().intValue(), 0);

        params.clear();
        params.add("--study-name");
        params.add("PDE_TEST");
        assertEquals(run(decider, params).getWorkflowRunCount().intValue(), 16);
    }

    @Test
    public void sequencerRunNameFilterTest() {

        sws.updateFileReport();

        Map<String, String> defaultParams = new HashMap<>();
        defaultParams.put("test_key", "test_value");
        Workflow downstreamWorkflow = sws.createWorkflow("DownstreamWorkflow", "0.0", "", defaultParams);

        OicrDecider decider = new OicrDecider();
        decider.setWorkflowAccession(downstreamWorkflow.getSwid());
        decider.setMetaType(Arrays.asList("text/plain"));

        List<String> params = new ArrayList<>();
        params.add("--sequencer-run-name");
        params.add("TEST_SEQUENCER_RUN_002_does_not_exist");
        //assertEquals(run(d, params).getWorkflowRunCount().intValue(), 0);

        params.clear();
        params.add("--sequencer-run-name");
        params.add("TEST_SEQUENCER_RUN_002");
        assertEquals(run(decider, params).getWorkflowRunCount().intValue(), 8);
    }

    private DeciderRunTestReport run(OicrDecider decider, List<String> params) {
        
        //create a workflow object from the decider's target workflow
        Workflow.Builder wb = new Workflow.Builder();
        wb.setSwid(decider.getWorkflowAccession());
        Workflow w = wb.build();

        //setup the decider object
        decider.setConfig(ConfigTools.getSettings());
        decider.setMetadata(MetadataFactory.get(ConfigTools.getSettings()));
        decider.setParams(params);

        //run the decider/plugin
        decider.parse_parameters();
        decider.init();
        decider.do_test();
        decider.do_run();
        decider.clean_up();

        //build the decider run report
        srs.update();
        srs.updateWorkflowRunRecords(w);
        return DeciderRunTestReport.generateReport(srs, w);
    }

    @AfterSuite
    public void shutdown() {
        //shutdown webservice, drop database
        r.shutdown();
    }

}
