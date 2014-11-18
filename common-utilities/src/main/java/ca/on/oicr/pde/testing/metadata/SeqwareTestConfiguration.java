package ca.on.oicr.pde.testing.metadata;

import ca.on.oicr.pde.utilities.Helpers;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.seqware.common.util.maptools.MapTools;

/**
 *
 * @author mlaszloffy
 */
public class SeqwareTestConfiguration {

    private final Map seqwareConfig;
    private final File seqwareSettings;

    //TODO: add option to set oozie env
    //TODO: refactor helpers.generateSeqwareSettings
    public SeqwareTestConfiguration(SeqwareTestWebservice ws) throws IOException {

        seqwareSettings = Helpers.generateSeqwareSettings(Files.createTempDir(), "http://" + ws.getHost() + ":" + Integer.toString(ws.getPort()), "oozie", "nah");

        Map tmpConfig = new HashMap<String, String>();
        MapTools.ini2Map(seqwareSettings.getAbsolutePath(), tmpConfig);
        seqwareConfig = Collections.unmodifiableMap(tmpConfig);
    }

    public Map getSeqwareConfig() {
        return seqwareConfig;
    }

    public File getSeqwareSettings() {
        return seqwareSettings;
    }

}
