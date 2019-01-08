package ca.on.oicr.gsi.provenance;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ca.on.oicr.gsi.provenance.model.AnalysisProvenance;
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
		return metadata.getAnalysisProvenance().stream().map(x->x).collect(Collectors.toList());
	}

	@Override
	public Collection<AnalysisProvenance> getAnalysisProvenance(Map<FileProvenanceFilter, Set<String>> filters) {
		return metadata.getAnalysisProvenance(filters).stream().map(x->x)
				.collect(Collectors.toList());
	}

	@Override
	public void close() throws Exception {
		metadata.clean_up();
	}

}
