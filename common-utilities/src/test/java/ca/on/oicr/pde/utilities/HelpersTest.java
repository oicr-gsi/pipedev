package ca.on.oicr.pde.utilities;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.testng.annotations.Test;

public class HelpersTest {
    
    public HelpersTest() {
    }

    @Test
    public void createTestWorkingDirectory() throws IOException {
        
        File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();

        File subDir = Helpers.generateTestWorkingDirectory(tempDir, "", "testDir", "1");
        subDir.deleteOnExit();

        Assert.assertTrue(subDir.exists());

    }

    @Test(expectedExceptions = IOException.class)
    public void createTestWorkingDirectoryThatAlreadyExists() throws IOException {

        File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();

        File subDir = Helpers.generateTestWorkingDirectory(tempDir, "", "testDir", "1");
        subDir.deleteOnExit();

        File subDir2 = Helpers.generateTestWorkingDirectory(tempDir, "", "testDir", "1"); //should fail

    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void missingWorkingDirectory() throws IOException {
        
        Helpers.generateTestWorkingDirectory(new File("/tmp/does/not/exist/"), "", "testDir", "1");
        
    }

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
        String path = Helpers.buildPathFromDirectory(initial, testDir);

        org.testng.Assert.assertEquals(path, initial + ":" + testDir + ":" + testDir + "/a:" + testDir + "/b:" + testDir + "/c");

    }

}
