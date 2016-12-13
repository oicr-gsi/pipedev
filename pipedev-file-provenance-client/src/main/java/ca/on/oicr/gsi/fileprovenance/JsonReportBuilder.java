package ca.on.oicr.gsi.fileprovenance;

import ca.on.oicr.gsi.provenance.model.FileProvenance;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;

/**
 *
 * @author mlaszloffy
 */
public class JsonReportBuilder implements ReportBuilder {

    @Override
    public void writeReport(Collection<FileProvenance> fps, Path outputFilePath) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JodaModule());
        om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        om.configure(SerializationFeature.INDENT_OUTPUT, true);
        try (BufferedWriter fw = Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
            om.writeValue(fw, fps);
        }
    }

}
