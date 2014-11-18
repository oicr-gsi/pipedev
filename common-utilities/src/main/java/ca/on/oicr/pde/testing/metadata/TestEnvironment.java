package ca.on.oicr.pde.testing.metadata;

import ca.on.oicr.pde.dao.writer.SeqwareWebservice;
import ca.on.oicr.pde.dao.writer.SeqwareWriteService;
import ca.on.oicr.pde.dao.reader.SeqwareReadService;
import ca.on.oicr.pde.dao.reader.SeqwareWebserviceImpl;
import ca.on.oicr.pde.dao.executor.PluginExecutor;
import ca.on.oicr.pde.dao.executor.SeqwareExecutor;
import java.io.File;
import java.io.IOException;
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

    public TestEnvironment(String host, String port, String user, String password) {
        this(host, port, user, password, false);
    }

    public TestEnvironment(String host, String port, String user, String password, boolean keepDatabase) {
        this.keepDatabase = keepDatabase;
        try {
            db = new SeqwareTestDatabase(host, Integer.parseInt(port), user, password);
            ws = new SeqwareTestWebservice(db);
            sc = new SeqwareTestConfiguration(ws);
            writeService = new SeqwareWebservice(sc.getSeqwareConfig());
            readService = new SeqwareWebserviceImpl("http://" + ws.getHost() + ":" + ws.getPort() + "/", ws.getUser(), ws.getPassword());
            execService = new PluginExecutor(sc.getSeqwareConfig());
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
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
