package ca.on.oicr.pde.dao.reader;

import ca.on.oicr.gsi.provenance.model.FileProvenance;
import ca.on.oicr.pde.model.FileProvenanceReportRecord;
import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * An interface to a seqware service that uses basic Seqware webservice based reports to build a local in-memory Seqware metadata
 * representation.
 * <p>
 * Currently the service makes use of the Seqware
 * <a href="http://seqware.github.io/docs/webservice-api/metadata/report/fileprovenance_get/">file provenance repoort</a> and the
 * <a href="http://seqware.github.io/docs/webservice-api/metadata/report/workflowrun_get/">workflow run report</a>.
 */
public class FileProvenanceClient {

    /**
     * The local file provenance report records.
     *
     * @see FileProvenanceReportRecord
     */
    protected List<FileProvenance> fileProvenanceReportRecords;

    /**
     * The lookup map of file provenance report key(s) to file provenance report record object.
     */
    protected final Multimap<Integer, FileProvenance> accessionToFileProvenanceReportRecords;

//    /**
//     * The lookup map for Workflow object to workflow run report record objects.
//     *
//     * @see WorkflowRunReportRecord
//     */
//    protected final Map<Workflow, List<WorkflowRunReportRecord>> workflowToWorkflowRunReportRecords;
    /**
     * Setup common
     *
     * @param fileProvenanceReportRecords list of records to use for client operations
     */
    public FileProvenanceClient(List<FileProvenance> fileProvenanceReportRecords) {
        this.fileProvenanceReportRecords = fileProvenanceReportRecords;
        accessionToFileProvenanceReportRecords = LinkedListMultimap.create();

        for (FileProvenance f : fileProvenanceReportRecords) {
            List<Integer> swids = Arrays.asList(f.getFileSWID(), f.getProcessingSWID(), f.getWorkflowSWID(), f.getWorkflowRunSWID());
            for (Integer swid : swids) {
                if (swid != null) {
                    accessionToFileProvenanceReportRecords.put(swid, f);
                }
            }
        }
    }

    /**
     * Get a list ReducedFileProvenanceReportRecord (file records) from a collection of accessions.
     * <p>
     * Two notable operations occur in this method:
     * <ol>
     * <li>All input accessions that uniquely (determined by file accession) reference a file will be converted directly into a
     * ReducedFileProvenanceReportRecord.
     * <li>Any input accessions that reference the same file (determined by file accession) will be merged into one
     * ReducedFileProvenanceReportRecord.
     * </ol>
     * <p>
     * For example:
     * <pre>
     * Case 1:
     * {@code
     * Accession 1 --> File1 --> ReducedFile1
     * Accession 2 --> File2 --> ReducedFile2
     * }
     *
     * Case 2:
     * {@code
     * Accession 3 --> File3 \
     * Accession 4 --> File3 --> ReducedFile3
     * Accession 5 --> File3 /
     * }
     * </pre>
     *
     * @param swids list of SWIDs to get related records for
     *
     * @return A list of ReducedFileProvenanceReportRecord
     */
    public List<ReducedFileProvenanceReportRecord> getFiles(List<Integer> swids) {

        List<FileProvenance> filesToBeProcessed = new ArrayList<>();
        for (Integer swid : swids) {
            filesToBeProcessed.addAll(accessionToFileProvenanceReportRecords.get(swid));
        }

        return convert(filesToBeProcessed);
    }

    public List<ReducedFileProvenanceReportRecord> getAllFiles() {
        return convert(fileProvenanceReportRecords);
    }

    public Collection<FileProvenance> getFileRecords(Integer accession) {
        return accessionToFileProvenanceReportRecords.get(accession);
    }

    private List<ReducedFileProvenanceReportRecord> convert(List<FileProvenance> records) {
        //Parition file provenance report records by file swid
        ImmutableListMultimap<Integer, FileProvenance> filesMap = Multimaps.index(records, new Function<FileProvenance, Integer>() {
            @Override
            public Integer apply(FileProvenance f) {
                return f.getFileSWID();
            }
        });

        //Convert set of FileProvenanceReportRecord to ReducedFileProvenanceReportRecord
        //If the accession only has one file, transform it into one ReducedFileProvenanceReportRecord
        //If the accession has multiple files, merge them all into one ReducedFileProvenanceReportRecord
        List<ReducedFileProvenanceReportRecord> files = new ArrayList<>();
        for (Entry<Integer, Collection<FileProvenance>> e : filesMap.asMap().entrySet()) {
            files.add(new ReducedFileProvenanceReportRecord(e.getValue()));
        }

        return files;
    }

    /**
     * Get all studies that have some relation to the list of accessions provided.
     *
     * @param swids list of seqware accessions
     *
     * @return A set of study names
     */
    public Set<String> getStudy(List<Integer> swids) {
        Set<String> studies = new HashSet<>();
        for (Integer swid : swids) {
            for (FileProvenance fpr : accessionToFileProvenanceReportRecords.get(swid)) {
                if (fpr.getStudyTitles() != null) {
                    studies.addAll(fpr.getStudyTitles());
                }
            }
        }
        return studies;
    }

    /**
     * Get all sequencer runs that have some relation to the list of accessions provided.
     *
     * @param swids list of seqware accessions
     *
     * @return A set of sequencer run names
     */
    public Set<String> getSequencerRuns(List<Integer> swids) {
        Set<String> sequencerRuns = new HashSet<>();
        for (Integer swid : swids) {
            for (FileProvenance fpr : accessionToFileProvenanceReportRecords.get(swid)) {
                if (fpr.getSequencerRunNames() != null) {
                    sequencerRuns.addAll(fpr.getSequencerRunNames());
                }
            }
        }
        return sequencerRuns;
    }

    /**
     * Get all lanes that have some relation to the list of accessions provided.
     *
     * @param swids list of seqware accessions
     *
     * @return A set of lanes names
     */
    public Set<String> getLanes(List<Integer> swids) {
        Set<String> lanes = new HashSet<>();
        for (Integer swid : swids) {
            for (FileProvenance fpr : accessionToFileProvenanceReportRecords.get(swid)) {
                if (fpr.getLaneNames() != null) {
                    lanes.addAll(fpr.getLaneNames());
                }
            }
        }
        return lanes;
    }

    /**
     * Get all samples that have some relation to the list of accessions provided.
     *
     * @param swids list of seqware accessions
     *
     * @return A set of sample names
     */
    public Set<String> getSamples(List<Integer> swids) {
        Set<String> samples = new HashSet<>();
        for (Integer swid : swids) {
            for (FileProvenance fpr : accessionToFileProvenanceReportRecords.get(swid)) {
                if (fpr.getSampleNames() != null) {
                    samples.addAll(fpr.getSampleNames());
                }
            }
        }
        return samples;
    }

    /**
     * Get all processing algorithms that have some relation to the list of accessions provided.
     *
     * @param swids list of seqware accessions
     *
     * @return A set of processing algorithm names
     */
    public Set<String> getProcessingAlgorithms(List<Integer> swids) {
        Set<String> processingAlgorithms = new HashSet<>();
        for (Integer swid : swids) {
            for (FileProvenance fpr : accessionToFileProvenanceReportRecords.get(swid)) {
                if (fpr.getProcessingAlgorithm() != null) {
                    processingAlgorithms.add(fpr.getProcessingAlgorithm());
                }
            }
        }
        return processingAlgorithms;
    }

    /**
     * Get all file metatypes that have some relation to the list of accessions provided.
     *
     * @param swids list of seqware accessions
     *
     * @return A list of file metatype names
     */
    public Set<String> getFileMetaTypes(List<Integer> swids) {
        Set<String> fileTypes = new HashSet<>();
        for (Integer swid : swids) {
            for (FileProvenance fpr : accessionToFileProvenanceReportRecords.get(swid)) {
                if (fpr.getFileMetaType() != null) {
                    fileTypes.add(fpr.getFileMetaType());
                }
            }
        }
        return fileTypes;
    }

    /**
     * Get all workflows that have some relation to the list of accessions provided.
     *
     * @param swids list of seqware accessions
     *
     * @return A set of workflow names
     */
    public Set<String> getWorkflows(List<Integer> swids) {
        Set<String> workflows = new HashSet<>();
        for (Integer swid : swids) {
            for (FileProvenance fpr : accessionToFileProvenanceReportRecords.get(swid)) {
                if (fpr.getWorkflowName() != null) {
                    workflows.add(fpr.getWorkflowName());
                }
            }
        }
        return workflows;
    }

}
