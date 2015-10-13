package ca.on.oicr.pde.testing.decider;

import ca.on.oicr.pde.diff.ObjectDiff;
import ca.on.oicr.pde.model.FileProvenanceReportRecord;
import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import ca.on.oicr.pde.parsers.FileProvenanceReport;
import ca.on.oicr.pde.utilities.Helpers;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RunTestReportComparison {

    @Test
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
        RunTestReport actual = RunTestReport.buildFromJson(actualJson);
        RunTestReport expected = RunTestReport.buildFromJson(expectedJson);

        Assert.assertTrue(actual.equals(expected), ObjectDiff.diff(actual, expected).toString());

    }

    @Test
    public void compareUnordered() throws IOException {

        RunTestReport expected = new RunTestReport();
        expected.setWorkflowRunCount(1);
        expected.addStudies(Arrays.asList("TestStudy"));
        expected.addSequencerRuns(Arrays.asList("TestSequencerRun1"));
        expected.addLanes(Arrays.asList("TestSequencerRun1_Lane1"));
        expected.addSamples(Arrays.asList("TES_0001"));
        expected.addWorkflows(Arrays.asList("TestWorkflow"));
        expected.addProcessingAlgorithms(Arrays.asList("TestProcessingAlgorithm"));
        expected.addFileMetaTypes(Arrays.asList("application/bam"));
        expected.setMaxInputFiles(10);
        expected.setMinInputFiles(1);

        List<ReducedFileProvenanceReportRecord> fs = new LinkedList<>();

        //Simulate files that are part of decider run test workflow run
        for (int i = 0; i < 10; i++) {
            ReducedFileProvenanceReportRecord r = new ReducedFileProvenanceReportRecord(new FileProvenanceReportRecord.Builder(0)
                    .setExperimentName("TestExperiment")
                    .setFileMetaType("application/bam")
                    .setFilePath(String.format("/tmp/file%s.bam", i))
                    .setLaneName("lane1")
                    .setLaneNumber("1")
                    .setParentSampleName("TEST_0001")
                    .setProcessingAlgorithm("produce_bam")
                    .setSampleName("TEST_0001_P")
                    .setSequencerRunName("Test_Sequencer_Run")
                    .setStudyTitle("TEST_STUDY")
                    .setWorkflowName("Test_Workflow")
                    .build());
            fs.add(r);
        }
        WorkflowRunReport expectedWR = new WorkflowRunReport();
        expectedWR.setWorkflowIni(ImmutableMap.<String, String>builder().put("prop1", "val1").build());
        expectedWR.setFiles(fs);

        expected.addWorkflowRun(expectedWR);

        //Test to json and conversion back
        RunTestReport actual = RunTestReport.buildFromJson(expected.toJson());

        //Shuffle workflow run file list order
        for (WorkflowRunReport w : actual.getWorkflowRuns()) {
            List<ReducedFileProvenanceReportRecord> rs = new ArrayList<>(w.getFiles());
            Collections.shuffle(rs);
            w.getFiles().clear();
            w.setFiles(rs);
        }

        Assert.assertTrue(actual.equals(expected), "Differences = " + ObjectDiff.diff(actual, expected).toString());

        Assert.assertEquals(actual.toJson(), expected.toJson());

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

        RunTestReport actualObject = RunTestReport.buildFromJson(actual);
        RunTestReport expectedObject = RunTestReport.buildFromJson(expected);

        Assert.assertEquals(2, actualObject.getWorkflowRuns().size());
        Assert.assertEquals(2, expectedObject.getWorkflowRuns().size());

        List<ReducedFileProvenanceReportRecord> files = actualObject.getWorkflowRuns().get(0).getFiles();
        for (ReducedFileProvenanceReportRecord r : files) {
            Collection actualAttrSet = r.getStudyAttributes().get("attr1");
            Collection expectedAttrSet = new ArrayList();
            expectedAttrSet.add(new TreeSet(Arrays.asList("1", "2", "3", "4")));
            Assert.assertEquals(actualAttrSet, expectedAttrSet);
        }

        Assert.assertTrue(actualObject.equals(expectedObject), "expected:\n" + expectedObject.toString());

    }

    @Test
    public void jsonConversion() throws IOException {

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
                + "  \"totalInputFiles\" : 0,\n"
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

        RunTestReport t = new RunTestReport();
        t.setWorkflowRuns(Arrays.asList(wrr));

        String actualJson = t.toJson();

        Assert.assertEquals(actualJson, expectedJson, "The actual json report does not equal the expected json report:");

    }

    @Test
    public void differencesTest() throws IOException {
        RunTestReport expected = new RunTestReport();
        expected.setWorkflowRunCount(10);
        expected.setMaxInputFiles(2);
        expected.setMinInputFiles(1);
        expected.addSamples(Arrays.asList("Sample2", "Sample3", "Sample1"));

        RunTestReport actual = new RunTestReport();
        actual.setWorkflowRunCount(10);
        actual.setMaxInputFiles(3);
        actual.setMinInputFiles(2);
        actual.addSamples(Arrays.asList("Sample1", "Sample2", "Sample3", "Sample4"));

        Assert.assertNotEquals(actual, expected);

        Map differences = ObjectDiff.diff(actual, expected);
        System.out.println(differences.toString());

        //There should be 3 changes
        Assert.assertEquals(differences.size(), 3);

        //The json doc should be different too
        Assert.assertNotEquals(actual.toJson(), expected.toJson());

        //Change summary tests
        String summary = ObjectDiff.diffReportSummary(actual, expected, 2);
        System.out.println(summary);
        Assert.assertEquals(summary, "There are 3 changes:\n"
                + "'/maxInputFiles' has changed from [ 2 ] to [ 3 ]\n"
                + "'/minInputFiles' has changed from [ 1 ] to [ 2 ]\n"
                + "... 1 more");
    }

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
