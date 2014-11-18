package ca.on.oicr.pde.dao.writer;

import ca.on.oicr.pde.model.Experiment;
import ca.on.oicr.pde.model.Ius;
import ca.on.oicr.pde.model.Lane;
import ca.on.oicr.pde.model.Sample;
import ca.on.oicr.pde.model.SequencerRun;
import ca.on.oicr.pde.model.SeqwareObject;
import ca.on.oicr.pde.model.Study;
import ca.on.oicr.pde.model.Workflow;
import java.util.List;

public interface SeqwareWriteService {

    public Experiment createExperiment(String description, String platformId, Study study, String title);

    public Ius createIus(String barcode, String description, Lane lane, String name, Sample sample, boolean skip);

    public Lane createLane(String cycleDescriptor, String description, String laneNumber, String librarySelectionId, 
            String librarySourceId, String libraryStrategyId, String name, SequencerRun sequencerRun, boolean skip, String studyTypeId);

    public Sample createSample(String description, Experiment experiment, String organismId, String title, Sample parentSample);

    public SequencerRun createSequencerRun(String description, String filePath, String name, boolean isPairedEnd, String platformId, boolean skip);

    public Study createStudy(String id, String centerName, String centerProjectName, String description, String studyType, String title);

    public String createWorkflowRun(Workflow workflow, List<? extends SeqwareObject> parents, List<FileInfo> files);

    public Workflow createWorkflow(String name, String version, String description);

    public void annotate(SeqwareObject o, String key, String value);

    public void annotate(SeqwareObject o, boolean skip, String reason);

    public void updateFileReport();

    public static class FileInfo {

        private final String type;
        private final String metaType;
        private final String filePath;

        public FileInfo(String type, String metaType, String filePath) {
            this.type = type;
            this.metaType = metaType;
            this.filePath = filePath;
        }

        public String getType() {
            return type;
        }

        public String getMetaType() {
            return metaType;
        }

        public String getFilePath() {
            return filePath;
        }

    }

}
