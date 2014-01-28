package ca.on.oicr.pde.common;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

public class CommandRunner {

    protected Map<String, String> additionalEnvironmentVariables;
    protected CommandLine command;
    protected File workingDirectory;

    public CommandRunner() {

        additionalEnvironmentVariables = new HashMap<String, String>();

    }

    public CommandRunner addEnvironmentVariable(String key, String value) {

        additionalEnvironmentVariables.put(key, value);
        return this;

    }

    public CommandRunner addEnvironmentVariable(Map<String, String> environmentVariables) {

        additionalEnvironmentVariables.putAll(environmentVariables);
        return this;

    }

    public CommandRunner setWorkingDirectory(File workingDirectory) {

        this.workingDirectory = workingDirectory;
        return this;
    }

    public CommandResult runCommand() throws IOException {

        CommandResult result = new CommandResult();

        //The following code is adapted from:
        //http://stackoverflow.com/questions/6295866/how-can-i-capture-the-output-of-a-command-as-a-string-with-commons-exec
        DefaultExecutor commandExecutor = new DefaultExecutor();

        //ByteArrayOutputStream as we don't know how much output we'll get back from the command
        ByteArrayOutputStream commandOutputStream = new ByteArrayOutputStream();

        //Copies standard output and error of subprocesses to standard output and error of the parent process
        PumpStreamHandler outputStreamHandler = new PumpStreamHandler(commandOutputStream);

        commandExecutor.setStreamHandler(outputStreamHandler);
        commandExecutor.setExitValue(0); //expected return value

        //Set the initial working directory for the command
        if (workingDirectory != null) {
            commandExecutor.setWorkingDirectory(workingDirectory);
        }

        //Get existing then add or replace additional environment variables
        Map env = EnvironmentUtils.getProcEnvironment();
        env.putAll(additionalEnvironmentVariables);

        long start = System.nanoTime();
        try {
            //blocks until command completes
            //exit code is returned and stdout is stored into commandOutputStream
            result.setExitCode(commandExecutor.execute(command, env));
        } catch (ExecuteException ee) {
            result.setExitCode(ee.getExitValue());
        }
        result.setOutput(commandOutputStream.toString());
        result.setExecutionTime(System.nanoTime() - start);

        return result;
    }

    public CommandRunner setCommand(String command) {

        this.command = CommandLine.parse(command);
        return this;

    }

    public CommandRunner setCommand(CommandLine command) {

        this.command = command;
        return this;

    }

    public class CommandResult {

        private String output = "";
        private int exitCode = 255;
        private long executionTime = 0;

        private CommandResult() {

        }

        public long getExecutionTime() {

            return executionTime;

        }

        private void setExecutionTime(long executionTime) {

            this.executionTime = executionTime;

        }

        public String getOutput() {

            return output;

        }

        private void setOutput(String output) {

            this.output = output;

        }

        public int getExitCode() {

            return exitCode;

        }

        private void setExitCode(int exitCode) {

            this.exitCode = exitCode;

        }

    }

}
