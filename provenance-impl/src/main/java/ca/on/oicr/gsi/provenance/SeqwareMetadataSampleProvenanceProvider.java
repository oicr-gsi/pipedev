package ca.on.oicr.gsi.provenance;

import ca.on.oicr.gsi.provenance.model.SampleProvenance;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.model.FileProvenanceParam;

/**
 *
 * @author mlaszloffy
 */
public class SeqwareMetadataSampleProvenanceProvider implements SampleProvenanceProvider {

    private final Metadata metadata;

    public SeqwareMetadataSampleProvenanceProvider(Metadata metadata) {
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
                if (!filters.get(FileProvenanceParam.sample.toString()).contains(sp.getSampleProvenanceId())) {
                    continue;
                }
            }
            sps.add(sp);
        }
        return sps;
    }

}
