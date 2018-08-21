package ca.on.oicr.pde.deciders.readgroups;

import ca.on.oicr.pde.deciders.FileAttributes;
import ca.on.oicr.pde.deciders.Lims;
import java.util.Map;
import net.sourceforge.seqware.common.hibernate.FindAllTheFiles;

/**
 *
 * @author mlaszloffy
 */
class DefaultOicrReadGroups implements ReadGroups {

    protected final FileAttributes fileAttributes;
    protected final Map<String, String> modelToPlatformMap;

    public DefaultOicrReadGroups(FileAttributes fileAttributes, Map<String, String> modelToPlatformMap) {
        this.fileAttributes = fileAttributes;
        this.modelToPlatformMap = modelToPlatformMap;
    }

    @Override
    public String getLibraryReadGroup() {
        return fileAttributes.getLibrarySample();
    }

    @Override
    public String getPlatformReadGroup() {
        String sequencerRunPlatformName = fileAttributes.getOtherAttribute("Sequencer Run Platform Name");
        if (modelToPlatformMap.containsKey(sequencerRunPlatformName)) {
            return modelToPlatformMap.get(sequencerRunPlatformName);
        } else {
            throw new InvalidDataException("Sequencer run model = [" + sequencerRunPlatformName + "] platform is missing");
        }
    }

    @Override
    public String getSampleReadGroup() {
        String groupId = fileAttributes.getLimsValue(Lims.GROUP_ID);
        StringBuilder sb = new StringBuilder().append(fileAttributes.getDonor()).append("_").append(fileAttributes.getLimsValue(Lims.TISSUE_ORIGIN)).append("_").append(fileAttributes.getLimsValue(Lims.TISSUE_TYPE));
        if (groupId != null && !groupId.isEmpty()) {
            sb.append("_").append(groupId);
        }
        return sb.toString();
    }

    @Override
    public String getPlatformUnitReadGroup() {
        String runName = fileAttributes.getOtherAttribute(FindAllTheFiles.Header.SEQUENCER_RUN_NAME);
        String lane = fileAttributes.getOtherAttribute(FindAllTheFiles.Header.LANE_NUM);
        String barcode = fileAttributes.getBarcode();
        return runName + "-" + barcode + "_" + lane;
    }

    @Override
    public String getId() {
        return getPlatformUnitReadGroup();
    }

}
