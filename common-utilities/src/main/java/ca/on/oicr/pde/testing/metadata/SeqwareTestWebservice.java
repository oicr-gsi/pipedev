package ca.on.oicr.pde.testing.metadata;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.commons.lang3.RandomUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
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
    private final DataSource dataSource;
    private final WebAppContext appContext;

    public SeqwareTestWebservice(SeqwareTestDatabase db, File seqwareWar) throws ClassNotFoundException, SQLException, InterruptedException,
            ServletException, MalformedURLException, IllegalStateException, NamingException, IOException, ConfigurationException {
        this(db.getHost(), db.getPort(), db.getUser(), db.getPassword(), db.getDatabaseName(), seqwareWar);
    }

    public SeqwareTestWebservice(String dbHost, int dbPort, String dbUser, String dbPassword, String dbName, File seqwareWar) throws SQLException,
            InterruptedException, ServletException, MalformedURLException, IllegalStateException, NamingException, IOException, ConfigurationException {

        PoolProperties p = new PoolProperties();
        p.setDriverClassName("org.postgresql.Driver");
        p.setUsername(dbUser);
        p.setPassword(dbPassword);
        p.setUrl("jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName);
        p.setInitialSize(10);

        dataSource = new DataSource();
        dataSource.setPoolProperties(p);
        port = RandomUtils.nextInt(2000, 65000);

        appContext = new WebAppContext(seqwareWar.getAbsolutePath(), "/");

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
        SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
        builder.bind("java:comp/env/jdbc/SeqWareMetaDB", dataSource);

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

        dataSource.close();

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
