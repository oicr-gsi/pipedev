package ca.on.oicr.pde.utilities.workflows;

import ca.on.oicr.pde.tools.common.GSIOntologyManager;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.sourceforge.seqware.pipeline.workflowV2.model.SqwFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this code extends OICRWorkflow and provides means for attaching vetted
 * comma-separated list of cvterms to SqwFiles using tag 'cvterms'
 */
public abstract class SemanticWorkflow extends OicrWorkflow {
    private final Logger logger = LoggerFactory.getLogger(SemanticWorkflow.class);

    private GSIOntologyManager ontologies;
    public static final String CVTERM_TAG = "cvterms";
    private static final String CVTERM_SEPARATOR = ":";

    /**
     * This method needs to be implemented, all terms declared as static Map
     *
     * @return Map container with all CV terms used by a workflow
     */
    protected abstract Map<String, Set<String>> getTerms();

    /**
     * Instantiates GSIOntologyManager with the default config, which is a path
     * only accessible inside OICR.
     */
    public SemanticWorkflow() {
        this(null);
    }

    /**
     * Instantiate the GSI ontology manager with a custom ontology configuration
     * file.
     * 
     * @param configFile the full path to a ontology configuration file
     */
    public SemanticWorkflow(String configFile) {
        ontologies = new GSIOntologyManager(configFile);
    }
    
    
    /**
     * Method for Accessing OntologyManager
     *
     * @return the ontology manager
     */
    protected GSIOntologyManager getOntologyManager() {
        return this.ontologies;
    }

    /**
     * @param configFilePath path to ontology configuration
     */
    protected void configureOntologyManager(String configFilePath) {
        this.ontologies = new GSIOntologyManager(configFilePath);
    }

    @Deprecated
    protected void attachCVterms(SqwFile file, String ontID, String commaSepTerms) {
        logger.warn("Use of attachCVTerms is deprecated. Please use attachCVlabels");
        attachCVlabels(file, ontID, commaSepTerms);
    }

    /**
     * Convert a comma-separated list of labels to terms from loaded ontologies 
     * and then attach to files.
     *
     * @param file seqware file to attach terms to
     * @param ontID the ontology id
     * @param commaSepLabels comma separated list of ontology terms (must be a registered term)
     */
    protected void attachCVlabels(SqwFile file, String ontID, String commaSepLabels) {
        if (null == file || null == ontID || ontID.isEmpty() || null == commaSepLabels || commaSepLabels.isEmpty()) {
            logger.warn("Incorrect parameters provided to SemanticWorkflow: attachCVterms won't work. file="+file+" ontID="+ontID+" labels="+commaSepLabels);
            return;
        }
        // Set holds only unique items, so we just add everything to the Set
        HashSet<String> VettedTerms = new HashSet<>();
        Map<String, Set<String>> registeredTerms = this.getTerms();

        // If we have terms already, extract them and add to the Set (with validation)
        String commaSepExisting = file.getAnnotations().get(CVTERM_TAG);
        if (null != commaSepExisting) {
            if (!commaSepExisting.isEmpty()) {
                String[] oldTermArray = commaSepExisting.split(",");

                for (String t : oldTermArray) {
                    String[] checks = t.split(CVTERM_SEPARATOR);
                    if (checks.length == 2 && this.ontologies.isOntologySupported(checks[0])) { // we split by separator and we have 2-element array
                        if (this.ontologies.hasTerm(checks[1], checks[0])) {
                            if (!registeredTerms.containsKey(ontID) || !registeredTerms.get(ontID).contains(this.ontologies.termToLabel(checks[1], ontID))) {
                                logger.warn("Term " + t + " is not available via getTerms(), make sure its description (label) is registered");
                                continue;
                            }
                            VettedTerms.add(t);
                        }
                    }
                }
            }
        }

        //Check submitted terms (labels)
        String[] newLabelArray = commaSepLabels.split(",");
        for (String l : newLabelArray) {
            // check that the ontology is available
            if (!registeredTerms.containsKey(ontID)) {
                logger.warn("Ontology " + ontID + " is not available via getTerms(), make sure it is registered");
                continue;
            }
            // check that the label was registered
            else if (!registeredTerms.get(ontID).contains(l)){
                logger.warn("Label " + l + " is not available via getTerms(), make sure it is registered");
                continue;
            }
            
            //convert to term and add to vettedTerms
            if (this.ontologies.hasLabel(l, ontID)) {
                String term = this.ontologies.labelToTerm(l, ontID);
                if (null != term) {
                    VettedTerms.add(ontID + CVTERM_SEPARATOR + term);
                }
            }
        }

        //If we have nothing, do not proceed
        if (VettedTerms.isEmpty()) {
            return;
        }
        
        //Make a comma-separated list of vetted terms
        StringBuilder sb = new StringBuilder();
        for (String vetted : VettedTerms) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(vetted);
        }

        // Attach terms
        file.getAnnotations().remove(CVTERM_TAG);
        file.getAnnotations().put(CVTERM_TAG, sb.toString());

    }
}
