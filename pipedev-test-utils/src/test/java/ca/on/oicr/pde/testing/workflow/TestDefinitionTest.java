package ca.on.oicr.pde.testing.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

public class TestDefinitionTest {

    TestDefinition td;

    @BeforeTest
    public void setup() throws IOException {
        String jsonDoc = Files.readFile(new File(this.getClass().getResource("/definition/workflow_test_definition.json").getFile()));
        td = TestDefinition.buildFromJson(jsonDoc);
    }

    @Test
    public void countTestInstances() throws IOException {
        assertNotNull(td);
        assertEquals(10, td.getTests().size());
    }

    @Test
    public void backToJsonTest() throws JsonProcessingException {
        ObjectMapper m = new ObjectMapper();
        m.enable(SerializationFeature.INDENT_OUTPUT);
        System.out.println(m.writeValueAsString(td));
    }

    @Test
    public void jsonIniParameterTest() throws IOException {
        assertNotNull(td);
        Optional<TestDefinition.Test> t = td.getTests().stream().filter(test -> "Test2".equals(test.getId())).findFirst();
        if (t.isPresent()) {
            assertEquals(t.get().getParameters().get("json_string_ini_param"), "{\"a\":\"1\",\"b\":\"2\"}");
            assertEquals(t.get().getParameters().get("default_json"), "{\"a\":\"\\\"escaped_string\\\"\",\"b\":2}");
        } else {
            fail("Unable to find Test2");
        }
    }

}
