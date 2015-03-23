package ca.on.oicr.pde.testing.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TestDefinitionTest {

    TestDefinition td;

    @BeforeTest
    public void setup() throws IOException {

        String jsonDoc = "{\n"
                + "    \"defaults\": {\n"
                + "        \"description\": \"Workflow test\",\n"
                + "        \"metrics_calculate\": \"calculate.sh\",\n"
                + "        \"metrics_compare\": \"compare.sh\",\n"
                + "        \"input_config_dir\": \"\",\n"
                + "        \"output_metrics_dir\":\"/some/dir\",\n"
                + "        \"parameters\": {}\n"
                + "    },\n"
                + "    \"tests\": [\n"
                + "        {\n"
                + "            \"input_config\": \"workflow_test_01.ini\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"input_config\": \"workflow_test_02.ini\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"input_config\": \"workflow_test_03.ini\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"input_config\": \"workflow_test_04.ini\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"input_config\": \"workflow_test_05.ini\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"input_config\": \"workflow_test_06.ini\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"input_config\": \"workflow_test_07.ini\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"input_config\": \"workflow_test_08.ini\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"id\": \"Test1\",\n"
                + "            \"parameters\":{\"a\":\"1\", \"b\":\"2\"}"
                + "        }\n"
                + "    ]\n"
                + "}";

        td = TestDefinition.buildFromJson(jsonDoc);

    }

    @Test
    public void countTestInstances() throws IOException {
        assertNotNull(td);
        assertEquals(9, td.getTests().size());
    }

    @Test
    public void backToJsonTest() throws JsonProcessingException {
        ObjectMapper m = new ObjectMapper();
        m.enable(SerializationFeature.INDENT_OUTPUT);
        System.out.println(m.writeValueAsString(td));
    }

}
