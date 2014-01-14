package ca.on.oicr.pde.utilities.workflows.jobfactory;

import net.sourceforge.seqware.pipeline.workflowV2.model.Job;
import net.sourceforge.seqware.pipeline.workflowV2.model.Workflow;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author mtaschuk
 */
public class PicardToolsTest {

    private PicardTools picard;

//    public PicardToolsTest(String testName) {
//        super(testName);
//    }
    @BeforeMethod
    protected void setUp() throws Exception {
        //super.setUp();
        picard = new PicardTools(new Workflow());
    }

//    @Override
//    protected void tearDown() throws Exception {
//        super.tearDown();
//    }
    @Test
    public void testMarkDuplicates() {
        System.out.println("testMarkDuplicates");
        Job job = picard.markDuplicates("java", "MarkDuplicates.jar", 4000, "/tmp", "input.bam",
                "output.bam", "file.metrics", null);
        String expected = ("java -Xmx4000M -jar MarkDuplicates.jar INPUT=input.bam OUTPUT=output.bam "
                + "VALIDATION_STRINGENCY=SILENT TMP_DIR=/tmp METRICS_FILE=file.metrics");

        System.out.println(job.getCommand().getArguments().get(0).toString());
        CommandChecker.checkEm(job.getCommand().getArguments(), expected);
        Assert.assertEquals(job.getMaxMemory(), "8000");
    }

    @Test
    public void testMergeBamFiles() {
        System.out.println("testMergeBamFiles");
        Job job = picard.mergeSamFiles("java", "MergeBam.jar", 2000, "/tmp",
                "coordinate", true, true, null, "output.bam", "input1.bam", "input2.bam");
        String expected = ("java -Xmx2000M -jar MergeBam.jar OUTPUT=output.bam "
                + "VALIDATION_STRINGENCY=SILENT TMP_DIR=/tmp SORT_ORDER=coordinate CREATE_INDEX=true "
                + "INPUT=input1.bam INPUT=input2.bam ASSUME_SORTED=true USE_THREADING=true");
        CommandChecker.checkEm(job.getCommand().getArguments(), expected);
        Assert.assertEquals(job.getMaxMemory(), "4000");
    }
    
    @Test
    public void testMergeBamFiles2() {
        System.out.println("testMergeBamFiles2");
        Job job = picard.mergeSamFiles("java", "MergeBam.jar", 2000, "/tmp",
                "coordinate", false, false, null,"output.bam", "input1.bam", "input2.bam");
        String expected = "java -Xmx2000M -jar MergeBam.jar OUTPUT=output.bam "
                + "VALIDATION_STRINGENCY=SILENT TMP_DIR=/tmp SORT_ORDER=coordinate CREATE_INDEX=true "
                + "INPUT=input1.bam INPUT=input2.bam";
        CommandChecker.checkEm(job.getCommand().getArguments(), expected);
        Assert.assertEquals(job.getMaxMemory(), "4000");
    }

    @Test
    public void testMergeBamFiles3() {
        System.out.println("testMergeBamFiles2");
        Job job = picard.mergeSamFiles("java", "MergeSamFiles.jar", 3000, "/tmp", "output.bam", "input1.bam", "input2.bam");
        String expected = ("java -Xmx3000M -jar MergeSamFiles.jar OUTPUT=output.bam "
                + "VALIDATION_STRINGENCY=SILENT TMP_DIR=/tmp SORT_ORDER=coordinate CREATE_INDEX=true "
                + "INPUT=input1.bam INPUT=input2.bam USE_THREADING=true");
        CommandChecker.checkEm(job.getCommand().getArguments(), expected);
        Assert.assertEquals(job.getMaxMemory(), "6000");
    }

    @Test
    public void testSortBamFile() {
        System.out.println("testSortBamFile");
        Job job = picard.sortSamFile("java", "SortSamFiles.jar", 3000, "/tmp", "coordinate", "output.bam", "input1.bam", null);
        String expected = ("java -Xmx3000M -jar SortSamFiles.jar OUTPUT=output.bam INPUT=input1.bam "
                + "VALIDATION_STRINGENCY=SILENT TMP_DIR=/tmp SORT_ORDER=coordinate CREATE_INDEX=true");
        CommandChecker.checkEm(job.getCommand().getArguments(), expected);
        Assert.assertEquals(job.getMaxMemory(), "6000");
    }

    @Test
    public void testFixMate() {
        System.out.println("testFixMate");
        Job job = picard.sortSamFile("java", "FixMate.jar", 3000, "/tmp", "coordinate", "output.bam", "input1.bam", null);
        String expected = ("java -Xmx3000M -jar FixMate.jar OUTPUT=output.bam INPUT=input1.bam "
                + "VALIDATION_STRINGENCY=SILENT TMP_DIR=/tmp SORT_ORDER=coordinate CREATE_INDEX=true");
        CommandChecker.checkEm(job.getCommand().getArguments(), expected);
        Assert.assertEquals(job.getMaxMemory(), "6000");
    }
}
