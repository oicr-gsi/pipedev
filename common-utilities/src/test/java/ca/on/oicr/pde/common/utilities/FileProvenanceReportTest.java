package ca.on.oicr.pde.common.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.testng.Assert;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FileProvenanceReportTest {

    FileProvenanceReport.FileProvenanceRecord rec1;
    FileProvenanceReport.FileProvenanceRecord rec2;
    FileProvenanceReport.FileProvenanceRecord rec3;
    
    List<FileProvenanceReport.FileProvenanceRecord> recs;

    public FileProvenanceReportTest() {
    }

    @BeforeClass
    public void initializeObjects() {

        rec1 = new FileProvenanceReport.FileProvenanceRecord();
        rec1.setLastModified("2013-09-06 10:01:59.193073");
        rec1.setStudyTitle("TestStudy");
        rec1.setStudySWID("22717");
        rec1.setStudyAttributes("study.geo_lab_group_id=125");
        rec1.setExperimentName("Geo_TestStudy_ILLUMINA_WG_{F*101}{..}{R*101}");
        rec1.setExperimentSWID("23792");
        rec1.setExperimentAttributes("");
        rec1.setParentSampleName("TES_0014:TES_0014_Pa_C_nn_2_D:TES_0014_Pa_C_nn_2_D_1:TES_0014_Pa_C_PE_706_WG:TES_0014_Pa_C_nn_2");
        rec1.setParentSampleSWID("284923:284928:284929:284930:284927");
        rec1.setParentSampleAttributes("parent_sample.geo_template_id.284927=23336;parent_sample.geo_template_type.284928=gDNA;parent_sample.geo_str_result.284929=Not Submitted;parent_sample.geo_external_name.284927=FN1385 Epcam+;parent_sample.geo_receive_date.284928=2012-08-22;parent_sample.geo_receive_date.284927=2012-08-22;parent_sample.geo_receive_date.284929=2012-08-22;parent_sample.geo_template_id.284928=23365;parent_sample.geo_template_type.284929=gDNA;parent_sample.geo_template_type.284927=Cell Line;parent_sample.geo_template_id.284930=24051;parent_sample.geo_str_result.284928=Not Submitted;parent_sample.geo_template_id.284923=23325;parent_sample.geo_external_name.284923=FN1385;parent_sample.geo_template_type.284930=Illumina PE Library;parent_sample.geo_template_type.284923=Identity;parent_sample.geo_purpose.284928=Research;parent_sample.geo_purpose.284929=Research;parent_sample.geo_template_id.284929=23376");
        rec1.setSampleName("TES_0014_Pa_C_PE_706_WG");
        rec1.setSampleSWID("284785");
        rec1.setSampleAttributes("sample.geo_template_type=Illumina PE Library Seq;sample.geo_tissue_type=C;sample.geo_template_id=24055;sample.geo_library_source_template_type=WG;sample.geo_run_id_and_position_and_slot=1179_4_1;1179_2_1;1179_1_1;1179_3_1;sample.geo_reaction_id=7938;7940;7941;7939;sample.geo_tissue_origin=Pa");
        rec1.setSequencerRunName("121010_SN7001179_0091_AC0VWMACXX");
        rec1.setSequencerRunSWID("285189");
        rec1.setSequencerRunAttributes("sequencerrun.geo_instrument_run_id=1179");
        rec1.setLaneName("121010_SN7001179_0091_AC0VWMACXX_lane_4");
        rec1.setLaneNumber("4");
        rec1.setLaneSWID("285198");
        rec1.setLaneAttributes("lane.geo_lane=4");
        rec1.setIusTag("NoIndex");
        rec1.setIusSWID("285199");
        rec1.setIusAttributes("");
        rec1.setWorkflowName("FastQC");
        rec1.setWorkflowVersion("2.1");
        rec1.setWorkflowSWID("599590");
        rec1.setWorkflowRunName("");
        rec1.setWorkflowRunStatus("completed");
        rec1.setWorkflowRunSWID("600781");
        rec1.setProcessingAlgorithm("ProvisionFiles");
        rec1.setProcessingSWID("600925");
        rec1.setProcessingAttributes("");
        rec1.setFileMetaType("application/zip-report-bundle");
        rec1.setFileSWID("600926");
        rec1.setFileAttributes("");
        rec1.setFilePath("/data/fastqc.zip");
        rec1.setFileMd5sum("");
        rec1.setFileSize("");
        rec1.setFileDescription("provisionFile_out");
        rec1.setSkip("false");

        rec2 = new FileProvenanceReport.FileProvenanceRecord();
        rec2.setLastModified("2013-02-04 01:46:10.061");
        rec2.setStudyTitle("TestStudy");
        rec2.setStudySWID("235066");
        rec2.setStudyAttributes("study.geo_lab_group_id=139");
        rec2.setExperimentName("Geo_TestStudy_ILLUMINA_EX_{F*101}{..}{R*101}");
        rec2.setExperimentSWID("347885");
        rec2.setExperimentAttributes("");
        rec2.setParentSampleName("TES_0003_Ly_P_nn_1_D_S1:TES_0003:TES_0003_Ly_P_PE_415_EX:TES_0003_Ly_P_nn_1_D:TES_0003_Ly_P_nn_1_D_1");
        rec2.setParentSampleSWID("350166:350152:350168:350164:350167");
        rec2.setParentSampleAttributes("parent_sample.geo_template_type.350152=Identity;parent_sample.geo_purpose.350164=Stock;parent_sample.geo_purpose.350167=Library;parent_sample.geo_template_id.350152=24311;parent_sample.geo_purpose.350166=Library;parent_sample.geo_template_id.350164=24330;parent_sample.geo_template_type.350166=gDNA_wga;parent_sample.geo_str_result.350164=Not Submitted;parent_sample.geo_template_type.350168=Illumina PE Library;parent_sample.geo_template_id.350166=24352;parent_sample.geo_template_type.350167=gDNA_wga;parent_sample.geo_str_result.350166=Submitted;parent_sample.geo_template_id.350167=24374;parent_sample.geo_prep_kit.350168=GA_multiplex;parent_sample.geo_template_id.350168=25133;parent_sample.geo_external_name.350152=Patient 3;parent_sample.geo_template_type.350164=gDNA;parent_sample.geo_str_result.350167=Submitted");
        rec2.setSampleName("TES_0003_Ly_P_PE_415_EX");
        rec2.setSampleSWID("347895");
        rec2.setSampleAttributes("sample.geo_targeted_resequencing=Agilent SureSelect ICGC/Sanger Exon;sample.geo_library_source_template_type=EX;sample.geo_tissue_type=P;sample.geo_tissue_origin=Ly;sample.geo_template_type=Illumina PE Library Seq;sample.geo_run_id_and_position_and_slot=1237_7_2;sample.geo_template_id=25134;sample.geo_reaction_id=8328");
        rec2.setSequencerRunName("121122_SN801_0090_BC1FKGACXX");
        rec2.setSequencerRunSWID("350302");
        rec2.setSequencerRunAttributes("sequencerrun.geo_instrument_run_id=1237");
        rec2.setLaneName("121122_SN801_0090_BC1FKGACXX_lane_7");
        rec2.setLaneNumber("7");
        rec2.setLaneSWID("350329");
        rec2.setLaneAttributes("lane.geo_lane=7");
        rec2.setIusTag("GGCTAC");
        rec2.setIusSWID("350330");
        rec2.setIusAttributes("");
        rec2.setWorkflowName("GATKRecalibrationAndVariantCallingHg19Exomes");
        rec2.setWorkflowVersion("1.3.16-6");
        rec2.setWorkflowSWID("286371");
        rec2.setWorkflowRunName("");
        rec2.setWorkflowRunStatus("completed");
        rec2.setWorkflowRunSWID("477004");
        rec2.setProcessingAlgorithm("SummaryOfWorkflowOutputs");
        rec2.setProcessingSWID("486605");
        rec2.setProcessingAttributes("");
        rec2.setFileMetaType("application/bam");
        rec2.setFileSWID("486607");
        rec2.setFileAttributes("");
        rec2.setFilePath("/data/data.bam");
        rec2.setFileMd5sum("");
        rec2.setFileSize("");
        rec2.setFileDescription("");
        rec2.setSkip("false");

        rec3 = new FileProvenanceReport.FileProvenanceRecord();
        rec3.setLastModified("2012-07-01 20:29:20.69934");
        rec3.setStudyTitle("TestStudy");
        rec3.setStudySWID("91");
        rec3.setStudyAttributes("study.geo_lab_group_id=69");
        rec3.setExperimentName("Geo_TestStudy_ILLUMINA_EX_{F*101}{..}{R*101}");
        rec3.setExperimentSWID("856");
        rec3.setExperimentAttributes("");
        rec3.setParentSampleName("TES_149_Li_X_PE_380_EX:TES_149_Li_X_02_1:TES_149_Li_X_02_1_D:TES_149_Li_X_02_1_D_1:TES_149");
        rec3.setParentSampleSWID("222570:222561:222562:222569:222540");
        rec3.setParentSampleAttributes("parent_sample.geo_external_name.222561=TES149-G1;parent_sample.geo_template_id.222569=16901;parent_sample.geo_template_type.222570=Illumina PE Library;parent_sample.geo_template_type.222561=Xenograft Tissue;parent_sample.geo_receive_date.222561=2011-12-07;parent_sample.geo_purpose.222562=Stock;parent_sample.geo_template_type.222569=gDNA;parent_sample.geo_prep_kit.222570=GA_TruSeq_Library;parent_sample.geo_template_id.222540=11991;parent_sample.geo_template_type.222540=Identity;parent_sample.geo_str_result.222562=Pass;parent_sample.geo_str_result.222569=Pass;parent_sample.geo_template_id.222570=20052;parent_sample.geo_template_type.222562=gDNA;parent_sample.geo_template_id.222561=16811;parent_sample.geo_template_id.222562=16812;parent_sample.geo_purpose.222569=Library");
        rec3.setSampleName("TES_149_Li_X_PE_380_EX");
        rec3.setSampleSWID("221935");
        rec3.setSampleAttributes("sample.geo_targeted_resequencing=Illumina TruSeq Exome;sample.geo_template_id=20059;sample.geo_template_type=Illumina PE Library Seq;sample.geo_tissue_type=X;sample.geo_tissue_origin=Li;sample.geo_run_id_and_position_and_slot=1029_8_1;1032_6_1;1029_4_1;1029_5_1;1065_3_1;1029_2_1;1029_6_1;1029_3_1;1029_7_1;1065_1_1;1032_7_1;1032_5_1;1065_2_1;1029_1_1;1028_3_1;sample.geo_reaction_id=7071;6913;6883;6877;sample.geo_library_source_template_type=EX");
        rec3.setSequencerRunName("120614_SN1068_0091_BC0W0AACXX");
        rec3.setSequencerRunSWID("222814");
        rec3.setSequencerRunAttributes("sequencerrun.geo_instrument_run_id=1029");
        rec3.setLaneName("120614_SN1068_0091_BC0W0AACXX_lane_1");
        rec3.setLaneNumber("1");
        rec3.setLaneSWID("222816");
        rec3.setLaneAttributes("lane.geo_lane=1");
        rec3.setIusTag("TGACCA");
        rec3.setIusSWID("230186");
        rec3.setIusAttributes("");
        rec3.setWorkflowName("GenomicAlignmentNovoalign");
        rec3.setWorkflowVersion("0.10.1");
        rec3.setWorkflowSWID("23341");
        rec3.setWorkflowRunName("");
        rec3.setWorkflowRunStatus("completed");
        rec3.setWorkflowRunSWID("231445");
        rec3.setProcessingAlgorithm("PicardAddReadGroups");
        rec3.setProcessingSWID("233286");
        rec3.setProcessingAttributes("");
        rec3.setFileMetaType("application/bam");
        rec3.setFileSWID("233335");
        rec3.setFileAttributes("");
        rec3.setFilePath("/data/data.bam");
        rec3.setFileMd5sum("");
        rec3.setFileSize("");
        rec3.setFileDescription("A longer description.");
        rec3.setSkip("false");
        
        String reportFile = "/fileprovenance/valid.tsv";

        recs = getReport(reportFile);
        
    }

    @Test
    public void validHeader() {

        String reportFile = "/fileprovenance/validHeader.tsv";

        List<FileProvenanceReport.FileProvenanceRecord> noRecs = getReport(reportFile);

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
        
        FileProvenanceReport.FileProvenanceRecord invalidObject = SerializationUtils.clone(rec1);

        invalidObject.setSkip("test equality failure");

        Assert.assertNotEquals(invalidObject, rec1, "Two different objects have evaluated to being equal:");

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

    private List<FileProvenanceReport.FileProvenanceRecord> getReport(String resource) {

        List<FileProvenanceReport.FileProvenanceRecord> recs = new ArrayList<FileProvenanceReport.FileProvenanceRecord>();

        InputStream i = this.getClass().getResourceAsStream(resource);

        if (i == null) {
            fail("Not able to access the resource: " + resource);
        }

        try {
            recs = FileProvenanceReport.parseFileProvenanceReport(new InputStreamReader(i));
        } catch (IOException ioe) {
            fail("There was a problem while reading the report: " + ioe.getMessage());
        }

        return recs;

    }

}
