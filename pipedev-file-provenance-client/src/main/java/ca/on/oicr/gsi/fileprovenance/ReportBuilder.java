package ca.on.oicr.gsi.fileprovenance;

import ca.on.oicr.gsi.provenance.model.FileProvenance;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 *
 * @author mlaszloffy
 */
public interface ReportBuilder {

    public void writeReport(Collection<FileProvenance> fps, Path outputFilePath) throws IOException;
}
