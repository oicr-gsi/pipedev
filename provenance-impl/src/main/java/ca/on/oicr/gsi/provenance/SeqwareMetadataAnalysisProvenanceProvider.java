package ca.on.oicr.gsi.provenance;

import ca.on.oicr.gsi.provenance.model.AnalysisProvenance;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.metadata.MetadataFactory;

/**
 *
 * @author mlaszloffy
 */
public class SeqwareMetadataAnalysisProvenanceProvider implements AnalysisProvenanceProvider {

    private final Metadata metadata;

    public SeqwareMetadataAnalysisProvenanceProvider(Map<String, String> settings) {
        this.metadata = MetadataFactory.get(settings);
    }

    public SeqwareMetadataAnalysisProvenanceProvider(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public Collection<AnalysisProvenance> getAnalysisProvenance() {
        return (List<AnalysisProvenance>) (List<?>) metadata.getAnalysisProvenance();
    }

    @Override
    public Collection<AnalysisProvenance> getAnalysisProvenance(Map<String, Set<String>> filters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
