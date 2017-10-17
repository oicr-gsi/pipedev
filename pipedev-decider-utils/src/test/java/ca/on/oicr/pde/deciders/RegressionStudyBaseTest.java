package ca.on.oicr.pde.deciders;

import java.util.Collections;
import java.util.HashMap;

import org.powermock.reflect.Whitebox;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import ca.on.oicr.gsi.provenance.DefaultProvenanceClient;
import ca.on.oicr.gsi.provenance.SeqwareMetadataAnalysisProvenanceProvider;
import ca.on.oicr.gsi.provenance.SeqwareMetadataLimsMetadataProvenanceProvider;
import ca.on.oicr.gsi.provenance.model.SampleProvenance;
import ca.on.oicr.pde.client.MetadataBackedSeqwareClient;
import ca.on.oicr.pde.client.MetadataBackedSeqwareLimsClient;
import ca.on.oicr.pde.testing.metadata.RegressionTestStudy;
import net.sourceforge.seqware.common.metadata.MetadataInMemory;
import net.sourceforge.seqware.common.model.IUS;
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.common.module.FileMetadata;

/**
 *
 * @author mlaszloffy
 */
public class RegressionStudyBaseTest extends RegressionStudyBase {

    @BeforeMethod
    public void setupSeqware() {
        metadata = new MetadataInMemory();
        config = new HashMap<>();
        config.put("SW_METADATA_METHOD", "inmemory");
        seqwareClient = new MetadataBackedSeqwareClient(metadata, config);
        seqwareLimsClient = new MetadataBackedSeqwareLimsClient(metadata, config);
        
        SeqwareMetadataLimsMetadataProvenanceProvider seqwareProvenanceProvider = new SeqwareMetadataLimsMetadataProvenanceProvider(metadata);
        DefaultProvenanceClient dpc = new DefaultProvenanceClient();
        dpc.registerAnalysisProvenanceProvider("seqware", new SeqwareMetadataAnalysisProvenanceProvider(metadata));
        dpc.registerSampleProvenanceProvider("seqware", seqwareProvenanceProvider);
        dpc.registerLaneProvenanceProvider("seqware", seqwareProvenanceProvider);
        provenanceClient = dpc;

        RegressionTestStudy regressionTestStudy = new RegressionTestStudy(seqwareLimsClient);
    }

    @BeforeMethod(dependsOnMethods = "setupSeqware")
    public void setupData() {
        Workflow upstreamWorkflow = seqwareClient.createWorkflow("UpstreamWorkflow", "0.0", "", DEFAULT_WORKFLOW_PARAMS);
        for (SampleProvenance sp : provenanceClient.getSampleProvenance()) {
            IUS i = seqwareClient.addLims("seqware", sp.getSampleProvenanceId(), sp.getVersion(), sp.getLastModified());
            FileMetadata file = new FileMetadata();
            file.setDescription("description");
            file.setMd5sum("md5sum");
            file.setFilePath("/tmp/file_" + i.getSwAccession());
            file.setMetaType("text/plain");
            file.setType("type?");
            file.setSize(1L);
            seqwareClient.createWorkflowRun(upstreamWorkflow, Sets.newHashSet(i), Collections.emptyList(), Lists.newArrayList(file));
        }
    }

    @AfterMethod
    public void destroySeqware() {
        Whitebox.<Table>getInternalState(MetadataInMemory.class, "STORE").clear();
    }

}
