package ca.on.oicr.gsi.provenance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author mlaszloffy
 */
public class ProviderLoader {

    private final Map<String, AnalysisProvenanceProvider> analysisProvenanceProviders = new HashMap<>();
    private final Map<String, SampleProvenanceProvider> sampleProvenanceProviders = new HashMap<>();
    private final Map<String, LaneProvenanceProvider> laneProvenanceProviders = new HashMap<>();
    private final List<Provider> providers;

    public ProviderLoader(String providerJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        providers = Arrays.asList(mapper.readValue(providerJson, Provider[].class));
        build();
    }

    public ProviderLoader(List<Provider> providers) {
        this.providers = providers;
        build();
    }

    public ProviderLoader(Path providerJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        providers = Arrays.asList(mapper.readValue(providerJson.toFile(), Provider[].class));
        build();
    }

    private void build() {
        for (Provider p : providers) {
            if (p.getType() == null || p.getProvider() == null) {
                throw new RuntimeException("Missing provider information: " + ToStringBuilder.reflectionToString(p));
            } else if (PineryProvenanceProvider.class.getCanonicalName().equals(p.getType())) {
                //two pinery providers are required for concurrency (current PineryClient can only handle one concurrent request)
                PineryProvenanceProvider pineryProvenanceProvider1 = new PineryProvenanceProvider(p.getProviderSettings());
                if (sampleProvenanceProviders.put(p.getProvider(), pineryProvenanceProvider1) != null) {
                    throw new RuntimeException("Duplicate provider: " + p.getProvider());
                }
                PineryProvenanceProvider pineryProvenanceProvider2 = new PineryProvenanceProvider(p.getProviderSettings());
                if (laneProvenanceProviders.put(p.getProvider(), pineryProvenanceProvider2) != null) {
                    throw new RuntimeException("Duplicate provider: " + p.getProvider());
                }
            } else if (SeqwareMetadataLimsMetadataProvenanceProvider.class.getCanonicalName().equals(p.getType())) {
                SeqwareMetadataLimsMetadataProvenanceProvider metadataProvenanceProvider = new SeqwareMetadataLimsMetadataProvenanceProvider(p.getProviderSettings());
                if (sampleProvenanceProviders.put(p.getProvider(), metadataProvenanceProvider) != null) {
                    throw new RuntimeException("Duplicate provider: " + p.getProvider());
                }
                if (laneProvenanceProviders.put(p.getProvider(), metadataProvenanceProvider) != null) {
                    throw new RuntimeException("Duplicate provider: " + p.getProvider());
                }
            } else if (SeqwareMetadataAnalysisProvenanceProvider.class.getCanonicalName().equals(p.getType())) {
                SeqwareMetadataAnalysisProvenanceProvider metadataProvenanceProvider = new SeqwareMetadataAnalysisProvenanceProvider(p.getProviderSettings());
                if (analysisProvenanceProviders.put(p.getProvider(), metadataProvenanceProvider) != null) {
                    throw new RuntimeException("Duplicate provider: " + p.getProvider());
                }
            } else {
                throw new RuntimeException("Unsupported provider: " + p.getType());
            }
        }
    }

    public Map<String, AnalysisProvenanceProvider> getAnalysisProvenanceProviders() {
        return analysisProvenanceProviders;
    }

    public Map<String, SampleProvenanceProvider> getSampleProvenanceProviders() {
        return sampleProvenanceProviders;
    }

    public Map<String, LaneProvenanceProvider> getLaneProvenanceProviders() {
        return laneProvenanceProviders;
    }

    public String getProvidersAsJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            return mapper.writeValueAsString(providers);
        } catch (JsonProcessingException ex) {
//            Logger.getLogger(ProviderLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    @Data
    public static class Provider {

        public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getProvider() {
			return provider;
		}
		public void setProvider(String provider) {
			this.provider = provider;
		}
		public Map<String, String> getProviderSettings() {
			return providerSettings;
		}
		public void setProviderSettings(Map<String, String> providerSettings) {
			this.providerSettings = providerSettings;
		}
		private String type;
        private String provider;
        private Map<String, String> providerSettings;
    }

}
