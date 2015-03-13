package ca.on.oicr.pde.testing.metadata;

import ca.on.oicr.pde.dao.reader.SeqwareReadService;
import ca.on.oicr.pde.dao.writer.SeqwareWriteService;
import ca.on.oicr.pde.model.SeqwareObject;
import java.io.File;
import java.util.Map;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 *
 * @author mlaszloffy
 */
public class RegressionTestStudyBase {

    RegressionTestStudy r;
    SeqwareWriteService seqwareWriter;
    SeqwareReadService seqwareReader;
    Map<String, SeqwareObject> seqwareObjects;

    public RegressionTestStudyBase() {

    }

    @BeforeClass
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

        r = new RegressionTestStudy(dbHost, dbPort, dbUser, dbPassword, seqwareWar);
        seqwareWriter = r.getSeqwareWriteService();
        seqwareReader = r.getSeqwareReadService();

        seqwareObjects = r.getSeqwareObjects();
    }

    @AfterClass
    public void cleanup() {
        r.shutdown();
    }
}
