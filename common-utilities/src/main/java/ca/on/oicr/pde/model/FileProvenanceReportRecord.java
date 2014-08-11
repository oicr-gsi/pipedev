package ca.on.oicr.pde.model;

import static com.google.common.base.Preconditions.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class FileProvenanceReportRecord implements Serializable {

    private static final Logger logger = LogManager.getLogger(FileProvenanceReportRecord.class.getName());

    private final Experiment experiment;
    private final Study study;
    private final Ius ius;
    private final Lane lane;
    private final SequencerRun sequencerRun;
    private final Processing processing;
    private final Workflow workflow;
    private final WorkflowRun workflowRun;
    private final File file;
    private final Sample sample;
    private final Sample rootSample;
    private final List<Sample> parentSamples;

    private final String lastModified;
    private final String skip;

    private FileProvenanceReportRecord(Builder b) {

        this.experiment = b.experiment;
        this.study = b.study;
        this.ius = b.ius;
        this.lane = b.lane;
        this.sequencerRun = b.sequencerRun;
        this.processing = b.processing;
        this.workflow = b.workflow;
        this.workflowRun = b.workflowRun;
        this.file = b.file;
        this.sample = b.sample;
        this.rootSample = b.rootSample;
        this.parentSamples = b.parentSamples;
        this.lastModified = b.lastModified;
        this.skip = b.skip;

    }

    public String getLastModified() {
        return lastModified;
    }

    public String getStudyTitle() {
        return study.getTitle();
    }

    public String getStudySwid() {
        return study.getSwid();
    }

    public Map<String, Set<String>> getStudyAttributes() {
        return study.getAttributes();
    }

    public String getExperimentName() {
        return experiment.getName();
    }

    public String getExperimentSwid() {
        return experiment.getSwid();
    }

    public Map<String, Set<String>> getExperimentAttributes() {
        return experiment.getAttributes();
    }

    public List<Sample> getParentSamples() {
        return Collections.unmodifiableList(parentSamples);
    }

    public String getSampleName() {
        return sample.getName();
    }

    public String getSampleSwid() {
        return sample.getSwid();
    }

    public Map<String, Set<String>> getSampleAttributes() {
        return sample.getAttributes();
    }

    public String getSequencerRunName() {
        return sequencerRun.getName();
    }

    public String getSequencerRunSwid() {
        return sequencerRun.getSwid();
    }

    public Map<String, Set<String>> getSequencerRunAttributes() {
        return sequencerRun.getAttributes();
    }

    public String getLaneName() {
        return lane.getName();
    }

    public String getLaneNumber() {
        return lane.getNumber();
    }

    public String getLaneSwid() {
        return lane.getSwid();
    }

    public Map<String, Set<String>> getLaneAttributes() {
        return lane.getAttributes();
    }

    public String getIusTag() {
        return ius.getTag();
    }

    public String getIusSwid() {
        return ius.getSwid();
    }

    public Map<String, Set<String>> getIusAttributes() {
        return ius.getAttributes();
    }

    public String getWorkflowName() {
        return workflow.getName();
    }

    public String getWorkflowVersion() {
        return workflow.getVersion();
    }

    public String getWorkflowSwid() {
        return workflow.getSwid();
    }

    public String getWorkflowRunName() {
        return workflowRun.getName();
    }

    public String getWorkflowRunStatus() {
        return workflowRun.getStatus();
    }

    public String getWorkflowRunSwid() {
        return workflowRun.getSwid();
    }

    public String getProcessingAlgorithm() {
        return processing.getAlgorithm();
    }

    public String getProcessingSwid() {
        return processing.getSwid();
    }

    public Map<String, Set<String>> getProcessingAttributes() {
        return processing.getAttributes();
    }

    public String getFileMetaType() {
        return file.getMetaType();
    }

    public String getFileSwid() {
        return file.getSwid();
    }

    public Map<String, Set<String>> getFileAttributes() {
        return file.getAttributes();
    }

    public String getFilePath() {
        return file.getPath();
    }

    public String getFileMd5sum() {
        return file.getMd5sum();
    }

    public String getFileSize() {
        return file.getSize();
    }

    public String getFileDescription() {
        return file.getDescription();
    }

    public String getSkip() {
        return skip;
    }

    public List<String> getSeqwareAccessions() {

        return Arrays.asList(study.getSwid(), experiment.getSwid(), sample.getSwid(), sequencerRun.getSwid(), lane.getSwid(), ius.getSwid(), workflow.getSwid(),
                workflowRun.getSwid(), processing.getSwid(), file.getSwid());

    }

    public boolean containsSeqwareAccession(Accessionable swid) {

        return getSeqwareAccessions().contains(swid.getSwid());

    }

    public boolean containsSeqwareAccession(Collection<? extends Accessionable> swids) {

        List<String> accessions = getSeqwareAccessions();

        for (Accessionable swid : swids) {
            if (accessions.contains(swid.getSwid())) {
                return true;
            }
        }
        return false;

    }

    public ReducedFileProvenanceReportRecord getSimpleFileProvenanceReportRecord() {

        return new ReducedFileProvenanceReportRecord(this);

    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        //
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public static class Builder {

        private Experiment experiment;
        private Study study;
        private Ius ius;
        private Lane lane;
        private SequencerRun sequencerRun;
        private Processing processing;
        private Workflow workflow;
        private WorkflowRun workflowRun;
        private File file;
        private Sample sample;
        private Sample rootSample;
        private List<Sample> parentSamples;

        private String lastModified = "";
        private String studyTitle = "";
        private String studySwid = "";
        private String studyAttributes = "";
        private String experimentName = "";
        private String experimentSwid = "";
        private String experimentAttributes = "";
        private String rootSampleName = "";
        private String rootSampleSwid = "";
        private String parentSampleNames = "";
        private String parentSampleSwids = "";
        private String parentSampleOrganismIds = "";
        private String parentSampleAttributes = "";
        private String sampleName = "";
        private String sampleSwid = "";
        private String sampleOrganismId = "";
        private String sampleOrganismCode = "";
        private String sampleAttributes = "";
        private String sequencerRunName = "";
        private String sequencerRunSwid = "";
        private String sequencerRunAttributes = "";
        private String sequencerRunPlatformId = "";
        private String sequencerRunPlatformName = "";
        private String laneName = "";
        private String laneNumber = "";
        private String laneSwid = "";
        private String laneAttributes = "";
        private String iusTag = "";
        private String iusSwid = "";
        private String iusAttributes = "";
        private String workflowName = "";
        private String workflowVersion = "";
        private String workflowSwid = "";
        private String workflowRunName = "";
        private String workflowRunStatus = "";
        private String workflowRunSwid = "";
        private String processingAlgorithm = "";
        private String processingSwid = "";
        private String processingAttributes = "";
        private String fileMetaType = "";
        private String fileSwid = "";
        private String fileAttributes = "";
        private String filePath = "";
        private String fileMd5sum = "";
        private String fileSize = "";
        private String fileDescription = "";
        private String skip = "";

        public Builder() {

        }

        public Builder setLastModified(String lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder setStudyTitle(String studyTitle) {
            this.studyTitle = studyTitle;
            return this;
        }

        public Builder setStudySwid(String studySwid) {
            this.studySwid = studySwid;
            return this;
        }

        public Builder setStudyAttributes(String studyAttributes) {
            this.studyAttributes = studyAttributes;
            return this;
        }

        public Builder setExperimentName(String experimentName) {
            this.experimentName = experimentName;
            return this;
        }

        public Builder setExperimentSwid(String experimentSwid) {
            this.experimentSwid = experimentSwid;
            return this;
        }

        public Builder setExperimentAttributes(String experimentAttributes) {
            this.experimentAttributes = experimentAttributes;
            return this;
        }

        public Builder setRootSampleName(String rootSampleName) {
            this.rootSampleName = rootSampleName;
            return this;
        }

        public Builder setRootSampleSwid(String rootSampleSwid) {
            this.rootSampleSwid = rootSampleSwid;
            return this;
        }

        public Builder setParentSampleName(String parentSampleName) {
            this.parentSampleNames = parentSampleName;
            return this;
        }

        public Builder setParentSampleSwid(String parentSampleSwid) {
            this.parentSampleSwids = parentSampleSwid;
            return this;
        }

        public Builder setParentSampleOrganismIds(String parentSampleOrganismIds) {
            this.parentSampleOrganismIds = parentSampleOrganismIds;
            return this;
        }

        public Builder setParentSampleAttributes(String parentSampleAttributes) {
            this.parentSampleAttributes = parentSampleAttributes;
            return this;
        }

        public Builder setSampleName(String sampleName) {
            this.sampleName = sampleName;
            return this;
        }

        public Builder setSampleSwid(String sampleSwid) {
            this.sampleSwid = sampleSwid;
            return this;
        }

        public Builder setSampleOrganismId(String sampleOrganismId) {
            this.sampleOrganismId = sampleOrganismId;
            return this;
        }

        public Builder setSampleOrganismCode(String sampleOrganismCode) {
            this.sampleOrganismCode = sampleOrganismCode;
            return this;
        }

        public Builder setSampleAttributes(String sampleAttributes) {
            this.sampleAttributes = sampleAttributes;
            return this;
        }

        public Builder setSequencerRunName(String sequencerRunName) {
            this.sequencerRunName = sequencerRunName;
            return this;
        }

        public Builder setSequencerRunSwid(String sequencerRunSwid) {
            this.sequencerRunSwid = sequencerRunSwid;
            return this;
        }

        public Builder setSequencerRunAttributes(String sequencerRunAttributes) {
            this.sequencerRunAttributes = sequencerRunAttributes;
            return this;
        }

        public Builder setSequencerRunPlatformId(String sequencerRunPlatformId) {
            this.sequencerRunPlatformId = sequencerRunPlatformId;
            return this;
        }

        public Builder setSequencerRunPlatformName(String sequencerRunPlatformName) {
            this.sequencerRunPlatformName = sequencerRunPlatformName;
            return this;
        }

        public Builder setLaneName(String laneName) {
            this.laneName = laneName;
            return this;
        }

        public Builder setLaneNumber(String laneNumber) {
            this.laneNumber = laneNumber;
            return this;
        }

        public Builder setLaneSwid(String laneSwid) {
            this.laneSwid = laneSwid;
            return this;
        }

        public Builder setLaneAttributes(String laneAttributes) {
            this.laneAttributes = laneAttributes;
            return this;
        }

        public Builder setIusTag(String iusTag) {
            this.iusTag = iusTag;
            return this;
        }

        public Builder setIusSwid(String iusSwid) {
            this.iusSwid = iusSwid;
            return this;
        }

        public Builder setIusAttributes(String iusAttributes) {
            this.iusAttributes = iusAttributes;
            return this;
        }

        public Builder setWorkflowName(String workflowName) {
            this.workflowName = workflowName;
            return this;
        }

        public Builder setWorkflowVersion(String workflowVersion) {
            this.workflowVersion = workflowVersion;
            return this;
        }

        public Builder setWorkflowSwid(String workflowSwid) {
            this.workflowSwid = workflowSwid;
            return this;
        }

        public Builder setWorkflowRunName(String workflowRunName) {
            this.workflowRunName = workflowRunName;
            return this;
        }

        public Builder setWorkflowRunStatus(String workflowRunStatus) {
            this.workflowRunStatus = workflowRunStatus;
            return this;
        }

        public Builder setWorkflowRunSwid(String workflowRunSwid) {
            this.workflowRunSwid = workflowRunSwid;
            return this;
        }

        public Builder setProcessingAlgorithm(String processingAlgorithm) {
            this.processingAlgorithm = processingAlgorithm;
            return this;
        }

        public Builder setProcessingSwid(String processingSwid) {
            this.processingSwid = processingSwid;
            return this;
        }

        public Builder setProcessingAttributes(String processingAttributes) {
            this.processingAttributes = processingAttributes;
            return this;
        }

        public Builder setFileMetaType(String fileMetaType) {
            this.fileMetaType = fileMetaType;
            return this;
        }

        public Builder setFileSwid(String fileSwid) {
            this.fileSwid = fileSwid;
            return this;
        }

        public Builder setFileAttributes(String fileAttributes) {
            this.fileAttributes = fileAttributes;
            return this;
        }

        public Builder setFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder setFileMd5sum(String fileMd5sum) {
            this.fileMd5sum = fileMd5sum;
            return this;
        }

        public Builder setFileSize(String fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public Builder setFileDescription(String fileDescription) {
            this.fileDescription = fileDescription;
            return this;
        }

        public Builder setSkip(String skip) {
            this.skip = skip;
            return this;
        }

        public FileProvenanceReportRecord build() {

            //Seqware attribute field formatting:  key1=value1&value2&...&valueN;key2=...;keyN=...
            //For example:
            //key1=value1&value2&value3;key2=value1;key3=value1&value2
            String attrKeyValuePairDelimiter = ";";
            String attrKeyValueSeparator = "=";
            String attrValueDelimiter = "&";

            //TODO: use factory to create these objects:
            experiment = new Experiment();
            experiment.setSwid(getSwid(experimentSwid));
            experiment.setName(experimentName);
            experiment.setAttributes(transformAttributeStringToMap(experimentAttributes,
                    attrKeyValuePairDelimiter, attrKeyValueSeparator, attrValueDelimiter));

            study = new Study();
            study.setSwid(getSwid(studySwid));
            study.setTitle(studyTitle);
            study.setAttributes(transformAttributeStringToMap(studyAttributes,
                    attrKeyValuePairDelimiter, attrKeyValueSeparator, attrValueDelimiter));

            ius = new Ius();
            ius.setSwid(getSwid(iusSwid));
            ius.setTag(iusTag);
            ius.setAttributes(transformAttributeStringToMap(iusAttributes,
                    attrKeyValuePairDelimiter, attrKeyValueSeparator, attrValueDelimiter));

            lane = new Lane();
            lane.setSwid(getSwid(laneSwid));
            lane.setName(laneName);
            lane.setNumber(laneNumber);
            lane.setAttributes(transformAttributeStringToMap(laneAttributes,
                    attrKeyValuePairDelimiter, attrKeyValueSeparator, attrValueDelimiter));

            sequencerRun = new SequencerRun();
            sequencerRun.setSwid(getSwid(sequencerRunSwid));
            sequencerRun.setName(sequencerRunName);
            sequencerRun.setPlatformId(sequencerRunPlatformId);
            sequencerRun.setPlatformName(sequencerRunPlatformName);

            sequencerRun.setAttributes(transformAttributeStringToMap(sequencerRunAttributes,
                    attrKeyValuePairDelimiter, attrKeyValueSeparator, attrValueDelimiter));

            processing = new Processing();
            processing.setSwid(getSwid(processingSwid));
            processing.setAlgorithm(processingAlgorithm);
            processing.setAttributes(transformAttributeStringToMap(processingAttributes,
                    attrKeyValuePairDelimiter, attrKeyValueSeparator, attrValueDelimiter));

            workflow = new Workflow();
            workflow.setSwid(getSwid(workflowSwid));
            workflow.setName(workflowName);
            workflow.setVersion(workflowVersion);

            workflowRun = new WorkflowRun();
            workflowRun.setSwid(getSwid(workflowRunSwid));
            workflowRun.setName(workflowRunName);
            workflowRun.setStatus(workflowRunStatus);

            file = new File();
            file.setSwid(getSwid(fileSwid));
            file.setMetaType(fileMetaType);
            file.setSize(fileSize);
            file.setMd5sum(fileMd5sum);
            file.setDescription(fileDescription);
            file.setPath(filePath);
            file.setAttributes(transformAttributeStringToMap(fileAttributes,
                    attrKeyValuePairDelimiter, attrKeyValueSeparator, attrValueDelimiter));

            sample = new Sample();
            sample.setSwid(getSwid(sampleSwid));
            sample.setName(sampleName);
            sample.setOrganismId(sampleOrganismId);
            sample.setOrganismCode(sampleOrganismCode);
            sample.setAttributes(transformAttributeStringToMap(sampleAttributes,
                    attrKeyValuePairDelimiter, attrKeyValueSeparator, attrValueDelimiter));

            rootSample = new Sample();
            rootSample.setSwid(rootSampleSwid);
            rootSample.setName(rootSampleName);

            parentSamples = new ArrayList<Sample>();
            parentSamples = buildParentSamples(parentSampleSwids, parentSampleNames, parentSampleOrganismIds, parentSampleAttributes);

            return new FileProvenanceReportRecord(this);

        }

        private List<Sample> buildParentSamples(String parentSampleSwids, String parentSampleNames, String parentSampleOrganismIds, String parentSampleAttributes) {

            if (parentSampleSwids == null || parentSampleSwids.isEmpty()) {
                return new ArrayList<Sample>();
            }

            List<String> swids = Arrays.asList(parentSampleSwids.split(":"));
            Map<String, String> names = parseFileProvenanceListStructure(swids, parentSampleNames);
            Map<String, String> organismIds = parseFileProvenanceListStructure(swids, parentSampleOrganismIds);
            Map<String, Map<String, Set<String>>> attributes = parseFileProvenanceMapStructure(swids, parentSampleAttributes);

            List<Sample> samples = new ArrayList<Sample>(swids.size());
            for (String swid : swids) {
                Sample s = new Sample();
                s.setSwid(swid);
                s.setName(names.get(swid));
                s.setOrganismId(organismIds.get(swid));
                s.setAttributes(attributes.get(swid));

                samples.add(s);
            }

            return samples;
        }

        private Map<String, String> parseFileProvenanceListStructure(List<String> keys, String listAsString) {
            checkNotNull(keys, "key list can not be null");
            checkNotNull(listAsString, "listAsString can not be null");

            List<String> values = Arrays.asList(listAsString.split(":")); //values are separated by colons
            checkArgument(keys.size() == values.size(), "key list is length = %s but listAsString has %s elements. \nkey set = [%s]\nlistAsString = [%s]",
                    keys.size(), values.size(), keys.toString(), listAsString);

            //iterate through both lists - it is assumed that the lists represent pairs
            Map<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < keys.size(); i++) {
                map.put(keys.get(i), values.get(i));
            }

            return map;
        }

        private String getSwid(String swid) {

            String result = "";

            if (swid != null && !swid.isEmpty()) {
                return swid;
            }

            return result;

        }

    }

    public static Map<String, Set<String>> transformAttributeStringToMap(String input,
            String keyValuePairDelimiter, String keyValueDelimiter, String valueDelimiter) {

        Map<String, Set<String>> out = new TreeMap<String, Set<String>>();

        for (String keyValuePair : StringUtils.split(input, keyValuePairDelimiter)) {
            String[] kv = StringUtils.split(keyValuePair, keyValueDelimiter);

            String key = kv[0];
            Set<String> values = kv.length == 1 ? new TreeSet<String>() : new TreeSet<String>(Arrays.asList(StringUtils.split(kv[1], valueDelimiter)));

            if (out.containsKey(key)) {
                out.get(key).addAll(values);
            } else {
                out.put(key, values);
            }
        }

        return out;

    }

    public static Map<String, Map<String, Set<String>>> parseFileProvenanceMapStructure(List<String> keys, String mapAsString) {
        //preconditions
        checkNotNull(keys);
        checkNotNull(mapAsString);
        checkArgument(!keys.isEmpty());
        checkArgument(!mapAsString.isEmpty());

        Map<String, Map<String, Set<String>>> map = new HashMap<String, Map<String, Set<String>>>();
        for (String s : keys) {
            if (map.get(s) != null) {
                throw new IllegalArgumentException("key list must have unique elements");
            }
            map.put(s, new HashMap<String, Set<String>>());
        }

        List<String> values = Arrays.asList(mapAsString.split(";")); //values are separated by semi-colons
        //each element has this structure: parent_<attr_key>.<primary_key>=<value>
        Pattern p = Pattern.compile("parent_(.*)\\.([0-9]*)=(.*)");
        Matcher m;
        for (String v : values) {
            m = p.matcher(v);
            m.find();
            String attrKey = m.group(1);
            String primaryKey = m.group(2);
            String attrValueString = m.group(3);
            logger.printf(Level.INFO, "key = %s, attr_key = %s, attr_value(s) = %s", primaryKey, attrKey, attrValueString);

            if (map.get(primaryKey) == null) {
                throw new IllegalArgumentException(String.format("the id [%s] (for parent attribute [%s]) does not exist in the key list", primaryKey, attrKey));
            }

            Set<String> attrValues = new TreeSet<String>(Arrays.asList(StringUtils.split(attrValueString, "&")));

            //Set the attr key value pair for the given primary key
            Set<String> previousValue = map.get(primaryKey).put(attrKey, attrValues);

            if (previousValue != null) {
                logger.printf(Level.WARN, "duplicate element detected for key = [%], attr key = [%s], attr value = [%s]", primaryKey, attrKey, attrValueString);
            }
        }

        return map;
    }
}
