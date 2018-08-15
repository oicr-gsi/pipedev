package ca.on.oicr.pde.deciders.readgroups;

import static ca.on.oicr.pde.TestUtils.getResourceFilePath;
import ca.on.oicr.pde.deciders.readgroups.NewReadGroupsGeneratorBuilder.NewReadGroups;
import ca.on.oicr.pde.deciders.readgroups.NewReadGroupsGeneratorBuilder.NewReadGroupsGenerator;
import java.io.IOException;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author mlaszloffy
 */
public class ReadGroupsOptionParserTest {

    @Test
    public void getDefaultReadGroupGeneratorTest() throws IOException {
        OptionParser parser = new OptionParser();
        ReadGroupsOptionParser readGroupsOptionParser = new ReadGroupsOptionParser(parser);
        ReadGroupsGenerator readGroupGenerator = readGroupsOptionParser.getReadGroupGenerator(
                "--model-to-platform-map",
                getResourceFilePath("readgroups/model_to_platform.csv").getAbsolutePath()
        );
        assertEquals(readGroupGenerator.getClass(), DefaultOicrReadGroupsGenerator.class);
        assertEquals(readGroupGenerator.getReadGroups(null).getClass(), DefaultOicrReadGroups.class);
    }

    @Test
    public void getTGLReadGroupGeneratorTest() throws IOException {
        OptionParser parser = new OptionParser();
        ReadGroupsOptionParser readGroupsOptionParser = new ReadGroupsOptionParser(parser);
        ReadGroupsGenerator readGroupGenerator = readGroupsOptionParser.getReadGroupGenerator(
                "--read-groups-generator",
                new TglReadGroupsGenerator.Builder().getName(),
                "--model-to-platform-map",
                getResourceFilePath("readgroups/model_to_platform.csv").getAbsolutePath()
        );
        assertEquals(readGroupGenerator.getClass(), TglReadGroupsGenerator.class);
        assertEquals(readGroupGenerator.getReadGroups(null).getClass(), TglReadGroups.class);
    }

    @Test
    public void getNewReadGroupGeneratorTest() throws IOException {
        OptionParser parser = new OptionParser();
        ReadGroupsOptionParser readGroupsOptionParser = new ReadGroupsOptionParser(parser);
        ReadGroupsGenerator readGroupGenerator = readGroupsOptionParser.getReadGroupGenerator(
                "--read-groups-generator",
                "NewReadGroupsGenerator",
                "--new-arg",
                "value"
        );
        assertEquals(readGroupGenerator.getClass(), NewReadGroupsGenerator.class);
        assertEquals(readGroupGenerator.getReadGroups(null).getClass(), NewReadGroups.class);
    }

    @Test(expectedExceptions = OptionException.class)
    public void missingConfigurationTest() throws IOException {
        OptionParser parser = new OptionParser();
        ReadGroupsOptionParser readGroupsOptionParser = new ReadGroupsOptionParser(parser);
        ReadGroupsGenerator readGroupGenerator = readGroupsOptionParser.getReadGroupGenerator();
    }

    @Test(expectedExceptions = OptionException.class)
    public void extraConfigurationTest() throws IOException {
        OptionParser parser = new OptionParser();
        ReadGroupsOptionParser readGroupsOptionParser = new ReadGroupsOptionParser(parser);
        ReadGroupsGenerator readGroupGenerator = readGroupsOptionParser.getReadGroupGenerator(
                "--read-groups-generator",
                "NewReadGroupsGenerator",
                "--not-handled",
                "value"
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void invalidReadGroupGeneratorTest() throws IOException {
        OptionParser parser = new OptionParser();
        ReadGroupsOptionParser readGroupsOptionParser = new ReadGroupsOptionParser(parser);
        ReadGroupsGenerator readGroupGenerator = readGroupsOptionParser.getReadGroupGenerator(
                "--read-groups-generator",
                "DoesNotExistReadGroupsGenerator"
        );
    }
}
