package ca.on.oicr.gsi.provenance.model;

import javax.xml.bind.annotation.XmlAttribute;

public class IusLimsKeyDtoType {

    private Integer iusSwid;
    private LimsKeyType limsKey;

    @XmlAttribute
    public Integer getIusSwid() {
        return iusSwid;
    }

    public void setIusSwid(Integer iusSwid) {
        this.iusSwid = iusSwid;
    }

    public LimsKeyType getLimsKey() {
        return limsKey;
    }

    public void setLimsKey(LimsKeyType limsKey) {
        this.limsKey = limsKey;
    }

}
