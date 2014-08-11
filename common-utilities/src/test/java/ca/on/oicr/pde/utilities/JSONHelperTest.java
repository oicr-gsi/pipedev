package ca.on.oicr.pde.utilities;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests
 *
 * @author Raunaq Suri
 */
public class JSONHelperTest {

    JSONHelper jman = new JSONHelper();

    /**
     * Checks if the JSON has any syntax errors in it
     */
    @Test(testName = "Testing if JSON is Malformed")
    public void malformedJSON() throws IOException {
        File f = File.createTempFile("malformed", ".json");
        f.deleteOnExit();

        String json = "{\n"
                + "    \"defaults\": {}\n"
                + "        \"description\": \"Tired of making jumbled names\",\n"
                + "        \"metrics_calculate\": \"calculate.sh\",\n"
                + "        \"metrics_compare\": \"compare.sh\",\n"
                + "        \"input_config_dir\": \"/i/input_config/\",\n"
                + "        \"output_metrics_dir\": \"/bin/bash/etc/lib/local/output_expectation/\"\n"
                + "    },\n"
                + "    \n"
                + "    \"tests\": [\n"
                + "        {\n"
                + "            \"input_config\": \"workflow_test_02.ini\"\n"
                + "        }\n"
                + "    ]\n"
                + "}";
        
        FileUtils.writeStringToFile(f, json);
        Assert.assertFalse(jman.isJSONValid(Helpers.getFileFromResource("json/schema.json"), f),
                "Error: JSON is valid, when it should be invaild");
    }

    /**
     * Checks to ensure that all required parameters are met
     */
    @Test(testName = "Missing required JSON parameter")
    public void missingRequired() throws IOException {
        Assert.assertFalse(jman.isJSONValid(Helpers.getFileFromResource("json/schema.json"), Helpers.getFileFromResource("json/missingField.json")),
                "ERROR: JSON is valid, when it should be invalid");
    }

    /**
     * Ensures that no key-value pair is a duplicate
     */
    @Test(testName = "Duplicate JSON fields")
    public void duplicateFields() throws IOException {
        Assert.assertFalse(jman.isJSONValid(Helpers.getFileFromResource("json/schema.json"), Helpers.getFileFromResource("json/duplicate.json")),
                "ERROR: JSON is valid when it should be invalid");
    }

    /**
     * Checks to make sure that there are configurations for the tests in the JSON
     */
    @Test(testName = "No Test Config")
    public void noTestConfig() throws IOException {
        Assert.assertFalse(jman.isJSONValid(Helpers.getFileFromResource("json/schema.json"), Helpers.getFileFromResource("json/noTestConfig.json")),
                "ERROR: JSON is valid when it should be invalid");
    }

    /**
     * Checks to make sure that an IOException is thrown if the input file does not exist
     */
    @Test(testName = "Bad File Input", expectedExceptions = IOException.class)
    public void badInputFile1() throws IOException {
        jman.isJSONValid(new File("/does/not/exist"), null);
    }

    /**
     * Checks to make sure that an IOException is thrown if the input file does not exist
     */
    @Test(testName = "Bad File Input", expectedExceptions = IOException.class)
    public void badInputFile2() throws IOException {
        jman.isJSONValid(Helpers.getFileFromResource("json/schema.json"), new File("/does/not/exist"));
    }

    /**
     * Checks to make sure that an IOException is thrown if the input is null
     */
    @Test(testName = "Bad File Input", expectedExceptions = IOException.class)
    public void badInputNull() throws IOException {
        jman.isJSONValid(null, null);
    }

    /**
     * Checks to make sure that if the environment data is not written properly, a false is returned
     */
    @Test(testName = "Environment Data Malformed")
    public void environmentDataMalformed() throws IOException {
        Assert.assertFalse(jman.isJSONValid(Helpers.getFileFromResource("json/schema.json"), Helpers.getFileFromResource("json/environmentMalformed.json")));
    }

    /**
     * The data perfectly matches the schema
     */
    @Test(testName = "Everything is Perfect")
    public void everythingIsPerfect() throws IOException {
        Assert.assertTrue(jman.isJSONValid(Helpers.getFileFromResource("json/schema.json"), Helpers.getFileFromResource("json/data.json")),
                "ERROR: Perfect match gave false instead of true");
    }
}
