package ca.on.oicr.pde.testing.common;

import ca.on.oicr.pde.dao.executor.SeqwareExecutor;
import ca.on.oicr.pde.dao.executor.ShellExecutor;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.metadata.MetadataFactory;
import net.sourceforge.seqware.common.util.maptools.MapTools;

public abstract class RunTestBase implements org.testng.ITest {

    protected final File seqwareDistribution;
    protected final File seqwareSettings;
    protected final File workingDirectory;
    protected final String testName;
    protected SeqwareExecutor seqwareExecutor;
    protected final Metadata metadata;

    public RunTestBase(File seqwareDistribution, File seqwareSettings, File workingDirectory, String testName) {

        this.seqwareDistribution = seqwareDistribution;
        this.seqwareSettings = seqwareSettings;
        this.workingDirectory = workingDirectory;
        this.testName = testName;

        this.seqwareExecutor = new ShellExecutor(testName, seqwareDistribution, seqwareSettings, workingDirectory);

        Map<String, String> settings = new HashMap<>();
        MapTools.ini2Map(seqwareSettings.getAbsolutePath(), settings, true);
        this.metadata = MetadataFactory.get(settings);
    }

    @Override
    public String getTestName() {
        return testName;
    }
    
    public void setSeqwareExecutor(SeqwareExecutor seqwareExecutor){
        this.seqwareExecutor = seqwareExecutor;
    }

}
