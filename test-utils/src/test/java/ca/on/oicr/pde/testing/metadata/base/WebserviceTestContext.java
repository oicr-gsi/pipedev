package ca.on.oicr.pde.testing.metadata.base;

import ca.on.oicr.gsi.provenance.DefaultProvenanceClient;
import ca.on.oicr.gsi.provenance.SeqwareMetadataAnalysisProvenanceProvider;
import ca.on.oicr.gsi.provenance.SeqwareMetadataSampleProvenanceProvider;
import ca.on.oicr.pde.testing.metadata.RegressionTestStudy;
import ca.on.oicr.pde.testing.metadata.SeqwareTestEnvironment;
import java.io.File;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 *
 * @author mlaszloffy
 */
public class WebserviceTestContext extends TestContext {

    SeqwareTestEnvironment te;

    public WebserviceTestContext() {
    }

    @Override
    public void setup() {
        String dbHost = System.getProperty("dbHost");
        String dbPort = System.getProperty("dbPort");
        String dbUser = System.getProperty("dbUser");
        String dbPassword = System.getProperty("dbPassword");

        assertNotNull(dbHost, "Set dbHost (-DdbHost=xxxxxx) to a test postgresql db host name.");
        assertNotNull(dbPort, "Set dbPort (-DdbPort=xxxxxx) to a test postgresql db port.");
        assertNotNull(dbUser, "Set dbUser (-DdbUser=xxxxxx) to a test postgresql db user name.");

        String seqwareWarPath = System.getProperty("seqwareWar");
        assertNotNull(seqwareWarPath, "The seqware webservice war is not set.");

        File seqwareWar = new File(seqwareWarPath);
        assertTrue(seqwareWar.exists(), "The seqware webservice war is not accessible.");

        te = new SeqwareTestEnvironment(dbHost, dbPort, dbUser, dbPassword, seqwareWar);
        r = new RegressionTestStudy(te.getSeqwareLimsClient());
        seqwareClient = te.getSeqwareClient();
        seqwareLimsClient = te.getSeqwareLimsClient();
        seqwareObjects = r.getSeqwareObjects();
        provenanceClient = new DefaultProvenanceClient(new SeqwareMetadataAnalysisProvenanceProvider(te.getMetadata()),
                new SeqwareMetadataSampleProvenanceProvider(te.getMetadata()));
    }

    @Override
    public void teardown() {
        te.shutdown();
    }
}
