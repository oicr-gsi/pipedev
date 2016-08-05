package ca.on.oicr.pde.client;

import ca.on.oicr.pde.experimental.PDEPluginRunner;
import io.seqware.common.model.SequencerRunStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.model.Annotatable;
import net.sourceforge.seqware.common.model.Experiment;
import net.sourceforge.seqware.common.model.FirstTierModel;
import net.sourceforge.seqware.common.model.IUS;
import net.sourceforge.seqware.common.model.Lane;
import net.sourceforge.seqware.common.model.Sample;
import net.sourceforge.seqware.common.model.SequencerRun;
import net.sourceforge.seqware.common.model.Study;
import net.sourceforge.seqware.common.module.ReturnValue;
import net.sourceforge.seqware.pipeline.plugins.AttributeAnnotator;
import org.apache.commons.lang3.math.NumberUtils;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @author mlaszloffy
 */
public class MetadataBackedSeqwareLimsClient implements SeqwareLimsClient {

    private final Metadata metadata;
    private final PDEPluginRunner runner;

    public MetadataBackedSeqwareLimsClient(Metadata metadata, Map config) {
        this.metadata = metadata;
        this.runner = new PDEPluginRunner(config);
    }

    @Override
    public Experiment createExperiment(String description, String platformId, Study study, String title) {

        checkNotNull(platformId);
        checkNotNull(study);
        checkNotNull(title);

        ReturnValue rv = metadata.addExperiment(study.getSwAccession(), Integer.parseInt(platformId), sanitize(description), title, null, null);

        if (!isReturnValueValid(rv)) {
            return null;
        }

        return metadata.getExperiment(Integer.parseInt(rv.getAttribute("sw_accession")));
    }

    @Override
    public IUS createIus(String barcode, String description, Lane lane, String name, Sample sample, boolean skip) {

        checkNotNull(barcode);
        checkNotNull(lane);
        checkNotNull(sample);
        checkNotNull(skip);

        ReturnValue rv = metadata.addIUS(lane.getSwAccession(), sample.getSwAccession(), sanitize(name), sanitize(description), barcode, skip);

        if (!isReturnValueValid(rv)) {
            return null;
        }

        return metadata.getIUS(Integer.parseInt(rv.getAttribute("sw_accession")));
    }

    @Override
    public Lane createLane(String cycleDescriptor, String description, String laneNumber, String librarySelectionId, String librarySourceId, String libraryStrategyId, String name, SequencerRun sequencerRun, boolean skip, String studyTypeId) {

        checkNotNull(laneNumber);
        checkNotNull(sequencerRun);
        checkNotNull(studyTypeId);
        checkNotNull(skip);
        checkNotNull(librarySourceId);
        checkNotNull(librarySelectionId);
        checkNotNull(libraryStrategyId);

        ReturnValue rv = metadata.addLane(sequencerRun.getSwAccession(), Integer.parseInt(studyTypeId), Integer.parseInt(libraryStrategyId), Integer.parseInt(librarySelectionId), Integer.parseInt(librarySourceId), sanitize(name), sanitize(description), sanitize(cycleDescriptor), skip, Integer.parseInt(laneNumber));

        if (!isReturnValueValid(rv)) {
            return null;
        }

        return metadata.getLane(Integer.parseInt(rv.getAttribute("sw_accession")));
    }

    @Override
    public Sample createSample(String description, Experiment experiment, String organismId, String title, Sample parentSample) {

        checkNotNull(experiment);
        checkNotNull(organismId);
        checkNotNull(title);

        Integer parentSampleId = parentSample != null ? parentSample.getSwAccession() : 0;

        ReturnValue rv = metadata.addSample(experiment.getSwAccession(), parentSampleId, Integer.parseInt(organismId), sanitize(description), title);

        if (!isReturnValueValid(rv)) {
            return null;
        }

        return metadata.getSample(Integer.parseInt(rv.getAttribute("sw_accession")));
    }

    @Override
    public SequencerRun createSequencerRun(String description, String filePath, String name, boolean isPairedEnd, String platformId, boolean skip) {

        checkNotNull(filePath);
        checkNotNull(name);
        checkNotNull(isPairedEnd);
        checkNotNull(platformId);
        checkNotNull(skip);

        ReturnValue rv = metadata.addSequencerRun(Integer.parseInt(platformId), name, sanitize(description), isPairedEnd, skip, filePath, SequencerRunStatus.Complete);

        if (!isReturnValueValid(rv)) {
            return null;
        }

        return metadata.getSequencerRun(Integer.parseInt(rv.getAttribute("sw_accession")));
    }

    @Override
    public Study createStudy(String id, String centerName, String centerProjectName, String description, String studyType, String title) {
        //TODO: id is not used - how to set it in seqware?
        checkNotNull(centerName);
        checkNotNull(centerProjectName);
        checkNotNull(studyType);
        checkNotNull(title);

        ReturnValue rv = metadata.addStudy(title, sanitize(description), centerName, centerProjectName, Integer.parseInt(studyType));

        if (!isReturnValueValid(rv)) {
            return null;
        }

        return metadata.getStudy(Integer.parseInt(rv.getAttribute("sw_accession")));
    }

    @Override
    public <T extends Annotatable & FirstTierModel> void annotate(T o, String key, String value) {

        checkNotNull(o);
        checkNotNull(key);
        checkNotNull(value);

        List<String> params = new ArrayList(Arrays.asList(
                "--" + getAnnotatableClassShortName(o) + "-accession", o.getSwAccession().toString(),
                "--key", key,
                "--val", value
        ));

        //io.seqware.cli.Main.annotate
        ReturnValue rv = runner.runPlugin(new AttributeAnnotator(), params);
    }

    @Override
    public <T extends Annotatable & FirstTierModel> void annotate(T o, boolean skip, String value) {

        checkNotNull(o);
        checkNotNull(skip);

        List<String> params = new ArrayList(Arrays.asList(
                "--" + getAnnotatableClassShortName(o) + "-accession", o.getSwAccession().toString(),
                "--skip", Boolean.toString(skip)
        ));

        if (value != null && !value.isEmpty()) {
            params.add("--value");
            params.add(value);
        }

        //io.seqware.cli.Main.annotate
        ReturnValue rv = runner.runPlugin(new AttributeAnnotator(), params);
    }

    private boolean isReturnValueValid(ReturnValue rv) {

        if (rv == null) {
            return false;
        }

        if (rv.getReturnValue() != ReturnValue.SUCCESS) {
            return false;
        }

        if (rv.getAttribute("sw_accession") == null) {
            return false;
        }

        if (!NumberUtils.isNumber(rv.getAttribute("sw_accession"))) {
            return false;
        }

        //rv is valid
        return true;
    }

    private String sanitize(String input) {
        if (input == null) {
            return " ";
        }
        return input;
    }

    private <T extends Annotatable> String getAnnotatableClassShortName(T o) {
        if (o == null) {
            throw new NullPointerException();
        } else if (o instanceof Study) {
            return "study";
        } else if (o instanceof Experiment) {
            return "experiment";
        } else if (o instanceof Sample) {
            return "sample";
        } else if (o instanceof IUS) {
            return "ius";
        } else if (o instanceof SequencerRun) {
            return "sequencer-run";
        } else if (o instanceof Lane) {
            return "lane";
        } else {
            throw new RuntimeException("Unsupported annotable class: " + o.getClass().getName());
        }
    }

}
