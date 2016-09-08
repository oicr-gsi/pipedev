package ca.on.oicr.pde.deciders.configuration;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.seqware.common.err.NotFoundException;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author mlaszloffy
 */
public class StudyToOutputPathConfig {

    private final Map<String, String> studyToOutputPathMap;

    public StudyToOutputPathConfig(File studyToOutputPathCsv) throws IOException {
        studyToOutputPathMap = new HashMap<>();
        String csvAsString = FileUtils.readFileToString(studyToOutputPathCsv, Charsets.UTF_8);
        for (String line : csvAsString.split("\n")) {
            if (line.startsWith("#") || line.isEmpty()) {
                continue;
            }
            List<String> data = Lists.newArrayList(Splitter.on(",").split(line));
            if (data.size() != 2) {
                throw new RuntimeException("Invalid study to output path line: [" + line + "]");
            }
            if (studyToOutputPathMap.put(data.get(0), data.get(1)) != null) {
                throw new RuntimeException("Duplicate entry for study = [" + data.get(0) + "]");
            }
        }
    }

    public StudyToOutputPathConfig(String studyToOutputPathCsv) throws IOException {
        this(new File(studyToOutputPathCsv));
    }

    public StudyToOutputPathConfig(Map<String, String> studyToOutputPathMap) {
        this.studyToOutputPathMap = new HashMap<>(studyToOutputPathMap);
    }

    public String getOutputPathForStudy(String studyTitle) {
        if (studyToOutputPathMap.containsKey(studyTitle)) {
            return studyToOutputPathMap.get(studyTitle);
        } else if (studyToOutputPathMap.containsKey("*")) {
            return studyToOutputPathMap.get("*");
        } else {
            throw new NotFoundException("The study [" + studyTitle + "] was not found in the \"study to output path\" map");
        }
    }

}
