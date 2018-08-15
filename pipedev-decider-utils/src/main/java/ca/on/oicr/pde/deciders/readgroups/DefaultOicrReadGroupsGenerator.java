package ca.on.oicr.pde.deciders.readgroups;

import ca.on.oicr.pde.deciders.FileAttributes;
import com.google.auto.service.AutoService;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author mlaszloffy
 */
public class DefaultOicrReadGroupsGenerator implements ReadGroupsGenerator {

    protected final Map<String, String> modelToPlatformMap;

    public DefaultOicrReadGroupsGenerator(Map<String, String> modelToPlatformMap) {
        this.modelToPlatformMap = modelToPlatformMap;
    }

    @Override
    public ReadGroups getReadGroups(FileAttributes fileAttributes) {
        return new DefaultOicrReadGroups(fileAttributes, modelToPlatformMap);
    }

    @AutoService(ReadGroupsGeneratorBuilder.class)
    public static class Builder extends ReadGroupsGeneratorBuilder {

        //configuration
        private static final String MODEL_TO_PLATFORM = "model-to-platform-map";

        public Builder() {
            configuration.put(MODEL_TO_PLATFORM, "");
        }

        public Map<String, String> getModelToPlatformMap() {
            Map<String, String> modelToPlatformMap = new HashMap<>();
            try {
                //populate model to platform map with known sequencer run models and their corresponding platform (@RG PL)
                //GATK expects one of: ILLUMINA,SLX,SOLEXA,SOLID,454,LS454,COMPLETE,PACBIO,IONTORRENT,CAPILLARY,HELICOS,UNKNOWN
                CSVParser csvFileParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new FileReader(new File(configuration.get(MODEL_TO_PLATFORM))));
                for (CSVRecord record : csvFileParser.getRecords()) {
                    if (modelToPlatformMap.put(record.get("model"), record.get("platform")) != null) {
                        throw new RuntimeException("Duplicate model detected in model to platform map file");
                    }
                }
            } catch (IOException ie) {
                throw new RuntimeException(ie);
            }
            return modelToPlatformMap;
        }

        @Override
        public ReadGroupsGenerator build() {
            return new DefaultOicrReadGroupsGenerator(getModelToPlatformMap());
        }

        @Override
        public String getName() {
            return "DefaultOicrReadGroupsGenerator";
        }

    }

}
