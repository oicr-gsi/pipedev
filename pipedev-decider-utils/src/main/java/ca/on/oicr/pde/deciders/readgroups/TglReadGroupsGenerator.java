package ca.on.oicr.pde.deciders.readgroups;

import ca.on.oicr.pde.deciders.FileAttributes;
import com.google.auto.service.AutoService;
import java.util.Map;

/**
 *
 * @author mlaszloffy
 */
public class TglReadGroupsGenerator extends DefaultOicrReadGroupsGenerator {

    public TglReadGroupsGenerator(Map<String, String> modelToPlatformMap) {
        super(modelToPlatformMap);
    }

    @Override
    public ReadGroups getReadGroups(FileAttributes fileAttributes) {
        return new TglReadGroups(fileAttributes, modelToPlatformMap);
    }

    @AutoService(ReadGroupsGeneratorBuilder.class)
    public static class Builder extends DefaultOicrReadGroupsGenerator.Builder {

        @Override
        public ReadGroupsGenerator build() {
            return new TglReadGroupsGenerator(getModelToPlatformMap());
        }

        @Override
        public String getName() {
            return "TglReadGroupsGenerator";
        }

    }

}
