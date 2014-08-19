package ca.on.oicr.pde.testing.decider;

import ca.on.oicr.pde.model.FileProvenanceReportRecord;
import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WorkflowRunReportTest {

    @Test
    public void testWorkflowRunReportCompare() {

        FileProvenanceReportRecord f1 = new FileProvenanceReportRecord.Builder(1).setExperimentName("Test1").setFilePath("/tmp/1").setFileSwid("1").build();
        FileProvenanceReportRecord f2 = new FileProvenanceReportRecord.Builder(2).setExperimentName("Test2").setFilePath("/tmp/2").setFileSwid("2").build();

        WorkflowRunReport expected = new WorkflowRunReport();
        expected.setFiles(Arrays.asList(new ReducedFileProvenanceReportRecord(f1), new ReducedFileProvenanceReportRecord(f2)));

        WorkflowRunReport actual = new WorkflowRunReport();
        actual.setFiles(Arrays.asList(new ReducedFileProvenanceReportRecord(f2), new ReducedFileProvenanceReportRecord(f1)));

        Assert.assertEquals(actual, expected);

    }

}
