package ca.on.oicr.pde.tools.common;

import com.hp.hpl.jena.ontology.OntModel;

public class OntoData {
    
    // Things that we need to know about ontology source
    private String SOURCE;
    private String NS;
    private String filePath;
    
    private OntModel model;
    
    public OntoData(String path, String s, String ns) throws NullPointerException {
        if (null != s && !s.isEmpty()) {
            this.setSOURCE(s);
        } else {
            throw new NullPointerException("Source can not be empty");
        }
        
        if (null != path && !path.isEmpty()) {
            this.setFilePath(path);
        } else {
            throw new NullPointerException("Path to the Ontology file can not be empty");    
        }
        
        // NS can be null
        this.setNS(ns);
    }

    /**
     * @return the SOURCE
     */
    public String getSOURCE() {
        return SOURCE;
    }

    /**
     * @param SOURCE the SOURCE to set
     */
    private void setSOURCE(String SOURCE) {
        this.SOURCE = SOURCE;
    }

    /**
     * @return the NS
     */
    public String getNS() {
        return NS;
    }

    /**
     * @param NS the NS to set
     */
    private void setNS(String NS) {
        this.NS = NS;
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath the filePath to set
     */
    private void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * @return the model
     */
    public OntModel getModel() {
        return model;
    }

    /**
     * @param model the model to set
     */
    public final void setModel(OntModel model) {
        this.model = model;
    }
    
    
}
