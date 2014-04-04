package ca.on.oicr.pde.utilities;

import ca.on.oicr.pde.model.Name;
import java.util.Arrays;
import junit.framework.Assert;
import org.testng.annotations.Test;

public class ShellExecutorTest {

    public ShellExecutorTest() {
    }

    private class ImplementsName implements Name {

        String name;

        public ImplementsName(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

    }

    @Test
    public void listToParamStringTest() {
        String expected = " --prefix 1 --prefix 2 --prefix 3";
        String actual = ShellExecutor.listToParamString(" --prefix ", Arrays.asList(new ImplementsName("1"), new ImplementsName("2"), new ImplementsName("3")));
        Assert.assertEquals(expected, actual);
    }

}
