package ca.on.oicr.pde.tools.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 *
 * @author mlaszloffy
 */
public class Configuration {

    public static Path getToolPath(String pathToTool, String defaultToolKey, String defaultToolLocation) throws IOException {
        Path p = Paths.get(pathToTool);
        if (p.endsWith(defaultToolLocation)) {
            return p;
        } else {
            Properties props = new Properties();
            try (InputStream is = Configuration.class.getClassLoader().getResourceAsStream("tool.properties")) {
                props.load(is);
            }
            return Paths.get(p.toString(), props.getProperty(defaultToolKey), defaultToolLocation);
        }
    }
}
