package ca.on.oicr.pde.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ReducedFileProvenanceReportRecord implements Comparable<ReducedFileProvenanceReportRecord> {

    private Set<String> studyTitle;
    private Multimap<String, Set<String>> studyAttributes;
    private Set<String> experimentName;
    private Multimap<String, Set<String>> experimentAttributes;
    private Set<String> sampleName;
    private Multimap<String, Set<String>> sampleAttributes;
    private Set<String> sequencerRunName;
    private Multimap<String, Set<String>> sequencerRunAttributes;
    private Set<String> laneName;
    private Multimap<String, Set<String>> laneAttributes;
    private Set<String> iusTag;
    private Multimap<String, Set<String>> iusAttributes;
    private Set<String> workflowName;
    private Set<String> processingAlgorithm;
    private Multimap<String, Set<String>> processingAttributes;
    private Set<String> fileMetaType;
    private Multimap<String, Set<String>> fileAttributes;
    private boolean skip = false;
    private int fileHash = 0;

    private static final Comparator stringComparator = new Comparator<String>() {
        @Override
        public int compare(String lhs, String rhs) {
            return lhs.compareTo(rhs);
        }
    };

    private static final Comparator stringSetComparator = new Comparator<Set<String>>() {
        @Override
        public int compare(Set<String> lhs, Set<String> rhs) {
            return lhs.toString().compareTo(rhs.toString());
        }
    };

    private ReducedFileProvenanceReportRecord() {
        studyTitle = new TreeSet<String>();
        studyAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        experimentName = new TreeSet<String>();
        experimentAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        sampleName = new TreeSet<String>();
        sampleAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        sequencerRunName = new TreeSet<String>();
        sequencerRunAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        laneName = new TreeSet<String>();
        laneAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        iusTag = new TreeSet<String>();
        iusAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        workflowName = new TreeSet<String>();
        processingAlgorithm = new TreeSet<String>();
        processingAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        fileMetaType = new TreeSet<String>();
        fileAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        fileHash = 0;
    }

    public ReducedFileProvenanceReportRecord(FileProvenanceReportRecord f) {
        this(Arrays.asList(f));
    }

    public ReducedFileProvenanceReportRecord(Collection<FileProvenanceReportRecord> fs) {
        this();
        for (FileProvenanceReportRecord f : fs) {
            this.studyTitle.add(f.getStudyTitle());
            this.studyAttributes.putAll(Multimaps.forMap(f.getStudyAttributes()));
            this.experimentName.add(f.getExperimentName());
            this.experimentAttributes.putAll(Multimaps.forMap(f.getExperimentAttributes()));
            this.sampleName.add(f.getSampleName());
            this.sampleAttributes.putAll(Multimaps.forMap(f.getSampleAttributes()));
            this.sequencerRunName.add(f.getSequencerRunName());
            this.sequencerRunAttributes.putAll(Multimaps.forMap(f.getSequencerRunAttributes()));
            this.laneName.add(f.getLaneName());
            this.laneAttributes.putAll(Multimaps.forMap(f.getLaneAttributes()));
            this.iusTag.add(f.getIusTag());
            this.iusAttributes.putAll(Multimaps.forMap(f.getIusAttributes()));
            this.workflowName.add(f.getWorkflowName());
            this.processingAlgorithm.add(f.getProcessingAlgorithm());
            this.processingAttributes.putAll(Multimaps.forMap(f.getProcessingAttributes()));
            this.fileMetaType.add(f.getFileMetaType());
            this.fileAttributes.putAll(Multimaps.forMap(f.getFileAttributes()));
            this.skip = this.skip == true ? skip : Boolean.valueOf(f.getSkip());
            this.fileHash = f.getFilePath().hashCode();
        }
    }

    public Set getStudyTitle() {
        return studyTitle;
    }

    public void setStudyTitle(Set studyTitle) {
        this.studyTitle = new TreeSet<String>(studyTitle);
    }

    public Map<String, Collection<Set<String>>> getStudyAttributes() {
        return studyAttributes.asMap();
    }

    public void setStudyAttributes(Map<String, Collection<TreeSet<String>>> studyAttributes) {
        this.studyAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        for (Entry<String, Collection<TreeSet<String>>> e : studyAttributes.entrySet()) {
            this.studyAttributes.putAll(e.getKey(), e.getValue());
        }
    }

    public Set getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(Set experimentName) {
        this.experimentName = new TreeSet<String>(experimentName);
    }

    public Map<String, Collection<Set<String>>> getExperimentAttributes() {
        return experimentAttributes.asMap();
    }

    public void setExperimentAttributes(Map<String, Collection<TreeSet<String>>> experimentAttributes) {
        this.experimentAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        for (Entry<String, Collection<TreeSet<String>>> e : experimentAttributes.entrySet()) {
            this.experimentAttributes.putAll(e.getKey(), e.getValue());
        }
    }

    public Set getSampleName() {
        return sampleName;
    }

    public void setSampleName(Set sampleName) {
        this.sampleName = new TreeSet<String>(sampleName);
    }

    public Map<String, Collection<Set<String>>> getSampleAttributes() {
        return sampleAttributes.asMap();
    }

    public void setSampleAttributes(Map<String, Collection<TreeSet<String>>> sampleAttributes) {
        this.sampleAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        for (Entry<String, Collection<TreeSet<String>>> e : sampleAttributes.entrySet()) {
            this.sampleAttributes.putAll(e.getKey(), e.getValue());
        }
    }

    public Set getSequencerRunName() {
        return sequencerRunName;
    }

    public void setSequencerRunName(Set sequencerRunName) {
        this.sequencerRunName = new TreeSet<String>(sequencerRunName);
    }

    public Map<String, Collection<Set<String>>> getSequencerRunAttributes() {
        return sequencerRunAttributes.asMap();
    }

    public void setSequencerRunAttributes(Map<String, Collection<TreeSet<String>>> sequencerRunAttributes) {
        this.sequencerRunAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        for (Entry<String, Collection<TreeSet<String>>> e : sequencerRunAttributes.entrySet()) {
            this.sequencerRunAttributes.putAll(e.getKey(), e.getValue());
        }
    }

    public Set getLaneName() {
        return laneName;
    }

    public void setLaneName(Set laneName) {
        this.laneName = new TreeSet<String>(laneName);
    }

    public Map<String, Collection<Set<String>>> getLaneAttributes() {
        return laneAttributes.asMap();
    }

    public void setLaneAttributes(Map<String, Collection<TreeSet<String>>> laneAttributes) {
        this.laneAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        for (Entry<String, Collection<TreeSet<String>>> e : laneAttributes.entrySet()) {
            this.laneAttributes.putAll(e.getKey(), e.getValue());
        }
    }

    public Set getIusTag() {
        return iusTag;
    }

    public void setIusTag(Set iusTag) {
        this.iusTag = new TreeSet<String>(iusTag);
    }

    public Map<String, Collection<Set<String>>> getIusAttributes() {
        return iusAttributes.asMap();
    }

    public void setIusAttributes(Map<String, Collection<TreeSet<String>>> iusAttributes) {
        this.iusAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        for (Entry<String, Collection<TreeSet<String>>> e : iusAttributes.entrySet()) {
            this.iusAttributes.putAll(e.getKey(), e.getValue());
        }
    }

    public Set getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(Set workflowName) {
        this.workflowName = new TreeSet<String>(workflowName);
    }

    public Set getProcessingAlgorithm() {
        return processingAlgorithm;
    }

    public void setProcessingAlgorithm(Set processingAlgorithm) {
        this.processingAlgorithm = new TreeSet<String>(processingAlgorithm);
    }

    public Map<String, Collection<Set<String>>> getProcessingAttributes() {
        return processingAttributes.asMap();
    }

    public void setProcessingAttributes(Map<String, Collection<TreeSet<String>>> processingAttributes) {
        this.processingAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        for (Entry<String, Collection<TreeSet<String>>> e : processingAttributes.entrySet()) {
            this.processingAttributes.putAll(e.getKey(), e.getValue());
        }
    }

    @JsonIgnore
    public Boolean getSkip() {
        return skip;
    }

    public Set getFileMetaType() {
        return fileMetaType;
    }

    public void setFileMetaType(Set fileMetaType) {
        this.fileMetaType = new TreeSet<String>(fileMetaType);
    }

    public Map<String, Collection<Set<String>>> getFileAttributes() {
        return fileAttributes.asMap();
    }

    public void setFileAttributes(Map<String, Collection<TreeSet<String>>> fileAttributes) {
        this.fileAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        for (Entry<String, Collection<TreeSet<String>>> e : fileAttributes.entrySet()) {
            this.fileAttributes.putAll(e.getKey(), e.getValue());
        }
    }

    public int getFileId() {
        return fileHash;
    }

    public void setFileId(int fileId) {
        this.fileHash = fileId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int compareTo(ReducedFileProvenanceReportRecord o) {
        //TODO: optimize
        return this.toString().compareTo(o.toString());
    }

}
