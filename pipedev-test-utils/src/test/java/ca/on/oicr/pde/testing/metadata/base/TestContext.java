package ca.on.oicr.pde.testing.metadata.base;

import ca.on.oicr.gsi.provenance.ProvenanceClient;
import ca.on.oicr.pde.testing.metadata.RegressionTestStudy;
import net.sourceforge.seqware.common.metadata.MetadataInMemory;
import ca.on.oicr.pde.client.SeqwareClient;
import ca.on.oicr.pde.client.SeqwareLimsClient;

/**
 *
 * @author mlaszloffy
 */
public abstract class TestContext {

    protected MetadataInMemory metadata;
    protected RegressionTestStudy r;
    protected SeqwareClient seqwareClient;
    protected SeqwareLimsClient seqwareLimsClient;
    protected RegressionTestStudy.SeqwareObjects seqwareObjects;
    protected ProvenanceClient provenanceClient;

    public MetadataInMemory getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataInMemory metadata) {
        this.metadata = metadata;
    }

    public RegressionTestStudy getR() {
        return r;
    }

    public void setR(RegressionTestStudy r) {
        this.r = r;
    }

    public SeqwareClient getSeqwareClient() {
        return seqwareClient;
    }

    public void setSeqwareClient(SeqwareClient seqwareClient) {
        this.seqwareClient = seqwareClient;
    }

    public SeqwareLimsClient getSeqwareLimsClient() {
        return seqwareLimsClient;
    }

    public void setSeqwareLimsClient(SeqwareLimsClient seqwareLimsClient) {
        this.seqwareLimsClient = seqwareLimsClient;
    }

    public RegressionTestStudy.SeqwareObjects getSeqwareObjects() {
        return seqwareObjects;
    }

    public void setSeqwareObjects(RegressionTestStudy.SeqwareObjects seqwareObjects) {
        this.seqwareObjects = seqwareObjects;
    }

    public ProvenanceClient getProvenanceClient() {
        return provenanceClient;
    }

    public void setProvenanceClient(ProvenanceClient provenanceClient) {
        this.provenanceClient = provenanceClient;
    }

    public abstract void setup();

    public abstract void teardown();

}
