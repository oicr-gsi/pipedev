package ca.on.oicr.pde.model;

import java.util.Set;
import net.sourceforge.seqware.common.model.Annotatable;
import net.sourceforge.seqware.common.model.FirstTierModel;

/**
 *
 * @author mlaszloffy
 */
public class SeqwareObject<T extends FirstTierModel & Annotatable> implements FirstTierModel, Annotatable{

    private final T t;
    
    public SeqwareObject(T t){
        this.t = t;
    }
    
    @Override
    public Integer getSwAccession() {
        return t.getSwAccession();
    }

    @Override
    public Set getAnnotations() {
        return t.getAnnotations();
    }

}
