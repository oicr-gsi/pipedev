package ca.on.oicr.pde.testing.metadata.base;

import ca.on.oicr.gsi.provenance.DefaultProvenanceClient;
import ca.on.oicr.gsi.provenance.SeqwareMetadataAnalysisProvenanceProvider;
import ca.on.oicr.gsi.provenance.SeqwareMetadataLimsMetadataProvenanceProvider;
import ca.on.oicr.pde.client.MetadataBackedSeqwareLimsClient;
import ca.on.oicr.pde.client.MetadataBackedSeqwareClient;
import ca.on.oicr.pde.testing.metadata.RegressionTestStudy;
import com.google.common.collect.Table;
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.seqware.common.metadata.MetadataInMemory;
import org.powermock.reflect.Whitebox;

/**
 *
 * @author mlaszloffy
 */
public class InMemoryTestContext extends TestContext {

    public InMemoryTestContext() {
        metadata = new MetadataInMemory();
        Map<String, String> config = new HashMap<>();
        config.put("SW_METADATA_METHOD", "inmemory");
        seqwareClient = new MetadataBackedSeqwareClient(metadata, config);
        seqwareLimsClient = new MetadataBackedSeqwareLimsClient(metadata, config);
        
        SeqwareMetadataLimsMetadataProvenanceProvider seqwareMetadataProvider = new SeqwareMetadataLimsMetadataProvenanceProvider(metadata);
        provenanceClient = new DefaultProvenanceClient(new SeqwareMetadataAnalysisProvenanceProvider(metadata),
                seqwareMetadataProvider, seqwareMetadataProvider);

        r = new RegressionTestStudy(seqwareLimsClient);
        seqwareObjects = r.getSeqwareObjects();
    }

    @Override
    public void setup() {
    }

    @Override
    public void teardown() {
        Whitebox.<Table>getInternalState(MetadataInMemory.class, "STORE").clear();
    }

}
