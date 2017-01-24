package ca.on.oicr.pde.testing.metadata;

import ca.on.oicr.pde.client.MetadataBackedSeqwareLimsClient;
import ca.on.oicr.pde.client.MetadataBackedSeqwareClient;
import ca.on.oicr.pde.dao.executor.PluginExecutor;
import ca.on.oicr.pde.dao.executor.SeqwareExecutor;
import static com.google.common.base.Preconditions.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.metadata.MetadataFactory;
import org.apache.commons.configuration.ConfigurationException;
import ca.on.oicr.pde.client.SeqwareClient;
import ca.on.oicr.pde.client.SeqwareLimsClient;

/**
 *
 * @author mlaszloffy
 */
public class SeqwareTestEnvironment {

    private final SeqwareTestDatabase db;
    private final SeqwareTestWebservice ws;
    private final SeqwareTestConfiguration sc;
    private final SeqwareClient seqwareClient;
    private final SeqwareLimsClient seqwareLimsClient;
    private final SeqwareExecutor execService;
    private final Metadata metadata;

    private final boolean keepDatabase;

    public SeqwareTestEnvironment(String host, String port, String user, String password, File seqwareWar) {
        this(host, port, user, password, false, seqwareWar);
    }

    public SeqwareTestEnvironment(String host, String port, String user, String password, boolean keepDatabase, File seqwareWar) {
        checkNotNull(host);
        checkNotNull(port);
        checkNotNull(user);
        checkNotNull(seqwareWar);

        this.keepDatabase = keepDatabase;

        try {
            if (password == null || password.isEmpty()) {
                //get password from pgpass
                db = new SeqwareTestDatabase(host, Integer.parseInt(port), user);
            } else {
                db = new SeqwareTestDatabase(host, Integer.parseInt(port), user, password);
            }
        } catch (ClassNotFoundException | SQLException | IOException ex) {
            throw new RuntimeException(ex);
        }

        try {
            ws = new SeqwareTestWebservice(db, seqwareWar);
        } catch (ClassNotFoundException | SQLException | InterruptedException | ServletException | IllegalStateException | NamingException | IOException | ConfigurationException ex) {
            throw new RuntimeException(ex);
        }

        try {
            sc = new SeqwareTestConfiguration(ws);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        Map<String, String> config = sc.getSeqwareConfig();
        metadata = MetadataFactory.getWS(sc.getSeqwareConfig());

        seqwareClient = new MetadataBackedSeqwareClient(metadata, config);
        seqwareLimsClient = new MetadataBackedSeqwareLimsClient(metadata, config);
        execService = new PluginExecutor(config);
    }

    public SeqwareClient getSeqwareClient() {
        return seqwareClient;
    }

    public SeqwareLimsClient getSeqwareLimsClient() {
        return seqwareLimsClient;
    }

    public SeqwareExecutor getSeqwareExecutor() {
        return execService;
    }

    public Map<String, String> getSeqwareConfig() {
        return sc.getSeqwareConfig();
    }

    public File getSeqwareSettings() {
        return sc.getSeqwareSettings();
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void shutdown() {
        ws.shutdown();
        if (!keepDatabase) {
            db.shutdown();
        }
    }

}
