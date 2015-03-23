package ca.on.oicr.pde.deciders;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.sourceforge.seqware.common.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A utility class to parse the rsconfig.xml file format into an Rsconfig object. The Rsconfig object is used to map reference (template
 * type + resequencing type pair) to their config key-value pairs. A typical rsconfig file will be in the following format:
 * <pre>
 * {@code
 * <references>
 *   <resequencing_type id="">
 *     <template_type>WG</template_type>
 *     <interval_file>/path/to/file0.bed</interval_file>
 *   </resequencing_type>
 *   <resequencing_type id="Type1">
 *     <template_type>EX</template_type>
 *     <interval_file>/path/to/file1.bed</interval_file>
 *   </resequencing_type>
 *   ...
 * </references>
 * }
 * </pre>
 *
 * @author mlaszloffy
 */
public class Rsconfig {

    private Map<String, Reference> references;

    private Rsconfig() {
    }

    /**
     * An object representation of the rsconfig.xml file format.
     *
     * The rsconfig.xml file is expected to be in the format of:
     * <pre>
     * {@code
     * <references>
     *   <resequencing_type id="">
     *     <template_type>WG</template_type>
     *     <interval_file>/path/to/file0.bed</interval_file>
     *   </resequencing_type>
     *   <resequencing_type id="Type1">
     *     <template_type>EX</template_type>
     *     <interval_file>/path/to/file1.bed</interval_file>
     *   </resequencing_type>
     *   ...
     * </references>
     * }
     * </pre>
     *
     * @param rsconfigPath Path to the rsconfig.xml file
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws InvalidFileFormatException
     */
    public Rsconfig(File rsconfigPath) throws ParserConfigurationException, SAXException, IOException, InvalidFileFormatException {
        references = loadRsconfig(rsconfigPath);
    }

    /**
     * Get a the config key value for the specific reference type (template type + resequencing type).
     *
     * @param templateType reference template type
     * @param resequencingType reference resequencing type
     * @param configKey the reference config value
     * @return the reference's config value
     */
    public String get(String templateType, String resequencingType, String configKey) {
        String value = null;
        Reference r = references.get(Reference.getKey(templateType, resequencingType));
        if (r != null) {
            value = r.get(configKey);
        }
        return value;
    }

    private Map<String, Reference> loadRsconfig(File rsconfigFilePath) throws ParserConfigurationException, SAXException, IOException, InvalidFileFormatException {
        Map<String, Reference> refs = new HashMap<>();
        Element eElement = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(rsconfigFilePath);
        doc.getDocumentElement().normalize();
        //aka references
        NodeList nList = doc.getElementsByTagName("resequencing_type");
        //NodeList nList = doc.getElementsByTagName("reference");
        if (nList.getLength() == 0) {
            //would be better if unchecked, but seqware catches unchecked :(
            throw new InvalidFileFormatException("Error: no references found in [" + rsconfigFilePath + "].");
        }
        //iterate through all references
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                eElement = (Element) nNode;

                if (!eElement.hasAttribute("id")) {
                    throw new InvalidFileFormatException("Error: reference is missing \"id\".");
                }
                String resequencingTypeId = eElement.getAttribute("id");
                //String resequencingType = eElement.getElementsByTagName("resequencing_type").item(0).getTextContent();

                if (eElement.getElementsByTagName("template_type").getLength() != 1) {
                    throw new InvalidFileFormatException("Error: reference \"template_type\" check failed.");
                }
                String templateType = eElement.getElementsByTagName("template_type").item(0).getTextContent();

                Reference r = new Reference(templateType, resequencingTypeId);
                Log.debug("Reference=[" + r.toString() + "]");
                NodeList l = eElement.getElementsByTagName("*");
                for (int j = 0; j < l.getLength(); j++) {
                    Element e = (Element) l.item(j);
                    String key = e.getNodeName();
                    String value = e.getTextContent();
                    Log.debug("Key=[" + key + "] Value=[" + value + "]");
                    if (r.put(key, value) != null) {
                        throw new InvalidFileFormatException("Error: duplicate config key found found in reference with template type = [" + templateType
                                + "], resequencing type = [" + resequencingTypeId + "], config key = [" + key + "].");
                    }
                }
                if (refs.put(r.toString(), r) != null) {
                    throw new InvalidFileFormatException("Error: duplicate reference found with template type = [" + templateType
                            + "], resequencing type = [" + resequencingTypeId + "].");
                }
            }
        }
        return refs;
    }

    public static class Reference {

        //attrs used for the key
        private final String templateType;
        private final String resequencingType;

        private final Map<String, String> config = new HashMap<>();

        public Reference(String templateType, String resequencingType) {
            this.templateType = templateType;
            this.resequencingType = resequencingType;
        }

        @Override
        public String toString() {
            return getKey(templateType, resequencingType);
        }

        public String put(String key, String value) {
            return config.put(key, value);
        }

        public String get(String key) {
            return config.get(key);
        }

        public static String getKey(String templateType, String resequencingType) {
            //TODO: escape special characters in template type and resquencing type
            return ((templateType == null) ? "" : templateType) + "_" + ((resequencingType == null) ? "" : resequencingType);
        }
    }

    public static class InvalidFileFormatException extends Exception {

        public InvalidFileFormatException(String message) {
            super(message);
        }
    }

}
