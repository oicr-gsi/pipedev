package ca.on.oicr.pde.testing.metadata.base;

import ca.on.oicr.gsi.provenance.ProvenanceClient;
import ca.on.oicr.pde.testing.metadata.RegressionTestStudy;
import net.sourceforge.seqware.common.metadata.MetadataInMemory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import ca.on.oicr.pde.client.SeqwareClient;
import ca.on.oicr.pde.client.SeqwareLimsClient;

/**
 *
 * @author mlaszloffy
 */
public abstract class Base {

    protected TestContext ctx;

    protected MetadataInMemory metadata;
    protected RegressionTestStudy r;
    protected SeqwareClient seqwareClient;
    protected SeqwareLimsClient seqwareLimsClient;
    protected RegressionTestStudy.SeqwareObjects seqwareObjects;
    protected ProvenanceClient provenanceClient;


    public Base(TestContext ctx) {
        this.ctx = ctx;
    }

    @BeforeClass
    public void setup() {
        ctx.setup();
    }

    @BeforeClass(dependsOnMethods = "setup")
    public void initCtx() {
        this.metadata = ctx.getMetadata();
        this.r = ctx.getR();
        this.seqwareClient = ctx.getSeqwareClient();
        this.seqwareLimsClient = ctx.getSeqwareLimsClient();
        this.seqwareObjects = ctx.getSeqwareObjects();
        this.provenanceClient = ctx.getProvenanceClient();
    }

    @AfterClass
    public void teardown() {
        ctx.teardown();
    }

}
