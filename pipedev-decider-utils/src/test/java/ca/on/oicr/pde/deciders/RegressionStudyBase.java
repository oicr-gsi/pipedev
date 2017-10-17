package ca.on.oicr.pde.deciders;

import ca.on.oicr.gsi.provenance.ExtendedProvenanceClient;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.sourceforge.seqware.common.metadata.Metadata;
import org.testng.annotations.*;
import static org.testng.Assert.*;
import ca.on.oicr.pde.client.MetadataBackedSeqwareLimsClient;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.common.util.ExitException;
import ca.on.oicr.pde.client.SeqwareClient;

public class RegressionStudyBase {

    protected ExtendedProvenanceClient provenanceClient;
    protected Metadata metadata;
    protected Map<String, String> config;
    protected SeqwareClient seqwareClient;
    protected MetadataBackedSeqwareLimsClient seqwareLimsClient;
    protected final Map<String,String> DEFAULT_WORKFLOW_PARAMS = ImmutableMap.of("test_key", "test_value");

    @Test
    public void test() {
        Workflow downstreamWorkflow = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", DEFAULT_WORKFLOW_PARAMS);

        OicrDecider decider = new OicrDecider(provenanceClient);
        decider.setWorkflowAccession(downstreamWorkflow.getSwAccession().toString());
        decider.setMetaType(Arrays.asList("text/plain"));

        List<String> params = new ArrayList<>();
        params.add("--all");
        try {
        run(decider, params);
        } catch (ExitException e) {
        	assertEquals(e.getExitCode(), 0);
        }
        assertEquals(decider.getWorkflowRuns().size(), 16);

        run(decider, params);
        assertEquals(decider.getWorkflowRuns().size(), 0); //no new workflow runs should be scheduled

        //Test for SEQWARE-2017
        params.add("--ignore-previous-runs");
        run(decider, params);
        assertEquals(decider.getWorkflowRuns().size(), 16);
    }

    @Test
    public void rootSampleNameFilterTest() {
        Workflow downstreamWorkflow = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", DEFAULT_WORKFLOW_PARAMS);

        OicrDecider decider = new OicrDecider(provenanceClient);
        decider.setWorkflowAccession(downstreamWorkflow.getSwAccession().toString());
        decider.setMetaType(Arrays.asList("text/plain"));

        List<String> params = new ArrayList<>();
        params.add("--root-sample-name");
        params.add("TEST_0001_does_not_exist");
        //assertEquals(run(d, params).getWorkflowRunCount().intValue(), 0);

        params.clear();
        params.add("--root-sample-name");
        params.add("TEST_0001");

        run(decider, params);
        assertEquals(decider.getWorkflowRuns().size(), 12);
    }

    @Test
    public void sampleNameFilterTest() {
        Workflow downstreamWorkflow = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", DEFAULT_WORKFLOW_PARAMS);

        OicrDecider decider = new OicrDecider(provenanceClient);
        decider.setWorkflowAccession(downstreamWorkflow.getSwAccession().toString());
        decider.setMetaType(Arrays.asList("text/plain"));

        List<String> params = new ArrayList<>();
        params.add("--sample-name");
        params.add("TEST_0001_Ly_R_does_not_exist");

        run(decider, params);
        assertEquals(decider.getWorkflowRuns().size(), 0);

        //TODO: wildcards are currently not supported
//        params.clear();
//        params.add("--sample-name");
//        params.add("TEST_0001_Ly_R%");
//        run(decider, params);
//        assertEquals(decider.getWorkflowRuns().size(), 4);
    }

    @Test
    public void sampleNameFilterTest2() {
        Workflow downstreamWorkflow = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", DEFAULT_WORKFLOW_PARAMS);

        OicrDecider decider = new OicrDecider(provenanceClient);
        decider.setWorkflowAccession(downstreamWorkflow.getSwAccession().toString());
        decider.setMetaType(Arrays.asList("text/plain"));

        List<String> params = new ArrayList<>();
        params.add("--sample-name");
        params.add("TEST_0001_Ly_R_PE_500_WG_does_not_exist");
        //assertEquals(run(d, params).getWorkflowRunCount().intValue(), 0);

        params.clear();
        params.add("--sample-name");
        params.add("TEST_0001_Ly_R_PE_500_WG");
        run(decider, params);
        assertEquals(decider.getWorkflowRuns().size(), 2);
    }

    @Test
    public void studyNameFilterTest() {
        Workflow downstreamWorkflow = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", DEFAULT_WORKFLOW_PARAMS);

        OicrDecider decider = new OicrDecider(provenanceClient);
        decider.setWorkflowAccession(downstreamWorkflow.getSwAccession().toString());
        decider.setMetaType(Arrays.asList("text/plain"));

        List<String> params = new ArrayList<>();
        params.add("--study-name");
        params.add("PDE_TEST_does_not_exist");
        //assertEquals(run(d, params).getWorkflowRunCount().intValue(), 0);

        params.clear();
        params.add("--study-name");
        params.add("PDE_TEST");
        run(decider, params);
        assertEquals(decider.getWorkflowRuns().size(), 16);
    }

    @Test
    public void sequencerRunNameFilterTest() {
        Workflow downstreamWorkflow = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", DEFAULT_WORKFLOW_PARAMS);

        OicrDecider decider = new OicrDecider(provenanceClient);
        decider.setWorkflowAccession(downstreamWorkflow.getSwAccession().toString());
        decider.setMetaType(Arrays.asList("text/plain"));

        List<String> params = new ArrayList<>();
        params.add("--sequencer-run-name");
        params.add("TEST_SEQUENCER_RUN_002_does_not_exist");
        //assertEquals(run(d, params).getWorkflowRunCount().intValue(), 0);

        params.clear();
        params.add("--sequencer-run-name");
        params.add("TEST_SEQUENCER_RUN_002");
        run(decider, params);
        assertEquals(decider.getWorkflowRuns().size(), 8);
    }

    private void run(OicrDecider decider, List<String> params) {
        decider.setMetadata(metadata);
        decider.setConfig(config);
        decider.setParams(params);
        decider.parse_parameters();
        decider.init();
        decider.do_test();
        decider.do_run();
        decider.clean_up();
    }
}
