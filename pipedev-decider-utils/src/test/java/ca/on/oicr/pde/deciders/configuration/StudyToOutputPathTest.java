package ca.on.oicr.pde.deciders.configuration;

import static ca.on.oicr.pde.TestUtils.getResourceFilePath;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import net.sourceforge.seqware.common.err.NotFoundException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author mlaszloffy
 */
public class StudyToOutputPathTest {

    public StudyToOutputPathTest() {
    }

    @Test
    public void definedStudy() throws IOException {
        StudyToOutputPathConfig s = new StudyToOutputPathConfig(getResourceFilePath("studyToOutputPathConfig/study-to-output-path.csv"));
        assertEquals(s.getOutputPathForStudy("TEST_STUDY"), "/test_study_output_prefix/");
    }

    @Test
    public void defaultOutputPath() throws IOException {
        StudyToOutputPathConfig s = new StudyToOutputPathConfig(getResourceFilePath("studyToOutputPathConfig/study-to-output-path.csv"));
        assertEquals(s.getOutputPathForStudy("not in list"), "/default_study_output_prefix/");
    }

    @Test
    public void noDefaultOutputPath() throws IOException {
        StudyToOutputPathConfig s = new StudyToOutputPathConfig(ImmutableMap.of("study", "outputpath"));
        assertEquals(s.getOutputPathForStudy("study"), "outputpath");
        try {
            s.getOutputPathForStudy("does not exist");
            fail("Expected NotFoundException");
        } catch (NotFoundException nfe) {
            //
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void malformedStudyToOutputPathCsv() throws IOException {
        StudyToOutputPathConfig s = new StudyToOutputPathConfig(getResourceFilePath("studyToOutputPathConfig/study-to-output-path_malformed.csv"));
        s.getOutputPathForStudy("TEST_STUDY");
    }

    @Test(expectedExceptions = IOException.class)
    public void missingStudyToOutputPathCsv() throws IOException {
        StudyToOutputPathConfig s = new StudyToOutputPathConfig("/does/not/exist/csv");
    }

}
