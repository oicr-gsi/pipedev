package ca.on.oicr.pde.testing.metadata;

import ca.on.oicr.pde.testing.RunTestSettings;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.sourceforge.seqware.common.util.maptools.MapTools;

/**
 *
 * @author mlaszloffy
 */
public class SeqwareTestConfiguration {

    private final Map seqwareConfig;
    private final File seqwareSettings;

    //TODO: add option to set oozie env
    public SeqwareTestConfiguration(SeqwareTestWebservice ws) throws IOException {
        RunTestSettings t = new RunTestSettings(Files.createTempDir().toPath(),
                new URL("http://" + ws.getHost() + ":" + Integer.toString(ws.getPort())), ws.getUser(), ws.getPassword(),
                "localhost", "oozie", UUID.randomUUID().toString());
        t.createOnDisk();
        seqwareSettings = t.getSettingsFilePath().toFile();

        Map tmpConfig = new HashMap<>();
        MapTools.ini2Map(seqwareSettings.toString(), tmpConfig);
        seqwareConfig = Collections.unmodifiableMap(tmpConfig);
    }

    public Map getSeqwareConfig() {
        return seqwareConfig;
    }

    public File getSeqwareSettings() {
        return seqwareSettings;
    }

}
