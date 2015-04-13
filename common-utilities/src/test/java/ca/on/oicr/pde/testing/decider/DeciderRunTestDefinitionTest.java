package ca.on.oicr.pde.testing.decider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class DeciderRunTestDefinitionTest {

    DeciderRunTestDefinition expected;

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

        expected = DeciderRunTestDefinition.buildFromJson(jsonDoc);
        System.out.println(expected);

        ObjectMapper m = new ObjectMapper();
        m.enable(SerializationFeature.INDENT_OUTPUT);
        System.out.println(m.writeValueAsString(expected));

    }

    @Test
    public void verifyBuildJson() {
        DeciderRunTestDefinition actual = new DeciderRunTestDefinition();
        actual.setDefaultDescription("BamQC decider test");
        actual.setDefaultParameters(ImmutableMap.of("a", "b", "c", "d"));
        actual.setDefaultMetricsDirectory("/tmp");

        DeciderRunTestDefinition.Test t;

        t = new DeciderRunTestDefinition.Test();
        t.setSamples(Sets.newHashSet("a", "a", "a", "a"));
        actual.add(t);

        t = new DeciderRunTestDefinition.Test();
        t.setStudies(Sets.newHashSet("b", "a"));
        actual.add(t);

        t = new DeciderRunTestDefinition.Test();
        t.setSequencerRuns(Sets.newHashSet("c", "a"));
        actual.add(t);

        t = new DeciderRunTestDefinition.Test();
        t.setSamples(Sets.newHashSet("d"));
        actual.add(t);

        t = new DeciderRunTestDefinition.Test();
        t.setSequencerRuns(Sets.newHashSet("e", "a"));
        actual.add(t);

        t = new DeciderRunTestDefinition.Test();
        t.setId("parameter override test");
        t.setSequencerRuns(Sets.newHashSet("f", "a"));
        t.setParameters(ImmutableMap.of("a", "overridden argument"));
        actual.add(t);

        t = new DeciderRunTestDefinition.Test();
        t.setId("parameter addition test");
        t.setSequencerRuns(Sets.newHashSet("g", "a"));
        t.setParameters(ImmutableMap.of("e", "f"));
        actual.add(t);

        t = new DeciderRunTestDefinition.Test();
        actual.add(t);

        t = new DeciderRunTestDefinition.Test();
        t.setSamples(Sets.newHashSet(""));
        t.setDescription("a different description");
        actual.add(t);

        Assert.assertEquals(actual, expected);
    }

    @Test()
    public void checkForMissingTests() throws IOException {
        Assert.assertNotNull(expected);
        Assert.assertEquals(9, expected.getTests().size());
    }

    @Test()
    public void checkParameterOverride() throws IOException {

        //Check that parameter overrides are working:
        DeciderRunTestDefinition.Test test = null;
        for (DeciderRunTestDefinition.Test t : expected.getTests()) {
            if (t.getId().equals("parameter override test")) {
                test = t;
            }
        }

        Map<String, String> expectedParameters;
        expectedParameters = new LinkedHashMap<>();
        expectedParameters.put("a", "overridden argument");
        expectedParameters.put("c", "d");

        Assert.assertNotNull(test);
        Assert.assertEquals(test.getParameters(), expectedParameters);

    }

    @Test()
    public void checkParameterAddition() throws IOException {

        //Check that parameter additions are working:
        DeciderRunTestDefinition.Test test = null;
        for (DeciderRunTestDefinition.Test t : expected.getTests()) {
            if (t.getId().equals("parameter addition test")) {
                test = t;
            }
        }

        Map<String, String> expectedParameters;
        expectedParameters = new LinkedHashMap<>();
        expectedParameters.put("a", "b");
        expectedParameters.put("c", "d");
        expectedParameters.put("e", "f");

        Assert.assertNotNull(test);
        Assert.assertEquals(test.getParameters(), expectedParameters);

    }

}
