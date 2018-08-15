package ca.on.oicr.pde.deciders.readgroups;

import ca.on.oicr.pde.deciders.FileAttributes;
import com.google.auto.service.AutoService;

/**
 *
 * @author mlaszloffy
 */
@AutoService(ReadGroupsGeneratorBuilder.class)
public class NewReadGroupsGeneratorBuilder extends ReadGroupsGeneratorBuilder {

    public NewReadGroupsGeneratorBuilder() {
        configuration.put("new-arg", "default");
    }

    @Override
    public ReadGroupsGenerator build() {
        return new NewReadGroupsGenerator();
    }

    @Override
    public String getName() {
        return "NewReadGroupsGenerator";
    }

    public class NewReadGroupsGenerator implements ReadGroupsGenerator {

        @Override
        public ReadGroups getReadGroups(FileAttributes fileAttributes) {
            return new NewReadGroups(fileAttributes);
        }
    }

    public class NewReadGroups implements ReadGroups {

        public NewReadGroups(FileAttributes fa) {
            //
        }

        @Override
        public String getLibraryReadGroup() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getPlatformReadGroup() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getSampleReadGroup() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getPlatformUnitReadGroup() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getId() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
