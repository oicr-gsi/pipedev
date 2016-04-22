package ca.on.oicr.pde.deciders;

import ca.on.oicr.gsi.provenance.DefaultProvenanceClient;
import ca.on.oicr.gsi.provenance.PinerySampleProvenanceProvider;
import ca.on.oicr.gsi.provenance.SeqwareMetadataAnalysisProvenanceProvider;
import ca.on.oicr.pde.client.MetadataBackedSeqwareClient;
import ca.on.oicr.pinery.client.HttpResponseException;
import ca.on.oicr.pinery.client.PineryClient;
import ca.on.oicr.pinery.client.SampleProvenanceClient;
import ca.on.oicr.pinery.service.SampleProvenanceService;
import ca.on.oicr.pinery.service.impl.DefaultSampleProvenanceService;
import ca.on.oicr.ws.dto.Dtos;
import ca.on.oicr.ws.dto.SampleProvenanceDto;
import com.google.common.collect.Table;
import java.util.HashMap;
import java.util.List;
import net.sourceforge.seqware.common.metadata.MetadataInMemory;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import static org.mockito.Mockito.mock;
import org.powermock.reflect.Whitebox;

/**
 *
 * @author mlaszloffy
 */
public class OicrDeciderBaseTest extends OicrDeciderBase {

    PineryClient pineryClientMock;

    @BeforeMethod(groups = "setup")
    public void setupPinery() {

        // The following code mocks:
        // Lims -> SampleProvenanceService -> ... (not mocked PineryWS) ... -> PineryClient -> ProvenanceClient
        limsMock = mock(ca.on.oicr.pinery.api.Lims.class);
        final SampleProvenanceService sampleProvenanceService = new DefaultSampleProvenanceService(limsMock);
        pineryClient = pineryClientMock = mock(PineryClient.class);
        SampleProvenanceClient sampleProvenanceClientMock = mock(SampleProvenanceClient.class);
        doReturn(sampleProvenanceClientMock).when(pineryClientMock).getSampleProvenance();
        try {
            when(sampleProvenanceClientMock.all()).thenAnswer(new Answer<List<SampleProvenanceDto>>() {
                @Override
                public List<SampleProvenanceDto> answer(InvocationOnMock invocation) throws Throwable {
                    return Dtos.asDto(sampleProvenanceService.getSampleProvenance());
                }
            });
        } catch (HttpResponseException ex) {
            throw new RuntimeException(ex);
        }
    }

    @BeforeMethod(groups = "setup")
    public void setupSeqware() {
        metadata = new MetadataInMemory(); //new MetadataNoConnection();
        config = new HashMap<>();
        config.put("SW_METADATA_METHOD", "inmemory");
        seqwareClient = new MetadataBackedSeqwareClient(metadata, config);
    }

    @BeforeMethod(dependsOnMethods = {"setupSeqware", "setupPinery"}, groups = "setup")
    public void setupProvenance() {
        provenanceClient = new DefaultProvenanceClient(new SeqwareMetadataAnalysisProvenanceProvider(metadata), 
                new PinerySampleProvenanceProvider(pineryClientMock));
    }

    @AfterMethod
    public void destroyProvenance() {
    }

    @AfterMethod
    public void destroyPinery() {
    }

    @AfterMethod
    public void destroySeqware() {
        Whitebox.<Table>getInternalState(MetadataInMemory.class, "STORE").clear();
    }
}
