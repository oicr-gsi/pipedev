/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.oicr.pde.tools.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class for manipulating CV terms
 * Changes to OICR Decider/Workflow are required to standardize access to this class (for JUnit testing)
 * @author pruzanov
 */
public class GSIOntologyManager {
    
 // Default config with supported ontologies and paths to owl files
 protected static final String DEFAULT_CONFIG   = "/.mount/labs/PDE/data/reference/ontology/ontologies.conf";
 protected static final String CVTERM_SEPARATOR = ":";
 // Store all cv terms in a set, no duplicates allowed
 private final Set<String> cvterms;
 private Map <String, String> ontologies;
 
 public GSIOntologyManager (String[] terms, String configFile) {
    this.cvterms = new HashSet<String>();
    this.cvterms.addAll(Arrays.asList(terms));
    
    String fileToLoad = configFile == null || configFile.isEmpty() ? DEFAULT_CONFIG : configFile;
    //TODO load file, supported ontologes and path to owl
    this.ontologies = loadSupportedOntologies(fileToLoad);
 }

 
 protected String formatCVterm (String ontology, String term) throws IllegalArgumentException {
     
    StringBuilder fterm = new StringBuilder();
     
    // Check if the term was added to set of terms
    if (!this.cvterms.contains(term)) {
        throw new IllegalArgumentException ("Term " + term + " needs to be in cvterms Set, aborting");
    }
     
    // Check if the ontology supported
    if (!this.ontologies.containsKey(ontology)) {
        throw new IllegalArgumentException ("Ontology " + ontology + " is not supported by current configuration, aborting");
    }
     
    fterm.append(ontology).append(CVTERM_SEPARATOR).append(term);
    return fterm.toString();    
     
 }
 

 protected Map<String,String> loadSupportedOntologies(String fileName) {

        Map<String,String> ontsMapped = new HashMap<String,String>();
        try {
            File fXmlFile = new File(fileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("ontology");
            for (int temp = 0; temp < nList.getLength(); temp++) {

                String currentOntology = "";
                String currentPath     = "";
                
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element nElement = (Element) nNode;
                    currentOntology = nElement.getAttribute("id");
                    currentPath     = nElement.getElementsByTagName("local_file").item(0).getTextContent();
                }
                if (!currentOntology.isEmpty() && !currentPath.isEmpty()) {
                    ontsMapped.put(currentOntology, currentPath);
                }
            }
        } catch (FileNotFoundException fnf) {
            System.err.println("File is not found");
        } catch (NullPointerException np) {
            System.err.println("Empty ontology entry in config xml file");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ontsMapped;
    }
 
 /**
  * methods below are for actual loading of ontologies and validating terms
  */
 
 //TODO method for reading ontology info from an owl file, depends on API choice
 private void readLocally () {  
     System.err.println("checkForUpdates Not implemented yet");
 }
 
 private void readRemotely() {
     System.err.println("checkForUpdates Not implemented yet");
 }
 
 //TODO method for checking for update (warn if update available)
 private void checkForUpdates () {
     System.err.println("checkForUpdates Not implemented yet");
 }
 //TODO methods for validation
 private boolean isTermValid (String term) {
     return false;
 }


}
