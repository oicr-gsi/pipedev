package ca.on.oicr.pde.testing.decider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class DeciderRunTestDefinitionTest {

    DeciderRunTestDefinition td;

    @BeforeTest
    public void setup() throws IOException {
        String jsonDoc = "{\n"
                + "    \"defaultDescription\": \"BamQC decider test\",\n"
                + "    \"defaultParameters\": {\"a\":\"b\",\"c\":\"d\"},\n"
                + "    \"defaultMetricsDirectory\":\"/tmp\",\n"
                + "    \"tests\": [\n"
                + "        {\n"
                + "            \"samples\": [\"a\",\"a\",\"a\",\"a\"]\n"
                + "        },\n"
                + "        {\n"
                + "            \"studies\": [\"b\",\"a\"]\n"
                + "        },\n"
                + "        {\n"
                + "            \"sequencerRuns\": [\"c\",\"a\"]\n"
                + "        },\n"
                + "        {\n"
                + "            \"samples\": [\"d\"]\n"
                + "        },\n"
                + "        {\n"
                + "            \"sequencerRuns\": [\"e\",\"a\"]\n"
                + "        },\n"
                + "        {\n"
                + "            \"id\": \"parameter override test\",\n"
                + "            \"sequencerRuns\": [\"f\",\"a\"],\n"
                + "            \"parameters\": {\"a\":\"overridden argument\"}\n "
                + "        },\n"
                + "        {\n"
                + "            \"id\": \"parameter addition test\",\n"
                + "            \"sequencerRuns\": [\"g\",\"a\"],\n"
                + "            \"parameters\": {\"e\":\"f\"}\n "
                + "        },\n"
                + "        {\n"
                + "         " //empty test, will use defaults
                + "        },\n"
                + "        {\n"
                + "            \"samples\": [\"\"],\n"
                + "            \"description\":\"a different description\"\n"
                + "        }\n"
                + "    ]\n"
                + "}";

        td = DeciderRunTestDefinition.buildFromJson(jsonDoc);
        System.out.println(td);

        ObjectMapper m = new ObjectMapper();
        m.enable(SerializationFeature.INDENT_OUTPUT);
        System.out.println(m.writeValueAsString(td));

    }

    @Test()
    public void checkForMissingTests() throws IOException {
        Assert.assertNotNull(td);
        Assert.assertEquals(9, td.tests.size());
    }

    @Test()
    public void checkParameterOverride() throws IOException {

        //Check that parameter overrides are working:
        DeciderRunTestDefinition.Test test = null;
        for (DeciderRunTestDefinition.Test t : td.tests) {
            if (t.getId().equals("parameter override test")) {
                test = t;
            }
        }

        Map<String, String> expected;
        expected = new LinkedHashMap<String, String>();
        expected.put("a", "overridden argument");
        expected.put("c", "d");

        Assert.assertNotNull(test);
        Assert.assertEquals(test.getParameters(), expected);

    }

    @Test()
    public void checkParameterAddition() throws IOException {

        //Check that parameter additions are working:
        DeciderRunTestDefinition.Test test = null;
        for (DeciderRunTestDefinition.Test t : td.tests) {
            if (t.getId().equals("parameter addition test")) {
                test = t;
            }
        }

        Map<String, String> expected;
        expected = new LinkedHashMap<String, String>();
        expected.put("a", "b");
        expected.put("c", "d");
        expected.put("e", "f");

        Assert.assertNotNull(test);
        Assert.assertEquals(test.getParameters(), expected);

    }

}
