package ca.on.oicr.pde.deciders;

import ca.on.oicr.gsi.provenance.DefaultProvenanceClient;
import ca.on.oicr.gsi.provenance.PineryProvenanceProvider;
import ca.on.oicr.gsi.provenance.SeqwareMetadataAnalysisProvenanceProvider;
import ca.on.oicr.pde.client.MetadataBackedSeqwareClient;
import ca.on.oicr.pinery.client.HttpResponseException;
import ca.on.oicr.pinery.client.LaneProvenanceClient;
import ca.on.oicr.pinery.client.PineryClient;
import ca.on.oicr.pinery.client.SampleProvenanceClient;
import ca.on.oicr.pinery.service.LaneProvenanceService;
import ca.on.oicr.pinery.service.SampleProvenanceService;
import ca.on.oicr.pinery.service.impl.DefaultLaneProvenanceService;
import ca.on.oicr.pinery.service.impl.DefaultSampleProvenanceService;
import ca.on.oicr.ws.dto.Dtos;
import ca.on.oicr.ws.dto.LaneProvenanceDto;
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
        // Lims -> Sample/LaneProvenanceService -> ... (not mocked PineryWS) ... -> PineryClient -> ProvenanceClient
        limsMock = mock(ca.on.oicr.pinery.api.Lims.class);
        pineryClient = pineryClientMock = mock(PineryClient.class);

        final SampleProvenanceService sampleProvenanceService = new DefaultSampleProvenanceService(limsMock);
        SampleProvenanceClient sampleProvenanceClientMock = mock(SampleProvenanceClient.class);
        doReturn(sampleProvenanceClientMock).when(pineryClientMock).getSampleProvenance();
        try {
            when(sampleProvenanceClientMock.all()).thenAnswer(new Answer<List<SampleProvenanceDto>>() {
                @Override
                public List<SampleProvenanceDto> answer(InvocationOnMock invocation) throws Throwable {
                    return Dtos.sampleProvenanceCollectionAsDto(sampleProvenanceService.getSampleProvenance());
                }
            });
        } catch (HttpResponseException ex) {
            throw new RuntimeException(ex);
        }

        final LaneProvenanceService laneProvenanceService = new DefaultLaneProvenanceService(limsMock);
        LaneProvenanceClient laneProvenanceClientMock = mock(LaneProvenanceClient.class);
        doReturn(laneProvenanceClientMock).when(pineryClientMock).getLaneProvenance();
        try {
            when(laneProvenanceClientMock.all()).thenAnswer(new Answer<List<LaneProvenanceDto>>() {
                @Override
                public List<LaneProvenanceDto> answer(InvocationOnMock invocation) throws Throwable {
                    return Dtos.laneProvenanceCollectionAsDto(laneProvenanceService.getLaneProvenance());
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
        PineryProvenanceProvider pineryProvenanceProvider = new PineryProvenanceProvider(pineryClientMock);
        DefaultProvenanceClient dpc = new DefaultProvenanceClient();
        dpc.registerAnalysisProvenanceProvider("seqware", new SeqwareMetadataAnalysisProvenanceProvider(metadata));
        dpc.registerSampleProvenanceProvider("pinery", pineryProvenanceProvider);
        dpc.registerLaneProvenanceProvider("pinery", pineryProvenanceProvider);
        provenanceClient = dpc;
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
