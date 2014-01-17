/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.oicr.pde.deciders;

import java.util.HashMap;
import java.util.Map;
import net.sourceforge.seqware.common.hibernate.FindAllTheFiles.Header;
import net.sourceforge.seqware.common.module.FileMetadata;
import net.sourceforge.seqware.common.module.ReturnValue;
import net.sourceforge.seqware.common.util.Log;
import org.junit.*;

/**
 *
 * @author mtaschuk
 */
public class AttributesTest {

    public AttributesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAttributes() {
        Log.stdout("testAttributes");

        FileMetadata fm = new FileMetadata();
        fm.setFilePath("/my/file/is/here");
        fm.setMetaType("txt/plain");
        fm.setMd5sum("md5");
        fm.setSize(Long.MAX_VALUE);

        String myRandomString = "TEST";
        
        int i = 0;
        Map<String, String> atts = new HashMap<String, String>();
        for (Header h : Header.values()) {
            atts.put(h.getTitle(), myRandomString+(i++));
        }
        atts.put(Header.LANE_NUM.getTitle(), "1");
        
        String donor = "TEST113";
        atts.put(Header.PARENT_SAMPLE_NAME.getTitle(), atts.get(Header.PARENT_SAMPLE_NAME.getTitle()).concat(":").concat(donor));

        ReturnValue ret = new ReturnValue();
        ret.setAttributes(atts);
        
        FileAttributes attributes = new FileAttributes(ret,fm);
        Assert.assertEquals(atts.get(Header.IUS_TAG.getTitle()), attributes.getBarcode());
        Assert.assertEquals(donor, attributes.getDonor());
        Assert.assertEquals(atts.get(Header.EXPERIMENT_NAME.getTitle()), attributes.getExperiment());
        //Assert.assertEquals(fm.getSize().longValue(), attributes.getFileSize().longValue());
        //Assert.assertEquals(Integer.parseInt(atts.get(Header.LANE_NUM.getTitle())), attributes.getLane());
        Assert.assertEquals(atts.get(Header.SAMPLE_NAME.getTitle()), attributes.getLibrarySample());
        Assert.assertEquals(fm.getMd5sum(), attributes.getMd5());
        Assert.assertEquals(fm.getMetaType(), attributes.getMetatype());
        Assert.assertEquals(fm.getFilePath(), attributes.getPath());
        Assert.assertEquals(atts.get(Header.SEQUENCER_RUN_NAME.getTitle()), attributes.getSequencerRun());
        Assert.assertEquals(atts.get(Header.STUDY_TITLE.getTitle()), attributes.getStudy());
    }
    
        @Test
    public void testNullAttributes() {
        Log.stdout("testAttributes");

        FileMetadata fm = new FileMetadata();
        fm.setFilePath("/my/file/is/here");
        fm.setMetaType("txt/plain");
        fm.setMd5sum("md5");
        fm.setSize(Long.MAX_VALUE);

        String myRandomString = "TEST";
        
        int i = 0;
        Map<String, String> atts = new HashMap<String, String>();
        for (Header h : Header.values()) {
            atts.put(h.getTitle(), myRandomString+(i++));
        }
        atts.put(Header.LANE_NUM.getTitle(), null);
        
        String donor = "TEST113";
        atts.put(Header.PARENT_SAMPLE_NAME.getTitle(), null);

        ReturnValue ret = new ReturnValue();
        ret.setAttributes(atts);
        
        FileAttributes attributes = new FileAttributes(ret,fm);
        Assert.assertEquals(atts.get(Header.IUS_TAG.getTitle()), attributes.getBarcode());
//        Assert.assertEquals(donor, attributes.getDonor());
        Assert.assertEquals(atts.get(Header.EXPERIMENT_NAME.getTitle()), attributes.getExperiment());
        //Assert.assertEquals(fm.getSize().longValue(), attributes.getFileSize().longValue());
        //Assert.assertEquals(Integer.parseInt(atts.get(Header.LANE_NUM.getTitle())), attributes.getLane());
        Assert.assertEquals(atts.get(Header.SAMPLE_NAME.getTitle()), attributes.getLibrarySample());
        Assert.assertEquals(fm.getMd5sum(), attributes.getMd5());
        Assert.assertEquals(fm.getMetaType(), attributes.getMetatype());
        Assert.assertEquals(fm.getFilePath(), attributes.getPath());
        Assert.assertEquals(atts.get(Header.SEQUENCER_RUN_NAME.getTitle()), attributes.getSequencerRun());
        Assert.assertEquals(atts.get(Header.STUDY_TITLE.getTitle()), attributes.getStudy());
    }
    
    @Test
    public void testExtractAttribute (){
        System.out.println("testExtractAttribute");
        String test1 = "parent_sample.geo_group_id.492930";
        String test2 = "parent_sample.geo_group_id_description.492930";
        FileAttributes fa = new FileAttributes();
        Assert.assertEquals("did not find string", "true", fa.extractAttribute(test1, "geo_group_id", "true"));
        Assert.assertNull("is not null!", fa.extractAttribute(test2, "geo_group_id", "true"));
        
    }        
        
}
