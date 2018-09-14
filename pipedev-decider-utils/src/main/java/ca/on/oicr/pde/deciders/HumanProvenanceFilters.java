package ca.on.oicr.pde.deciders;

import net.sourceforge.seqware.common.model.FileProvenanceParam;

public enum HumanProvenanceFilters {
	LANE_SWID("lane-SWID", "Lane sw_accession", true, FileProvenanceParam.lane), //
	IUS_SWID("ius-SWID", "IUS sw_accession", true, FileProvenanceParam.ius), //
	STUDY_NAME("study-name", "Full study name", false, FileProvenanceParam.study), //
	SAMPLE_NAME("sample-name", "Full sample name", false, FileProvenanceParam.sample), //
	ROOT_SAMPLE_NAME("root-sample-name", "Full root sample name", false, FileProvenanceParam.root_sample), //
	SEQUENCER_RUN_NAME("sequencer-run-name", "Full sequencer run name", false, FileProvenanceParam.sequencer_run), //
	ORGANISM("organism", "organism id", true, FileProvenanceParam.organism), //
	WORKFLOW_RUN_STATUS("workflow-run-status", "Workflow run status", true, FileProvenanceParam.workflow_run_status), //
	PROCESSING_STATUS("processing-status", "Processing status", true, FileProvenanceParam.processing_status), //
	PROCESSING("processing-SWID", "processing sw_accession", true, FileProvenanceParam.processing), //
	LANE_NAME("lane-name", "Full lane name", false, FileProvenanceParam.lane);
	public final String human_str;
	public final String desc;
	public final boolean standard;
	public final FileProvenanceParam mappedParam;

	private HumanProvenanceFilters(String human_str, String desc, boolean standard, FileProvenanceParam mappedParam) {
		this.human_str = human_str;
		this.desc = desc;
		this.standard = standard;
		this.mappedParam = mappedParam;
	}

	@Override
	public String toString() {
		if (human_str == null) {
			return super.toString();
		}
		return human_str;
	}
}