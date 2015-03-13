package ca.on.oicr.pde.testing.metadata;

import ca.on.oicr.pde.utilities.Helpers;
import static com.google.common.base.Preconditions.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * @author mlaszloffy
 */
public class SeqwareTestDatabase {

    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String databaseName;

    public SeqwareTestDatabase(String host, int port, String user) throws ClassNotFoundException, SQLException, IOException {
        this(host, port, user, Helpers.getPgpassPassword(host, port, user));
    }

    public SeqwareTestDatabase(String host, int port, String user, String password) throws ClassNotFoundException, SQLException, IOException {
        checkNotNull(host, "The database host can not be null.");
        checkArgument(port > 0 && port < 65535, "The database port must be valid.");
        checkNotNull(user, "The database user can not be null.");
        checkNotNull(password, "The database password can not be null.");

        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.databaseName = "testing" + "_" + DateTime.now().toString(DateTimeFormat.forPattern("MMdd_HHmmss")) + "_" + RandomStringUtils.randomAlphanumeric(6).toLowerCase();

        //load the postgres driver
        Class.forName("org.postgresql.Driver");

        //create the test database
        try (Connection postgresConnection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/" + "postgres", user, password)) {
            postgresConnection.createStatement().execute("create database " + databaseName + ";");
        }

        //populate the test database
        try (Connection dbConnection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/" + databaseName, user, password);
                Statement db = dbConnection.createStatement()) {
            db.execute(Helpers.getStringFromResource("/io/seqware/metadb/util/seqware_meta_db.sql").replaceAll("OWNER TO seqware;", "OWNER TO " + this.user + ";"));
            db.execute(Helpers.getStringFromResource("/io/seqware/metadb/util/seqware_meta_db_data.sql"));
        }
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

    public String getDatabaseName() {
        return databaseName;
    }

    public void shutdown() {
        //drop the database
        try (Connection postgresConnection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/" + "postgres", user, password)) {
            postgresConnection.createStatement().execute("drop database " + databaseName + ";");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
