package ca.on.oicr.pde.testing.decider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
                + "    \"defaultParameters\": {\"a\":[\"b\"],\"c\":\"d\"},\n"
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
                + "            \"parameters\": {\"a\":[\"overridden argument\"]}\n "
                + "        },\n"
                + "        {\n"
                + "            \"id\": \"parameter addition test\",\n"
                + "            \"sequencerRuns\": [\"g\",\"a\"],\n"
                + "            \"parameters\": {\"e\":[\"f\"]}\n "
                + "        },\n"
                + "        {\n"
                + "         " //empty test, will use defaults
                + "        },\n"
                + "        {\n"
                + "            \"samples\": [\"\"],\n"
                + "            \"description\":\"a different description\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"id\": \"multiple parameters test\",\n"
                + "            \"samples\": [\"a\"],\n"
                + "            \"parameters\": {\"e\":[\"f\",\"g\"], \"x\":[\"y\"]}\n "
                + "        },\n"
                + "        {\n"
                + "            \"id\": \"no arguments test\",\n"
                + "            \"samples\": [\"a\"],\n"
                + "            \"parameters\": {\"e\":[], \"x\":[]}\n "
                + "        },\n"
                + "        {\n"
                + "            \"id\": \"backwards compatibility test\",\n"
                + "            \"parameters\": {\"e\":\"f\", \"a\":\"b\", \"c\":\"d\"}\n "
                + "        },\n"
                + "        {\n"
                + "            \"id\": \"remove defaults test\",\n"
                + "            \"parameters\": {\"c\":null}\n "
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
        Assert.assertEquals(9, td.getTests().size());
    }

    @Test()
    public void checkParameterOverride() throws IOException {
        Map<String, List<String>> expected;
        expected = new LinkedHashMap<>();
        expected.put("a", Arrays.asList("overridden argument"));
        expected.put("c", Arrays.asList("d"));

        Assert.assertEquals(getTest("parameter override test").getParameters(), expected);
    }

    @Test()
    public void checkParameterAddition() throws IOException {
        Map<String, List<String>> expected;
        expected = new LinkedHashMap<>();
        expected.put("a", Arrays.asList("b"));
        expected.put("c", Arrays.asList("d"));
        expected.put("e", Arrays.asList("f"));

        Assert.assertEquals(getTest("parameter addition test").getParameters(), expected);
    }

    @Test()
    public void testMultipleParams() throws IOException {
        Map<String, List<String>> expected;
        expected = new LinkedHashMap<>();
        expected.put("a", Arrays.asList("b"));
        expected.put("c", Arrays.asList("d"));
        expected.put("e", Arrays.asList("f", "g"));
        expected.put("x", Arrays.asList("y"));

        Assert.assertEquals(getTest("multiple parameters test").getParameters(), expected);
    }

    @Test()
    public void noArgumentsTest() throws IOException {
        Map<String, List<String>> expected;
        expected = new LinkedHashMap<>();
        expected.put("a", Arrays.asList("b"));
        expected.put("c", Arrays.asList("d"));
        expected.put("e", Collections.EMPTY_LIST);
        expected.put("x", Collections.EMPTY_LIST);

        Assert.assertEquals(getTest("no arguments test").getParameters(), expected);
    }

    @Test()
    public void backwardsCompatibilityTest() throws IOException {
        Map<String, List<String>> expected;
        expected = new LinkedHashMap<>();
        expected.put("a", Arrays.asList("b"));
        expected.put("c", Arrays.asList("d"));
        expected.put("e", Arrays.asList("f"));

        Assert.assertEquals(getTest("backwards compatibility test").getParameters(), expected);
    }

    @Test()
    public void removeDefaultsTest() throws IOException {
        Map<String, List<String>> expected;
        expected = new LinkedHashMap<>();
        expected.put("a", Arrays.asList("b"));
        expected.put("c", null);

        Assert.assertEquals(getTest("remove defaults test").getParameters(), expected);
    }

    private DeciderRunTestDefinition.Test getTest(String id) {
        //Check that parameter additions are working:
        DeciderRunTestDefinition.Test test = null;
        for (DeciderRunTestDefinition.Test t : td.getTests()) {
            if (t.getId().equals(id)) {
                test = t;
            }
        }
        return test;
    }

}
