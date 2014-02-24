package ca.on.oicr.pde.common.utilities;

/**
 * Allows you to restore a database from a .gz file and then apply patches to it
 * Then, it will create a seqware webservice based off of that database by running the startService() method
 * you can choose to destroy that webservice with the destroy() method
 * @author Raunaq Suri
 * @link https://github.com/pipedev/pipedev
 * 
 */
import ca.on.oicr.pde.common.utilities.CommandRunner;
import ca.on.oicr.pde.common.utilities.CommandRunner.CommandResult;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.exec.CommandLine;
import org.testng.Assert;
import org.apache.commons.lang3.RandomStringUtils;


public class WebserviceManager 
{
    private String  dbDump, patchDir, dbHost, dbPort, dbUser, dbPass, dbName, seqwareDir; //variables required
    public String jobID;
    private String javaHome = "/usr/";
    private String tomcatPort = "8181";
    private String webservice = null;
    
   /**
     *
     * @param dbDump the database you want to restore from
     * @param patchDir the directory in which the sql patches are located
     * @param dbHost the database host
     * @param dbPort the port of the host
     * @param dbUser the username
     * @param dbPass password
     * @param webserviceDir the seqware-webservice directory
     * @param dbName the name of the database
     */
    
    public WebserviceManager(String dbDump, String patchDir, String dbHost, String dbPort, String dbUser, String dbPass, String dbName, String webserviceDir )
    {
        /**allows user to choose the database name
         * 
         */
        this.dbDump = dbDump;
        this.patchDir = patchDir;
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
        this.seqwareDir = webserviceDir;
        this.dbName = dbName.toLowerCase();
    }
     /**Call the constructor with the bare minimum number of parameters
     * @param dbDump the database you want to restore from. Must be in a .gz format
     * @param patchDir the directory in which the sql patches are located
     * @param dbHost the host which the database will be on
     * @param dbPort the port of that host
     * @param dbUser the database user's name
     * @param dbPass the database user's password
     * @param webserviceDir the seqware-webservice directory
     */
    public WebserviceManager( String dbDump, String patchDir, String dbHost, String dbPort, String dbUser, String dbPass, String webserviceDir)
    {   
        //generates a random name for the database
        this(dbDump, patchDir, dbHost, dbPort, dbUser, dbPass,  "webservice_db_"+RandomStringUtils.randomAlphabetic(8).toLowerCase(), webserviceDir);
    }
    

    
    /**Sets where the home directory for java is (/bin/java)
     * 
     * @param javaHome The default is /usr/
     **/
    public void setJavaHome(String javaHome)
    {
     
        this.javaHome = javaHome;
    }
    
    /**The default tomcatPort number is 8181. Run this method if you wish to change it
     * @param tomcatPort the port number you want to run tomcat on **/
    public void setTomcatPort(String tomcatPort)
    {
    
        this.tomcatPort = tomcatPort;
    }
    /**
     * Gets the name of the database that was created
     * @return the name of the database
     */
    public String getDbName()
    {
        return dbName;
    }
    
     /**Returns the url of the webservice
     * 
     * @return The url of the webservice 
     */
    public String getUrl() {
        return webservice;
    }
    
    /** Creates a webservice with a new database created from the patches + database dump provided
     * 
     * @throws IOException 
     */

    public void startService() throws IOException
    {
        final int MAX_WAIT = 300;
        int count = 0;
        //Restores the database
        restoreDB();
        
                
        createWebservice();
        

        //Finds out the hostname
        CommandLine command = new CommandLine("/bin/bash");
        command.addArgument("-c");
        command.addArgument("qstat", false);
        CommandRunner getUrl = new CommandRunner();
        getUrl.setCommand(command);
        
        
        CommandResult u = getUrl.runCommand();
        
      //Waits until the queue is updated to running before it gets hostID. If it takes too long, then error message is shown.

        while(u.getOutput().trim().contains("qw"))
        {
           try
           {
               Thread.sleep(1000);
               count++;
           } catch (InterruptedException ex)
           {
               System.err.println(ex.getMessage());
           }
            //Refreshes command
           u = getUrl.runCommand();
           
           if(count > MAX_WAIT)
           {
               System.err.println("Timing out. Please manually delete this job.");
               break;

           }

        }
        
        webservice = "http://"+getWebserviceBaseUrl(u.getOutput().trim())+":"+tomcatPort+"/seqware-webservice/";
        
        //Continues to connect to the webservice until a code of 200 is reached or until time is up
        CommandLine curlCommand = new CommandLine("/bin/bash");
        curlCommand.addArgument("-c");
        curlCommand.addArgument("curl -k admin@admin.com:admin -s -I "+ webservice +" | grep HTTP/1.1 | awk {'print $2'}", false);
        CommandRunner getCurl = new CommandRunner();
        getCurl.setCommand(curlCommand);
        CommandResult v = getCurl.runCommand();

         for(int i = 0; i < 6; i++)
        {
           try
           {
               Thread.sleep(10000);
           } catch (InterruptedException ex)
           {
               System.err.println(ex.getMessage());
           }
            //Refreshes command
           v = getCurl.runCommand();

           //Breaks out of loop if the webservice has been created
           if(v.getOutput().trim().contains("200"))
           {
               break;
           }

        }

    }
    
    /**
     * Destroys the webservice and the database that were created
     * By  default if the destruction of the webservice fails, the database will still be deleted
     * @throws IOException 
     */
    public void destroy() throws IOException
    {
        try{
            destroyWebservice();
            
        } finally{
            destroyDB();
        }
        
        
    }
    
    /**
     * restores a database onto another server and applies the patches necessary 
     * @throws IOException 
     */
    
    private void restoreDB() throws IOException
    {
        //Adds all the parameters to the command
        StringBuilder restoreScript = new StringBuilder();
        restoreScript.append("src/main/resources/dbpatcher.sh");
        restoreScript.append(" ").append(dbDump);
        restoreScript.append(" ").append(patchDir);
        restoreScript.append(" ").append(dbHost);
        restoreScript.append(" ").append(dbPort);
        restoreScript.append(" ").append(dbName);
        restoreScript.append(" ").append(dbUser);
        restoreScript.append(" ").append(dbPass);
        String dbPatcher = restoreScript.toString();
       
        //SET WORKING DIRECTORY HERE
        File workingDirectory = new File(".");
        
       //Creates a new database and restores it
        
        CommandLine command = new CommandLine("/bin/bash");
        command.addArgument("-c");
        command.addArgument(dbPatcher, false);
       // command.addArgument("echo \"HELLO WORLD\"", false);
        CommandRunner restoreDB = new CommandRunner();
        restoreDB.setCommand(command);
        restoreDB.setWorkingDirectory(workingDirectory.getCanonicalFile());
        //Runs the command and returns any error codes
        CommandResult r = restoreDB.runCommand();
        Assert.assertTrue(r.getExitCode()==0, "The database was unable to restore due to errors." + " " + r.getOutput());
    }
    
    /**
     * Creates a webservice based on the database that was previously restored
     * @throws IOException 
     */
    
    private void createWebservice() throws IOException{      
        //calls the webservice script with sufficient params
        StringBuilder startService = new StringBuilder();
        startService.append("qsub -l h_vmem=16G -q default").append(" ");
        startService.append("src/main/resources/webservicestarter.sh");
        startService.append(" ").append(tomcatPort);
        startService.append(" ").append(dbHost);
        startService.append(" ").append(dbPort);
        startService.append(" ").append(dbName);
        startService.append(" ").append(dbUser);
        startService.append(" ").append(dbPass);
        startService.append(" ").append(seqwareDir);
        startService.append(" ").append(javaHome);
        
        //runs the command
        File workingDirectory = new File(".");
        
       //Creates a new database and restores it
        
        CommandLine command = new CommandLine("/bin/bash");
        command.addArgument("-c");
        command.addArgument(startService.toString(), false);
       // command.addArgument("echo \"HELLO WORLD\"", false);
        CommandRunner webStart = new CommandRunner();
        webStart.setCommand(command);
        webStart.setWorkingDirectory(workingDirectory.getCanonicalFile());
        //Runs the command and returns any error codes
        CommandResult r = webStart.runCommand();
        //The webservice is now started.
        //Gets the jobID

        
        jobID = r.getOutput().trim();
        if(jobID.matches("Your job [0-9]* (.*) has been submitted")){
            jobID = jobID.replaceAll("[^0-9]","");
        }

    }
    /**
     * Destroys the webservice that was created
     * @throws IOException 
     */
    private void destroyWebservice() throws IOException
    {
        //sets up command to destroy webservice
        StringBuilder destroyWeb = new StringBuilder();
        destroyWeb.append("qdel");
        destroyWeb.append(" ").append(jobID);
        
        CommandLine command = new CommandLine("/bin/bash");
        command.addArgument("-c");
        command.addArgument(destroyWeb.toString(), false);
        
        //runs command
        CommandRunner goodbyeWeb = new CommandRunner();
        goodbyeWeb.setCommand(command);
        
        CommandResult r = goodbyeWeb.runCommand();
        
        if(r.getExitCode()!= 0)
        {
            System.err.println("The job could not be deleted");
            System.err.println(r.getOutput());
            throw new IOException();
        }
        
    }
    
    /**
     * Deletes the database that was created
     * @throws IOException 
     */
    
    private void destroyDB() throws IOException
    {
         //sets up command to destroy database
        StringBuilder destroyDB = new StringBuilder();
        destroyDB.append("psql -U").append(" ");
        destroyDB.append(dbUser).append(" ");
        destroyDB.append("-h").append(" ");
        destroyDB.append(dbHost).append(" ");
        destroyDB.append("-p").append(" ");
        destroyDB.append(dbPort).append(" ");
        destroyDB.append("-d postgres -c").append(" ");
        destroyDB.append("\"DROP DATABASE ").append(dbName).append(";\"");
        
        //Runs command
        CommandLine command2 = new CommandLine("/bin/bash");
        command2.addArgument("-c");
        command2.addArgument(destroyDB.toString(), false);
        
        CommandRunner goodbyeDB = new CommandRunner();
        goodbyeDB.setEnvironmentVariable("PGPASSWORD", dbPass );
        goodbyeDB.setCommand(command2);
        
        CommandResult s = goodbyeDB.runCommand();
        if(s.getExitCode()!=0)
        {
            System.err.println("Error deleting database: "+ s.getOutput());
            throw new IOException();
        }
    }
    /**
     * gets the base url of the webservice
     * @param data the output of the qstat or other command which contains the host name somewhere within it
     * @return The hostname of the webservice
     */
    private String getWebserviceBaseUrl(String data)
    {
        String webservice = null;
        String[] lines = data.split("\n");
        
        //Gets the line in the data which contains the host's name
        for( String s : lines)
        {
            if(s.contains(jobID))
            {
                webservice = s;
            }
        }
        //checks if webservice is local host
        //if it is, then the returned url is localhost, or else it will find the node which it is on
        
        if(webservice.contains("oll-")){
            
            webservice = "localhost";
        } else{
            
            webservice = webservice.substring(webservice.indexOf('@')+1, webservice.indexOf(' ', webservice.indexOf('@') ) );
            
        }
        
        return webservice;
        
    }

       
}