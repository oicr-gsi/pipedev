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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
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
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.h2.util.IOUtils;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

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

    @BeforeMethod
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
        providerSettings = FileUtils.getFile(tmpDir, "provider-settings.json");
        providerSettings.deleteOnExit();
        FileUtils.writeStringToFile(providerSettings, pl.getProvidersAsJson());
    }

    @BeforeMethod(dependsOnMethods = "setup")
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
    public void clientTest() throws IOException {
        File output = FileUtils.getFile(tmpDir, "fpr.tsv");
        output.deleteOnExit();
        output.delete();
        Client.main(new String[]{"--settings", providerSettings.getCanonicalPath(), "--out", output.getCanonicalPath()});

        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(IOUtils.getAsciiReader(FileUtils.openInputStream(output)));
        Map<String, CSVRecord> recs = new HashMap<>();
        for (CSVRecord rec : records) {
            recs.put(Long.toString(rec.getRecordNumber()), rec);
        }
        assertEquals(recs.size(), 16);
    }

    @AfterMethod
    public void destroySeqware() {
        te.shutdown();
    }
}
