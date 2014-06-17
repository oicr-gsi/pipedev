package ca.on.oicr.pde.testing;

import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import ca.on.oicr.pde.testing.decider.TestResult;
import static ca.on.oicr.pde.testing.decider.DeciderRunTest.compareReports;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DeciderRunTestTest {

    //TODO: write some helper functions so that the json documents below can be dynamically generated
    @Test(enabled = true)
    public void jsonReportComparision() throws IOException {

        String expectedJson = "{\n"
                + "  \"workflowRunCount\" : 1,\n"
                + "  \"studies\" : [ \"Test Study 1\" ],\n"
                + "  \"sequencerRuns\" : [ \"AAA_BBB_CCC_DDD\" ],\n"
                + "  \"lanes\" : [ \"AAA_BBB_CCC_DDD_lane1\" ],\n"
                + "  \"samples\" : [ \"TEST_0001_Ov_X_PE_389_EX\" ],\n"
                + "  \"workflows\" : [ \"GenomicAlignmentNovoalign\" ],\n"
                + "  \"processingAlgorithms\" : [ \"PicardAddReadGroups\" ],\n"
                + "  \"fileMetaTypes\" : [ \"application/bam\" ],\n"
                + "  \"maxInputFiles\" : 1,\n"
                + "  \"minInputFiles\" : 1,\n"
                + "  \"workflowRuns\" : [ {\n"
                + "  \"workflowIni\" : { "
                + "  },"
                + "    \"files\" : [ {\n"
                + "      \"studyTitle\" : [\"Test Study 1\"],\n"
                + "      \"studyAttributes\" : {\n"
                + "        \"study.geo_lab_group_id\" : [[\"121\"]]\n"
                + "      },\n"
                + "      \"experimentName\" : [\"Geo_Ovarian_Brain_Colon_Exome_Seq_ILLUMINA_EX_{F*101}{..}{R*101}\"],\n"
                + "      \"experimentAttributes\" : { },\n"
                + "      \"sampleName\" : [\"OBC_0001_Ov_X_PE_389_EX\"],\n"
                + "      \"sampleAttributes\" : {\n"
                + "        \"sample.geo_template_id\" : [[\"1\",\"2\" ], [\"15687\",\"15688\"]]\n"
                + "      },\n"
                + "      \"sequencerRunName\" : [\"120106_h804_0076_AD0LFWACXX\"],\n"
                + "      \"sequencerRunAttributes\" : {\n"
                + "        \"sequencerrun.geo_instrument_run_id\" : [[\"889\"]]\n"
                + "      },\n"
                + "      \"laneName\" : [\"120106_h804_0076_AD0LFWACXX_lane_1\"],\n"
                + "      \"laneAttributes\" : {\n"
                + "        \"lane.geo_lane\" : [[\"1\"]]\n"
                + "      },\n"
                + "      \"iusTag\" : [\"ATCACG\"],\n"
                + "      \"iusAttributes\" : { },\n"
                + "      \"workflowName\" : [\"GenomicAlignmentNovoalign\"],\n"
                + "      \"processingAlgorithm\" : [\"PicardAddReadGroups\"],\n"
                + "      \"processingAttributes\" : { },\n"
                + "      \"fileMetaType\" : [\"application/bam\"],\n"
                + "      \"fileAttributes\" : { }\n"
                + "    } ] } ] }";

        String actualJson = "{\n"
                + "  \"workflowRunCount\" : 1,\n"
                + "  \"studies\" : [ \"Test Study 1\" ],\n"
                + "  \"sequencerRuns\" : [ \"AAA_BBB_CCC_DDD\" ],\n"
                + "  \"lanes\" : [ \"AAA_BBB_CCC_DDD_lane1\" ],\n"
                + "  \"samples\" : [ \"TEST_0001_Ov_X_PE_389_EX\" ],\n"
                + "  \"workflows\" : [ \"GenomicAlignmentNovoalign\" ],\n"
                + "  \"processingAlgorithms\" : [ \"PicardAddReadGroups\" ],\n"
                + "  \"fileMetaTypes\" : [ \"application/bam\" ],\n"
                + "  \"maxInputFiles\" : 1,\n"
                + "  \"minInputFiles\" : 1,\n"
                + "  \"workflowRuns\" : [ {\n"
                + "  \"workflowIni\" : { "
                + "  },"
                + "    \"files\" : [ {\n"
                + "      \"studyTitle\" : [\"Test Study 1\"],\n"
                + "      \"studyAttributes\" : {\n"
                + "        \"study.geo_lab_group_id\" : [[\"121\"]]\n"
                + "      },\n"
                + "      \"experimentName\" : [\"Geo_Ovarian_Brain_Colon_Exome_Seq_ILLUMINA_EX_{F*101}{..}{R*101}\"],\n"
                + "      \"experimentAttributes\" : { },\n"
                + "      \"sampleName\" : [\"OBC_0001_Ov_X_PE_389_EX\"],\n"
                + "      \"sampleAttributes\" : {\n"
                + "        \"sample.geo_template_id\" : [[ \"15687\",\"15688\"], [\"1\",\"2\" ]]\n"
                + "      },\n"
                + "      \"sequencerRunName\" : [\"120106_h804_0076_AD0LFWACXX\"],\n"
                + "      \"sequencerRunAttributes\" : {\n"
                + "        \"sequencerrun.geo_instrument_run_id\" : [[\"889\"]]\n"
                + "      },\n"
                + "      \"laneName\" : [\"120106_h804_0076_AD0LFWACXX_lane_1\"],\n"
                + "      \"laneAttributes\" : {\n"
                + "        \"lane.geo_lane\" : [[\"1\"]]\n"
                + "      },\n"
                + "      \"iusTag\" : [\"ATCACG\"],\n"
                + "      \"iusAttributes\" : { },\n"
                + "      \"workflowName\" : [\"GenomicAlignmentNovoalign\"],\n"
                + "      \"processingAlgorithm\" : [\"PicardAddReadGroups\"],\n"
                + "      \"processingAttributes\" : { },\n"
                + "      \"fileMetaType\" : [\"application/bam\"],\n"
                + "      \"fileAttributes\" : { }\n"
                + "    } ] } ] }";

        //Assert.assertTrue(compareReports(actual, expected));
        TestResult actual = TestResult.buildFromJson(actualJson);
        TestResult expected = TestResult.buildFromJson(expectedJson);

        Assert.assertTrue(compareReports(actual, expected), "Expected: " + expected.toString() +"\nActual: " + actual);

    }

    @Test(enabled = false)
    public void compareUnordered() throws IOException {

        String expected = "{\n"
                + "  \"workflowRunCount\" : 1,\n"
                + "  \"studies\" : [ \"Ovarian_Brain_Colon_Exome_Seq\" ],\n"
                + "  \"sequencerRuns\" : [ \"120106_h804_0076_AD0LFWACXX\" ],\n"
                + "  \"lanes\" : [ \"111212_h1080_0086_AC045BACXX_lane_1\" ],\n"
                + "  \"samples\" : [ \"OBC_0001_Ov_X_PE_389_EX\" ],\n"
                + "  \"workflows\" : [ \"GenomicAlignmentNovoalign\" ],\n"
                + "  \"processingAlgorithms\" : [ \"PicardAddReadGroups\" ],\n"
                + "  \"fileMetaTypes\" : [ \"application/bam\" ],\n"
                + "  \"maxInputFiles\" : 1,\n"
                + "  \"minInputFiles\" : 1,\n"
                + "  \"workflowRuns\" : [ \n"
                + "     {\n"
                + "      \"files\" : [ {\n"
                + "         \"studyTitle\" : \"Study 2\",\n"
                + "         \"experimentName\" : \"Geo_Ovarian_Brain_Colon_Exome_Seq_ILLUMINA_EX_{F*101}{..}{R*101}\",\n"
                + "         \"sampleName\" : \"OBC_0001_Ov_X_PE_389_EX\",\n"
                + "         \"sequencerRunName\" : \"120106_h804_0076_AD0LFWACXX\",\n"
                + "         \"laneName\" : \"120106_h804_0076_AD0LFWACXX_lane_1\",\n"
                + "         \"iusTag\" : \"ATCACG\",\n"
                + "         \"workflowName\" : \"GenomicAlignmentNovoalign\",\n"
                + "         \"processingAlgorithm\" : \"PicardAddReadGroups\",\n"
                + "         \"fileMetaType\" : \"application/bam\",\n"
                + "         \"fileId\": \"2\"\n"
                + "     } ] },"
                + "     {\n"
                + "         \"files\" : [ {\n"
                + "         \"studyTitle\" : \"Study 2\",\n"
                + "         \"experimentName\" : \"Geo_Ovarian_Brain_Colon_Exome_Seq_ILLUMINA_EX_{F*101}{..}{R*101}\",\n"
                + "         \"sampleName\" : \"OBC_0001_Ov_X_PE_389_EX\",\n"
                + "         \"sequencerRunName\" : \"120106_h804_0076_AD0LFWACXX\",\n"
                + "         \"laneName\" : \"120106_h804_0076_AD0LFWACXX_lane_1\",\n"
                + "         \"iusTag\" : \"ATCACG\",\n"
                + "         \"workflowName\" : \"GenomicAlignmentNovoalign\",\n"
                + "         \"processingAlgorithm\" : \"PicardAddReadGroups\",\n"
                + "         \"fileMetaType\" : \"application/bam\",\n"
                + "         \"fileId\": \"1\"\n"
                + "     } ] },"
                + "     {\n"
                + "      \"files\" : [ {\n"
                + "         \"studyTitle\" : \"Study 1\",\n"
                + "         \"experimentName\" : \"Geo_Ovarian_Brain_Colon_Exome_Seq_ILLUMINA_EX_{F*101}{..}{R*101}\",\n"
                + "         \"sampleName\" : \"OBC_0001_Ov_X_PE_389_EX\",\n"
                + "         \"sequencerRunName\" : \"120106_h804_0076_AD0LFWACXX\",\n"
                + "         \"laneName\" : \"120106_h804_0076_AD0LFWACXX_lane_1\",\n"
                + "         \"iusTag\" : \"ATCACG\",\n"
                + "         \"workflowName\" : \"GenomicAlignmentNovoalign\",\n"
                + "         \"processingAlgorithm\" : \"PicardAddReadGroups\",\n"
                + "         \"fileMetaType\" : \"application/bam\",\n"
                + "         \"fileId\": \"1\"\n"
                + "     } ] }"
                + " ] }";

        String actual = "{\n"
                + "  \"workflowRunCount\" : 1,\n"
                + "  \"studies\" : [ \"Ovarian_Brain_Colon_Exome_Seq\" ],\n"
                + "  \"sequencerRuns\" : [ \"120106_h804_0076_AD0LFWACXX\" ],\n"
                + "  \"lanes\" : [ \"111212_h1080_0086_AC045BACXX_lane_1\" ],\n"
                + "  \"samples\" : [ \"OBC_0001_Ov_X_PE_389_EX\" ],\n"
                + "  \"workflows\" : [ \"GenomicAlignmentNovoalign\" ],\n"
                + "  \"processingAlgorithms\" : [ \"PicardAddReadGroups\" ],\n"
                + "  \"fileMetaTypes\" : [ \"application/bam\" ],\n"
                + "  \"maxInputFiles\" : 1,\n"
                + "  \"minInputFiles\" : 1,\n"
                + "  \"workflowRuns\" : [ "
                + "     {\n"
                + "      \"files\" : [ {\n"
                + "         \"studyTitle\" : \"Study 1\",\n"
                + "         \"experimentName\" : \"Geo_Ovarian_Brain_Colon_Exome_Seq_ILLUMINA_EX_{F*101}{..}{R*101}\",\n"
                + "         \"sampleName\" : \"OBC_0001_Ov_X_PE_389_EX\",\n"
                + "         \"sequencerRunName\" : \"120106_h804_0076_AD0LFWACXX\",\n"
                + "         \"laneName\" : \"120106_h804_0076_AD0LFWACXX_lane_1\",\n"
                + "         \"iusTag\" : \"ATCACG\",\n"
                + "         \"workflowName\" : \"GenomicAlignmentNovoalign\",\n"
                + "         \"processingAlgorithm\" : \"PicardAddReadGroups\",\n"
                + "         \"fileMetaType\" : \"application/bam\",\n"
                + "         \"fileId\": \"1\"\n"
                + "     } ] },"
                + "     {\n"
                + "      \"files\" : [ {\n"
                + "         \"studyTitle\" : \"Study 2\",\n"
                + "         \"experimentName\" : \"Geo_Ovarian_Brain_Colon_Exome_Seq_ILLUMINA_EX_{F*101}{..}{R*101}\",\n"
                + "         \"sampleName\" : \"OBC_0001_Ov_X_PE_389_EX\",\n"
                + "         \"sequencerRunName\" : \"120106_h804_0076_AD0LFWACXX\",\n"
                + "         \"laneName\" : \"120106_h804_0076_AD0LFWACXX_lane_1\",\n"
                + "         \"iusTag\" : \"ATCACG\",\n"
                + "         \"workflowName\" : \"GenomicAlignmentNovoalign\",\n"
                + "         \"processingAlgorithm\" : \"PicardAddReadGroups\",\n"
                + "         \"fileMetaType\" : \"application/bam\",\n"
                + "         \"fileId\": \"1\"\n"
                + "     } ] },"
                + "     {\n"
                + "      \"files\" : [ {\n"
                + "         \"studyTitle\" : \"Study 2\",\n"
                + "         \"experimentName\" : \"Geo_Ovarian_Brain_Colon_Exome_Seq_ILLUMINA_EX_{F*101}{..}{R*101}\",\n"
                + "         \"sampleName\" : \"OBC_0001_Ov_X_PE_389_EX\",\n"
                + "         \"sequencerRunName\" : \"120106_h804_0076_AD0LFWACXX\",\n"
                + "         \"laneName\" : \"120106_h804_0076_AD0LFWACXX_lane_1\",\n"
                + "         \"iusTag\" : \"ATCACG\",\n"
                + "         \"workflowName\" : \"GenomicAlignmentNovoalign\",\n"
                + "         \"processingAlgorithm\" : \"PicardAddReadGroups\",\n"
                + "         \"fileMetaType\" : \"application/bam\",\n"
                + "         \"fileId\": \"2\"\n"
                + "     } ] }"
                + " ] }";

        //Assert.assertTrue(compareReports(actual, expected));
        TestResult actualObject = TestResult.buildFromJson(actual);
        TestResult expectedObject = TestResult.buildFromJson(expected);

        Assert.assertEquals(3, actualObject.getWorkflowRuns().size());
        Assert.assertEquals(3, expectedObject.getWorkflowRuns().size());

        System.out.println("actual: \n" + actualObject);
        System.out.println("expected: \n" + expectedObject);

        Assert.assertTrue(compareReports(actualObject, expectedObject));

    }

    @Test
    public void compareMergedFiles() throws IOException {

        String actual = "{\n"
                + "  \"workflowRunCount\" : 1,\n"
                + "  \"studies\" : [ \"Study 1\" ],\n"
                + "  \"sequencerRuns\" : [ \"B\", \"A\", \"C\" ],\n"
                + "  \"lanes\" : [ \"1\", \"2\", \"3\", \"4\", \"5\" ],\n"
                + "  \"samples\" : [ \"1\", \"3\", \"2\" ],\n"
                + "  \"workflows\" : [ \"1\", \"2\" ],\n"
                + "  \"processingAlgorithms\" : [ \"1\", \"3\", \"2\" ],\n"
                + "  \"fileMetaTypes\" : [ \"1\" ],\n"
                + "  \"maxInputFiles\" : 1,\n"
                + "  \"minInputFiles\" : 1,\n"
                + "  \"workflowRuns\" : [ {\n"
                + "    \"workflowIni\" : {\n"
                + "      \"test\" : \"1500\"\n"
                + "    },"
                + "    \"files\" : [ {\n"
                + "      \"studyTitle\" : [ \"2\" ],\n"
                + "      \"studyAttributes\" : {\n"
                + "        \"attr1\" : [[ \"1\", \"3\", \"2\", \"4\" ]]\n"
                + "      }"
                + "    } ]\n"
                + "  }, "
                + "{\n"
                + "    \"workflowIni\" : {\n"
                + "      \"test\" : \"1500\"\n"
                + "    },"
                + "    \"files\" : [ {\n"
                + "      \"studyTitle\" : [ \"1\" ],\n"
                + "      \"studyAttributes\" : {\n"
                + "        \"attr1\" : [[ \"4\", \"1\", \"2\", \"3\" ]]\n"
                + "      }"
                + "    } ]\n"
                + "  } ] }";

        String expected = "{\n"
                + "  \"workflowRunCount\" : 1,\n"
                + "  \"studies\" : [ \"Study 1\" ],\n"
                + "  \"sequencerRuns\" : [ \"A\", \"B\", \"C\" ],\n"
                + "  \"lanes\" : [ \"1\", \"5\", \"3\", \"4\", \"2\" ],\n"
                + "  \"samples\" : [ \"1\", \"2\", \"3\" ],\n"
                + "  \"workflows\" : [ \"1\", \"2\" ],\n"
                + "  \"processingAlgorithms\" : [ \"2\", \"3\", \"1\" ],\n"
                + "  \"fileMetaTypes\" : [ \"1\" ],\n"
                + "  \"maxInputFiles\" : 1,\n"
                + "  \"minInputFiles\" : 1,\n"
                + "  \"workflowRuns\" : [ {\n"
                + "    \"workflowIni\" : {\n"
                + "      \"test\" : \"1500\"\n"
                + "    },"
                + "    \"files\" : [ {\n"
                + "      \"studyTitle\" : [ \"1\" ],\n"
                + "      \"studyAttributes\" : {\n"
                + "        \"attr1\" : [[ \"1\", \"4\", \"2\", \"3\" ]]\n"
                + "      }"
                + "    } ]\n"
                + "  }, {\n"
                + "    \"workflowIni\" : {\n"
                + "      \"test\" : \"1500\"\n"
                + "    },"
                + "    \"files\" : [ {\n"
                + "      \"studyTitle\" : [ \"2\" ],\n"
                + "      \"studyAttributes\" : {\n"
                + "        \"attr1\" : [[ \"1\", \"4\", \"2\", \"3\" ]]\n"
                + "      }"
                + "    } ]\n"
                + "  } ] }";

        TestResult actualObject = TestResult.buildFromJson(actual);
        TestResult expectedObject = TestResult.buildFromJson(expected);

        Assert.assertEquals(2, actualObject.getWorkflowRuns().size());
        Assert.assertEquals(2, expectedObject.getWorkflowRuns().size());
        
        List<ReducedFileProvenanceReportRecord> files = actualObject.getWorkflowRuns().get(0).getFiles();
        for(ReducedFileProvenanceReportRecord r : files){
            Collection actualAttrSet = r.getStudyAttributes().get("attr1");
            Collection expectedAttrSet = new ArrayList();
            expectedAttrSet.add(new TreeSet(Arrays.asList("1", "2" , "3", "4")));
            Assert.assertEquals(actualAttrSet, expectedAttrSet);
        }
        
        Assert.assertTrue(actualObject.equals(expectedObject), "expected:\n" + expectedObject.toString());

    }

}
