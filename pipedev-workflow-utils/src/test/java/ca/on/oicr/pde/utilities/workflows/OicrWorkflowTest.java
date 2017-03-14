package ca.on.oicr.pde.utilities.workflows;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.seqware.pipeline.workflowV2.model.BashJob;
import net.sourceforge.seqware.pipeline.workflowV2.model.Job;
import net.sourceforge.seqware.pipeline.workflowV2.model.SqwFile;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author mtaschuk
 */
public class OicrWorkflowTest {

    private OicrWorkflow instance;
    private HashMap<String, String> outputFiles;

    @BeforeMethod
    public void setUp() throws Exception {
        Map<String, String> configs = new HashMap<>();
        //for getProperty
        configs.put("valid", "valid result");
        //for getInputFiles
        configs.put("valid_files", "/u/me/in1.txt,/u/me/in2.txt");
        configs.put("valid_files_one", "/u/me/in1.txt");
        //for defineOutputFiles

        outputFiles = new HashMap();
        outputFiles.put("/u/me/out1.txt", "txt/test1");
        outputFiles.put("/u/me/out2.txt", "txt/test2");
        String out = "";
        for (String path : outputFiles.keySet()) {
            if (out.length() > 0) {
                out += ",";
            }
            out += path + "::" + outputFiles.get(path);
        }
        configs.put("output_files", out);
        configs.put("optional_key", "optional_value");
        instance = new OicrWorkflowImpl();
        instance.setConfigs(configs);

        //super.setUp();
    }

    @Test
    public void testGetProperty() {
        System.out.println("getProperty");
        String key = "valid";
        String expResult = "valid result";
        String result = instance.getProperty(key);
        Assert.assertTrue(instance.isWorkflowIsValid(), "Workflow should be valid");
        Assert.assertEquals(result, expResult, "");
    }

    @Test
    public void testGetPropertyNull() {
        System.out.println("getPropertyNull");
        String key = "invalid";
        String result = instance.getProperty(key);
        Assert.assertFalse(instance.isWorkflowIsValid(), "Workflow should be invalid");
        Assert.assertNull(result);
    }

    @Test
    public void testGetOptionalProperty_Absent() {
        System.out.println("getOptionalProperty");
        String key = "opional_and_is_not_set";
        String defaultValue = "default_value";
        String result = instance.getOptionalProperty(key, defaultValue);
        Assert.assertTrue(instance.isWorkflowIsValid(), "Workflow should be valid");
        Assert.assertEquals(result, defaultValue, String.format("\"%s\" != \"%s\"", defaultValue, result));
    }

    @Test
    public void testGetOptionalProperty_Present() {
        System.out.println("getOptionalProperty");
        String key = "optional_key";
        String defaultValue = null;
        String expected = "optional_value";
        String result = instance.getOptionalProperty(key, defaultValue);
        Assert.assertTrue(instance.isWorkflowIsValid(), "Workflow should be valid");
        Assert.assertEquals(result, expected, String.format("\"%s\" != \"%s\"", expected, result));
    }

    @Test
    public void testGetInputFiles() {
        System.out.println("getInputFiles");
        String[] twoFiles = instance.getInputFiles("valid_files");
        Assert.assertEquals(twoFiles.length, 2, "There should be two input files");
        Assert.assertEquals(twoFiles[0], "/u/me/in1.txt");
        Assert.assertEquals(twoFiles[1], "/u/me/in2.txt");

        String[] oneFile = instance.getInputFiles("valid_files_one");
        Assert.assertEquals(oneFile.length, 1, "There should be one input file");
        Assert.assertEquals(oneFile[0], "/u/me/in1.txt");
    }

    @Test
    public void testGetInputFilesNull() {
        System.out.println("getInputFilesNull");
        String[] invalidFiles = instance.getInputFiles("invalid_files");
        Assert.assertEquals(invalidFiles.length, 0, "getInputFiles should return a 0 length array");
        Assert.assertFalse(instance.isWorkflowIsValid(), "Workflow should be invalid");
    }

    @Test
    public void testProvisionInputFilesMany() {
        System.out.println("provisionInputFilesMany");
        SqwFile[] twoFiles = instance.provisionInputFiles("valid_files");
        Assert.assertEquals(twoFiles.length, 2, "There should be two input files");
        Assert.assertEquals(twoFiles[0].getSourcePath(), "/u/me/in1.txt");
        Assert.assertEquals(twoFiles[1].getSourcePath(), "/u/me/in2.txt");
        Assert.assertTrue(twoFiles[0].isInput(), "The 1st file should be an input file");
        Assert.assertTrue(twoFiles[1].isInput(), "The 2nd file should be an input file");
    }

    @Test
    public void testProvisionInputFilesOne() {
        System.out.println("provisionInputFilesOne");
        SqwFile[] oneFile = instance.provisionInputFiles("valid_files_one");
        Assert.assertEquals(oneFile.length, 1, "There should be one input file");
        Assert.assertEquals(oneFile[0].getSourcePath(), "/u/me/in1.txt");
        Assert.assertTrue(oneFile[0].isInput(), "The file should be an input file");
    }

    @Test
    public void testProvisionInputFilesNull() {
        System.out.println("provisionInputFiles");
        SqwFile[] invalidFiles = instance.provisionInputFiles("invalid_files");
        Assert.assertEquals(invalidFiles.length, 0, "getInputFiles should return a 0 length array");
        Assert.assertFalse(instance.isWorkflowIsValid(), "Workflow should be invalid");
    }

    @Test
    public void testNewJob() {
        System.out.println("newJob");
        Job j = instance.newJob("validjob");
        Map<String, Job> jobs = instance.getJobs();
        Job result = jobs.get("validjob_0");
        Assert.assertEquals(result, j, "Valid Job's title is incorrect. Expecting validjob_0 but "
                + "couldn't find the job");
        Assert.assertNotNull(j, "Job should not be null!");
    }

    @Test
    public void testNewJobMultiple() {
        System.out.println("newJobMultiple");
        Job j = null;
        for (int i = 0; i < 2; i++) {
            j = instance.newJob("validjob");
        }
        Map<String, Job> jobs = instance.getJobs();
        Job result1 = jobs.get("validjob_0");
        Job result2 = jobs.get("validjob_1");
        Assert.assertEquals(result2, j, "Valid Job's title is incorrect. Expecting validjob_1 but "
                + "couldn't find the job");
        Assert.assertNotNull(j, "Job should not be null!");
        Assert.assertNotNull(result1, "Job should not be null!");
    }

    @Test
    public void testDefineOutputFiles() throws Exception {
        System.out.println("defineOutputFiles");
        BashJob job = new BashJob("ValidJob");
        instance.defineOutputFiles(job);

        Collection<SqwFile> files = job.getFiles();
        Assert.assertEquals(files.size(), outputFiles.size(), "Wrong number of files produced");

        for (SqwFile file : files) {
            Assert.assertNotNull(outputFiles.get(file.getSourcePath()));
            Assert.assertEquals(file.getType(), outputFiles.get(file.getSourcePath()));
        }
    }

    @Test
    public void testDefineOutputFilesManualOutput() throws Exception {
        System.out.println("defineOutputFilesManualOutput");
        BashJob job = new BashJob("ValidJob");
        instance.getConfigs().put("manual_output", "true");
        instance.defineOutputFiles(job);

        Collection<SqwFile> files = job.getFiles();
        Assert.assertEquals(files.size(), outputFiles.size(), "Wrong number of files produced");

        for (SqwFile file : files) {
            Assert.assertNotNull(outputFiles.get(file.getSourcePath()));
            Assert.assertEquals(file.getType(), outputFiles.get(file.getSourcePath()));
        }
    }

    public class OicrWorkflowImpl extends OicrWorkflow {

        @Override
        public void buildWorkflow() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
