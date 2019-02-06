package ca.on.oicr.gsi.provenance;

import ca.on.oicr.gsi.provenance.model.SampleProvenance;
import ca.on.oicr.gsi.provenance.model.LaneProvenance;
import ca.on.oicr.pinery.client.HttpResponseException;
import ca.on.oicr.pinery.client.PineryClient;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mlaszloffy
 */
public class PineryProvenanceProvider implements SampleProvenanceProvider, LaneProvenanceProvider {

    private static final String DEFAULT_VERSION = "v1";
	private final PineryClient pineryClient;
    private final String version;

    public PineryProvenanceProvider(Map<String, String> settings) {
        if (!settings.containsKey("url")) {
            throw new RuntimeException("PineryProvenanceProvider \"url\" setting is missing");
        }
        this.pineryClient = new PineryClient(settings.get("url"), true);
        version = settings.get("version");
    }

    public PineryProvenanceProvider(String url) {
        this(new PineryClient(url, true));
    }
    
    public PineryProvenanceProvider(PineryClient pineryClient) {
        this(pineryClient, DEFAULT_VERSION);
    }
    
    public PineryProvenanceProvider(PineryClient pineryClient, String version) {
        this.pineryClient = pineryClient;
        this.version = version;
    }

    @Override
    public Collection<? extends SampleProvenance> getSampleProvenance() {
        try {
            return pineryClient.getSampleProvenance().version(version);
        } catch (HttpResponseException hre) {
            throw new RuntimeException(hre);
        }
    }

    @Override
    public Collection<? extends SampleProvenance> getSampleProvenance(Map<FileProvenanceFilter, Set<String>> filters) {
        Set<SampleProvenance> sps = new HashSet<>();
        for (SampleProvenance sp : getSampleProvenance()) {
            if (filters.containsKey(FileProvenanceFilter.sample)) {
                Set<String> sampleValues = new HashSet<>();
                if (sp.getSampleProvenanceId() != null) {
                    sampleValues.add(sp.getSampleProvenanceId());
                }
                if (sp.getSampleName() != null) {
                    sampleValues.add(sp.getSampleName());
                }
                if (Sets.intersection(sampleValues, filters.get(FileProvenanceFilter.sample)).isEmpty()) {
                    continue;
                }
            }

            if (filters.containsKey(FileProvenanceFilter.sequencer_run)) {
                Set<String> sampleValues = new HashSet<>();
                if (sp.getSequencerRunName() != null) {
                    sampleValues.add(sp.getSequencerRunName());
                }
                if (Sets.intersection(sampleValues, filters.get(FileProvenanceFilter.sequencer_run)).isEmpty()) {
                    continue;
                }
            }

            if (filters.containsKey(FileProvenanceFilter.lane)) {
                Set<String> sampleValues = new HashSet<>();
                if (sp.getLaneNumber() != null) {
                    sampleValues.add(sp.getLaneNumber());
                }
//                if (sp.getLaneName() != null) {
//                    sampleValues.add(sp.getLaneName());
//                }
                if (Sets.intersection(sampleValues, filters.get(FileProvenanceFilter.lane)).isEmpty()) {
                    continue;
                }
            }

            sps.add(sp);
        }
        return sps;
    }

    @Override
    public Collection<? extends LaneProvenance> getLaneProvenance() {
        try {
            return pineryClient.getLaneProvenance().version(version);
        } catch (HttpResponseException hre) {
            throw new RuntimeException(hre);
        }
    }

    @Override
    public Collection<LaneProvenance> getLaneProvenance(Map<FileProvenanceFilter, Set<String>> filters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	@Override
	public void close() throws Exception {
        pineryClient.close();
	}

}
