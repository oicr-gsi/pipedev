package ca.on.oicr.gsi.provenance;

import ca.on.oicr.gsi.provenance.model.SampleProvenance;
import ca.on.oicr.gsi.provenance.model.FileProvenanceParam;
import ca.on.oicr.gsi.provenance.model.LaneProvenance;
import ca.on.oicr.pinery.client.HttpResponseException;
import ca.on.oicr.pinery.client.PineryClient;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mlaszloffy
 */
public class PineryProvenanceProvider implements SampleProvenanceProvider, LaneProvenanceProvider {

    private final PineryClient pineryClient;

    public PineryProvenanceProvider(Map<String, String> settings) {
        if (!settings.containsKey("url")) {
            throw new RuntimeException("PineryProvenanceProvider \"url\" setting is missing");
        }
        this.pineryClient = new PineryClient(settings.get("url"), true);
    }

    public PineryProvenanceProvider(String url) {
        this.pineryClient = new PineryClient(url, true);
    }

    public PineryProvenanceProvider(PineryClient pineryClient) {
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
                Set<String> sampleValues = new HashSet<>();
                if (sp.getSampleProvenanceId() != null) {
                    sampleValues.add(sp.getSampleProvenanceId());
                }
                if (sp.getSampleName() != null) {
                    sampleValues.add(sp.getSampleName());
                }
                if (Sets.intersection(sampleValues, filters.get(FileProvenanceParam.sample.toString())).isEmpty()) {
                    continue;
                }
            }

            if (filters.containsKey(FileProvenanceParam.sequencer_run.toString())) {
                Set<String> sampleValues = new HashSet<>();
                if (sp.getSequencerRunName() != null) {
                    sampleValues.add(sp.getSequencerRunName());
                }
                if (Sets.intersection(sampleValues, filters.get(FileProvenanceParam.sequencer_run.toString())).isEmpty()) {
                    continue;
                }
            }

            if (filters.containsKey(FileProvenanceParam.lane.toString())) {
                Set<String> sampleValues = new HashSet<>();
                if (sp.getLaneNumber() != null) {
                    sampleValues.add(sp.getLaneNumber());
                }
//                if (sp.getLaneName() != null) {
//                    sampleValues.add(sp.getLaneName());
//                }
                if (Sets.intersection(sampleValues, filters.get(FileProvenanceParam.lane.toString())).isEmpty()) {
                    continue;
                }
            }

            sps.add(sp);
        }
        return sps;
    }

    @Override
    public Collection<LaneProvenance> getLaneProvenance() {
        try {
            return (List<LaneProvenance>) (List<?>) pineryClient.getLaneProvenance().all();
        } catch (HttpResponseException hre) {
            throw new RuntimeException(hre);
        }
    }

    @Override
    public Collection<LaneProvenance> getLaneProvenance(Map<String, Set<String>> filters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
