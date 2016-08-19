package ca.on.oicr.gsi.provenance;

import ca.on.oicr.gsi.provenance.model.FileProvenanceParam;
import ca.on.oicr.gsi.provenance.model.LaneProvenance;
import ca.on.oicr.gsi.provenance.model.SampleProvenance;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.metadata.MetadataFactory;

/**
 *
 * @author mlaszloffy
 */
public class SeqwareMetadataLimsMetadataProvenanceProvider implements SampleProvenanceProvider, LaneProvenanceProvider {

    private final Metadata metadata;

    public SeqwareMetadataLimsMetadataProvenanceProvider(Map<String, String> settings) {
        this.metadata = MetadataFactory.get(settings);
    }

    public SeqwareMetadataLimsMetadataProvenanceProvider(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public Collection<SampleProvenance> getSampleProvenance() {
        return (List<SampleProvenance>) (List<?>) metadata.getSampleProvenance();
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
        return (List<LaneProvenance>) (List<?>) metadata.getLaneProvenance();
    }

    @Override
    public Collection<LaneProvenance> getLaneProvenance(Map<String, Set<String>> filters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}