package ca.on.oicr.pde.utilities.workflows;

import ca.on.oicr.pde.tools.common.GSIOntologyManager;
import java.util.HashSet;
import java.util.Map;
import net.sourceforge.seqware.pipeline.workflowV2.model.SqwFile;

/**
 * this code extends OICRWorkflow and provides means for
 * attaching vetted comma-separated list of cvterms
 * to SqwFiles using tag 'cvterms'
 */
public abstract class SemanticWorkflow extends OicrWorkflow {

    private GSIOntologyManager  om              = new GSIOntologyManager(null);
    public  static final String CVTERM_TAG      = "cvterms";
    private static final String CVTERM_SEPARATOR = ":";

    /**
     * This method needs to be implemented, all terms declared as static Map
     * @return Map container with all CV terms used by a workflow
     */
    abstract Map<String, String[]> getTerms();

    /**
     * Method for Accessing OntologyManager
     * @return
     */
    protected GSIOntologyManager getOntologyManager() {
        return this.om;
    }

    /**
     * @param configFilePath
     */
    protected void configureOntologyManager(String configFilePath) {
        this.om = new GSIOntologyManager(configFilePath);
    }

    /**
     * While we accept a list of Human-readable terms (ontology labels) we
     * convert them to alphanumeric term ids and then attach to files
     *
     * @param file
     * @param ontID
     * @param commaSepTerms
     */
    protected void attachCVterms(SqwFile file, String ontID, String commaSepTerms) {

        if (null == file || null == ontID || ontID.isEmpty() || null == commaSepTerms || commaSepTerms.isEmpty()) {
            System.err.println("Nothing to do, attchCVterms won't work");
            return;
        }
        // Set holds only unique items, so we just add everything to the Set
        HashSet<String> VettedTerms = new HashSet<String>();

        // If we have terms already, extract them and add to the Set (with validation)
        String commaSepExisiting = file.getAnnotations().get(CVTERM_TAG);
        if (null != commaSepExisiting) {
            if (!commaSepExisiting.isEmpty()) {
                String[] oldTermArray = commaSepExisiting.split(",");

                for (String t : oldTermArray) {
                    String[] checks = t.split(CVTERM_SEPARATOR);
                    if (checks.length == 2 && this.om.isOntologySupported(checks[0])) { // we split by separator and we have 2-element array
                        if (this.om.hasTerm(checks[1], checks[0])) {
                            StringBuilder vettedFormatted = new StringBuilder();
                            vettedFormatted.append(checks[0])
                                    .append(CVTERM_SEPARATOR)
                                    .append(t);
                            VettedTerms.add(vettedFormatted.toString());
                        }
                    }
                }
            }
        }

        //Check submitted terms (labels)
        String[] newTermArray = commaSepTerms.split(",");
        for (String t : newTermArray) {

            if (this.om.hasLabel(t, ontID)) {
                String term = this.om.labelToTerm(ontID, t);
                if (null != term) {
                    StringBuilder vettedFormatted = new StringBuilder();
                    vettedFormatted.append(ontID)
                            .append(CVTERM_SEPARATOR)
                            .append(term);
                    VettedTerms.add(vettedFormatted.toString());
                }
            }
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
