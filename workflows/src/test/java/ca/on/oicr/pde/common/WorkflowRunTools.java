package ca.on.oicr.pde.common;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

public class WorkflowRunTools {

    public static String buildPathFromDirectory(String initialPath, File dir) throws IOException {

        File[] softwarePackages = dir.listFiles((FileFilter)DirectoryFileFilter.DIRECTORY);
        Arrays.sort(softwarePackages);

        StringBuilder path = new StringBuilder();
        path.append(initialPath).append(":").append(dir);

        for (File d : softwarePackages) {
            path.append(":").append(d.getAbsolutePath());
        }

        return path.toString();

    }

}
