package ca.on.oicr.pde.testing.workflow;

import com.google.common.base.Joiner;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import net.sourceforge.seqware.common.util.workflowtools.WorkflowInfo;
import net.sourceforge.seqware.pipeline.bundle.Bundle;
import net.sourceforge.seqware.pipeline.workflowV2.AbstractWorkflowDataModel;
import net.sourceforge.seqware.pipeline.workflowV2.WorkflowV2Utility;
import net.sourceforge.seqware.pipeline.workflowV2.model.AbstractJob;
import net.sourceforge.seqware.pipeline.workflowV2.model.SqwFile;
import org.apache.commons.io.FileUtils;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.reflect.Whitebox;
import org.testng.Assert;

/**
 *
 * @author mlaszloffy
 */
public class DryRun<T extends AbstractWorkflowDataModel> {

    private final String bundleDirectory;
    private final Map<String, String> config;
    private AbstractWorkflowDataModel w;

    public DryRun(String bundleDirectory, Map<String, String> config, Class<T> workflowClass) throws IOException, InstantiationException, IllegalAccessException {
        this.bundleDirectory = bundleDirectory;

        Properties p = new Properties();
        WorkflowInfo wi = Bundle.findBundleInfo(new File(bundleDirectory)).getWorkflowInfo().get(0);
        String configPath = wi.getConfigPath();
        configPath = configPath.replace(wi.getBaseDir(), "${workflow_bundle_dir}");
        configPath = configPath.replace("${workflow_bundle_dir}", bundleDirectory);
        File defaults = new File(configPath);
        p.load(FileUtils.openInputStream(defaults));
        Map<String, String> defaultConfig = new HashMap<>((Map) p);

        this.config = new HashMap<>(defaultConfig);
        this.config.putAll(config);

        this.w = (AbstractWorkflowDataModel) workflowClass.newInstance();
    }

    // Mocking of net.sourceforge.seqware.pipeline.workflowV2.WorkflowDataModelFactory.getWorkflowDataModel()
    public void buildWorkflowModel() throws IllegalAccessException, Exception {

        w.setConfigs(config);

        //Need to mock basedir as it is retrieved from the WS
        MemberModifier.field(AbstractWorkflowDataModel.class, "basedir").set(w, bundleDirectory);

        //Get the data from metadata.xml
        Map<String, String> metaInfo = WorkflowV2Utility.parseMetaInfo(FileUtils.getFile(bundleDirectory));

        //Build the workflow model
        AbstractWorkflowDataModel.prepare(w);
        Whitebox.invokeMethod(w, "setMetadata_output_file_prefix", w.getConfigs().get("output_prefix"));
        Whitebox.invokeMethod(w, "setMetadata_output_dir", w.getConfigs().get("output_dir"));
        w.setName(metaInfo.get("name"));
        w.setVersion(metaInfo.get("workflow_version"));
        w.setRandom("" + new Random(System.currentTimeMillis()).nextInt(100000000)); //seqware's random int method
        w.setupDirectory();
        w.setupFiles();
        w.setupWorkflow();
        w.setupEnvironment();
        w.buildWorkflow();
        w.wrapup();
    }

    public void validateWorkflow() {

        //check for null string
        for (AbstractJob j : w.getWorkflow().getJobs()) {

            String c = Joiner.on(" ").useForNull("null").join(j.getCommand().getArguments());

            //check for null string
            Assert.assertFalse(c.contains("null"), "Warning: command contains \"null\":\n" + c + "\n");

            // check for missing spaces
            Assert.assertFalse(c.matches("(.*)[^ ]--(.*)"));
        }

        //view output files
        for (AbstractJob j : w.getWorkflow().getJobs()) {
            for (SqwFile f : j.getFiles()) {
                if (f.isOutput()) {
                    System.out.println(f.getProvisionedPath());
                }
            }
        }
    }

}
