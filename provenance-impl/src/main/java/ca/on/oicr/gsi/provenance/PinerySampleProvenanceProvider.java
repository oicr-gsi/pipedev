package ca.on.oicr.gsi.provenance;

import ca.on.oicr.gsi.provenance.model.SampleProvenance;
import ca.on.oicr.gsi.provenance.model.FileProvenanceParam;
import ca.on.oicr.pinery.client.HttpResponseException;
import ca.on.oicr.pinery.client.PineryClient;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mlaszloffy
 */
public class PinerySampleProvenanceProvider implements SampleProvenanceProvider {

    private final PineryClient pineryClient;

    public PinerySampleProvenanceProvider(String url) {
        this.pineryClient = new PineryClient(url, true);
    }

    public PinerySampleProvenanceProvider(PineryClient pineryClient) {
        this.pineryClient = pineryClient;
    }

    @Override
    public Collection<SampleProvenance> getSampleProvenance() {
        try {
            return (List<SampleProvenance>) (List<?>) pineryClient.getSampleProvenance().all();
        } catch (HttpResponseException hre) {
            throw new RuntimeException(hre);
        }
    }

    @Override
    public Collection<SampleProvenance> getSampleProvenance(Map<String, Set<String>> filters) {
        Set<SampleProvenance> sps = new HashSet<>();
        for (SampleProvenance sp : getSampleProvenance()) {
            if (filters.containsKey(FileProvenanceParam.sample.toString())) {
                if (!filters.get(FileProvenanceParam.sample.toString()).contains(sp.getSampleProvenanceId())) {
                    continue;
                }
            }
            sps.add(sp);
        }
        return sps;
    }

}
