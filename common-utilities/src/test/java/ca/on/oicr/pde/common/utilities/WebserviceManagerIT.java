package ca.on.oicr.pde.common.utilities;

import ca.on.oicr.pde.common.utilities.WebserviceManager;
import ca.on.oicr.pde.common.utilities.CommandRunner;
import java.io.IOException;
import org.apache.commons.exec.CommandLine;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * The test cases for webservice manager The following test cases are included: creating a service then destroying it creating multiple webservices destroying
 * multiple webservices attempting to destroy a webservice that doesn't exist ensuring that proper procedure is carried out incase the engine decides to cancel
 * the job
 *
 * @author Raunaq Suri .
 */
public class WebserviceManagerIT {

    /**
     * The variables used. Make sure that they change if you are unable to access some of them
     */
    String dbdump = getRequiredSystemProperty("dbFile");
    String patchDir = getRequiredSystemProperty("dbPatchDir");
    String host = getRequiredSystemProperty("dbHost");
    String port = getRequiredSystemProperty("dbPort");
    String user = getRequiredSystemProperty("dbUser");
    String pass = getRequiredSystemProperty("dbPass");
    String seqDir = getRequiredSystemProperty("seqwareWebserviceDir");
    WebserviceManager service1 = new WebserviceManager(dbdump, patchDir, host, port, user, pass, seqDir);
    WebserviceManager service2 = new WebserviceManager(dbdump, patchDir, host, port, user, pass, seqDir);
    WebserviceManager service3 = new WebserviceManager(dbdump, patchDir, host, port, user, pass, seqDir);
    WebserviceManager service4 = new WebserviceManager(dbdump, patchDir, host, port, user, pass, seqDir);
    WebserviceManager service5 = new WebserviceManager(dbdump, patchDir, host, port, user, pass, seqDir);

    /**
     * Creates two webservices with random db names
     *
     * @throws IOException
     */
    @Test
    public void testCreationMinParams() throws IOException {
        System.out.println("Attempting to create multiple webservices...");
        service1.setJavaHome("/oicr/local/lib/jvm/jdk1.6.0_10");
        service2.setJavaHome("/oicr/local/lib/jvm/jdk1.6.0_10");
        System.out.println("Creating the first webservice");
        service1.startService();
        String url = service1.getUrl();
        
        System.out.println("Creating the second webservice");
        service2.startService();
        String url2 = service2.getUrl();
        
        System.out.println("This is the url of the first webservice: " + url);
        System.out.println("This is the url of the second webservice: " + url2);
        Assert.assertTrue(url.contains("/seqware-webservice"), "Error starting up webservice. This is the url given:" + url);
    }

    /**
     * Creates a webservice, then attempts to destroy it
     *
     * @throws IOException
     */
    @Test
    public void testCreateDestroyService() throws IOException {
        System.out.println("Attempting to create, then destroy, a webservice...");
        service3.setJavaHome("/oicr/local/lib/jvm/jdk1.6.0_10");
        service3.startService();
        String url3 = service3.getUrl();
        System.out.println("Created a webservice with url " + url3);

        service3.destroy();
        System.out.println("Destroyed the webservice");

    }

    /**
     * Destroys multiple webservices
     *
     * @throws IOException
     */
    @Test
    public void testDestroyServices() throws IOException {
        System.out.println("Attempting to destroy multiple webservices... ");
        service1.destroy();
        System.out.println("First service destroyed");
        service2.destroy();
        System.out.println("Second service destroyed");
    }

    /**
     * Attempts to destroy a service which doesn't exist
     *
     * @throws IOException
     */
    @Test(expectedExceptions = IOException.class)
    public void testNoServiceYetDestroy() throws IOException {
        service4.setJavaHome("/oicr/local/lib/jvm/jdk1.6.0_10");
        System.out.println("Attempting to destroy a webservice which doesn't exist...");
        service4.destroy();
        Assert.fail("Exception was not thrown");

    }

    /**
     * Simulates what occurs when the engine decides to cancel the job
     *
     * @throws IOException
     */
    @Test
    public void testKilledJob() throws IOException {
        System.out.println("Attempting to see what would happen if the Engine kills the webservice...");
        service5.setJavaHome("/oicr/local/lib/jvm/jdk1.6.0_10");
        service5.startService();
        String url5 = service5.getUrl();
        System.out.println("Webservice was killed by the engine");

        //Deletes the job beforehand so only the database is left
        //sets up command to destroy webservice
        StringBuilder destroyWeb = new StringBuilder();
        destroyWeb.append("qdel");
        destroyWeb.append(" ").append(service5.jobID);

        CommandLine command = new CommandLine("/bin/bash");
        command.addArgument("-c");
        command.addArgument(destroyWeb.toString(), false);

        //runs command
        CommandRunner goodbyeWeb = new CommandRunner();
        goodbyeWeb.setCommand(command);

        CommandRunner.CommandResult r = goodbyeWeb.runCommand();
        //Assert.assertTrue(r.getExitCode()==0);
        //Now the job has been deleted, so I can attempt to destroy it
        System.out.println("Attempting to delete the webservice when the engine had already deleted it");
        service5.destroy(); //There is no webservice to be deleted, but the stray db shuold be deleted as well

    }

    public static String getRequiredSystemProperty(String key) throws IllegalArgumentException {

        String value = System.getProperty(key);

        if (value == null) {
            throw new IllegalArgumentException("Required system property [" + key + "] is not set.");
        }

        return value;

    }
}
