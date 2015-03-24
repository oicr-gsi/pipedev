package ca.on.oicr.pde.deciders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import mockit.*;
import net.sourceforge.seqware.common.module.ReturnValue;
import net.sourceforge.seqware.pipeline.deciders.BasicDecider;
import org.testng.annotations.*;
import org.testng.Assert;

public class OicrDeciderTest {

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
        Assert.assertEquals(result, expResult);
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

        Assert.assertTrue(instance.isBeforeDate(jan1, jan2Date), "isBeforeDate Test for: Jan 1 before Jan 2?");
        Assert.assertFalse(instance.isBeforeDate(jan2, jan2Date), "isBeforeDate Test for: Jan 2 before Jan 2?");
        Assert.assertFalse(instance.isBeforeDate(jan2, jan1Date), "isBeforeDate Test for: Jan 2 before Jan 1?");

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

        Assert.assertTrue(instance.isAfterDate(jan2, jan1Date), "isAfterDate Test for: Jan 2 after Jan 1?");
        Assert.assertFalse(instance.isAfterDate(jan1, jan1Date), "isAfterDate Test for: Jan 1 after Jan 1?");
        Assert.assertFalse(instance.isAfterDate(jan1, jan2Date), "isAfterDate Test for: Jan 1 after Jan 2?");

    }

    @Test
    public void outputPathInvalid() {
        Assert.assertEquals(getOicrDeciderInitExitStatus("--output-path", "/tmp/does/not/exist"), ReturnValue.INVALIDPARAMETERS);
        Assert.assertEquals(getOicrDeciderInitExitStatus("--output-path", "/tmp/*"), ReturnValue.INVALIDPARAMETERS);
        Assert.assertEquals(getOicrDeciderInitExitStatus("--output-path", "/root/"), ReturnValue.INVALIDPARAMETERS);
        Assert.assertEquals(getOicrDeciderInitExitStatus("--output-path", "/"), ReturnValue.INVALIDPARAMETERS);
    }

    @Test
    public void outputPathValid() {
        Assert.assertEquals(getOicrDeciderInitExitStatus("--output-path", "/tmp/"), ReturnValue.SUCCESS);
        Assert.assertEquals(getOicrDeciderInitExitStatus("--output-path", "/dev/null"), ReturnValue.SUCCESS);
        Assert.assertEquals(getOicrDeciderInitExitStatus("--output-path", "./"), ReturnValue.SUCCESS);
        Assert.assertEquals(getOicrDeciderInitExitStatus(), ReturnValue.SUCCESS); //default output-path is ./
    }

    private int getOicrDeciderInitExitStatus(String... params) {
        BasicDeciderMock basicDeciderMock = new BasicDeciderMock();
        Assert.assertFalse(basicDeciderMock.initCalled);

        OicrDecider od = new OicrDecider();
        od.setParams(Arrays.asList(params));
        od.parse_parameters();
        
        ReturnValue rv = od.init();
        Assert.assertTrue(basicDeciderMock.initCalled);

        return rv.getExitStatus();
    }

    public static class BasicDeciderMock extends MockUp<BasicDecider> {

        private boolean initCalled = false;

        @Mock
        public ReturnValue init() {
            this.initCalled = true;
            return new ReturnValue();
        }
    }
}
