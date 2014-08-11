package ca.on.oicr.pde.testing.decider;

import ca.on.oicr.pde.model.FileProvenanceReportRecord;
import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import ca.on.oicr.pde.parsers.FileProvenanceReport;
import ca.on.oicr.pde.utilities.Helpers;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DeciderRunTestTest {

    @Test
    public void json() throws IOException {

        String expectedJson = "{\n"
                + "  \"workflowRunCount\" : null,\n"
                + "  \"studies\" : [ ],\n"
                + "  \"sequencerRuns\" : [ ],\n"
                + "  \"lanes\" : [ ],\n"
                + "  \"samples\" : [ ],\n"
                + "  \"workflows\" : [ ],\n"
                + "  \"processingAlgorithms\" : [ ],\n"
                + "  \"fileMetaTypes\" : [ ],\n"
                + "  \"maxInputFiles\" : -2147483648,\n"
                + "  \"minInputFiles\" : 2147483647,\n"
                + "  \"workflowRuns\" : [ {\n"
                + "    \"workflowIni\" : { },\n"
                + "    \"files\" : [ {\n"
                + "      \"studyTitle\" : [ \"TestStudy\" ],\n"
                + "      \"studyAttributes\" : {\n"
                + "        \"study.geo_lab_group_id\" : [ [ \"125\" ], [ \"139\" ], [ \"69\" ] ]\n"
                + "      },\n"
                + "      \"experimentName\" : [ \"Geo_TestStudy_ILLUMINA_EX_{F*101}{..}{R*101}\", \"Geo_TestStudy_ILLUMINA_WG_{F*101}{..}{R*101}\" ],\n"
                + "      \"experimentAttributes\" : { },\n"
                + "      \"sampleName\" : [ \"TES_0003_Ly_P_PE_415_EX\", \"TES_0014_Pa_C_PE_706_WG\", \"TES_149_Li_X_PE_380_EX\" ],\n"
                + "      \"sampleAttributes\" : {\n"
                + "        \"sample.geo_library_source_template_type\" : [ [ \"EX\" ], [ \"WG\" ] ],\n"
                + "        \"sample.geo_reaction_id\" : [ [ \"6877\", \"6883\", \"6913\", \"7071\" ], [ \"7938\", \"7939\", \"7940\", \"7941\" ], [ \"8328\" ] ],\n"
                + "        \"sample.geo_run_id_and_position_and_slot\" : [ [ \"1028_3_1\", \"1029_1_1\", \"1029_2_1\", \"1029_3_1\", \"1029_4_1\", \"1029_5_1\", \"1029_6_1\", \"1029_7_1\", \"1029_8_1\", \"1032_5_1\", \"1032_6_1\", \"1032_7_1\", \"1065_1_1\", \"1065_2_1\", \"1065_3_1\" ], [ \"1179_1_1\", \"1179_2_1\", \"1179_3_1\", \"1179_4_1\" ], [ \"1237_7_2\" ] ],\n"
                + "        \"sample.geo_targeted_resequencing\" : [ [ \"Agilent SureSelect ICGC/Sanger Exon\" ], [ \"Illumina TruSeq Exome\" ] ],\n"
                + "        \"sample.geo_template_id\" : [ [ \"20059\" ], [ \"24055\" ], [ \"25134\" ] ],\n"
                + "        \"sample.geo_template_type\" : [ [ \"Illumina PE Library Seq\" ] ],\n"
                + "        \"sample.geo_tissue_origin\" : [ [ \"Li\" ], [ \"Ly\" ], [ \"Pa\" ] ],\n"
                + "        \"sample.geo_tissue_type\" : [ [ \"C\" ], [ \"P\" ], [ \"X\" ] ]\n"
                + "      },\n"
                + "      \"sequencerRunName\" : [ \"120614_SN1068_0091_BC0W0AACXX\", \"121010_SN7001179_0091_AC0VWMACXX\", \"121122_SN801_0090_BC1FKGACXX\" ],\n"
                + "      \"sequencerRunAttributes\" : {\n"
                + "        \"sequencerrun.geo_instrument_run_id\" : [ [ \"1029\" ], [ \"1179\" ], [ \"1237\" ] ]\n"
                + "      },\n"
                + "      \"laneName\" : [ \"120614_SN1068_0091_BC0W0AACXX_lane_1\", \"121010_SN7001179_0091_AC0VWMACXX_lane_4\", \"121122_SN801_0090_BC1FKGACXX_lane_7\" ],\n"
                + "      \"laneAttributes\" : {\n"
                + "        \"lane.geo_lane\" : [ [ \"1\" ], [ \"4\" ], [ \"7\" ] ]\n"
                + "      },\n"
                + "      \"iusTag\" : [ \"GGCTAC\", \"NoIndex\", \"TGACCA\" ],\n"
                + "      \"iusAttributes\" : { },\n"
                + "      \"workflowName\" : [ \"FastQC\", \"GATKRecalibrationAndVariantCallingHg19Exomes\", \"GenomicAlignmentNovoalign\" ],\n"
                + "      \"processingAlgorithm\" : [ \"PicardAddReadGroups\", \"ProvisionFiles\", \"SummaryOfWorkflowOutputs\" ],\n"
                + "      \"processingAttributes\" : { },\n"
                + "      \"fileMetaType\" : [ \"application/bam\", \"application/zip-report-bundle\" ],\n"
                + "      \"fileAttributes\" : { },\n"
                + "      \"fileId\" : 936174784\n"
                + "    } ]\n"
                + "  } ]\n"
                + "}";

        List<FileProvenanceReportRecord> fs = FileProvenanceReport.parseFileProvenanceReport(Helpers.getFileFromResource("fileprovenance/valid.tsv"));

        WorkflowRunReport wrr = new WorkflowRunReport();
        wrr.setFiles(Arrays.asList(new ReducedFileProvenanceReportRecord(fs)));

        TestResult t = new TestResult();
        t.setWorkflowRuns(Arrays.asList(wrr));

        String actualJson = DeciderRunTest.testResultToJson(t);

        Assert.assertEquals(actualJson, expectedJson, "The actual json report does not equal the expected json report:");

    }

}
