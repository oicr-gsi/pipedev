package ca.on.oicr.pde.deciders;

import ca.on.oicr.gsi.provenance.ExtendedProvenanceClient;
import ca.on.oicr.pde.client.SeqwareClient;
import ca.on.oicr.pinery.api.Attribute;
import ca.on.oicr.pinery.api.Run;
import ca.on.oicr.pinery.api.RunPosition;
import ca.on.oicr.pinery.api.RunSample;
import ca.on.oicr.pinery.api.Sample;
import ca.on.oicr.pinery.api.SampleProject;
import ca.on.oicr.pinery.client.PineryClient;
import ca.on.oicr.pinery.lims.DefaultAttribute;
import ca.on.oicr.pinery.lims.DefaultRun;
import ca.on.oicr.pinery.lims.DefaultRunPosition;
import ca.on.oicr.pinery.lims.DefaultRunSample;
import ca.on.oicr.pinery.lims.DefaultSample;
import ca.on.oicr.pinery.lims.DefaultSampleProject;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.sourceforge.seqware.common.metadata.Metadata;
import org.joda.time.DateTime;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;

/**
 *
 * @author mlaszloffy
 */
public class DeciderBase {

    protected List<Sample> samples;
    protected SampleProject project;
    protected Sample sample;
    protected RunSample runSample;
    protected RunPosition lane;
    protected Run run;

    protected ExtendedProvenanceClient provenanceClient;
    protected Metadata metadata;
    protected Map<String, String> config;
    protected SeqwareClient seqwareClient;
    protected ca.on.oicr.pinery.api.Lims limsMock;
    protected PineryClient pineryClient;

    @BeforeMethod(dependsOnGroups = "setup")
    public void setupData() {
        // Populate the LIMS mock with some data
        samples = new ArrayList<>();

        project = new DefaultSampleProject();
        project.setName("TEST_PROJECT");

        Attribute a;
        a = new DefaultAttribute();
        a.setName("Tissue Type");
        a.setValue("R");

        sample = new DefaultSample();
        sample.setName("TEST_SAMPLE");
        sample.setProject("TEST_PROJECT");
        sample.setAttributes(Sets.newHashSet(a));
        sample.setId("1");
        sample.setModified(DateTime.parse("2015-01-01T00:00:00.000Z").toDate());
        samples.add(sample);

        a = new DefaultAttribute();
        a.setName("Tissue Type");
        a.setValue("R");

        runSample = new DefaultRunSample();
        runSample.setId("1");
        runSample.setAttributes(Sets.newHashSet(a));

        lane = new DefaultRunPosition();
        lane.setPosition(1);
        lane.setRunSample(Sets.newHashSet(runSample));

        run = new DefaultRun();
        run.setId(1);
        run.setName("ABC_123");
        run.setSample(Sets.newHashSet(lane));

        when(limsMock.getSampleProjects()).thenReturn(Lists.newArrayList(project));
        when(limsMock.getSamples(null, null, null, null, null)).thenReturn(samples);
        when(limsMock.getRuns()).thenReturn(Lists.newArrayList(run));

        //check preconditions
        assertEquals(provenanceClient.getSampleProvenance().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 0);
        assertEquals(Iterables.getOnlyElement(provenanceClient.getSampleProvenance()).getSampleName(), "TEST_SAMPLE");
    }

    protected void run(OicrDecider decider, List<String> params) {
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
