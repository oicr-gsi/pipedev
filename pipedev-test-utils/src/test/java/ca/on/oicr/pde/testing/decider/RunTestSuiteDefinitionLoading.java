package ca.on.oicr.pde.testing.decider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;

public class RunTestSuiteDefinitionLoading {

    RunTestSuiteDefinition expected;

    @BeforeTest
    public void setup() throws IOException {
        String jsonDoc = "{\n"
                + "     \"defaults\" : {\n"
                + "         \"description\" : \"BamQC decider test\",\n"
                + "         \"parameters\" : {\"a\":[\"b\"],\"c\":\"d\"},\n"
                + "         \"metricsDirectory\" : \"/tmp\""
                + "     }\n,"
                + "     \"tests\": [\n"
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

        expected = RunTestSuiteDefinition.buildFromJson(jsonDoc);
//        ObjectMapper m = new ObjectMapper();
//        m.enable(SerializationFeature.INDENT_OUTPUT);
//        System.out.println(m.writeValueAsString(expected));
    }

    @Test
    public void verifyBuildJson() throws JsonProcessingException {
        RunTestSuiteDefinition actual = new RunTestSuiteDefinition();

        RunTestDefinition defaults = new RunTestDefinition();
        defaults.setDescription("BamQC decider test");
        Map<String, Object> params = new HashMap<>();
        params.put("a", "b");
        params.put("c", "d");
        defaults.setParameters(params);
        defaults.setMetricsDirectory("/tmp");
        actual.setDefaults(defaults);

        RunTestDefinition t;

        t = actual.getDeciderRunTestDefinition();
        t.setSamples(Sets.newHashSet("a", "a", "a", "a"));
        actual.add(t);

        t = actual.getDeciderRunTestDefinition();
        t.setStudies(Sets.newHashSet("b", "a"));
        actual.add(t);

        t = actual.getDeciderRunTestDefinition();
        t.setSequencerRuns(Sets.newHashSet("c", "a"));
        actual.add(t);

        t = actual.getDeciderRunTestDefinition();
        t.setSamples(Sets.newHashSet("d"));
        actual.add(t);

        t = actual.getDeciderRunTestDefinition();
        t.setSequencerRuns(Sets.newHashSet("e", "a"));
        actual.add(t);

        t = actual.getDeciderRunTestDefinition();
        t.setId("parameter override test");
        t.setSequencerRuns(Sets.newHashSet("f", "a"));
        params = new HashMap<>();
        params.put("a", "overridden argument");
        t.setParameters(params);
        actual.add(t);

        t = actual.getDeciderRunTestDefinition();
        t.setId("parameter addition test");
        t.setSequencerRuns(Sets.newHashSet("g", "a"));
        params = new HashMap<>();
        params.put("e", "f");
        t.setParameters(params);
        actual.add(t);

        t = actual.getDeciderRunTestDefinition();
        actual.add(t);

        t = actual.getDeciderRunTestDefinition();
        t.setSamples(Sets.newHashSet(""));
        t.setDescription("a different description");
        actual.add(t);

        t = actual.getDeciderRunTestDefinition();
        t.setId("multiple parameters test");
        t.setSamples(Sets.newHashSet("a"));
        params = new HashMap<>();
        params.put("e", Arrays.asList("f", "g"));
        params.put("x", "y");
        t.setParameters(params);
        actual.add(t);

        t = actual.getDeciderRunTestDefinition();
        t.setId("no arguments test");
        t.setSamples(Sets.newHashSet("a"));
        params = new HashMap<>();
        params.put("e", Collections.EMPTY_LIST);
        params.put("x", Collections.EMPTY_LIST);
        t.setParameters(params);
        actual.add(t);

        t = actual.getDeciderRunTestDefinition();
        t.setId("backwards compatibility test");
        params = new HashMap<>();
        params.put("e", "f");
        params.put("a", "b");
        params.put("c", "d");
        t.setParameters(params);
        actual.add(t);

        t = actual.getDeciderRunTestDefinition();
        t.setId("remove defaults test");
        params = new HashMap<>();
        params.put("c", null);
        t.setParameters(params);
        actual.add(t);

//        ObjectMapper m = new ObjectMapper();
//        m.enable(SerializationFeature.INDENT_OUTPUT);
//        System.out.println(m.writeValueAsString(actual));

//        DiffNode root = ObjectDifferBuilder.buildDefault().compare(actual, expected);
//        root.visit(new DiffNode.Visitor() {
//
//            @Override
//            public void node(DiffNode node, Visit visit) {
//                System.out.println(node.getPath() + " => " + node.getState());
//            }
//        });
        ReflectionAssert.assertReflectionEquals(expected, actual, ReflectionComparatorMode.IGNORE_DEFAULTS);

//        Assert.assertFalse(root.hasChanges());
    }

    @Test()
    public void checkForMissingTests() throws IOException {
        Assert.assertNotNull(expected);
        Assert.assertEquals(13, expected.getTests().size());
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

    private RunTestDefinition getTest(String id) {
        //Check that parameter additions are working:
        RunTestDefinition test = null;
        for (RunTestDefinition t : expected.getTests()) {
            if (t.getId().equals(id)) {
                test = t;
            }
        }
        return test;
    }

}
