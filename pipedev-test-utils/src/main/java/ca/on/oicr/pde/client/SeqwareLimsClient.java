package ca.on.oicr.pde.client;

import net.sourceforge.seqware.common.model.IUS;
import net.sourceforge.seqware.common.model.Annotatable;
import net.sourceforge.seqware.common.model.Experiment;
import net.sourceforge.seqware.common.model.FirstTierModel;
import net.sourceforge.seqware.common.model.Lane;
import net.sourceforge.seqware.common.model.Sample;
import net.sourceforge.seqware.common.model.SequencerRun;
import net.sourceforge.seqware.common.model.Study;

/**
 *
 * @author mlaszloffy
 */
public interface SeqwareLimsClient {

    public Experiment createExperiment(String description, String platformId, Study study, String title);

    public IUS createIus(String barcode, String description, Lane lane, String name, Sample sample, boolean skip);

    public Lane createLane(String cycleDescriptor, String description, String laneNumber, String librarySelectionId,
            String librarySourceId, String libraryStrategyId, String name, SequencerRun sequencerRun, boolean skip, String studyTypeId);

    public Sample createSample(String description, Experiment experiment, String organismId, String title, Sample parentSample);

    public SequencerRun createSequencerRun(String description, String filePath, String name, boolean isPairedEnd, String platformId, boolean skip);

    public Study createStudy(String id, String centerName, String centerProjectName, String description, String studyType, String title);

    public <T extends Annotatable<?> & FirstTierModel> void annotate(T o, String key, String value);

    public <T extends Annotatable<?> & FirstTierModel> void annotate(T o, boolean skip, String reason);

    public void refresh();

}
