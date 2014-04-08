package ca.on.oicr.pde.common;

import ca.on.oicr.pde.testing.DeciderRunTestFactory;
import java.io.IOException;
import java.util.Properties;
import org.testng.annotations.Test;

public class DeciderRunTestFactoryTest {

    public DeciderRunTestFactoryTest() {
    }

    //TODO: currently failing with java.lang.reflect.InvocationTargetException
    @Test(enabled = false)
    public void tester() throws IOException {

        Properties p = new Properties();
        p.setProperty("seqwareDistribution", "test");
        p.setProperty("deciderName", "test");
        p.setProperty("deciderVersion", "test");
        p.setProperty("deciderClass", "test");
        p.setProperty("workingDirectory", "test");
        p.setProperty("schedulingSystem", "test");
        p.setProperty("schedulingHost", "test");
        p.setProperty("webserviceUrl", "test");
        p.setProperty("deciderJar", "test");

        System.setProperties(p);

        System.out.println("Starting factory");
        DeciderRunTestFactory d = new DeciderRunTestFactory();
        //d.createTests("");

    }
}
