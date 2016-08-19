package ca.on.oicr.pde.deciders;

import net.sourceforge.seqware.common.hibernate.FindAllTheFiles.Header;

/**
 *
 * @author mtaschuk
 */
public enum Group {
    STUDY(Header.STUDY_SWA, Header.STUDY_TITLE),
    EXPERIMENT(Header.EXPERIMENT_SWA, Header.EXPERIMENT_NAME),
    DONOR(Header.PARENT_SAMPLE_SWA, Header.PARENT_SAMPLE_NAME),
    ROOT_SAMPLE_NAME(Header.ROOT_SAMPLE_NAME, Header.ROOT_SAMPLE_NAME),
    LIBRARY(Header.SAMPLE_SWA, Header.SAMPLE_NAME),
    BARCODE(Header.IUS_SWA, Header.IUS_TAG),
    LANE(Header.LANE_SWA, Header.LANE_NUM),
    SEQUENCER_RUN(Header.SEQUENCER_RUN_SWA, Header.SEQUENCER_RUN_NAME),
    FILE(Header.FILE_SWA, Header.FILE_SWA);
    
    private final Header groupByHeader;
    private final Header groupByHeaderName;
    Group(Header swa, Header name) {
        this.groupByHeader = swa;
        this.groupByHeaderName = name;
    }

    public Header getSwaHeader() {
        return groupByHeader;
    }
    
    public Header getNameHeader() {
        return groupByHeaderName;
    }
}