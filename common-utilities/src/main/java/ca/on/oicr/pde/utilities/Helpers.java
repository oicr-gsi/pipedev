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
import com.jcabi.manifests.Manifests;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class Helpers {

    private final static Logger log = LogManager.getLogger(Helpers.class);

    public static File getFileFromResource(String resourceFilePath, Path outputDirectory) throws IOException {
        checkArgument(outputDirectory.toFile().exists() && outputDirectory.toFile().isDirectory() && outputDirectory.toFile().canWrite());
        String fileName = FilenameUtils.getName(resourceFilePath);
        checkArgument(fileName != null && StringUtils.isNotEmpty(fileName));

        Path outputPath = outputDirectory.resolve(fileName);
        try (InputStream resourceStream = Helpers.class.getClassLoader().getResourceAsStream(resourceFilePath)) {
            if (resourceStream == null) {
                throw new IOException("The resource [" + resourceFilePath + "] is not accessible");
            }
            Files.copy(resourceStream, outputPath);
        }

        return outputPath.toFile();
    }

    public static File getFileFromResource(String resourceFilePath) throws IOException {
        Path tempDir = Files.createTempDirectory(null);
        return getFileFromResource(resourceFilePath, tempDir);
    }

    public static File getScriptFromResource(String scriptName, Path directory) throws IOException {
        File scriptFile = getFileFromResource(scriptName, directory);
        scriptFile.setExecutable(true);
        return scriptFile;
    }

    public static File getScriptFromResource(String scriptName) throws IOException {
        Path tempDir = Files.createTempDirectory(null);
        File scriptFile = getFileFromResource(scriptName, tempDir);
        scriptFile.setExecutable(true);
        scriptFile.deleteOnExit();
        tempDir.toFile().deleteOnExit();
        return scriptFile;
    }

    public static String getStringFromResource(String resourceFilePath) throws IOException {

        InputStream resourceStream = Helpers.class.getResourceAsStream(resourceFilePath);

        if (resourceStream == null) {
            throw new IOException("The resource [" + resourceFilePath + "] is not accessible");
        }

        String result = IOUtils.toString(resourceStream);

        return result;
    }

    public static String executeCommand(String id, String command, File workingDirectory,
            Map<String, String>... environmentVariables) throws IOException {
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

        log.printf(Level.INFO, "[%s] is executing a bash shell command:\nCommand: [%s]\nWorking directory: [%s]\nStd out/err file: [%s]",
                id, command, workingDirectory, outputFilePath);

        CommandRunner.CommandResult r = cr.runCommand();
        Assert.assertTrue(r.getExitCode() == 0,
                String.format("The following command returned a non-zero exit code [%s]:\n%s\nOutput from command:\n%s\n",
                        r.getExitCode(), command, r.getOutput()));

        return r.getOutput().trim();

    }

    public static File generateSeqwareSettings(File workingDirectory, String webserviceUrl, String schedulingSystem,
            String schedulingHost) throws IOException {

        //TODO: implement this in java
        StringBuilder command = new StringBuilder();
        command.append(getScriptFromResource("generateSeqwareSettings.sh"));
        command.append(" ").append(workingDirectory);
        command.append(" ").append(webserviceUrl);
        command.append(" ").append(schedulingSystem);
        command.append(" ").append(schedulingHost);
        command.append(" ").append(UUID.randomUUID());

        return new File(executeCommand("generateSeqwareSettings()", command.toString(), workingDirectory));

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

    public static File generateTestWorkingDirectory(File baseWorkingDirectory, String prefix, String testName,
            String suffix) throws IOException {

        checkArgument(baseWorkingDirectory != null && baseWorkingDirectory.isDirectory(),
                "The base working directory [%s] does not exist.", baseWorkingDirectory.getAbsolutePath());

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

    public static <T> T deepCopy(T object) {
        return (T) SerializationUtils.deserialize(SerializationUtils.serialize((Serializable) object));
    }

    public static File getBundledWorkflow() {
        File bundledWorkflow = null;

        //try to load config.properties
        Configuration config = null;
        try {
            config = new PropertiesConfiguration("config.properties");
        } catch (ConfigurationException ce) {
            //do nothing, try the next steps
        }

        //try to retrieve the workflow bundle path
        if (config != null) {
            String tmp = config.getString("workflow_bundle_path");
            if (tmp != null && !tmp.isEmpty()) {
                bundledWorkflow = new File(tmp);
            }
        }

        if (Manifests.exists("Workflow-Bundle-Path") && !Manifests.read("Workflow-Bundle-Path").isEmpty()) {
            bundledWorkflow = new File(Manifests.read("Workflow-Bundle-Path"));
        }

        if (System.getProperty("bundledWorkflow") != null && !System.getProperty("bundledWorkflow").isEmpty()) {
            bundledWorkflow = getRequiredSystemPropertyAsFile("bundledWorkflow");
        }

        return bundledWorkflow;
    }

    public static String getPgpassPassword(String host, int port, String user) throws IOException {

        String pgpass = System.getProperty("PGPASSFILE");

        File pgpassFile = null;
        if (pgpass != null && !pgpass.isEmpty()) {
            pgpassFile = new File(pgpass);
        } else {
            pgpassFile = new File(System.getProperty("user.home"), ".pgpass");
        }

        if (!pgpassFile.exists() || !pgpassFile.canRead()) {
            throw new RuntimeException("Unable to read pgpass file [" + pgpassFile + "]");
        }

        String pgpassPassword = null;
        Pattern p = Pattern.compile("([A-Za-z0-9_.-]+?):([A-Za-z0-9_.]+?):([A-Za-z0-9_.*]+?):([A-Za-z0-9_.]+?):([A-Za-z0-9_.]+?)");
        for (String line : FileUtils.readFileToString(pgpassFile).split("\\r?\\n")) {
            Matcher m = p.matcher(line);
            if (m.matches()) {
                String matchedHost = m.group(1);
                int matchedPort = Integer.parseInt(m.group(2));
                String matchedDatabaseName = m.group(3);
                String matchedUser = m.group(4);
                String matchedPassword = m.group(5);

                if (matchedHost.equals(host) && matchedPort == port && matchedDatabaseName.equals("*") && matchedUser.equals(user)) {
                    if (pgpassPassword != null) {
                        throw new RuntimeException("There are multiple pgpass matches.");
                    }
                    pgpassPassword = matchedPassword;
                }
            }
        }

        return pgpassPassword;
    }
}
