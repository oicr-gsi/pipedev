package ca.on.oicr.pde.deciders;

import ca.on.oicr.pde.deciders.Rsconfig.InvalidFileFormatException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;
import org.testng.Assert;
import org.testng.annotations.*;

/**
 *
 * @author mlaszloffy
 */
public class RsconfigTest {

    //File filepath = FileUtils.toFile(this.getClass().getResource("/rsconfig.xml"));
    public RsconfigTest() {
    }

    private File getResourceFilePath(String path) throws IOException {

        URL u = this.getClass().getResource(path);
        File f = null;

        if (u != null) {
            f = FileUtils.toFile(u);
        }

        if (f == null || !f.exists() || !f.canRead()) {
            throw new IOException("Can not access file=[" + path + "]");
        }

        return f;

    }

    @Test(expectedExceptions = IOException.class)
    public void missingXMLFile() throws ParserConfigurationException, SAXException, IOException, InvalidFileFormatException {

        Rsconfig rs = new Rsconfig(new File("doesNotExist.xml"));

    }

    @Test(expectedExceptions = IOException.class)
    public void resourceFilePathMissing() throws ParserConfigurationException, SAXException, IOException, InvalidFileFormatException {

        getResourceFilePath("no");

    }

    @Test
    public void resourceFilePathOkay() throws ParserConfigurationException, SAXException, IOException, InvalidFileFormatException {

        getResourceFilePath("/rsconfig/missingTargetResequencingID.xml");

    }

    @Test(expectedExceptions = InvalidFileFormatException.class)
    public void missingResequencingId() throws ParserConfigurationException, SAXException, IOException, InvalidFileFormatException {

        Rsconfig rs = new Rsconfig(getResourceFilePath("/rsconfig/missingTargetResequencingID.xml"));

    }

    @Test(expectedExceptions = InvalidFileFormatException.class)
    public void missingResequencingTag() throws ParserConfigurationException, SAXException, IOException, InvalidFileFormatException {

        Rsconfig rs = new Rsconfig(getResourceFilePath("/rsconfig/missingTargetResequencingTag.xml"));

    }

    @Test(expectedExceptions = InvalidFileFormatException.class)
    public void missingTemplateType() throws ParserConfigurationException, SAXException, IOException, InvalidFileFormatException {

        Rsconfig rs = new Rsconfig(getResourceFilePath("/rsconfig/missingTemplateType.xml"));

    }

    @Test(expectedExceptions = SAXException.class)
    public void notXml() throws ParserConfigurationException, SAXException, IOException, InvalidFileFormatException {

        Rsconfig rs = new Rsconfig(getResourceFilePath("/rsconfig/notAnXmlFile.txt"));

    }

    @Test(expectedExceptions = InvalidFileFormatException.class)
    public void duplicateConfig() throws ParserConfigurationException, SAXException, IOException, InvalidFileFormatException {

        Rsconfig rs = new Rsconfig(getResourceFilePath("/rsconfig/rsconfig-duplicateConfig.xml"));

    }

    @Test(expectedExceptions = InvalidFileFormatException.class)
    public void duplicateTemplateType() throws ParserConfigurationException, SAXException, IOException, InvalidFileFormatException {

        Rsconfig rs = new Rsconfig(getResourceFilePath("/rsconfig/rsconfig-duplicateTemplateType.xml"));

    }

    @Test(expectedExceptions = InvalidFileFormatException.class)
    public void duplicates() throws ParserConfigurationException, SAXException, IOException, InvalidFileFormatException {

        Rsconfig rs = new Rsconfig(getResourceFilePath("/rsconfig/rsconfig-duplicates.xml"));

    }

    @Test
    public void validRsconfig() throws ParserConfigurationException, SAXException, IOException, InvalidFileFormatException {

        Rsconfig rs = new Rsconfig(getResourceFilePath("/rsconfig/rsconfig.xml"));
        Assert.assertEquals(rs.get("WG", "", "interval_file"), "/path/to/file0.bed");
        Assert.assertEquals(rs.get("WG", null, "interval_file"), "/path/to/file0.bed");
        Assert.assertEquals(rs.get("TS", "Type1", "interval_file"), "/path/to/file6.bed");
        Assert.assertEquals(rs.get("TS", "Type2", "interval_file"), "/path/to/file7.bed");
        Assert.assertEquals(rs.get("EX", "Type2", "interval_file"), "/path/to/file2.bed");
        Assert.assertEquals(rs.get("TS", "Type5", "interval_file"), "/path/to/file10.bed");
        Assert.assertNull(rs.get("Type2", "EX", "interval_file"));
        Assert.assertNull(rs.get("", "WG", "interval_file"));
        Assert.assertNull(rs.get("TS", "Type5", "interval_fileee"));

    }

    @Test
    public void returnsNullWhenMissing() throws ParserConfigurationException, SAXException, IOException, InvalidFileFormatException {

        Rsconfig rs = new Rsconfig(getResourceFilePath("/rsconfig/rsconfig.xml"));
        Assert.assertNull(rs.get("", "", ""));
        Assert.assertNull(rs.get("no", "", ""));
        Assert.assertNull(rs.get("no", "no", ""));
        Assert.assertNull(rs.get("no", "no", "no"));
        Assert.assertNull(rs.get(null, null, null));
        Assert.assertNull(rs.get("WG", "", null));
        Assert.assertNull(rs.get(null, "HALT", null));
        Assert.assertNull(rs.get(null, null, "interval_file"));

    }

}
