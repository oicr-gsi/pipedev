package ca.on.oicr.pde.deciders;

import java.util.Map;
import net.sourceforge.seqware.common.module.FileMetadata;
import net.sourceforge.seqware.common.module.ReturnValue;
import org.junit.Test;
import static org.junit.Assert.*;

public class OicrDeciderTest {
    
    public OicrDeciderTest() {
    }

    /**
     * Test of escapeString method, of class OicrDecider.
     */
    @Test
    public void testEscapeString() {
        System.out.println("escapeString test");
        String input = "' !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~'";
        OicrDecider instance = new OicrDecider();
        String expResult = "&#39; &#33;&#34;&#35;&#36;&#37;&#38;&#39;&#40;&#41;&#42;&#43;&#44;-&#46;&#47;0123456789&#58;&#59;&#60;&#61;&#62;&#63;&#64;ABCDEFGHIJKLMNOPQRSTUVWXYZ&#91;&#92;&#92;&#93;&#94;_&#96;abcdefghijklmnopqrstuvwxyz&#123;&#124;&#125;&#126;&#39;";
        String result = instance.escapeString(input);
        assertEquals(expResult, result);
    }
}