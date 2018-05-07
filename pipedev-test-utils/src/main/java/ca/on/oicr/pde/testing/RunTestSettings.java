package ca.on.oicr.pde.testing;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author mlaszloffy
 */
public class RunTestSettings {

    private final Path baseDir;
    private final Path bundleDir;
    private final Path bundleRepoDir;
    private final Path workingDir;
    private final URL webServiceUrl;
    private final String webServiceUser;
    private final String webServicePassword;
    private final String schedulerHostName;
    private final String schedulerUser;
    private final String lockId;
    private final Path settingsFilePath;

    public RunTestSettings(Path baseDir, URL webServiceUrl, String webServiceUser, String webServicePassword, String schedulerHostName, String schedulerUser, String lockId) {
        this.baseDir = baseDir;
        this.bundleDir = Paths.get(baseDir.toString(), "provisionedBundles");
        this.bundleRepoDir = Paths.get(baseDir.toString(), "bundleRepo");
        this.webServiceUrl = webServiceUrl;
        this.webServiceUser = webServiceUser;
        this.webServicePassword = webServicePassword;
        this.schedulerHostName = schedulerHostName;
        this.schedulerUser = schedulerUser;
        this.workingDir = Paths.get(baseDir.toString(), "workingDir");
        this.lockId = lockId;

        this.settingsFilePath = Paths.get(baseDir.toString(), "seqware_settings");
    }

    public String getSettingsFileAsString() {
        MessageFormat mf = new MessageFormat("SW_DEFAULT_WORKFLOW_ENGINE=oozie-sge\n"
                + "SW_METADATA_METHOD=webservice\n"
                + "SW_REST_URL={0}\n"
                + "SW_REST_USER={1}\n"
                + "SW_REST_PASS={2}\n"
                + "SW_BUNDLE_DIR={3}\n"
                + "SW_BUNDLE_REPO_DIR={4}\n"
                + "OOZIE_URL=http://{5}:11000/oozie\n"
                + "OOZIE_APP_ROOT=seqware_workflow\n"
                + "OOZIE_APP_PATH=hdfs://{5}:8020/user/{6}/\n"
                + "OOZIE_JOBTRACKER={5}:8021\n"
                + "OOZIE_NAMENODE=hdfs://{5}:8020\n"
                + "OOZIE_QUEUENAME=default\n"
                + "OOZIE_WORK_DIR={7}\n"
                + "OOZIE_SGE_MAX_MEMORY_PARAM_FORMAT=-l h_vmem=$'{maxMemory}'M\n"
                + "OOZIE_SGE_THREADS_PARAM_FORMAT=-pe smp $'{threads}'\n"
                + "HBASE.ZOOKEEPER.QUORUM={5}\n"
                + "HBASE.ZOOKEEPER.PROPERTY.CLIENTPORT=2181\n"
                + "HBASE.MASTER={5}:60000\n"
                + "MAPRED.JOB.TRACKER={5}:8021\n"
                + "FS.DEFAULTFS=hdfs://{5}:8020\n"
                + "FS.HDFS.IMPL=org.apache.hadoop.hdfs.DistributedFileSystem\n"
                + "SW_LOCK_ID={8}\n"
                + "OOZIE_BATCH_THRESHOLD=10\n"
                + "OOZIE_BATCH_SIZE=5");

        return mf.format(new Object[]{webServiceUrl.toString(), webServiceUser, webServicePassword, bundleDir.toString(), bundleRepoDir.toString(), schedulerHostName, schedulerUser, workingDir.toString(), lockId});
    }

    public void createOnDisk() throws IOException {
        Files.createDirectory(bundleDir);
        Files.createDirectory(bundleRepoDir);
        Files.createDirectory(workingDir);
        FileUtils.writeStringToFile(Paths.get(baseDir.toString(), "seqware_settings").toFile(), getSettingsFileAsString());
    }

    public Path getSettingsFilePath() {
        return settingsFilePath;
    }
}
