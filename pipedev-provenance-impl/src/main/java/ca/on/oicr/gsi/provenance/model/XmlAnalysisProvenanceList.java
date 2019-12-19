package ca.on.oicr.gsi.provenance.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "AnalysisProvenanceDtoList")
public class XmlAnalysisProvenanceList {

    private List<XmlAnalysisProvenance> analysisProvenanceDtos = new ArrayList<>();

    @XmlElement(name = "dto")
    public List<XmlAnalysisProvenance> getAnalysisProvenanceDtos() {
        return analysisProvenanceDtos;
    }

    public void setAnalysisProvenanceDtos(List<XmlAnalysisProvenance> analysisProvenanceDtos) {
        this.analysisProvenanceDtos = analysisProvenanceDtos;
    }

}
