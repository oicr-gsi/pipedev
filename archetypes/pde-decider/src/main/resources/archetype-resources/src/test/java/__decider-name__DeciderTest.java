#set( $symbol_dollar = '$' )
package ${package};

import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ${decider-name}DeciderTest {

    public ${decider-name}DeciderTest() {

    }

    @Test
    public void checkDeciderMetaType(){
        ${decider-name}Decider d = new ${decider-name}Decider();
        d.setParams(Arrays.asList(""));
        d.parse_parameters();
        d.init();
        Assert.assertEquals(d.getMetaType(), Arrays.asList("application/bam"));
    }
    
}