package ca.on.oicr.gsi.fileprovenance;

import ca.on.oicr.gsi.provenance.DefaultProvenanceClient;
import ca.on.oicr.gsi.provenance.SeqwareMetadataAnalysisProvenanceProvider;
import ca.on.oicr.gsi.provenance.SeqwareMetadataLimsMetadataProvenanceProvider;
import ca.on.oicr.gsi.provenance.model.SampleProvenance;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import net.sourceforge.seqware.common.model.IUS;
import net.sourceforge.seqware.common.module.FileMetadata;
import ca.on.oicr.gsi.provenance.ExtendedProvenanceClient;
import ca.on.oicr.gsi.provenance.ProviderLoader;
import ca.on.oicr.gsi.provenance.ProviderLoader.Provider;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import net.sourceforge.seqware.common.model.Workflow;
import ca.on.oicr.pde.client.SeqwareClient;
import ca.on.oicr.pde.testing.metadata.RegressionTestStudy;
import ca.on.oicr.pde.testing.metadata.SeqwareTestEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.h2.util.IOUtils;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 *
 * @author mlaszloffy
 */
public class ClientIT {

    protected SeqwareTestEnvironment te;
    protected ExtendedProvenanceClient provenanceClient;
    protected SeqwareClient seqwareClient;
    protected final Map<String, String> DEFAULT_WORKFLOW_PARAMS = ImmutableMap.of("test_key", "test_value");
    protected File providerSettings;
    protected File tmpDir;

    @BeforeClass
    public void setup() throws IOException {
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
        RegressionTestStudy r = new RegressionTestStudy(te.getSeqwareLimsClient());
        seqwareClient = te.getSeqwareClient();

        SeqwareMetadataLimsMetadataProvenanceProvider seqwareProvenanceProvider = new SeqwareMetadataLimsMetadataProvenanceProvider(te.getMetadata());
        DefaultProvenanceClient dpc = new DefaultProvenanceClient();
        dpc.registerAnalysisProvenanceProvider("seqware", new SeqwareMetadataAnalysisProvenanceProvider(te.getMetadata()));
        dpc.registerSampleProvenanceProvider("seqware", seqwareProvenanceProvider);
        dpc.registerLaneProvenanceProvider("seqware", seqwareProvenanceProvider);
        provenanceClient = dpc;

        Provider analysisProvider = new ProviderLoader.Provider();
        analysisProvider.setProvider("seqware");
        analysisProvider.setType(SeqwareMetadataAnalysisProvenanceProvider.class.getCanonicalName());
        analysisProvider.setProviderSettings(te.getSeqwareConfig());

        Provider limsProvenanceProvider = new ProviderLoader.Provider();
        limsProvenanceProvider.setProvider("seqware");
        limsProvenanceProvider.setType(SeqwareMetadataLimsMetadataProvenanceProvider.class.getCanonicalName());
        limsProvenanceProvider.setProviderSettings(te.getSeqwareConfig());

        ProviderLoader pl = new ProviderLoader(Arrays.asList(analysisProvider, limsProvenanceProvider));
        tmpDir = FileUtils.getTempDirectory();
        providerSettings = File.createTempFile("provider-settings", ".json", tmpDir);
        providerSettings.deleteOnExit();
        providerSettings.delete();
        FileUtils.writeStringToFile(providerSettings, pl.getProvidersAsJson());
    }

    @BeforeClass(dependsOnMethods = "setup")
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
            seqwareClient.createWorkflowRun(upstreamWorkflow, Sets.newHashSet(i), Collections.EMPTY_LIST, Lists.newArrayList(file));
        }
    }

    @Test
    public void defaultFiltersTest() throws IOException {
        Map<String, CSVRecord> recs = executeClient(Collections.EMPTY_LIST);

        //16 records okay, 6 skipped
        assertEquals(recs.size(), 16);
    }

    @Test
    public void noFiltersTest() throws IOException {
        Map<String, CSVRecord> recs = executeClient(Arrays.asList(
                "--all"
        ));

        //16 records okay, 6 skipped
        assertEquals(recs.size(), 22);
    }

    @Test
    public void rootSampleFilterTest() throws IOException {
        Map<String, CSVRecord> recs = executeClient(Arrays.asList(
                "--root-sample", "TEST_0003",
                "--skip", "false"
        ));

        //2 records okay, 1 skipped
        assertEquals(recs.size(), 2);
    }

    private Map<String, CSVRecord> executeClient(List<String> inputArgs) throws IOException {
        File output = File.createTempFile("fpr", ".tsv", tmpDir);
        output.deleteOnExit();
        output.delete();

        List<String> args = new ArrayList<>();
        args.addAll(Arrays.asList(
                "--settings", providerSettings.getCanonicalPath(),
                "--out", output.getCanonicalPath()
        ));
        args.addAll(inputArgs);

        Client.main(args.toArray(new String[0]));

        Iterable<CSVRecord> records = CSVFormat.newFormat('\t').withFirstRecordAsHeader().parse(IOUtils.getAsciiReader(FileUtils.openInputStream(output)));
        Map<String, CSVRecord> recs = new HashMap<>();
        for (CSVRecord rec : records) {
            recs.put(Long.toString(rec.getRecordNumber()), rec);
        }

        return recs;
    }

    @AfterClass
    public void destroySeqware() {
        te.shutdown();
    }
}
