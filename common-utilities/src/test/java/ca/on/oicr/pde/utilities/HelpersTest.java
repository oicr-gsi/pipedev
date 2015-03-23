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

    @Test
    public void getPgpassPasswordValidTest1() throws IOException {
        String host = "test.host";
        int port = 99999;
        String databaseName = "*";
        String user = "test_user";
        String password = UUID.randomUUID().toString().replace("-", "");

        String pgpassString = "###pgpass file\n" + host + ":" + port + ":" + databaseName + ":" + user + ":" + password + "\n" + "###pgpassfile";

        File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();
        File pgpassFile = new File(tempDir, ".pgpass");
        pgpassFile.deleteOnExit();

        FileUtils.write(pgpassFile, pgpassString);

        System.setProperty("PGPASSFILE", pgpassFile.getAbsolutePath());
        String pgpassPassword = Helpers.getPgpassPassword(host, port, user);

        Assert.assertEquals(pgpassPassword, password);
    }

    @Test
    public void getPgpassPasswordValidTest2() throws IOException {
        String host = "test.host";
        int port = 99999;
        String databaseName = "*";
        String user = "test_user";
        String password = UUID.randomUUID().toString().replace("-", "");

        String pgpassString = "###pgpass file\n" + host + ":" + port + ":" + databaseName + ":" + user + ":" + password + "\n" + "###pgpassfile";

        File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();
        File pgpassFile = new File(tempDir, ".pgpass");
        pgpassFile.deleteOnExit();

        FileUtils.write(pgpassFile, pgpassString);

        System.clearProperty("PGPASSFILE");
        System.setProperty("user.home", tempDir.getAbsolutePath());
        String pgpassPassword = Helpers.getPgpassPassword(host, port, user);

        Assert.assertEquals(pgpassPassword, password);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void getPgpassPasswordInvalidTest1() throws IOException {
        String host = "test.host";
        int port = 99999;
        String databaseName = "*";
        String user = "test_user";
        String password = UUID.randomUUID().toString().replace("-", "");

        String pgpassString = "###pgpass file\n" + host + ":" + port + ":" + databaseName + ":" + user + ":" + password + "\n"
                + host + ":" + port + ":" + databaseName + ":" + user + ":" + password + "_" + "\n" + "###pgpassfile";

        File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();
        File pgpassFile = new File(tempDir, ".pgpass");
        pgpassFile.deleteOnExit();

        FileUtils.write(pgpassFile, pgpassString);

        System.setProperty("PGPASSFILE", pgpassFile.getAbsolutePath());
        Helpers.getPgpassPassword(host, port, user);

        Assert.fail();
    }

    @Test
    public void getPgpassPasswordInvalidTest2() throws IOException {
        String host = "test.host";
        int port = 99999;
        String databaseName = "*";
        String user = "test_user";
        String password = UUID.randomUUID().toString().replace("-", "");

        String pgpassString = "###pgpass file\n" + host + "::" + port + ":" + databaseName + ":" + user + ":" + password + "\n" + "###pgpassfile";

        File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();
        File pgpassFile = new File(tempDir, ".pgpass");
        pgpassFile.deleteOnExit();

        FileUtils.write(pgpassFile, pgpassString);

        System.setProperty("PGPASSFILE", pgpassFile.getAbsolutePath());
        String pgpassPassword = Helpers.getPgpassPassword(host, port, user);

        Assert.assertNull(pgpassPassword);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void getPgpassPasswordInvalidTest3() throws IOException {
        System.setProperty("PGPASSFILE", "/tmp/does_not_exist/.pgpass");
        Helpers.getPgpassPassword("host", 99999, "user");

        Assert.fail();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void getPgpassPasswordInvalidTest4() throws IOException {
        System.clearProperty("PGPASSFILE");
        File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();
        System.setProperty("user.home", tempDir.getAbsolutePath());
        Helpers.getPgpassPassword("host", 99999, "user");

        Assert.fail();
    }

}
