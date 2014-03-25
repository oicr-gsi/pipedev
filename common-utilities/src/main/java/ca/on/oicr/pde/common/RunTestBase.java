package ca.on.oicr.pde.common;

import ca.on.oicr.pde.utilities.SeqwareExecutor;
import ca.on.oicr.pde.utilities.ShellExecutor;
import java.io.File;

public abstract class RunTestBase implements org.testng.ITest{

    protected final File seqwareDistribution;
    protected final File seqwareSettings;
    protected final File workingDirectory;
    protected final String testName;
    protected final SeqwareExecutor exec;

    public RunTestBase(File seqwareDistribution, File seqwareSettings, File workingDirectory, String testName) {

        this.seqwareDistribution = seqwareDistribution;
        this.seqwareSettings = seqwareSettings;
        this.workingDirectory = workingDirectory;
        this.testName = testName;

        exec = new ShellExecutor(testName, seqwareDistribution, seqwareSettings, workingDirectory);

    }
    
    @Override
    public String getTestName(){
        
        return testName;
        
    }

}
