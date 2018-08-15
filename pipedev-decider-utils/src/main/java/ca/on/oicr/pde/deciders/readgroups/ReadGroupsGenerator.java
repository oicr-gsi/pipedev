package ca.on.oicr.pde.deciders.readgroups;

import ca.on.oicr.pde.deciders.FileAttributes;

/**
 * Generates ReadGroup objects from various inputs.
 * 
 * @author mlaszloffy
 */
public interface ReadGroupsGenerator {

    public ReadGroups getReadGroups(FileAttributes fileAttributes);

}
