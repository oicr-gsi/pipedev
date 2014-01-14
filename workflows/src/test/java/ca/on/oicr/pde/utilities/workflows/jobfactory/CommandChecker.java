package ca.on.oicr.pde.utilities.workflows.jobfactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;

/**
 *
 * @author mtaschuk
 */
public class CommandChecker {

    public static void checkEm(List<String> actual, String expected) {
        String[] expectedArr = expected.trim().split("\\s");

        ArrayList<String> list = new ArrayList<String>();
        for (String arg : actual) {
            list.addAll(Arrays.asList(arg.split("\\s")));
        }
        int i = 0;
//        Assert.assertEquals("Incorrect number of arguments", expectedArr.length, list.size());
        for (String arg : list) {
            System.out.println("-\nA:" +arg + "\nE:" + expectedArr[i]);
            //Assert.assertEquals(expectedArr[i++].trim(), arg.trim());
            Assert.assertEquals(arg.trim(), expectedArr[i].trim(), "Expected: "+expectedArr[i]+" but was "+ arg.trim()+"; ");
            i++;
        }
    }
}
