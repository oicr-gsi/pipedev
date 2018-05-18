package ca.on.oicr.pde.testing.common;

import ca.on.oicr.pde.testing.RunTestSettings;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mlaszloffy
 */
public class RunTestSettingsTest {

    public RunTestSettingsTest() {
    }

    @Test
    public void testGetSettingFile() throws MalformedURLException {
        RunTestSettings s = new RunTestSettings(Paths.get("/tmp/" + "base_dir"), new URL("http://seqware:8080/seqware-webservice"), "admin@admin.com", "admin", "dev-scheduler.domain", "user_name", "random_string");

        Assert.assertEquals(s.getSettingsFileAsString(), "SW_DEFAULT_WORKFLOW_ENGINE=oozie-sge\n"
                + "SW_METADATA_METHOD=webservice\n"
                + "SW_REST_URL=http://seqware:8080/seqware-webservice\n"
                + "SW_REST_USER=admin@admin.com\n"
                + "SW_REST_PASS=admin\n"
                + "SW_BUNDLE_DIR=/tmp/base_dir/provisionedBundles\n"
                + "SW_BUNDLE_REPO_DIR=/tmp/base_dir/bundleRepo\n"
                + "OOZIE_URL=http://dev-scheduler.domain:11000/oozie\n"
                + "OOZIE_APP_ROOT=seqware_workflow\n"
                + "OOZIE_APP_PATH=hdfs://dev-scheduler.domain:8020/user/user_name/\n"
                + "OOZIE_JOBTRACKER=dev-scheduler.domain:8021\n"
                + "OOZIE_NAMENODE=hdfs://dev-scheduler.domain:8020\n"
                + "OOZIE_QUEUENAME=default\n"
                + "OOZIE_WORK_DIR=/tmp/base_dir/workingDir\n"
                + "OOZIE_SGE_MAX_MEMORY_PARAM_FORMAT=-l h_vmem=${maxMemory}M\n"
                + "OOZIE_SGE_THREADS_PARAM_FORMAT=-pe smp ${threads}\n"
                + "HBASE.ZOOKEEPER.QUORUM=dev-scheduler.domain\n"
                + "HBASE.ZOOKEEPER.PROPERTY.CLIENTPORT=2181\n"
                + "HBASE.MASTER=dev-scheduler.domain:60000\n"
                + "MAPRED.JOB.TRACKER=dev-scheduler.domain:8021\n"
                + "FS.DEFAULTFS=hdfs://dev-scheduler.domain:8020\n"
                + "FS.HDFS.IMPL=org.apache.hadoop.hdfs.DistributedFileSystem\n"
                + "SW_LOCK_ID=random_string\n"
                + "OOZIE_BATCH_THRESHOLD=10\n"
                + "OOZIE_BATCH_SIZE=5");
    }

    @Test
    public void createOnDisk() throws MalformedURLException, IOException {
        File createTempDir = Files.createTempDir();
        createTempDir.deleteOnExit();
        Path baseDir = createTempDir.toPath();

        RunTestSettings s = new RunTestSettings(baseDir, new URL("http://seqware:8080/seqware-webservice"), "admin@admin.com", "admin", "dev-scheduler.domain", "user_name", "random_string");
        s.createOnDisk();
        Assert.assertTrue(s.getSettingsFilePath().toFile().exists());
    }

}
