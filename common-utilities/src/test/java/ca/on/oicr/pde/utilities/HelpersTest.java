package ca.on.oicr.pde.utilities;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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

    @Test
    public void getResourceAsFile() throws IOException {

        File f = Helpers.getFileFromResource("helpers/test.txt");
        Assert.assertEquals(FileUtils.readFileToString(f), "test content");

    }

    @Test
    public void getScriptFromResource_withDirectory() throws IOException {
        Path tmpDir = java.nio.file.Files.createDirectory(Paths.get("/tmp/test_dir/"));
        tmpDir.toFile().deleteOnExit();

        File f = Helpers.getScriptFromResource("helpers/test.txt", tmpDir);
        f.deleteOnExit();

        Assert.assertTrue(f.exists() && f.isFile() && f.canExecute());
    }

    @Test
    public void getScriptFromResource_withoutDirectory() throws IOException {
        File f = Helpers.getScriptFromResource("helpers/test.txt");
        f.deleteOnExit();

        Assert.assertTrue(f.exists() && f.isFile() && f.canExecute());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void getScriptFromResource_badResource() throws IOException {
        Helpers.getScriptFromResource("helpers/");
    }

    @Test(expectedExceptions = IOException.class)
    public void getScriptFromResource_missingResource() throws IOException {
        Helpers.getScriptFromResource("helpers/does_not_exist.sh");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void getScriptFromResource_missingDirectory() throws IOException {
        Helpers.getScriptFromResource("helpers/test.txt", Paths.get("/tmp/does_not_exist_" + UUID.randomUUID() + "/"));
    }

}
