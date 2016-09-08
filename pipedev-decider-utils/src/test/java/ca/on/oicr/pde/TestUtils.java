/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.oicr.pde;

import ca.on.oicr.pde.deciders.Rsconfig;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author mlaszloffy
 */
public class TestUtils {

    public static File getResourceFilePath(String path) throws IOException {
        URL u = TestUtils.class.getClassLoader().getResource(path);
        File f = null;
        if (u != null) {
            f = FileUtils.toFile(u);
        }
        if (f == null || !f.exists() || !f.canRead() || !f.isFile()) {
            throw new IOException("Can not access file=[" + path + "]");
        }
        return f;
    }

    @Test(expectedExceptions = IOException.class)
    public void resourceFilePathMissing() throws ParserConfigurationException, SAXException, IOException, Rsconfig.InvalidFileFormatException {
        getResourceFilePath("no");
    }

    @Test
    public void resourceFilePathOkay() throws ParserConfigurationException, SAXException, IOException, Rsconfig.InvalidFileFormatException {
        getResourceFilePath("rsconfig/missingTargetResequencingID.xml");
    }

    @Test(expectedExceptions = IOException.class)
    public void resourceFileNotAFile() throws ParserConfigurationException, SAXException, IOException, Rsconfig.InvalidFileFormatException {
        getResourceFilePath("rsconfig/");
    }
}
