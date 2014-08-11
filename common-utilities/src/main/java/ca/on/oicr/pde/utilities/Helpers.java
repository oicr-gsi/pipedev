package ca.on.oicr.pde.utilities;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.testng.Assert;
import static com.google.common.base.Preconditions.*;
import com.google.common.io.Files;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class Helpers {

    private final static Logger log = LogManager.getRootLogger();

    public static File getFileFromResource(String resourceFilePath) throws IOException {

        File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();

        File tempFile = new File(tempDir + FilenameUtils.getName(resourceFilePath));
        tempFile.deleteOnExit();

        InputStream resourceStream = Helpers.class.getClassLoader().getResourceAsStream(resourceFilePath);

        if (resourceStream == null) {
            throw new IOException("The resource [" + resourceFilePath + "] is not accessible");
        }

        FileUtils.writeStringToFile(tempFile, IOUtils.toString(resourceStream));

        return tempFile;

    }

    public static File getScriptFromResource(String scriptName) throws IOException {

        File script = File.createTempFile(scriptName, ".sh");
        script.setExecutable(true);
        script.deleteOnExit();

        InputStream resourceStream = Helpers.class.getClassLoader().getResourceAsStream(scriptName);
        Assert.assertNotNull(resourceStream, String.format("Script resource [%s] was not found - verify that script exists as a resource.", scriptName));

        FileUtils.writeStringToFile(script, IOUtils.toString(resourceStream));

        return script;

    }

    public static String executeCommand(String id, String command, File workingDirectory, Map<String, String>... environmentVariables) throws IOException {

        return executeCommand(id, command, workingDirectory, null, environmentVariables);

    }

    public static String executeCommand(String id, String command, File workingDirectory, File outputFilePath,
            Map<String, String>... environmentVariables) throws IOException {

        //TODO: if saveOutputToWorkingDirectory record to workingDirectory
        CommandLine c = new CommandLine("/bin/bash");
        c.addArgument("-c");
        c.addArgument(command, false);

        CommandRunner cr = new CommandRunner();

        //Setup command to execute
        cr.setCommand(c);

        //Setup environment variables for command's shell
        for (Map<String, String> e : environmentVariables) {
            cr.setEnvironmentVariable(e);
        }

        //Set the initial directory the command should execute from within
        cr.setWorkingDirectory(workingDirectory);

        //Set the path where the command's shell std out and std err should be saved to
        if (outputFilePath != null) {
            cr.setCommandOutputFile(outputFilePath);
        }

        System.out.println(id + "{\n" + "Executing (with initial directory: " + workingDirectory + "):\n" + command.toString() + "\n}");

        CommandRunner.CommandResult r = cr.runCommand();
        Assert.assertTrue(r.getExitCode() == 0,
                String.format("The following command returned a non-zero exit code [%s]:\n%s\nOutput from command:\n%s\n",
                        r.getExitCode(), command, r.getOutput()));

        return r.getOutput().trim();

    }

    public static File generateSeqwareSettings(File workingDirectory, String webserviceUrl, String schedulingSystem, String schedulingHost) throws IOException {

        //TODO: implement this in java
        StringBuilder command = new StringBuilder();
        command.append(getScriptFromResource("generateSeqwareSettings.sh"));
        command.append(" ").append(workingDirectory);
        command.append(" ").append(webserviceUrl);
        command.append(" ").append(schedulingSystem);
        command.append(" ").append(schedulingHost);
        command.append(" ").append(UUID.randomUUID());

        return new File(executeCommand("", command.toString(), workingDirectory));

    }

    public static String buildPathFromDirectory(String initialPath, File dir) throws IOException {

        File[] softwarePackages = dir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        Arrays.sort(softwarePackages);

        StringBuilder path = new StringBuilder();
        path.append(initialPath).append(":").append(dir);

        for (File d : softwarePackages) {
            path.append(":").append(d.getAbsolutePath());
        }

        return path.toString();

    }

    public static File getRequiredSystemPropertyAsFile(String propertyName) {

        String value = System.getProperty(propertyName);

        if (value == null || value.isEmpty()) {
            throw new RuntimeException("The required property \"" + propertyName + "\" was not found.");
        }

        return new File(value);

    }

    public static String getRequiredSystemPropertyAsString(String propertyName) {

        String value = System.getProperty(propertyName);

        if (value == null || value.isEmpty()) {
            throw new RuntimeException("The required property \"" + propertyName + "\" was not found.");
        }

        return value;

    }

    public static File generateTestWorkingDirectory(File baseWorkingDirectory, String prefix, String testName, String suffix) throws IOException {

        checkArgument(baseWorkingDirectory != null && baseWorkingDirectory.isDirectory(), "The base working directory [%s] does not exist.", baseWorkingDirectory.getAbsolutePath());

        File testWorkingDirectory = new File(baseWorkingDirectory + "/" + prefix + "_" + testName + "_" + suffix + "/");

        if (testWorkingDirectory.exists()) {
            throw new IOException("The directory [" + testWorkingDirectory + "] already exists.");
        }

        if (!testWorkingDirectory.mkdir()) {
            throw new IOException("The directory [" + testWorkingDirectory + "] could not be created.");
        }

        return testWorkingDirectory;

    }

    public static boolean isFileAccessible(String s) {

        if (s == null || s.isEmpty()) {
            log.printf(Level.DEBUG, "Null or empty file [%s]", s);
            return false;
        }

        File f = FileUtils.getFile(s);

        if (f == null) {
            log.printf(Level.DEBUG, "Null file object created from [%s]", s);
            return false;
        }

        if (!f.exists()) {
            log.printf(Level.DEBUG, "File does not exist [%s]", s);
            return false;
        }

        if (!f.isFile()) {
            log.printf(Level.DEBUG, "Not a file [%s]", s);
            return false;
        }

        if (!f.canRead()) {
            log.printf(Level.DEBUG, "Can not read file [%s]", s);
            return false;
        }

        return true;
    }

}
