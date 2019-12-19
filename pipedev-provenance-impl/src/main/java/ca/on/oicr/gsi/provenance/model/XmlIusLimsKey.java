package ca.on.oicr.gsi.provenance.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class XmlIusLimsKey implements IusLimsKey {

    private Integer iusSWID;
    private XmlLimsKey limsKey;

    @Override
    public Integer getIusSWID() {
        return iusSWID;
    }

    public void setIusSWID(Integer iusSWID) {
        this.iusSWID = iusSWID;
    }

    @Override
    public LimsKey getLimsKey() {
        return limsKey;
    }

    public void setLimsKey(XmlLimsKey limsKey) {
        this.limsKey = limsKey;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((iusSWID == null) ? 0 : iusSWID.hashCode());
        result = prime * result + ((limsKey == null) ? 0 : limsKey.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof XmlIusLimsKey)) {
            return false;
        }
        XmlIusLimsKey other = (XmlIusLimsKey) obj;
        if (iusSWID == null) {
            if (other.iusSWID != null) {
                return false;
            }
        } else if (!iusSWID.equals(other.iusSWID)) {
            return false;
        }
        if (limsKey == null) {
            if (other.limsKey != null) {
                return false;
            }
        } else if (!limsKey.equals(other.limsKey)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
