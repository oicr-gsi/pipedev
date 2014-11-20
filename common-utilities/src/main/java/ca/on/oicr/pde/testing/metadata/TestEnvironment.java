package ca.on.oicr.pde.testing.metadata;

import ca.on.oicr.pde.dao.writer.SeqwareWebservice;
import ca.on.oicr.pde.dao.writer.SeqwareWriteService;
import ca.on.oicr.pde.dao.reader.SeqwareReadService;
import ca.on.oicr.pde.dao.reader.SeqwareWebserviceImpl;
import ca.on.oicr.pde.dao.executor.PluginExecutor;
import ca.on.oicr.pde.dao.executor.SeqwareExecutor;
import static com.google.common.base.Preconditions.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Map;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import org.apache.commons.configuration.ConfigurationException;

/**
 *
 * @author mlaszloffy
 */
public class TestEnvironment {

    private final SeqwareTestDatabase db;
    private final SeqwareTestWebservice ws;
    private final SeqwareTestConfiguration sc;
    private final SeqwareWriteService writeService;
    private final SeqwareReadService readService;
    private final SeqwareExecutor execService;

    private final boolean keepDatabase;

    public TestEnvironment(String host, String port, String user, String password, File seqwareWar) {
        this(host, port, user, password, false, seqwareWar);
    }

    public TestEnvironment(String host, String port, String user, String password, boolean keepDatabase, File seqwareWar) {
        checkNotNull(host);
        checkNotNull(port);
        checkNotNull(user);
        checkNotNull(password);
        checkNotNull(seqwareWar);

        this.keepDatabase = keepDatabase;

        try {
            db = new SeqwareTestDatabase(host, Integer.parseInt(port), user, password);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        try {
            ws = new SeqwareTestWebservice(db, seqwareWar);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch (ServletException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalStateException ex) {
            throw new RuntimeException(ex);
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ConfigurationException ex) {
            throw new RuntimeException(ex);
        }

        try {
            sc = new SeqwareTestConfiguration(ws);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        writeService = new SeqwareWebservice(sc.getSeqwareConfig());

        try {
            readService = new SeqwareWebserviceImpl("http://" + ws.getHost() + ":" + ws.getPort() + "/", ws.getUser(), ws.getPassword());
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }

        execService = new PluginExecutor(sc.getSeqwareConfig());

    }

    public SeqwareWriteService getWriteService() {
        return writeService;
    }

    public SeqwareReadService getReadService() {
        return readService;
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

    public void shutdown() {
        ws.shutdown();
        if (!keepDatabase) {
            db.shutdown();
        }
    }

}
