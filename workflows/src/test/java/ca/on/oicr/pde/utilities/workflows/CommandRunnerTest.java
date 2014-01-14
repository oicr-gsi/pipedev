package ca.on.oicr.pde.utilities.workflows;

import ca.on.oicr.pde.common.CommandRunner;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.exec.CommandLine;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CommandRunnerTest {

    static final boolean ENABLE_QUOTING = true;
    static final boolean DISABLE_QUOTING = false;
    
    public CommandRunnerTest() {
        
    }

    @Test
    public void commandWithRedirectTest() throws IOException {
        File expectedFile = File.createTempFile("redirectTest", ".out");
        
         //when test is done, delete file
        expectedFile.deleteOnExit();
        
        //delete the temporary file, as the redirect command should recreate it
        expectedFile.delete(); //delete file
        
        //Check that output file doesn't exist, the redirect command will recreate this file
        Assert.assertFalse(expectedFile.exists(), "Temporary file deletion failed.");
        
        CommandLine c = new CommandLine("/bin/bash");
        c.addArgument("-c");
        c.addArgument("echo test > " + expectedFile.getAbsolutePath(), DISABLE_QUOTING);
        CommandRunner.CommandResult r = new CommandRunner().setCommand(c).runCommand();
        
        Assert.assertNotNull(r, "Command execution was not successful, null result returned.");
        Assert.assertEquals(r.getExitCode(), 0, "Exit code of command is non-zero.  \nCommand output: {\n" + r.getOutput() +"\n}\n");
        Assert.assertTrue(expectedFile.exists(), "Redirect command failed, file was not created.");
    }
    
    @Test
    public void commandWithProcessSubstitutionTest() throws IOException {
        CommandLine c = new CommandLine("/bin/bash");
        c.addArgument("-c");
        c.addArgument("diff -s <(ls -l) <(ls -l)", DISABLE_QUOTING);
        CommandRunner.CommandResult r = new CommandRunner().setCommand(c).runCommand();

        Assert.assertNotNull(r, "Command execution was not successful, null result returned.");
        Assert.assertEquals(r.getExitCode(), 0, "Exit code of command is non-zero.  \nCommand output: {\n" + r.getOutput() +"\n}\n");
    }
}
