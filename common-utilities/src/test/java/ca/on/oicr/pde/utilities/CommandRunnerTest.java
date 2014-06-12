package ca.on.oicr.pde.utilities;

import java.io.File;
import java.io.IOException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CommandRunnerTest {

    static final boolean ENABLE_QUOTING = true;
    static final boolean DISABLE_QUOTING = false;

    public CommandRunnerTest() {

    }

    @Test
    public void commandWithRedirectTest() throws IOException {
        File expectedFile = createTempFile("redirectTest", ".out");

        CommandLine c = new CommandLine("/bin/bash");
        c.addArgument("-c");
        c.addArgument("echo test > " + expectedFile.getAbsolutePath(), DISABLE_QUOTING);
        CommandRunner.CommandResult r = new CommandRunner().setCommand(c).runCommand();

        Assert.assertNotNull(r, "Command execution was not successful, null result returned.");
        Assert.assertEquals(r.getExitCode(), 0, "Exit code of command is non-zero.  \nCommand output: {\n" + r.getOutput() + "\n}\n");
        Assert.assertTrue(expectedFile.exists(), "Redirect command failed, file was not created.");
    }

    @Test
    public void commandWithProcessSubstitutionTest() throws IOException {
        CommandLine c = new CommandLine("/bin/bash");
        c.addArgument("-c");
        c.addArgument("diff -s <(ls -l) <(ls -l)", DISABLE_QUOTING);
        CommandRunner.CommandResult r = new CommandRunner().setCommand(c).runCommand();

        Assert.assertNotNull(r, "Command execution was not successful, null result returned.");
        Assert.assertEquals(r.getExitCode(), 0, "Exit code of command is non-zero.  \nCommand output: {\n" + r.getOutput() + "\n}\n");
    }

    @Test
    public void commandInitialWorkingDirectoryTest() throws IOException {
        File initialDirectory = new File("/usr");

        CommandLine c = new CommandLine("/bin/bash");
        c.addArgument("-c");
        c.addArgument("pwd", DISABLE_QUOTING);
        CommandRunner.CommandResult r = new CommandRunner().setCommand(c).setWorkingDirectory(initialDirectory).runCommand();

        Assert.assertNotNull(r, "Command execution was not successful, null result returned.");
        Assert.assertEquals(r.getOutput().trim(), initialDirectory.toString(), "Setting initial working directory failed.");
    }

    @Test(expectedExceptions = IOException.class)
    public void commandWithMissingInitialWorkingDirectoryTest() throws IOException {
        File initialDirectory = new File("/doesnotexist");

        CommandLine c = new CommandLine("/bin/bash");
        c.addArgument("-c");
        c.addArgument("pwd", DISABLE_QUOTING);
        CommandRunner.CommandResult r = new CommandRunner().setCommand(c).setWorkingDirectory(initialDirectory).runCommand();

    }

    @Test
    public void saveCommandStdAndErrToFile() throws IOException {
        File initialDirectory = new File("/tmp");

        File commandOutputFile = createTempFile("commandOutput", ".out");
        
        //Generate some unique output
        CommandLine c = new CommandLine("/bin/bash");
        c.addArgument("-c");
        c.addArgument("for i in `seq 1 5`; do date +%s%N; sleep 0.1; done", DISABLE_QUOTING);
        CommandRunner.CommandResult r = new CommandRunner().setCommand(c).setWorkingDirectory(initialDirectory).setCommandOutputFile(commandOutputFile).runCommand();

        String fileContents = FileUtils.readFileToString(commandOutputFile).trim();
        String memoryContents = r.getOutput().trim();
        
        Assert.assertTrue(fileContents.equals(memoryContents),
                "File stream output and in-memory stream output are not equal.");

    }

    private File createTempFile(String prefix, String suffix) throws IOException {

        File tempFile = File.createTempFile(prefix, suffix);

        //when test is done, delete file
        tempFile.deleteOnExit();

        //delete the temporary file, as the redirect command should recreate it
        tempFile.delete(); //delete file

        //Check that output file doesn't exist, the redirect command will recreate this file
        Assert.assertFalse(tempFile.exists(), "Temporary file deletion failed.");

        return tempFile;

    }
}
