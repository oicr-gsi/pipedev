package ca.on.oicr.pde.deciders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.seqware.common.hibernate.FindAllTheFiles;
import net.sourceforge.seqware.common.module.FileMetadata;
import net.sourceforge.seqware.common.module.ReturnValue;
import org.testng.annotations.*;
import static org.testng.Assert.*;

public class OicrDeciderHelpersTest {

    public OicrDeciderHelpersTest() {
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
        assertEquals(result, expResult);
    }

    @Test
    public void testIsBeforeDate() throws ParseException {
        System.out.println("isBeforeDate test");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String jan1 = "2014-01-01";
        String jan2 = "2014-01-02";

        Date jan1Date = format.parse(jan1);
        Date jan2Date = format.parse(jan2);

        OicrDecider instance = new OicrDecider();

        assertTrue(instance.isBeforeDate(jan1, jan2Date), "isBeforeDate Test for: Jan 1 before Jan 2?");
        assertFalse(instance.isBeforeDate(jan2, jan2Date), "isBeforeDate Test for: Jan 2 before Jan 2?");
        assertFalse(instance.isBeforeDate(jan2, jan1Date), "isBeforeDate Test for: Jan 2 before Jan 1?");
    }

    @Test
    public void testIsAfterDate() throws ParseException {
        System.out.println("isAfterDate test");

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String jan1 = "2014-01-01";
        String jan2 = "2014-01-02";

        Date jan1Date = format.parse(jan1);
        Date jan2Date = format.parse(jan2);

        OicrDecider instance = new OicrDecider();

        assertTrue(instance.isAfterDate(jan2, jan1Date), "isAfterDate Test for: Jan 2 after Jan 1?");
        assertFalse(instance.isAfterDate(jan1, jan1Date), "isAfterDate Test for: Jan 1 after Jan 1?");
        assertFalse(instance.isAfterDate(jan1, jan2Date), "isAfterDate Test for: Jan 1 after Jan 2?");
    }

    @Test
    public void testGetPrefixFromFileMetadata() {
        OicrDecider od = new OicrDecider();
        ReturnValue rv;
        FileAttributes fa;

        Map<String, String> attributes = new HashMap<>();
        attributes.put(FindAllTheFiles.Header.IUS_SWA.getTitle(), "IUSSWA");
        attributes.put(FindAllTheFiles.Header.SAMPLE_NAME.getTitle(), "SAMPLENAME");
        attributes.put(FindAllTheFiles.Header.SEQUENCER_RUN_NAME.getTitle(), "SEQUENCERRUNNAME");
        attributes.put(FindAllTheFiles.Header.IUS_TAG.getTitle(), "IUSTAG");
        attributes.put(FindAllTheFiles.Header.LANE_NUM.getTitle(), "1");

        rv = new ReturnValue();
        rv.setAttributes(attributes);
        fa = new FileAttributes(rv, new FileMetadata());
        assertEquals(od.getPrefixFromFileMetadata(fa), "SWID_IUSSWA_SAMPLENAME_SEQUENCERRUNNAME_IUSTAG_L001_R1_001_");

        attributes.put(Lims.GROUP_ID.getAttributeTitle(), "GROUPID");
        rv = new ReturnValue();
        rv.setAttributes(attributes);
        fa = new FileAttributes(rv, new FileMetadata());
        assertEquals(od.getPrefixFromFileMetadata(fa), "SWID_IUSSWA_SAMPLENAME_GROUPID_SEQUENCERRUNNAME_IUSTAG_L001_R1_001_");
    }

}
