package ca.on.oicr.pde.deciders;

import java.util.HashMap;
import java.util.Map;
import net.sourceforge.seqware.common.hibernate.FindAllTheFiles;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeTest;

/**
 *
 * @author mlaszloffy
 */
public class FileAttributesTest {

    private final Map<String, String> attributes;

    public FileAttributesTest() {
        attributes = new HashMap<>();
    }

    @BeforeTest
    public void setup() {
        attributes.clear();
    }

    @Test
    public void assignLastInStringTest_normalString() {
        attributes.put(FindAllTheFiles.Header.PARENT_SAMPLE_NAME.getTitle(), "TEST1:TEST2:TEST3");
        String last = FileAttributes.assignLastInString(attributes, FindAllTheFiles.Header.PARENT_SAMPLE_NAME, ":");
        assertEquals(last, "TEST3");

        attributes.put(FindAllTheFiles.Header.PARENT_SAMPLE_NAME.getTitle(), "\"\":::!@#$%^&*()_:::\n\rTEST1     :    TEST2   :::::     TEST3    ");
        last = FileAttributes.assignLastInString(attributes, FindAllTheFiles.Header.PARENT_SAMPLE_NAME, ":");
        assertEquals(last, "TEST3");

        attributes.put(FindAllTheFiles.Header.PARENT_SAMPLE_NAME.getTitle(), "TEST3");
        last = FileAttributes.assignLastInString(attributes, FindAllTheFiles.Header.PARENT_SAMPLE_NAME, ":");
        assertEquals(last, "TEST3");
    }

    @Test
    public void assignLastInStringTest_normalStringEndingWithDelimiter() {
        attributes.put(FindAllTheFiles.Header.PARENT_SAMPLE_NAME.getTitle(), "TEST1:TEST2:TEST3:");
        String last = FileAttributes.assignLastInString(attributes, FindAllTheFiles.Header.PARENT_SAMPLE_NAME, ":");
        assertEquals(last, "");

        attributes.put(FindAllTheFiles.Header.PARENT_SAMPLE_NAME.getTitle(), ":");
        last = FileAttributes.assignLastInString(attributes, FindAllTheFiles.Header.PARENT_SAMPLE_NAME, ":");
        assertEquals(last, "");

        attributes.put(FindAllTheFiles.Header.PARENT_SAMPLE_NAME.getTitle(), "::::::::::::::::::");
        last = FileAttributes.assignLastInString(attributes, FindAllTheFiles.Header.PARENT_SAMPLE_NAME, ":");
        assertEquals(last, "");
    }

    @Test
    public void assignLastInStringTest_emptyString() {
        attributes.put(FindAllTheFiles.Header.PARENT_SAMPLE_NAME.getTitle(), "");
        String last = FileAttributes.assignLastInString(attributes, FindAllTheFiles.Header.PARENT_SAMPLE_NAME, ":");
        assertEquals(last, "");
    }

    @Test
    public void assignLastInStringTest_nullString() {
        attributes.put(FindAllTheFiles.Header.PARENT_SAMPLE_NAME.getTitle(), null);
        String last = FileAttributes.assignLastInString(attributes, FindAllTheFiles.Header.PARENT_SAMPLE_NAME, ":");
        assertEquals(last, null);
    }

    @Test
    public void assignLastInStringTest_missingAttribute() {
        attributes.clear();
        String last = FileAttributes.assignLastInString(attributes, FindAllTheFiles.Header.PARENT_SAMPLE_NAME, ":");
        assertEquals(last, null);
    }

}
