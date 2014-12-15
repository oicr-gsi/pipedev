package ca.on.oicr.pde.testing.metadata;

import ca.on.oicr.pde.dao.writer.SeqwareWriteService;
import ca.on.oicr.pde.dao.reader.SeqwareReadService;
import ca.on.oicr.pde.model.Experiment;
import ca.on.oicr.pde.model.Ius;
import ca.on.oicr.pde.model.Lane;
import ca.on.oicr.pde.model.Sample;
import ca.on.oicr.pde.model.SequencerRun;
import ca.on.oicr.pde.model.SeqwareObject;
import ca.on.oicr.pde.model.Study;
import ca.on.oicr.pde.dao.executor.SeqwareExecutor;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mlaszloffy
 */
public class RegressionTestStudy {

    private final TestEnvironment te;
    private final Map<String, SeqwareObject> objects;

    public RegressionTestStudy(String host, String port, String user, String password, File seqwareWar) {

        te = new TestEnvironment(host, port, user, password, seqwareWar);
        SeqwareWriteService writeService = te.getWriteService();

        objects = new HashMap<>();

        Study study = writeService.createStudy("111", "OICR", "PDE_TEST", null, "11", "PDE_TEST");
        Experiment experiment = writeService.createExperiment(null, "20", study, "PDE_ILLUMINA");

        objects.put("TEST_SEQUENCER_RUN_001", writeService.createSequencerRun(null, "/tmp/data1/", "TEST_SEQUENCER_RUN_001", true, "20", false));
        objects.put("TEST_SEQUENCER_RUN_002", writeService.createSequencerRun(null, "/tmp/data2/", "TEST_SEQUENCER_RUN_002", true, "20", false));
        objects.put("TEST_SEQUENCER_RUN_003", writeService.createSequencerRun(null, "/tmp/data3/", "TEST_SEQUENCER_RUN_003", true, "20", false));

        objects.put("TEST_0001", writeService.createSample(" ", experiment, "34", "TEST_0001", null));
        objects.put("TEST_0001_Ly_R", writeService.createSample(null, experiment, "34", "TEST_0001_Ly_R", (Sample) objects.get("TEST_0001")));
        objects.put("TEST_0001_Pa_P", writeService.createSample(null, experiment, "34", "TEST_0001_Pa_P", (Sample) objects.get("TEST_0001")));
        objects.put("TEST_0001_Pa_X", writeService.createSample(null, experiment, "34", "TEST_0001_Pa_X", (Sample) objects.get("TEST_0001")));
        objects.put("TEST_0001_Pa_C", writeService.createSample(null, experiment, "34", "TEST_0001_Pa_C", (Sample) objects.get("TEST_0001")));

        objects.put("TEST_0002", writeService.createSample(null, experiment, "34", "TEST_0002", null));
        objects.put("TEST_0002_Pa_P", writeService.createSample(null, experiment, "34", "TEST_0002_Pa_P", (Sample) objects.get("TEST_0002")));
        objects.put("TEST_0002_Ly_R", writeService.createSample(null, experiment, "34", "TEST_0002_Ly_R", (Sample) objects.get("TEST_0002")));

        objects.put("TEST_0003", writeService.createSample(null, experiment, "34", "TEST_0003", null));
        objects.put("TEST_0003_Pa_X", writeService.createSample(null, experiment, "34", "TEST_0003_Pa_X", (Sample) objects.get("TEST_0003")));
        objects.put("TEST_0003_Ly_R", writeService.createSample(null, experiment, "34", "TEST_0003_Ly_R", (Sample) objects.get("TEST_0003")));

        objects.put("TEST_0004", writeService.createSample(null, experiment, "34", "TEST_0004", null));
        objects.put("TEST_0004_Pa_C", writeService.createSample(null, experiment, "34", "TEST_0004_Pa_C", (Sample) objects.get("TEST_0004")));

        objects.put("TEST_0005", writeService.createSample(null, experiment, "34", "TEST_0005", null));
        objects.put("TEST_0005_Pa_X", writeService.createSample(null, experiment, "34", "TEST_0005_Pa_X", (Sample) objects.get("TEST_0005")));

        Lane lane;
        Sample sample;
        Ius ius;

        //TEST_SEQUENCER_RUN_001
        //lane 1
        lane = writeService.createLane(null, null, "1", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_001"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_001_1", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0001_Ly_R_PE_500_WG", (Sample) objects.get("TEST_0001_Ly_R"));
        objects.put("TEST_0001_Ly_R_PE_500_WG", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Ly.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.R.toString());
        writeService.annotate(sample, "geo_library_size_code", "500");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = writeService.createIus("Noindex", null, lane, null, sample, false);
        objects.put("IUS1", ius);

        //lane 2
        lane = writeService.createLane(null, null, "2", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_001"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_001_2", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0001_Ly_R_PE_600_WG", (Sample) objects.get("TEST_0001_Ly_R"));
        objects.put("TEST_0001_Ly_R_PE_600_WG", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Ly.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.R.toString());
        writeService.annotate(sample, "geo_library_size_code", "600");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = writeService.createIus("Noindex", null, lane, null, sample, false);
        objects.put("IUS2", ius);

        //lane 3
        lane = writeService.createLane(null, null, "3", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_001"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_001_3", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0001_Pa_P_PE_400_WG", (Sample) objects.get("TEST_0001_Pa_P"));
        objects.put("TEST_0001_Pa_P_PE_400_WG", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        writeService.annotate(sample, "geo_library_size_code", "400");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = writeService.createIus("Noindex", null, lane, null, sample, false);
        objects.put("IUS3", ius);

        //lane 4
        lane = writeService.createLane(null, null, "4", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_001"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_001_4", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0001_Pa_P_PE_300_WG", (Sample) objects.get("TEST_0001_Pa_P"));
        objects.put("TEST_0001_Pa_P_PE_300_WG", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        writeService.annotate(sample, "geo_library_size_code", "300");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = writeService.createIus("Noindex", null, lane, null, sample, false);
        objects.put("IUS4", ius);

        //lane 5
        lane = writeService.createLane(null, null, "5", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_001"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_001_5", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0001_Pa_P_PE_396_MR", (Sample) objects.get("TEST_0001_Pa_P"));
        objects.put("TEST_0001_Pa_P_PE_396_MR", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        writeService.annotate(sample, "geo_library_size_code", "396");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.MR.toString());
        ius = writeService.createIus("Noindex", null, lane, null, sample, false);
        objects.put("IUS5", ius);

        //lane 6
        lane = writeService.createLane(null, null, "6", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_001"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_001_6", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0001_Pa_P_PE_296_WT", (Sample) objects.get("TEST_0001_Pa_P"));
        objects.put("TEST_0001_Pa_P_PE_296_WT", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        writeService.annotate(sample, "geo_library_size_code", "296");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.WT.toString());
        ius = writeService.createIus("Noindex", null, lane, null, sample, false);
        objects.put("IUS6", ius);

        //lane 7
        lane = writeService.createLane(null, null, "7", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_001"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_001_7", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0001_Ly_R_PE_250_EX", (Sample) objects.get("TEST_0001_Ly_R"));
        objects.put("TEST_0001_Ly_R_PE_250_EX", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Ly.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.R.toString());
        writeService.annotate(sample, "geo_library_size_code", "250");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        ius = writeService.createIus("Noindex", null, lane, null, sample, false);
        objects.put("IUS7", ius);

        //lane 8
        lane = writeService.createLane(null, null, "8", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_001"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_001_8", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0001_Ly_R_PE_250_EX", (Sample) objects.get("TEST_0001_Ly_R"));
        objects.put("TEST_0001_Ly_R_PE_250_EX", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        writeService.annotate(sample, "geo_library_size_code", "350");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        ius = writeService.createIus("Noindex", null, lane, null, sample, false);
        objects.put("IUS8", ius);

        //TEST_SEQUENCER_RUN_002
        //lane 1
        lane = writeService.createLane(null, null, "1", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_002"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_002_1", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0001_Ly_R_PE_500_WG", (Sample) objects.get("TEST_0001_Ly_R"));
        objects.put("TEST_0001_Ly_R_PE_500_WG", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Ly.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.R.toString());
        writeService.annotate(sample, "geo_library_size_code", "500");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = writeService.createIus("Noindex", null, lane, null, sample, false);
        objects.put("IUS9", ius);

        //lane 2
        lane = writeService.createLane(null, null, "2", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_002"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_002_2", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0001_Pa_P_PE_300_WG", (Sample) objects.get("TEST_0001_Pa_P"));
        objects.put("TEST_0001_Pa_P_PE_300_WG", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        writeService.annotate(sample, "geo_library_size_code", "300");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = writeService.createIus("Noindex", null, lane, null, sample, false);
        objects.put("IUS10", ius);

        //lane 3
        lane = writeService.createLane(null, null, "3", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_002"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_002_3", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0002_Pa_P_PE_400_EX", (Sample) objects.get("TEST_0002_Pa_P"));
        objects.put("TEST_0002_Pa_P_PE_400_EX", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        writeService.annotate(sample, "geo_library_size_code", "400");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        ius = writeService.createIus("ACAGTG", null, lane, null, sample, false);
        objects.put("IUS11", ius);

        sample = writeService.createSample(null, experiment, "34", "TEST_0003_Pa_X_PE_401_EX", (Sample) objects.get("TEST_0003_Pa_X"));
        objects.put("TEST_0003_Pa_X_PE_401_EX", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.X.toString());
        writeService.annotate(sample, "geo_library_size_code", "401");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        ius = writeService.createIus("TTAGGC", null, lane, null, sample, false);
        objects.put("IUS12", ius);

        sample = writeService.createSample(null, experiment, "34", "TEST_0004_Pa_C_PE_501_EX", (Sample) objects.get("TEST_0004_Pa_C"));
        objects.put("TEST_0004_Pa_C_PE_501_EX", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.C.toString());
        writeService.annotate(sample, "geo_library_size_code", "501");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        ius = writeService.createIus("CGATGT", null, lane, null, sample, false);
        objects.put("IUS13", ius);

        sample = writeService.createSample(null, experiment, "34", "TEST_0005_Pa_X_PE_150_WG", (Sample) objects.get("TEST_0005_Pa_X"));
        objects.put("TEST_0005_Pa_X_PE_150_WG", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.X.toString());
        writeService.annotate(sample, "geo_library_size_code", "150");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = writeService.createIus("ATCACG", null, lane, null, sample, false);
        objects.put("IUS14", ius);

        //lane 4
        lane = writeService.createLane(null, null, "4", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_002"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_002_4", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0002_Pa_P_PE_400_EX", (Sample) objects.get("TEST_0002_Pa_P"));
        objects.put("TEST_0002_Pa_P_PE_400_EX", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        writeService.annotate(sample, "geo_library_size_code", "400");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        ius = writeService.createIus("TGACCA", null, lane, null, sample, false);
        objects.put("IUS15", ius);

        sample = writeService.createSample(null, experiment, "34", "TEST_0003_Pa_X_PE_401_EX", (Sample) objects.get("TEST_0003_Pa_X"));
        objects.put("TEST_0003_Pa_X_PE_401_EX", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.X.toString());
        writeService.annotate(sample, "geo_library_size_code", "401");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        writeService.createIus("ACAGTG", null, lane, null, sample, false);
        objects.put("IUS16", ius);

        //lane 5
        lane = writeService.createLane(null, null, "5", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_002"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_002_5", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0001_Pa_X_PE_200_WG", (Sample) objects.get("TEST_0001_Pa_X"));
        objects.put("TEST_0001_Pa_X_PE_200_WG", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.X.toString());
        writeService.annotate(sample, "geo_library_size_code", "200");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = writeService.createIus("Noindex", null, lane, null, sample, false);
        objects.put("IUS17", ius);

        //lane 6
        lane = writeService.createLane(null, null, "6", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_002"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_002_6", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0001_Pa_C_PE_700_WG", (Sample) objects.get("TEST_0001_Pa_C"));
        objects.put("TEST_0001_Pa_C_PE_700_WG", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.C.toString());
        writeService.annotate(sample, "geo_library_size_code", "700");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = writeService.createIus("Noindex", null, lane, null, sample, false);
        objects.put("IUS18", ius);

        //lane 7
        lane = writeService.createLane(null, null, "7", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_002"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_002_7", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0002_Ly_R_PE_500_EX", (Sample) objects.get("TEST_0002_Ly_R"));
        objects.put("TEST_0002_Ly_R_PE_500_EX", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Ly.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.R.toString());
        writeService.annotate(sample, "geo_library_size_code", "500");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        ius = writeService.createIus("Noindex", null, lane, null, sample, false);
        objects.put("IUS19", ius);

        //lane 8
        lane = writeService.createLane(null, null, "8", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_002"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_002_8", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0003_Ly_R_PE_402_EX", (Sample) objects.get("TEST_0003_Ly_R"));
        objects.put("TEST_0003_Ly_R_PE_402_EX", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Ly.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.R.toString());
        writeService.annotate(sample, "geo_library_size_code", "402");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        ius = writeService.createIus("Noindex", null, lane, null, sample, false);
        objects.put("IUS20", ius);

        //TEST_SEQUENCER_RUN_003
        //lane 1
        lane = writeService.createLane(null, null, "1", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_003"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_003_1", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0001_Ly_R_PE_500_WG", (Sample) objects.get("TEST_0001_Ly_R"));
        objects.put("TEST_0001_Ly_R_PE_500_WG", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Ly.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.R.toString());
        writeService.annotate(sample, "geo_library_size_code", "500");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = writeService.createIus("Noindex", null, lane, null, sample, false);
        objects.put("IUS21", ius);

        //lane 2
        lane = writeService.createLane(null, null, "2", LibraryType.PE.getId(), "5", "14", null, (SequencerRun) objects.get("TEST_SEQUENCER_RUN_003"), false, "11");
        objects.put("TEST_SEQUENCER_RUN_003_2", lane);

        sample = writeService.createSample(null, experiment, "34", "TEST_0001_Pa_P_PE_400_WG", (Sample) objects.get("TEST_0001_Pa_P"));
        objects.put("TEST_0001_Pa_P_PE_400_WG", sample);
        writeService.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        writeService.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        writeService.annotate(sample, "geo_library_size_code", "400");
        writeService.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = writeService.createIus("Noindex", null, lane, null, sample, false);
        objects.put("IUS22", ius);

        writeService.annotate(objects.get("TEST_SEQUENCER_RUN_002_4"), true, "lane skip test");
        writeService.annotate(objects.get("TEST_SEQUENCER_RUN_003"), true, "sequencer run skip test");
        writeService.annotate(objects.get("IUS13"), true, "ius skip test");
        writeService.annotate(objects.get("IUS18"), true, "ius skip test");

    }

    public Map<String, SeqwareObject> getSeqwareObjects() {
        return Collections.unmodifiableMap(objects);
    }

    public SeqwareWriteService getSeqwareWriteService() {
        return te.getWriteService();
    }

    public SeqwareReadService getSeqwareReadService() {
        return te.getReadService();
    }

    public SeqwareExecutor getSeqwareExecutor() {
        return te.getSeqwareExecutor();
    }

    public Map<String, String> getSeqwareConfig() {
        return te.getSeqwareConfig();
    }

    public File getSeqwareSettings() {
        return te.getSeqwareSettings();
    }

    public void shutdown() {
        te.shutdown();
    }

    public enum LibraryType {

        PE(2);
        private final int id;

        private LibraryType(int id) {
            this.id = id;
        }

        public String getId() {
            //return id;
            return Integer.toString(id);
        }

    }

    public enum TemplateType {

        WG(7), EX(2), MR(3), WT(8);
        private final int id;

        private TemplateType(int id) {
            this.id = id;
        }

        public String getId() {
            //return id;
            return Integer.toString(id);
        }

    }

    public enum TissueOrigin {

        Ly(9), Pa(14);
        private final int id;

        private TissueOrigin(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

    }

    public enum TissueType {

        R(5), P(4), X(6), C(1);
        private final int id;

        private TissueType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

    }

}
