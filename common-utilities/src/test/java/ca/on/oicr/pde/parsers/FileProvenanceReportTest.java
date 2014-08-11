package ca.on.oicr.pde.parsers;

import ca.on.oicr.pde.model.FileProvenanceReportRecord;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FileProvenanceReportTest {

    FileProvenanceReportRecord rec1;
    FileProvenanceReportRecord rec1Modified;
    FileProvenanceReportRecord rec2;
    FileProvenanceReportRecord rec3;

    List<FileProvenanceReportRecord> recs;

    public FileProvenanceReportTest() {
    }

    @BeforeClass
    public void initializeObjects() {

        FileProvenanceReportRecord.Builder rec1Builder = new FileProvenanceReportRecord.Builder();
        rec1Builder.setLastModified("2013-09-06 10:01:59.193073");
        rec1Builder.setStudyTitle("TestStudy");
        rec1Builder.setStudySwid("22717");
        rec1Builder.setStudyAttributes("study.geo_lab_group_id=125");
        rec1Builder.setExperimentName("Geo_TestStudy_ILLUMINA_WG_{F*101}{..}{R*101}");
        rec1Builder.setExperimentSwid("23792");
        rec1Builder.setExperimentAttributes("");
        rec1Builder.setParentSampleName("TES_0014:TES_0014_Pa_C_nn_2_D:TES_0014_Pa_C_nn_2_D_1:TES_0014_Pa_C_PE_706_WG:TES_0014_Pa_C_nn_2");
        rec1Builder.setParentSampleSwid("284923:284928:284929:284930:284927");
        rec1Builder.setParentSampleAttributes("parent_sample.geo_template_id.284927=23336;parent_sample.geo_template_type.284928=gDNA;parent_sample.geo_str_result.284929=Not Submitted;parent_sample.geo_external_name.284927=FN1385 Epcam+;parent_sample.geo_receive_date.284928=2012-08-22;parent_sample.geo_receive_date.284927=2012-08-22;parent_sample.geo_receive_date.284929=2012-08-22;parent_sample.geo_template_id.284928=23365;parent_sample.geo_template_type.284929=gDNA;parent_sample.geo_template_type.284927=Cell Line;parent_sample.geo_template_id.284930=24051;parent_sample.geo_str_result.284928=Not Submitted;parent_sample.geo_template_id.284923=23325;parent_sample.geo_external_name.284923=FN1385;parent_sample.geo_template_type.284930=Illumina PE Library;parent_sample.geo_template_type.284923=Identity;parent_sample.geo_purpose.284928=Research;parent_sample.geo_purpose.284929=Research;parent_sample.geo_template_id.284929=23376");
        rec1Builder.setSampleName("TES_0014_Pa_C_PE_706_WG");
        rec1Builder.setSampleSwid("284785");
        rec1Builder.setSampleAttributes("sample.geo_template_type=Illumina PE Library Seq;sample.geo_tissue_type=C;sample.geo_template_id=24055;sample.geo_library_source_template_type=WG;sample.geo_run_id_and_position_and_slot=1179_4_1&1179_2_1&1179_1_1&1179_3_1;sample.geo_reaction_id=7938&7940&7941&7939;sample.geo_tissue_origin=Pa");
        rec1Builder.setSequencerRunName("121010_SN7001179_0091_AC0VWMACXX");
        rec1Builder.setSequencerRunSwid("285189");
        rec1Builder.setSequencerRunAttributes("sequencerrun.geo_instrument_run_id=1179");
        rec1Builder.setLaneName("121010_SN7001179_0091_AC0VWMACXX_lane_4");
        rec1Builder.setLaneNumber("4");
        rec1Builder.setLaneSwid("285198");
        rec1Builder.setLaneAttributes("lane.geo_lane=4");
        rec1Builder.setIusTag("NoIndex");
        rec1Builder.setIusSwid("285199");
        rec1Builder.setIusAttributes("");
        rec1Builder.setWorkflowName("FastQC");
        rec1Builder.setWorkflowVersion("2.1");
        rec1Builder.setWorkflowSwid("599590");
        rec1Builder.setWorkflowRunName("");
        rec1Builder.setWorkflowRunStatus("completed");
        rec1Builder.setWorkflowRunSwid("600781");
        rec1Builder.setProcessingAlgorithm("ProvisionFiles");
        rec1Builder.setProcessingSwid("600925");
        rec1Builder.setProcessingAttributes("");
        rec1Builder.setFileMetaType("application/zip-report-bundle");
        rec1Builder.setFileSwid("600926");
        rec1Builder.setFileAttributes("");
        rec1Builder.setFilePath("/data/fastqc.zip");
        rec1Builder.setFileMd5sum("");
        rec1Builder.setFileSize("");
        rec1Builder.setFileDescription("provisionFile_out");
        rec1Builder.setSkip("false");
        rec1Builder.setRootSampleName("TES_0014");
        rec1Builder.setRootSampleSwid("9");
        rec1Builder.setParentSampleOrganismIds("31:31:31:31:31");
        rec1Builder.setSampleOrganismId("31");
        rec1Builder.setSampleOrganismCode("Homo_sapiens");
        rec1Builder.setSequencerRunPlatformId("20");
        rec1Builder.setSequencerRunPlatformName("ILLUMINA");
        rec1 = rec1Builder.build();
        rec1Modified = rec1Builder.setSkip("invalid").build();

        FileProvenanceReportRecord.Builder rec2Builder = new FileProvenanceReportRecord.Builder();
        rec2Builder.setLastModified("2013-02-04 01:46:10.061");
        rec2Builder.setStudyTitle("TestStudy");
        rec2Builder.setStudySwid("235066");
        rec2Builder.setStudyAttributes("study.geo_lab_group_id=139");
        rec2Builder.setExperimentName("Geo_TestStudy_ILLUMINA_EX_{F*101}{..}{R*101}");
        rec2Builder.setExperimentSwid("347885");
        rec2Builder.setExperimentAttributes("");
        rec2Builder.setParentSampleName("TES_0003_Ly_P_nn_1_D_S1:TES_0003:TES_0003_Ly_P_PE_415_EX:TES_0003_Ly_P_nn_1_D:TES_0003_Ly_P_nn_1_D_1");
        rec2Builder.setParentSampleSwid("350166:350152:350168:350164:350167");
        rec2Builder.setParentSampleAttributes("parent_sample.geo_template_type.350152=Identity;parent_sample.geo_purpose.350164=Stock;parent_sample.geo_purpose.350167=Library;parent_sample.geo_template_id.350152=24311;parent_sample.geo_purpose.350166=Library;parent_sample.geo_template_id.350164=24330;parent_sample.geo_template_type.350166=gDNA_wga;parent_sample.geo_str_result.350164=Not Submitted;parent_sample.geo_template_type.350168=Illumina PE Library;parent_sample.geo_template_id.350166=24352;parent_sample.geo_template_type.350167=gDNA_wga;parent_sample.geo_str_result.350166=Submitted;parent_sample.geo_template_id.350167=24374;parent_sample.geo_prep_kit.350168=GA_multiplex;parent_sample.geo_template_id.350168=25133;parent_sample.geo_external_name.350152=Patient 3;parent_sample.geo_template_type.350164=gDNA;parent_sample.geo_str_result.350167=Submitted");
        rec2Builder.setSampleName("TES_0003_Ly_P_PE_415_EX");
        rec2Builder.setSampleSwid("347895");
        rec2Builder.setSampleAttributes("sample.geo_targeted_resequencing=Agilent SureSelect ICGC/Sanger Exon;sample.geo_library_source_template_type=EX;sample.geo_tissue_type=P;sample.geo_tissue_origin=Ly;sample.geo_template_type=Illumina PE Library Seq;sample.geo_run_id_and_position_and_slot=1237_7_2;sample.geo_template_id=25134;sample.geo_reaction_id=8328");
        rec2Builder.setSequencerRunName("121122_SN801_0090_BC1FKGACXX");
        rec2Builder.setSequencerRunSwid("350302");
        rec2Builder.setSequencerRunAttributes("sequencerrun.geo_instrument_run_id=1237");
        rec2Builder.setLaneName("121122_SN801_0090_BC1FKGACXX_lane_7");
        rec2Builder.setLaneNumber("7");
        rec2Builder.setLaneSwid("350329");
        rec2Builder.setLaneAttributes("lane.geo_lane=7");
        rec2Builder.setIusTag("GGCTAC");
        rec2Builder.setIusSwid("350330");
        rec2Builder.setIusAttributes("");
        rec2Builder.setWorkflowName("GATKRecalibrationAndVariantCallingHg19Exomes");
        rec2Builder.setWorkflowVersion("1.3.16-6");
        rec2Builder.setWorkflowSwid("286371");
        rec2Builder.setWorkflowRunName("");
        rec2Builder.setWorkflowRunStatus("completed");
        rec2Builder.setWorkflowRunSwid("477004");
        rec2Builder.setProcessingAlgorithm("SummaryOfWorkflowOutputs");
        rec2Builder.setProcessingSwid("486605");
        rec2Builder.setProcessingAttributes("");
        rec2Builder.setFileMetaType("application/bam");
        rec2Builder.setFileSwid("486607");
        rec2Builder.setFileAttributes("");
        rec2Builder.setFilePath("/data/data.bam");
        rec2Builder.setFileMd5sum("");
        rec2Builder.setFileSize("");
        rec2Builder.setFileDescription("");
        rec2Builder.setSkip("false");
        rec2Builder.setRootSampleName("TES_0003");
        rec2Builder.setRootSampleSwid("99");
        rec2Builder.setParentSampleOrganismIds("31:31:31:31:31");
        rec2Builder.setSampleOrganismId("31");
        rec2Builder.setSampleOrganismCode("Homo_sapiens");
        rec2Builder.setSequencerRunPlatformId("20");
        rec2Builder.setSequencerRunPlatformName("ILLUMINA");
        rec2 = rec2Builder.build();

        FileProvenanceReportRecord.Builder rec3Builder = new FileProvenanceReportRecord.Builder();
        rec3Builder.setLastModified("2012-07-01 20:29:20.69934");
        rec3Builder.setStudyTitle("TestStudy");
        rec3Builder.setStudySwid("91");
        rec3Builder.setStudyAttributes("study.geo_lab_group_id=69");
        rec3Builder.setExperimentName("Geo_TestStudy_ILLUMINA_EX_{F*101}{..}{R*101}");
        rec3Builder.setExperimentSwid("856");
        rec3Builder.setExperimentAttributes("");
        rec3Builder.setParentSampleName("TES_149_Li_X_PE_380_EX:TES_149_Li_X_02_1:TES_149_Li_X_02_1_D:TES_149_Li_X_02_1_D_1:TES_149");
        rec3Builder.setParentSampleSwid("222570:222561:222562:222569:222540");
        rec3Builder.setParentSampleAttributes("parent_sample.geo_external_name.222561=TES149-G1;parent_sample.geo_template_id.222569=16901;parent_sample.geo_template_type.222570=Illumina PE Library;parent_sample.geo_template_type.222561=Xenograft Tissue;parent_sample.geo_receive_date.222561=2011-12-07;parent_sample.geo_purpose.222562=Stock;parent_sample.geo_template_type.222569=gDNA;parent_sample.geo_prep_kit.222570=GA_TruSeq_Library;parent_sample.geo_template_id.222540=11991;parent_sample.geo_template_type.222540=Identity;parent_sample.geo_str_result.222562=Pass;parent_sample.geo_str_result.222569=Pass;parent_sample.geo_template_id.222570=20052;parent_sample.geo_template_type.222562=gDNA;parent_sample.geo_template_id.222561=16811;parent_sample.geo_template_id.222562=16812;parent_sample.geo_purpose.222569=Library");
        rec3Builder.setSampleName("TES_149_Li_X_PE_380_EX");
        rec3Builder.setSampleSwid("221935");
        rec3Builder.setSampleAttributes("sample.geo_targeted_resequencing=Illumina TruSeq Exome;sample.geo_template_id=20059;sample.geo_template_type=Illumina PE Library Seq;sample.geo_tissue_type=X;sample.geo_tissue_origin=Li;sample.geo_run_id_and_position_and_slot=1029_8_1&1032_6_1&1029_4_1&1029_5_1&1065_3_1&1029_2_1&1029_6_1&1029_3_1&1029_7_1&1065_1_1&1032_7_1&1032_5_1&1065_2_1&1029_1_1&1028_3_1;sample.geo_reaction_id=7071&6913&6883&6877;sample.geo_library_source_template_type=EX");
        rec3Builder.setSequencerRunName("120614_SN1068_0091_BC0W0AACXX");
        rec3Builder.setSequencerRunSwid("222814");
        rec3Builder.setSequencerRunAttributes("sequencerrun.geo_instrument_run_id=1029");
        rec3Builder.setLaneName("120614_SN1068_0091_BC0W0AACXX_lane_1");
        rec3Builder.setLaneNumber("1");
        rec3Builder.setLaneSwid("222816");
        rec3Builder.setLaneAttributes("lane.geo_lane=1");
        rec3Builder.setIusTag("TGACCA");
        rec3Builder.setIusSwid("230186");
        rec3Builder.setIusAttributes("");
        rec3Builder.setWorkflowName("GenomicAlignmentNovoalign");
        rec3Builder.setWorkflowVersion("0.10.1");
        rec3Builder.setWorkflowSwid("23341");
        rec3Builder.setWorkflowRunName("");
        rec3Builder.setWorkflowRunStatus("completed");
        rec3Builder.setWorkflowRunSwid("231445");
        rec3Builder.setProcessingAlgorithm("PicardAddReadGroups");
        rec3Builder.setProcessingSwid("233286");
        rec3Builder.setProcessingAttributes("");
        rec3Builder.setFileMetaType("application/bam");
        rec3Builder.setFileSwid("233335");
        rec3Builder.setFileAttributes("");
        rec3Builder.setFilePath("/data/data.bam");
        rec3Builder.setFileMd5sum("");
        rec3Builder.setFileSize("");
        rec3Builder.setFileDescription("A longer description.");
        rec3Builder.setSkip("false");
        rec3Builder.setRootSampleName("TES_149");
        rec3Builder.setRootSampleSwid("999");
        rec3Builder.setParentSampleOrganismIds("31:31:31:31:31");
        rec3Builder.setSampleOrganismId("31");
        rec3Builder.setSampleOrganismCode("Homo_sapiens");
        rec3Builder.setSequencerRunPlatformId("20");
        rec3Builder.setSequencerRunPlatformName("ILLUMINA");
        rec3 = rec3Builder.build();

        String reportFile = "/fileprovenance/valid.tsv";

        recs = getReport(reportFile);

    }

    @Test
    public void validHeader() {

        String reportFile = "/fileprovenance/validHeader.tsv";

        List<FileProvenanceReportRecord> noRecs = getReport(reportFile);

        Assert.assertTrue(noRecs != null, "The report is null:");
        Assert.assertEquals(0, noRecs.size(), "There are an unexpected number of records:");

    }

    @Test
    public void validData() {

        Assert.assertTrue(recs != null, "The report is null:");
        Assert.assertEquals(3, recs.size(), "There are an unexpected number of records:");
        Assert.assertEquals(rec1, recs.get(0), "The imported record does not equal the expected record:");
        Assert.assertEquals(rec2, recs.get(1), "The imported record does not equal the expected record:");
        Assert.assertEquals(rec3, recs.get(2), "The imported record does not equal the expected record:");

    }

    @Test
    public void invalidData() {

        Assert.assertNotEquals(rec1Modified, rec1, "Two different objects have evaluated to being equal:");

    }

    @Test(expectedExceptions = FileProvenanceReport.ValidationException.class)
    public void missingColumn() {

        String reportFile = "/fileprovenance/missingColumn.tsv";

        getReport(reportFile);

    }

    @Test(expectedExceptions = FileProvenanceReport.ValidationException.class)
    public void newColumn() {

        String reportFile = "/fileprovenance/newColumn.tsv";

        getReport(reportFile);

    }

    @Test(expectedExceptions = FileProvenanceReport.ValidationException.class)
    public void swappedColumns() {

        String reportFile = "/fileprovenance/swappedColumns.tsv";

        getReport(reportFile);

    }

    @Test(expectedExceptions = AssertionError.class)
    public void missingResource() {

        String reportFile = "/fileprovenance/doesNotExist";

        getReport(reportFile);

    }

    @Test
    public void checkAttributes() {

        Assert.assertEquals(rec1.getSampleAttributes().get("sample.geo_run_id_and_position_and_slot"),
                Arrays.asList("1179_1_1", "1179_2_1", "1179_3_1", "1179_4_1"));

    }

    private List<FileProvenanceReportRecord> getReport(String resource) {

        List<FileProvenanceReportRecord> rs = new ArrayList<FileProvenanceReportRecord>();

        InputStream i = this.getClass().getResourceAsStream(resource);

        if (i == null) {
            fail("Not able to access the resource: " + resource);
        }

        try {
            rs = FileProvenanceReport.parseFileProvenanceReport(new InputStreamReader(i));
        } catch (IOException ioe) {
            fail("There was a problem while reading the report: " + ioe.getMessage());
        }

        return rs;

    }

}
