package ca.on.oicr.pde.deciders.readgroups;

import ca.on.oicr.pde.deciders.FileAttributes;
import ca.on.oicr.pde.deciders.Lims;
import java.util.Map;

/**
 *
 * @author mlaszloffy
 */
public class TglReadGroups extends DefaultOicrReadGroups {

    public TglReadGroups(FileAttributes fileAttributes, Map<String, String> modelToPlatform) {
        super(fileAttributes, modelToPlatform);
    }

    @Override
    public String getSampleReadGroup() {
        String groupId = fileAttributes.getLimsValue(Lims.GROUP_ID);
        if (groupId == null || groupId.isEmpty()) {
            throw new InvalidDataException("Group ID is required for sample read group");
        }
        return groupId;
    }

}
