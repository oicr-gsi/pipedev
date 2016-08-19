package ca.on.oicr.pde.testing.metadata;

import ca.on.oicr.pde.model.SeqwareObject;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.HashMap;
import net.sourceforge.seqware.common.model.Annotatable;
import net.sourceforge.seqware.common.model.Experiment;
import net.sourceforge.seqware.common.model.FirstTierModel;
import net.sourceforge.seqware.common.model.Lane;
import net.sourceforge.seqware.common.model.Sample;
import net.sourceforge.seqware.common.model.SequencerRun;
import net.sourceforge.seqware.common.model.Study;
import net.sourceforge.seqware.common.model.IUS;
import ca.on.oicr.pde.client.SeqwareLimsClient;

/**
 *
 * @author mlaszloffy
 */
public class RegressionTestStudy {

//    private final Map<String, SeqwareObject> objects;
//    public RegressionTestStudy(String host, String port, String user, String password, File seqwareWar) {
//        this(new TestEnvironment(host, port, user, password, seqwareWar).getWriteService());
//    }
    SeqwareObjects s;

    public class SeqwareObjects {

        private final Table<String, Class<?>, Object> data = HashBasedTable.create();
        private final HashMap<String, SeqwareObject> data2 = new HashMap<>();

        public <T extends FirstTierModel & Annotatable> void add(String key, T t) {
            data.put(key, t.getClass(), t);
            data2.put(key, new SeqwareObject(t));
        }

        public Sample getSample(String key) {
            return (Sample) data.get(key, Sample.class);
        }

        public SequencerRun getSequencerRun(String key) {
            return (SequencerRun) data.get(key, SequencerRun.class);
        }

        public Lane getLane(String key) {
            return (Lane) data.get(key, Lane.class);
        }

        public Study getStudy(String key) {
            return (Study) data.get(key, Study.class);
        }

        public Experiment getExperiment(String key) {
            return (Experiment) data.get(key, Experiment.class);
        }

        public IUS getIus(String key) {
            return (IUS) data.get(key, IUS.class);
        }

        public SeqwareObject get(String key) {
            return data2.get(key);
        }

    }

    public RegressionTestStudy(SeqwareLimsClient lims) {

        s = new SeqwareObjects();

        Study study = lims.createStudy("111", "OICR", "PDE_TEST", null, "11", "PDE_TEST");
        Experiment experiment = lims.createExperiment(null, "20", study, "PDE_ILLUMINA");

        s.add("TEST_SEQUENCER_RUN_001", lims.createSequencerRun(null, "/tmp/data1/", "TEST_SEQUENCER_RUN_001", true, "20", false));
        s.add("TEST_SEQUENCER_RUN_002", lims.createSequencerRun(null, "/tmp/data2/", "TEST_SEQUENCER_RUN_002", true, "20", false));
        s.add("TEST_SEQUENCER_RUN_003", lims.createSequencerRun(null, "/tmp/data3/", "TEST_SEQUENCER_RUN_003", true, "20", false));

        s.add("TEST_0001", lims.createSample(" ", experiment, "34", "TEST_0001", null));
        s.add("TEST_0001_Ly_R", lims.createSample(null, experiment, "34", "TEST_0001_Ly_R", s.getSample("TEST_0001")));
        s.add("TEST_0001_Pa_P", lims.createSample(null, experiment, "34", "TEST_0001_Pa_P", s.getSample("TEST_0001")));
        s.add("TEST_0001_Pa_X", lims.createSample(null, experiment, "34", "TEST_0001_Pa_X", s.getSample("TEST_0001")));
        s.add("TEST_0001_Pa_C", lims.createSample(null, experiment, "34", "TEST_0001_Pa_C", s.getSample("TEST_0001")));

        s.add("TEST_0002", lims.createSample(null, experiment, "34", "TEST_0002", null));
        s.add("TEST_0002_Pa_P", lims.createSample(null, experiment, "34", "TEST_0002_Pa_P", s.getSample("TEST_0002")));
        s.add("TEST_0002_Ly_R", lims.createSample(null, experiment, "34", "TEST_0002_Ly_R", s.getSample("TEST_0002")));

        s.add("TEST_0003", lims.createSample(null, experiment, "34", "TEST_0003", null));
        s.add("TEST_0003_Pa_X", lims.createSample(null, experiment, "34", "TEST_0003_Pa_X", s.getSample("TEST_0003")));
        s.add("TEST_0003_Ly_R", lims.createSample(null, experiment, "34", "TEST_0003_Ly_R", s.getSample("TEST_0003")));

        s.add("TEST_0004", lims.createSample(null, experiment, "34", "TEST_0004", null));
        s.add("TEST_0004_Pa_C", lims.createSample(null, experiment, "34", "TEST_0004_Pa_C", s.getSample("TEST_0004")));

        s.add("TEST_0005", lims.createSample(null, experiment, "34", "TEST_0005", null));
        s.add("TEST_0005_Pa_X", lims.createSample(null, experiment, "34", "TEST_0005_Pa_X", s.getSample("TEST_0005")));

        Lane lane;
        Sample sample;
        IUS ius;

        //TEST_SEQUENCER_RUN_001
        //lane 1
        lane = lims.createLane(null, null, "1", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_001"), false, "11");
        s.add("TEST_SEQUENCER_RUN_001_1", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0001_Ly_R_PE_500_WG", s.getSample("TEST_0001_Ly_R"));
        s.add("TEST_0001_Ly_R_PE_500_WG", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Ly.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.R.toString());
        lims.annotate(sample, "geo_library_size_code", "500");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = lims.createIus("Noindex", null, lane, null, sample, false);
        s.add("IUS1", ius);

        //lane 2
        lane = lims.createLane(null, null, "2", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_001"), false, "11");
        s.add("TEST_SEQUENCER_RUN_001_2", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0001_Ly_R_PE_600_WG", s.getSample("TEST_0001_Ly_R"));
        s.add("TEST_0001_Ly_R_PE_600_WG", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Ly.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.R.toString());
        lims.annotate(sample, "geo_library_size_code", "600");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = lims.createIus("Noindex", null, lane, null, sample, false);
        s.add("IUS2", ius);

        //lane 3
        lane = lims.createLane(null, null, "3", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_001"), false, "11");
        s.add("TEST_SEQUENCER_RUN_001_3", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0001_Pa_P_PE_400_WG", s.getSample("TEST_0001_Pa_P"));
        s.add("TEST_0001_Pa_P_PE_400_WG", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        lims.annotate(sample, "geo_library_size_code", "400");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = lims.createIus("Noindex", null, lane, null, sample, false);
        s.add("IUS3", ius);

        //lane 4
        lane = lims.createLane(null, null, "4", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_001"), false, "11");
        s.add("TEST_SEQUENCER_RUN_001_4", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0001_Pa_P_PE_300_WG", s.getSample("TEST_0001_Pa_P"));
        s.add("TEST_0001_Pa_P_PE_300_WG", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        lims.annotate(sample, "geo_library_size_code", "300");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = lims.createIus("Noindex", null, lane, null, sample, false);
        s.add("IUS4", ius);

        //lane 5
        lane = lims.createLane(null, null, "5", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_001"), false, "11");
        s.add("TEST_SEQUENCER_RUN_001_5", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0001_Pa_P_PE_396_MR", s.getSample("TEST_0001_Pa_P"));
        s.add("TEST_0001_Pa_P_PE_396_MR", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        lims.annotate(sample, "geo_library_size_code", "396");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.MR.toString());
        ius = lims.createIus("Noindex", null, lane, null, sample, false);
        s.add("IUS5", ius);

        //lane 6
        lane = lims.createLane(null, null, "6", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_001"), false, "11");
        s.add("TEST_SEQUENCER_RUN_001_6", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0001_Pa_P_PE_296_WT", s.getSample("TEST_0001_Pa_P"));
        s.add("TEST_0001_Pa_P_PE_296_WT", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        lims.annotate(sample, "geo_library_size_code", "296");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.WT.toString());
        ius = lims.createIus("Noindex", null, lane, null, sample, false);
        s.add("IUS6", ius);

        //lane 7
        lane = lims.createLane(null, null, "7", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_001"), false, "11");
        s.add("TEST_SEQUENCER_RUN_001_7", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0001_Ly_R_PE_250_EX", s.getSample("TEST_0001_Ly_R"));
        s.add("TEST_0001_Ly_R_PE_250_EX", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Ly.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.R.toString());
        lims.annotate(sample, "geo_library_size_code", "250");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        ius = lims.createIus("Noindex", null, lane, null, sample, false);
        s.add("IUS7", ius);

        //lane 8
        lane = lims.createLane(null, null, "8", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_001"), false, "11");
        s.add("TEST_SEQUENCER_RUN_001_8", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0001_Pa_P_PE_350_EX", s.getSample("TEST_0001_Pa_P"));
        s.add("TEST_0001_Pa_P_PE_350_EX", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        lims.annotate(sample, "geo_library_size_code", "350");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        ius = lims.createIus("Noindex", null, lane, null, sample, false);
        s.add("IUS8", ius);

        //TEST_SEQUENCER_RUN_002
        //lane 1
        lane = lims.createLane(null, null, "1", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_002"), false, "11");
        s.add("TEST_SEQUENCER_RUN_002_1", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0001_Ly_R_PE_500_WG", s.getSample("TEST_0001_Ly_R"));
        s.add("TEST_0001_Ly_R_PE_500_WG", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Ly.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.R.toString());
        lims.annotate(sample, "geo_library_size_code", "500");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = lims.createIus("Noindex", null, lane, null, sample, false);
        s.add("IUS9", ius);

        //lane 2
        lane = lims.createLane(null, null, "2", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_002"), false, "11");
        s.add("TEST_SEQUENCER_RUN_002_2", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0001_Pa_P_PE_300_WG", s.getSample("TEST_0001_Pa_P"));
        s.add("TEST_0001_Pa_P_PE_300_WG", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        lims.annotate(sample, "geo_library_size_code", "300");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = lims.createIus("Noindex", null, lane, null, sample, false);
        s.add("IUS10", ius);

        //lane 3
        lane = lims.createLane(null, null, "3", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_002"), false, "11");
        s.add("TEST_SEQUENCER_RUN_002_3", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0002_Pa_P_PE_400_EX", s.getSample("TEST_0002_Pa_P"));
        s.add("TEST_0002_Pa_P_PE_400_EX", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        lims.annotate(sample, "geo_library_size_code", "400");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        ius = lims.createIus("ACAGTG", null, lane, null, sample, false);
        s.add("IUS11", ius);

        sample = lims.createSample(null, experiment, "34", "TEST_0003_Pa_X_PE_401_EX", s.getSample("TEST_0003_Pa_X"));
        s.add("TEST_0003_Pa_X_PE_401_EX", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.X.toString());
        lims.annotate(sample, "geo_library_size_code", "401");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        ius = lims.createIus("TTAGGC", null, lane, null, sample, false);
        s.add("IUS12", ius);

        sample = lims.createSample(null, experiment, "34", "TEST_0004_Pa_C_PE_501_EX", s.getSample("TEST_0004_Pa_C"));
        s.add("TEST_0004_Pa_C_PE_501_EX", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.C.toString());
        lims.annotate(sample, "geo_library_size_code", "501");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        ius = lims.createIus("CGATGT", null, lane, null, sample, false);
        s.add("IUS13", ius);

        sample = lims.createSample(null, experiment, "34", "TEST_0005_Pa_X_PE_150_WG", s.getSample("TEST_0005_Pa_X"));
        s.add("TEST_0005_Pa_X_PE_150_WG", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.X.toString());
        lims.annotate(sample, "geo_library_size_code", "150");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = lims.createIus("ATCACG", null, lane, null, sample, false);
        s.add("IUS14", ius);

        //lane 4
        lane = lims.createLane(null, null, "4", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_002"), false, "11");
        s.add("TEST_SEQUENCER_RUN_002_4", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0002_Pa_P_PE_400_EX", s.getSample("TEST_0002_Pa_P"));
        s.add("TEST_0002_Pa_P_PE_400_EX", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        lims.annotate(sample, "geo_library_size_code", "400");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        ius = lims.createIus("TGACCA", null, lane, null, sample, false);
        s.add("IUS15", ius);

        sample = lims.createSample(null, experiment, "34", "TEST_0003_Pa_X_PE_401_EX", s.getSample("TEST_0003_Pa_X"));
        s.add("TEST_0003_Pa_X_PE_401_EX", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.X.toString());
        lims.annotate(sample, "geo_library_size_code", "401");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        lims.createIus("ACAGTG", null, lane, null, sample, false);
        s.add("IUS16", ius);

        //lane 5
        lane = lims.createLane(null, null, "5", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_002"), false, "11");
        s.add("TEST_SEQUENCER_RUN_002_5", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0001_Pa_X_PE_200_WG", s.getSample("TEST_0001_Pa_X"));
        s.add("TEST_0001_Pa_X_PE_200_WG", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.X.toString());
        lims.annotate(sample, "geo_library_size_code", "200");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = lims.createIus("Noindex", null, lane, null, sample, false);
        s.add("IUS17", ius);

        //lane 6
        lane = lims.createLane(null, null, "6", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_002"), false, "11");
        s.add("TEST_SEQUENCER_RUN_002_6", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0001_Pa_C_PE_700_WG", s.getSample("TEST_0001_Pa_C"));
        s.add("TEST_0001_Pa_C_PE_700_WG", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.C.toString());
        lims.annotate(sample, "geo_library_size_code", "700");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = lims.createIus("Noindex", null, lane, null, sample, false);
        s.add("IUS18", ius);

        //lane 7
        lane = lims.createLane(null, null, "7", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_002"), false, "11");
        s.add("TEST_SEQUENCER_RUN_002_7", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0002_Ly_R_PE_500_EX", s.getSample("TEST_0002_Ly_R"));
        s.add("TEST_0002_Ly_R_PE_500_EX", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Ly.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.R.toString());
        lims.annotate(sample, "geo_library_size_code", "500");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        ius = lims.createIus("Noindex", null, lane, null, sample, false);
        s.add("IUS19", ius);

        //lane 8
        lane = lims.createLane(null, null, "8", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_002"), false, "11");
        s.add("TEST_SEQUENCER_RUN_002_8", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0003_Ly_R_PE_402_EX", s.getSample("TEST_0003_Ly_R"));
        s.add("TEST_0003_Ly_R_PE_402_EX", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Ly.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.R.toString());
        lims.annotate(sample, "geo_library_size_code", "402");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.EX.toString());
        ius = lims.createIus("Noindex", null, lane, null, sample, false);
        s.add("IUS20", ius);

        //TEST_SEQUENCER_RUN_003
        //lane 1
        lane = lims.createLane(null, null, "1", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_003"), false, "11");
        s.add("TEST_SEQUENCER_RUN_003_1", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0001_Ly_R_PE_500_WG", s.getSample("TEST_0001_Ly_R"));
        s.add("TEST_0001_Ly_R_PE_500_WG", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Ly.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.R.toString());
        lims.annotate(sample, "geo_library_size_code", "500");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = lims.createIus("Noindex", null, lane, null, sample, false);
        s.add("IUS21", ius);

        //lane 2
        lane = lims.createLane(null, null, "2", LibraryType.PE.getId(), "5", "14", null, s.getSequencerRun("TEST_SEQUENCER_RUN_003"), false, "11");
        s.add("TEST_SEQUENCER_RUN_003_2", lane);

        sample = lims.createSample(null, experiment, "34", "TEST_0001_Pa_P_PE_400_WG", s.getSample("TEST_0001_Pa_P"));
        s.add("TEST_0001_Pa_P_PE_400_WG", sample);
        lims.annotate(sample, "geo_tissue_origin", TissueOrigin.Pa.toString());
        lims.annotate(sample, "geo_tissue_type", TissueType.P.toString());
        lims.annotate(sample, "geo_library_size_code", "400");
        lims.annotate(sample, "geo_library_source_template_type", TemplateType.WG.toString());
        ius = lims.createIus("Noindex", null, lane, null, sample, false);
        s.add("IUS22", ius);

        lims.annotate(s.getLane("TEST_SEQUENCER_RUN_002_4"), true, "lane skip test");
        lims.annotate(s.getSequencerRun("TEST_SEQUENCER_RUN_003"), true, "sequencer run skip test");
        lims.annotate(s.getIus("IUS13"), true, "ius skip test");
        lims.annotate(s.getIus("IUS19"), true, "ius skip test");

    }

    public SeqwareObjects getSeqwareObjects() {
        return s;
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
