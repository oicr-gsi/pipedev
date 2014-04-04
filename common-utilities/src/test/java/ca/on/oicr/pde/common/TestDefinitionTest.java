package ca.on.oicr.pde.common;

import ca.on.oicr.pde.testing.decider.TestDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestDefinitionTest {

    @Test()
    public void buildFromJson() throws IOException {

        String jsonDoc = "{\n"
                + "    \"defaultDescription\": \"BamQC workflow test\",\n"
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
                + "            \"sequencerRuns\": [\"f\",\"a\"]\n"
                + "        },\n"
                + "        {\n"
                + "            \"sequencerRuns\": [\"g\",\"a\"]\n"
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

        TestDefinition t = TestDefinition.buildFromJson(jsonDoc);
        System.out.println(t);

        ObjectMapper m = new ObjectMapper();
        m.enable(SerializationFeature.INDENT_OUTPUT);
        System.out.println(m.writeValueAsString(t));

        Assert.assertEquals(9, t.tests.size());

    }

}
