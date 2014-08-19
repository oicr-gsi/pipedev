package ca.on.oicr.pde.testing.common;

import ca.on.oicr.pde.utilities.SeqwareExecutor;
import ca.on.oicr.pde.utilities.ShellExecutor;
import java.io.File;

public abstract class RunTestBase implements org.testng.ITest {

    protected final File seqwareDistribution;
    protected final File seqwareSettings;
    protected final File workingDirectory;
    protected final String testName;
    protected SeqwareExecutor seqwareExecutor;

    public RunTestBase(File seqwareDistribution, File seqwareSettings, File workingDirectory, String testName) {

        this.seqwareDistribution = seqwareDistribution;
        this.seqwareSettings = seqwareSettings;
        this.workingDirectory = workingDirectory;
        this.testName = testName;

        this.seqwareExecutor = new ShellExecutor(testName, seqwareDistribution, seqwareSettings, workingDirectory);

    }

    @Override
    public String getTestName() {
        return testName;
    }
    
    public void setSeqwareExecutor(SeqwareExecutor seqwareExecutor){
        this.seqwareExecutor = seqwareExecutor;
    }

}
