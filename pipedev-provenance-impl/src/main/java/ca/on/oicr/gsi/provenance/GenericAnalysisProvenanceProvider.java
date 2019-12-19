package ca.on.oicr.gsi.provenance;

import ca.on.oicr.gsi.provenance.model.AnalysisProvenance;
import ca.on.oicr.gsi.provenance.model.XmlAnalysisProvenance;
import ca.on.oicr.gsi.provenance.model.XmlAnalysisProvenanceList;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 *
 * @author mlaszloffy
 */
public class GenericAnalysisProvenanceProvider implements AnalysisProvenanceProvider {

    private final CloseableHttpClient httpClient;
    private final String baseUrl;
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    }

    public static GenericAnalysisProvenanceProvider getProvider(Map<String, String> settings) {
        HttpClientBuilder client = HttpClientBuilder.create();

        if (settings.containsKey("user")) {
            String user = settings.get("user");
            String password = settings.get("password");
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password);
            credentialsProvider.setCredentials(AuthScope.ANY, credentials);
            client.setDefaultCredentialsProvider(credentialsProvider);
        }

        RequestConfig.Builder config = RequestConfig.custom();
        if (settings.containsKey("timeout")) {
            config.setSocketTimeout(Integer.parseInt(settings.get("timeout")) * 1000);

        }
        client.setDefaultRequestConfig(config.build());

        return new GenericAnalysisProvenanceProvider(settings.get("url"), client.build());
    }

    public GenericAnalysisProvenanceProvider(String url, CloseableHttpClient client) {
        this.httpClient = client;
        this.baseUrl = url;
    }

    public List<AnalysisProvenance> get(Map<FileProvenanceFilter, Set<String>> filters) throws JAXBException, IOException, XMLStreamException {
        HttpPost request = new HttpPost(baseUrl);

        StringEntity entity = new StringEntity(MAPPER.writeValueAsString(filters.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue))));
        request.setEntity(entity);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                Unmarshaller m = JAXBContext.newInstance(XmlAnalysisProvenanceList.class).createUnmarshaller();
                XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(response.getEntity().getContent());
                JAXBElement<XmlAnalysisProvenanceList> unmarshal = m.unmarshal(xmlStreamReader, XmlAnalysisProvenanceList.class);
                return new ArrayList<>(unmarshal.getValue().getAnalysisProvenanceDtos());
            }
        }

        return null;
    }

    @Override
    public Collection<AnalysisProvenance> getAnalysisProvenance() {
        try {
            return get(Collections.EMPTY_MAP);
        } catch (JAXBException | IOException | XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Collection<AnalysisProvenance> getAnalysisProvenance(Map<FileProvenanceFilter, Set<String>> map) {
        try {
            return get(map);
        } catch (JAXBException | IOException | XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() throws Exception {
        httpClient.close();
    }

}
