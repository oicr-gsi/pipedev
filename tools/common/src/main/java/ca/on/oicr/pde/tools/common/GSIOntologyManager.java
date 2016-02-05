/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.oicr.pde.tools.common;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class for manipulating CV terms Changes to OICR Decider/Workflow are required
 * to standardize access to this class (for JUnit testing)
 *
 * @author pruzanov
 */
public class GSIOntologyManager {

    // Default config with supported ontologies and paths to owl files
    public static final String DEFAULT_CONFIG = "/.mounts/labs/PDE/data/reference/ontology/ontologies.conf";
    private Map<String, OntoData> ontSupported;
    public boolean initialized = false;

    /**
     * Constructor that accepts configFile as an argument
     *
     * @param configFile path to config file (external classes using
     * GSIOntologyManager may supply a custom file)
     */
    public GSIOntologyManager(String configFile) {

        String configPath = null != configFile ? configFile : DEFAULT_CONFIG;
        try {
            this.init(configPath);
        } catch (IllegalArgumentException e) {
            System.err.append("There was an error Initializing OntoTester");
            System.exit(0);
        }
    }

    /**
     * Load supported oOntologies and write this information into private Map
     *
     * @param config
     * @throws IllegalArgumentException
     */
    private void init(String config) throws IllegalArgumentException {
        this.ontSupported = this.loadSupportedOntologies(config);
        if (null == this.ontSupported) {
            throw new IllegalArgumentException("Error Initializing Ontologies");
        }
        this.initialized = true;
    }

    /**
     * loadIfNeeded checks if we have an appropriate OntModel available, if not
     * tries to load it returns a booleans value indicating success
     *
     * @param ONT
     * @return
     * @throws FileNotFoundException
     */
    private boolean loadIfNeeded(String ONT) throws FileNotFoundException {

        if (!this.ontSupported.containsKey(ONT)) {
            System.err.println("Unsupported ontology requested");
            return false;
        }

        // If needed, read model
        if (null == this.ontSupported.get(ONT).getModel()) {
            this.ontSupported.get(ONT).setModel(this.loadOntologyModel(ONT));
            return true;
        }

        return null != this.ontSupported.get(ONT).getModel();

    }

    /**
     * A check method that may be useful for term vetting
     *
     * @param ontID
     * @return
     */
    public boolean isOntologySupported(String ontID) {
        return this.ontSupported.containsKey(ontID);
    }

    /**
     * Test term passed to this method - such as data_0005 (EDAM)
     *
     * @param ontID string identifying ontology (EDAM, OBI, MGED...)
     * @param term alphanumeric ontology class id such as data_0005 (EDAM)
     * @return
     */
    public boolean hasTerm(String term, String ontID) {
        // If Model is null, load it
        try {
            if (!this.loadIfNeeded(ontID)) {
                return false;
            }
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load Ontologies");
            return false;
        }

        String NS = this.ontSupported.get(ontID).getNS();
        String SOURCE = this.ontSupported.get(ontID).getSOURCE();
        OntModel m = this.ontSupported.get(ontID).getModel();

        String searchTerm = null != NS ? SOURCE + NS + term : SOURCE + term;
        OntClass testClass = m.getOntClass(searchTerm);
        return null != testClass;

    }

    /**
     * Test if we have any classes with label matching the argument
     *
     * @param ontID string identifying ontology (EDAM, OBI, MGED...)
     * @param label human-readable ontology label
     * @return
     */
    public boolean hasLabel(String label, String ontID) {
        // If Model is null, load it
        try {
            if (!this.loadIfNeeded(ontID)) {
                return false;
            }
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load Ontologies");
            return false;
        }

        OntModel m = this.ontSupported.get(ontID).getModel();

        List<OntClass> classes = m.listClasses().toList();
        for (OntClass c : classes) {

            String cLabel = c.getLabel(null);
            if (cLabel != null) {
                if (cLabel.equalsIgnoreCase(label)) {
                    System.out.println("Matching label found");
                    return true;
                } else if (cLabel.contains(label) || label.contains(cLabel)) {
                    System.out.println("Partial match between " + label + " And " + cLabel);
                }
            }
        }

        return false;
    }

    /**
     * Convert a term to a human-readable label
     *
     * @param ontID string identifying ontology (EDAM, OBI, MGED...)
     * @param term alphanumeric ontology class id such as data_0005 (EDAM)
     * @return
     */
    public String termToLabel(String term, String ontID) {
        String NS     = this.ontSupported.get(ontID).getNS();
        String SOURCE = this.ontSupported.get(ontID).getSOURCE();

        try {
            // If Model is null, load it
            if (!this.loadIfNeeded(ontID)) {
                return null;
            }
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load Ontologies");
            return null;
        } catch (NullPointerException np) {
            System.err.println("Ontology is not supported");
            return null;
        }

        OntModel m = this.ontSupported.get(ontID).getModel();

        String searchTerm = null != NS ? SOURCE + NS + term : SOURCE + term;

        OntClass testPizza = m.getOntClass(searchTerm);
        if (null != testPizza) {
            System.out.println("Successfully retrieved class");
            return testPizza.getLabel(null);
        } else {
            System.err.println("Error getting label for term " + term);
        }

        return null;
    }

    /**
     * Convert a human-readable label to Ontology term
     *
     * @param ontID string identifying ontology (EDAM, OBI, MGED...)
     * @param label human-readable ontology label
     * @return
     */
    public String labelToTerm(String label, String ontID) {

        String NS     = this.ontSupported.get(ontID).getNS();;
        String SOURCE = this.ontSupported.get(ontID).getSOURCE();

        try {
            // If Model is null, load it
            if (!this.loadIfNeeded(ontID)) {
                return null;
            }
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load Ontologies");
            return null;
        } catch (NullPointerException np) {
            System.err.println("Ontology is not supported");
            return null;
        }

        OntModel m = this.ontSupported.get(ontID).getModel();

        List<OntClass> classes = m.listClasses().toList();
        for (OntClass c : classes) {

            String cLabel = c.getLabel(null);
            if (cLabel != null) {
                if (cLabel.equalsIgnoreCase(label)) {
                    System.out.println("Matching label found");
                    String term = c.asNode().toString();
                    if (term.contains(SOURCE)) {
                        term = term.substring(term.indexOf(SOURCE) + SOURCE.length());
                    }

                    if (null != NS) {
                        term = term.substring(term.indexOf(NS) + NS.length());
                    }
                    return term;

                } else if (cLabel.contains(label) || label.contains(cLabel)) {
                    System.out.println("Partial match between " + label + " And " + cLabel);
                }
            }
        }

        return null;
    }

    /**
     * ========================Utility Methods===================== Loading
     * ontology meta data and reading actual RDFS/OWL files
     * ============================================================
     */
    /**
     * Service method for loading ontology data and initializing Ont models
     */
    private OntModel loadOntologyModel(String ontID) throws FileNotFoundException {

        String fileName = this.ontSupported.get(ontID).getFilePath();
        String SOURCE = this.ontSupported.get(ontID).getSOURCE();

        FileManager.get().addLocatorClassLoader(GSIOntologyManager.class.getClassLoader());

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        OntDocumentManager dm = model.getDocumentManager();
        dm.setCacheModels(false);
        dm.addAltEntry(SOURCE, "file:" + fileName);
        model.read(SOURCE);
        if (model.isEmpty()) {
            throw new FileNotFoundException("Error Loading Ontology information from file for " + ontID);
        }

        return model;
    }

    /**
     * Service method for loading ontology meta data
     */
    private Map<String, OntoData> loadSupportedOntologies(String confFilePath) {

        Map<String, OntoData> ontsMapped = new HashMap<String, OntoData>();
        try {
            File fXmlFile = new File(confFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("ontology");
            for (int temp = 0; temp < nList.getLength(); temp++) {

                String currentOntology = "";
                String currentPath = "";
                String currentSource = "";
                String currentNs = null;

                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element nElement = (Element) nNode;
                    currentOntology = nElement.getAttribute("id");
                    currentPath = nElement.getElementsByTagName("local_file").item(0).getTextContent();
                    currentSource = nElement.getElementsByTagName("source").item(0).getTextContent();
                    if (null != nElement.getElementsByTagName("ns") && nElement.getElementsByTagName("ns").getLength() > 0) {
                        currentNs = nElement.getElementsByTagName("ns").item(0).getTextContent();
                    }
                }
                if (!currentOntology.isEmpty() && !currentPath.isEmpty()) {
                    ontsMapped.put(currentOntology, new OntoData(currentPath, currentSource, currentNs));
                }
            }
        } catch (FileNotFoundException fnf) {
            System.err.println("File is not found");
            return null;
        } catch (NullPointerException np) {
            System.err.println("Empty ontology entry in config xml file");
            return null;
        } catch (ParserConfigurationException | SAXException | IOException | DOMException e) {
            System.err.println("Error parsing ontology data");
            return null;
        }
        return ontsMapped;
    }

}
