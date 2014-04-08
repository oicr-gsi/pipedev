package ca.on.oicr.pde.common;

import ca.on.oicr.pde.testing.decider.TestResult;
import static ca.on.oicr.pde.testing.decider.DeciderRunTest.compareReports;
import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DeciderRunTestTest {

    //TODO: write some helper functions so that the json documents below can be dynamically generated
    @Test(enabled = false)
    public void testSomeMethod() throws IOException {

        String expected = "{\n"
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
                + "    \"files\" : [ {\n"
                + "      \"studyTitle\" : \"Test Study 1\",\n"
                + "      \"studyAttributes\" : {\n"
                + "        \"study.geo_lab_group_id\" : \"121\"\n"
                + "      },\n"
                + "      \"experimentName\" : \"Geo_Ovarian_Brain_Colon_Exome_Seq_ILLUMINA_EX_{F*101}{..}{R*101}\",\n"
                + "      \"experimentAttributes\" : { },\n"
                + "      \"sampleName\" : \"OBC_0001_Ov_X_PE_389_EX\",\n"
                + "      \"sampleAttributes\" : {\n"
                + "        \"sample.geo_template_id\" : \"15687\",\n"
                + "        \"sample.geo_tissue_type\" : \"X\",\n"
                + "        \"sample.geo_template_type\" : \"Illumina PE Library Seq\",\n"
                + "        \"sample.geo_run_id_and_position_and_slot\" : \"866_1_1&889_1_1\",\n"
                + "        \"sample.geo_library_source_template_type\" : \"EX\",\n"
                + "        \"sample.geo_reaction_id\" : \"5581&5773\",\n"
                + "        \"sample.geo_tissue_origin\" : \"Ov\",\n"
                + "        \"sample.geo_targeted_resequencing\" : \"Agilent SureSelect ICGC/Sanger Exon\"\n"
                + "      },\n"
                + "      \"sequencerRunName\" : \"120106_h804_0076_AD0LFWACXX\",\n"
                + "      \"sequencerRunAttributes\" : {\n"
                + "        \"sequencerrun.geo_instrument_run_id\" : \"889\"\n"
                + "      },\n"
                + "      \"laneName\" : \"120106_h804_0076_AD0LFWACXX_lane_1\",\n"
                + "      \"laneAttributes\" : {\n"
                + "        \"lane.geo_lane\" : \"1\"\n"
                + "      },\n"
                + "      \"iusTag\" : \"ATCACG\",\n"
                + "      \"iusAttributes\" : { },\n"
                + "      \"workflowName\" : \"GenomicAlignmentNovoalign\",\n"
                + "      \"processingAlgorithm\" : \"PicardAddReadGroups\",\n"
                + "      \"processingAttributes\" : { },\n"
                + "      \"fileMetaType\" : \"application/bam\",\n"
                + "      \"fileAttributes\" : { }\n"
                + "    } ] } ] }";

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
                + "  \"workflowRuns\" : [ {\n"
                + "    \"files\" : [ {\n"
                + "      \"studyTitle\" : \"Ovarian_Brain_Colon_Exome_Seq\",\n"
                + "      \"studyAttributes\" : {\n"
                + "        \"study.geo_lab_group_id\" : \"121\"\n"
                + "      },\n"
                + "      \"experimentName\" : \"Geo_Ovarian_Brain_Colon_Exome_Seq_ILLUMINA_EX_{F*101}{..}{R*101}\",\n"
                + "      \"experimentAttributes\" : { },\n"
                + "      \"sampleName\" : \"OBC_0001_Ov_X_PE_389_EX\",\n"
                + "      \"sampleAttributes\" : {\n"
                + "        \"sample.geo_template_id\" : \"15687\",\n"
                + "        \"sample.geo_tissue_type\" : \"X\",\n"
                + "        \"sample.geo_template_type\" : \"Illumina PE Library Seq\",\n"
                + "        \"sample.geo_run_id_and_position_and_slot\" : \"866_1_1&889_1_1\",\n"
                + "        \"sample.geo_library_source_template_type\" : \"EX\",\n"
                + "        \"sample.geo_reaction_id\" : \"5581&5773\",\n"
                + "        \"sample.geo_tissue_origin\" : \"Ov\",\n"
                + "        \"sample.geo_targeted_resequencing\" : \"Agilent SureSelect ICGC/Sanger Exon\"\n"
                + "      },\n"
                + "      \"sequencerRunName\" : \"120106_h804_0076_AD0LFWACXX\",\n"
                + "      \"sequencerRunAttributes\" : {\n"
                + "        \"sequencerrun.geo_instrument_run_id\" : \"889\"\n"
                + "      },\n"
                + "      \"laneName\" : \"120106_h804_0076_AD0LFWACXX_lane_1\",\n"
                + "      \"laneAttributes\" : {\n"
                + "        \"lane.geo_lane\" : \"1\"\n"
                + "      },\n"
                + "      \"iusTag\" : \"ATCACG\",\n"
                + "      \"iusAttributes\" : { },\n"
                + "      \"workflowName\" : \"GenomicAlignmentNovoalign\",\n"
                + "      \"processingAlgorithm\" : \"PicardAddReadGroups\",\n"
                + "      \"processingAttributes\" : { },\n"
                + "      \"fileMetaType\" : \"application/bam\",\n"
                + "      \"fileAttributes\" : { }\n"
                + "    } ] } ] }";

        //Assert.assertTrue(compareReports(actual, expected));
        Assert.assertTrue(compareReports(TestResult.buildFromJson(actual), TestResult.buildFromJson(expected)));

    }

    @Test(enabled = true)
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

}
