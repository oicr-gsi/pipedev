package ca.on.oicr.pde.model;

import ca.on.oicr.gsi.provenance.model.FileProvenance;
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
        studyTitle = new TreeSet<>();
        studyAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        experimentName = new TreeSet<>();
        experimentAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        sampleName = new TreeSet<>();
        sampleAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        sequencerRunName = new TreeSet<>();
        sequencerRunAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        laneName = new TreeSet<>();
        laneAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        iusTag = new TreeSet<>();
        iusAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        workflowName = new TreeSet<>();
        processingAlgorithm = new TreeSet<>();
        processingAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        fileMetaType = new TreeSet<>();
        fileAttributes = TreeMultimap.create(stringComparator, stringSetComparator);
        fileHash = 0;
    }

    public ReducedFileProvenanceReportRecord(FileProvenance f) {
        this(Arrays.asList(f));
    }

    public ReducedFileProvenanceReportRecord(Collection<FileProvenance> fs) {
        this();
        for (FileProvenance f : fs) {
            if (f.getStudyTitles() != null) {
                this.studyTitle.addAll(f.getStudyTitles());
            }
            if (f.getSampleAttributes() != null) {
                this.studyAttributes.putAll(Multimaps.forMap(f.getStudyAttributes()));
            }
            if (f.getSampleNames() != null) {
                this.sampleName.addAll(f.getSampleNames());
            }
            if (f.getSampleAttributes() != null) {
                this.sampleAttributes.putAll(Multimaps.forMap(f.getSampleAttributes()));
            }
            if (f.getSequencerRunNames() != null) {
                this.sequencerRunName.addAll(f.getSequencerRunNames());
            }
            if (f.getSequencerRunAttributes() != null) {
                this.sequencerRunAttributes.putAll(Multimaps.forMap(f.getSequencerRunAttributes()));
            }
            if (f.getLaneNames() != null) {
                this.laneName.addAll(f.getLaneNames());
            }
            if (f.getLaneAttributes() != null) {
                this.laneAttributes.putAll(Multimaps.forMap(f.getLaneAttributes()));
            }
            if (f.getIusTags() != null) {
                this.iusTag.addAll(f.getIusTags());
            }
            if (f.getIusAttributes() != null) {
                this.iusAttributes.putAll(Multimaps.forMap(f.getIusAttributes()));
            }
            if (f.getWorkflowName() != null) {
                this.workflowName.add(f.getWorkflowName());
            }
            if (f.getProcessingAlgorithm() != null) {
                this.processingAlgorithm.add(f.getProcessingAlgorithm());
            }
            if (f.getProcessingAttributes() != null) {
                this.processingAttributes.putAll(Multimaps.forMap(f.getProcessingAttributes()));
            }
            if (f.getFileMetaType() != null) {
                this.fileMetaType.add(f.getFileMetaType());
            }
            if (f.getFileAttributes() != null) {
                this.fileAttributes.putAll(Multimaps.forMap(f.getFileAttributes()));
            }
            if (f.getSkip() != null && !this.skip) {
                this.skip = Boolean.valueOf(f.getSkip());
            }
            if (f.getFilePath() != null) {
                this.fileHash = f.getFilePath().hashCode();
            }
        }
    }

    public static ReducedFileProvenanceReportRecord from(FileProvenanceReportRecord f) {
        return from(Arrays.asList(f));
    }

    public static ReducedFileProvenanceReportRecord from(Collection<FileProvenanceReportRecord> fs) {
        ReducedFileProvenanceReportRecord rfp = new ReducedFileProvenanceReportRecord();
        boolean skip = false;
        for (FileProvenanceReportRecord f : fs) {
            rfp.studyTitle.add(f.getStudyTitle());
            rfp.studyAttributes.putAll(Multimaps.forMap(f.getStudyAttributes()));
            rfp.experimentName.add(f.getExperimentName());
            rfp.experimentAttributes.putAll(Multimaps.forMap(f.getExperimentAttributes()));
            rfp.sampleName.add(f.getSampleName());
            rfp.sampleAttributes.putAll(Multimaps.forMap(f.getSampleAttributes()));
            rfp.sequencerRunName.add(f.getSequencerRunName());
            rfp.sequencerRunAttributes.putAll(Multimaps.forMap(f.getSequencerRunAttributes()));
            rfp.laneName.add(f.getLaneName());
            rfp.laneAttributes.putAll(Multimaps.forMap(f.getLaneAttributes()));
            rfp.iusTag.add(f.getIusTag());
            rfp.iusAttributes.putAll(Multimaps.forMap(f.getIusAttributes()));
            rfp.workflowName.add(f.getWorkflowName());
            rfp.processingAlgorithm.add(f.getProcessingAlgorithm());
            rfp.processingAttributes.putAll(Multimaps.forMap(f.getProcessingAttributes()));
            rfp.fileMetaType.add(f.getFileMetaType());
            rfp.fileAttributes.putAll(Multimaps.forMap(f.getFileAttributes()));
            rfp.fileHash = f.getFilePath().hashCode();

            if (Boolean.valueOf(f.getSkip())) {
                skip = true;
            }
        }
        rfp.skip = skip;

        return rfp;
    }

    public Set<String> getStudyTitle() {
        return studyTitle;
    }

    public void setStudyTitle(Set<String> studyTitle) {
        this.studyTitle = new TreeSet<>(studyTitle);
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

    public Set<String> getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(Set<String> experimentName) {
        this.experimentName = new TreeSet<>(experimentName);
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

    public Set<String> getSampleName() {
        return sampleName;
    }

    public void setSampleName(Set<String> sampleName) {
        this.sampleName = new TreeSet<>(sampleName);
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

    public Set<String> getSequencerRunName() {
        return sequencerRunName;
    }

    public void setSequencerRunName(Set<String> sequencerRunName) {
        this.sequencerRunName = new TreeSet<>(sequencerRunName);
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

    public Set<String> getLaneName() {
        return laneName;
    }

    public void setLaneName(Set<String> laneName) {
        this.laneName = new TreeSet<>(laneName);
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

    public Set<String> getIusTag() {
        return iusTag;
    }

    public void setIusTag(Set<String> iusTag) {
        this.iusTag = new TreeSet<>(iusTag);
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

    public Set<String> getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(Set<String> workflowName) {
        this.workflowName = new TreeSet<>(workflowName);
    }

    public Set<String> getProcessingAlgorithm() {
        return processingAlgorithm;
    }

    public void setProcessingAlgorithm(Set<String> processingAlgorithm) {
        this.processingAlgorithm = new TreeSet<>(processingAlgorithm);
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

    public Set<String> getFileMetaType() {
        return fileMetaType;
    }

    public void setFileMetaType(Set<String> fileMetaType) {
        this.fileMetaType = new TreeSet<>(fileMetaType);
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
