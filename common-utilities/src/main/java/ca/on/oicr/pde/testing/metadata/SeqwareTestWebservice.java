package ca.on.oicr.pde.testing.metadata;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.RandomUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

/**
 *
 * @author mlaszloffy
 */
public class SeqwareTestWebservice {

    private final String host = "localhost";
    private final int port;
    private final String user = "admin@admin.com";
    private final String password = "admin";

    private final Server webservice;
    private final PGPoolingDataSource pg;
    private final WebAppContext appContext;

    public SeqwareTestWebservice(SeqwareTestDatabase db) throws ClassNotFoundException, SQLException, InterruptedException, 
            ServletException, MalformedURLException, IllegalStateException, NamingException, IOException, ConfigurationException {
        this(db.getHost(), db.getPort(), db.getUser(), db.getPassword(), db.getDatabaseName());
    }

    public SeqwareTestWebservice(String dbHost, int dbPort, String dbUser, String dbPassword, String dbName) throws SQLException, 
            InterruptedException, ServletException, MalformedURLException, IllegalStateException, NamingException, IOException, ConfigurationException {

        port = RandomUtils.nextInt(2000, 65000);

        pg = new PGPoolingDataSource();
        pg.setDatabaseName(dbName);
        pg.setUser(dbUser);
        pg.setPassword(dbPassword);
        pg.setServerName(dbHost);
        pg.setPortNumber(dbPort);
        pg.setMaxConnections(10);
        
        Configuration config = new PropertiesConfiguration("config.properties");

        appContext = new WebAppContext(config.getString("seqwareWar"), "/");

        webservice = new Server(port);
        webservice.setHandler(appContext);

//        ResourceHandler resourceHandler = new ResourceHandler();
//        resourceHandler.("jdbc/SeqWareMetaDB", pg);
//        //https://blogs.oracle.com/randystuph/entry/injecting_jndi_datasources_for_junit
//        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
//                "org.apache.naming.java.javaURLContextFactory");
//        System.setProperty(Context.URL_PKG_PREFIXES,
//                "org.apache.naming");
//
//        InitialContext c = new InitialContext();
//        c.createSubcontext("java:").createSubcontext("java:comp").createSubcontext("java:comp/env").createSubcontext("java:comp/env/jdbc");
//        c.bind("java:comp/env/jdbc/SeqWareMetaDB", pg);
//        //c.addToEnvironment("java:comp/env/jdbc/SeqWareMetaDB", pg);
        SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
        builder.bind("java:comp/env/jdbc/SeqWareMetaDB", pg);
        builder.activate();

        try {
            webservice.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        try {
            webservice.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            appContext.stop();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        pg.close();

    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

}
