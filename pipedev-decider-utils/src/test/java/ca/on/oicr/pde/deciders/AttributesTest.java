package ca.on.oicr.pde.deciders;

import java.util.HashMap;
import java.util.Map;
import net.sourceforge.seqware.common.hibernate.FindAllTheFiles.Header;
import net.sourceforge.seqware.common.module.FileMetadata;
import net.sourceforge.seqware.common.module.ReturnValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;
import org.testng.Assert;
/**
 *
 * @author mtaschuk
 */
public class AttributesTest {
    private final Logger logger = LoggerFactory.getLogger(AttributesTest.class);

    public AttributesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeClass
    public void setUp() {
    }

    @AfterClass
    public void tearDown() {
    }

    @Test
    public void testAttributes() {
        logger.debug("testAttributes");

        FileMetadata fm = new FileMetadata();
        fm.setFilePath("/my/file/is/here");
        fm.setMetaType("txt/plain");
        fm.setMd5sum("md5");
        fm.setSize(Long.MAX_VALUE);

        String myRandomString = "TEST";
        
        int i = 0;
        Map<String, String> atts = new HashMap<>();
        for (Header h : Header.values()) {
            atts.put(h.getTitle(), myRandomString+(i++));
        }
        atts.put(Header.LANE_NUM.getTitle(), "1");
        
        String donor = "TEST113";
        atts.put(Header.PARENT_SAMPLE_NAME.getTitle(), atts.get(Header.PARENT_SAMPLE_NAME.getTitle()).concat(":").concat(donor));

        ReturnValue ret = new ReturnValue();
        ret.setAttributes(atts);
        
        FileAttributes attributes = new FileAttributes(ret,fm);
        Assert.assertEquals(attributes.getBarcode(), atts.get(Header.IUS_TAG.getTitle()));
        Assert.assertEquals(attributes.getDonor(), donor);
        Assert.assertEquals(attributes.getExperiment(), atts.get(Header.EXPERIMENT_NAME.getTitle()));
        //assertEquals(fm.getSize().longValue(), attributes.getFileSize().longValue());
        //assertEquals(Integer.parseInt(atts.get(Header.LANE_NUM.getTitle())), attributes.getLane());
        Assert.assertEquals(attributes.getLibrarySample(), atts.get(Header.SAMPLE_NAME.getTitle()));
        Assert.assertEquals(attributes.getMd5(), fm.getMd5sum());
        Assert.assertEquals(attributes.getMetatype(), fm.getMetaType());
        Assert.assertEquals(attributes.getPath(), fm.getFilePath());
        Assert.assertEquals(attributes.getSequencerRun(), atts.get(Header.SEQUENCER_RUN_NAME.getTitle()));
        Assert.assertEquals(attributes.getStudy(), atts.get(Header.STUDY_TITLE.getTitle()));
    }
    
        @Test
    public void testNullAttributes() {
        logger.debug("testAttributes");

        FileMetadata fm = new FileMetadata();
        fm.setFilePath("/my/file/is/here");
        fm.setMetaType("txt/plain");
        fm.setMd5sum("md5");
        fm.setSize(Long.MAX_VALUE);

        String myRandomString = "TEST";
        
        int i = 0;
        Map<String, String> atts = new HashMap<>();
        for (Header h : Header.values()) {
            atts.put(h.getTitle(), myRandomString+(i++));
        }
        atts.put(Header.LANE_NUM.getTitle(), null);
        
        String donor = "TEST113";
        atts.put(Header.PARENT_SAMPLE_NAME.getTitle(), null);

        ReturnValue ret = new ReturnValue();
        ret.setAttributes(atts);
        
        FileAttributes attributes = new FileAttributes(ret,fm);
        Assert.assertEquals(attributes.getBarcode(), atts.get(Header.IUS_TAG.getTitle()));
//        assertEquals(donor, attributes.getDonor());
        Assert.assertEquals(attributes.getExperiment(), atts.get(Header.EXPERIMENT_NAME.getTitle()));
        //assertEquals(fm.getSize().longValue(), attributes.getFileSize().longValue());
        //assertEquals(Integer.parseInt(atts.get(Header.LANE_NUM.getTitle())), attributes.getLane());
        Assert.assertEquals(attributes.getLibrarySample(), atts.get(Header.SAMPLE_NAME.getTitle()));
        Assert.assertEquals(attributes.getMd5(), fm.getMd5sum());
        Assert.assertEquals(attributes.getMetatype(), fm.getMetaType());
        Assert.assertEquals(attributes.getPath(), fm.getFilePath());
        Assert.assertEquals(attributes.getSequencerRun(), atts.get(Header.SEQUENCER_RUN_NAME.getTitle()));
        Assert.assertEquals(attributes.getStudy(), atts.get(Header.STUDY_TITLE.getTitle()));
    }
    
    @Test
    public void testExtractAttribute (){
        logger.debug("testExtractAttribute");
        String test1 = "parent_sample.geo_group_id.492930";
        String test2 = "parent_sample.geo_group_id_description.492930";
        FileAttributes fa = new FileAttributes();
        Assert.assertEquals(fa.extractAttribute(test1, "geo_group_id", "true"), "true", "did not find string");
        Assert.assertNull(fa.extractAttribute(test2, "geo_group_id", "true"), "is not null!");
        
    }        

}
