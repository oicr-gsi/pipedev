package ca.on.oicr.pde.common.tests;

import ca.on.oicr.pde.common.WorkflowRunTools;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WorkflowRunToolsTest {

    @Test
    public void getDirectoriesInLocationTest() throws IOException {

        File testDir = File.createTempFile("test", "");
        testDir.delete();
        testDir.mkdir();
        testDir.deleteOnExit();

        File tmpFile;
        File tmpDir;
        tmpFile = new File(testDir + "/testFile");
        tmpFile.createNewFile();
        tmpFile.deleteOnExit();

        List<String> dirs = Arrays.asList("a", "b", "c", "a/1", "a/2", "a/3", "c/1");
        for (String d : dirs) {
            tmpDir = new File(testDir + "/" + d);
            tmpDir.mkdir();
            tmpDir.deleteOnExit();
            tmpFile = new File(testDir + "/" + d + "/testFile");
            tmpFile.createNewFile();
            tmpFile.deleteOnExit();
        }

        String initial = "/usr/bin:/bin:/usr/sbin:/sbin";
        String path = WorkflowRunTools.buildPathFromDirectory(initial, testDir);

        Assert.assertEquals(path, initial + ":" + testDir + ":" + testDir + "/a:" + testDir + "/b:" + testDir + "/c");

    }

}
