package ca.on.oicr.pde.parsers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

public class SeqwareOutputParser {

    public static String getSwidFromOutput(String seqwareExecutionOutput) {

        String regex = "SWID: (\\d*)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(seqwareExecutionOutput);

        List<String> accessions = new ArrayList<>();
        while (m.find()) {
            accessions.add(m.group(1).trim());
        }

        if (accessions.isEmpty()) {
            try {
                File seqwareOutputFile = File.createTempFile("seqwareCommandExecutionOutput", "");
                FileUtils.writeStringToFile(seqwareOutputFile, seqwareExecutionOutput);
                System.out.println("Seqware execution output has been exported to [" + seqwareOutputFile.getAbsolutePath() + "]");
            } catch (IOException ioe) {
                //not able to write seqware output to disk, continue.
            }

            throw new RuntimeException("No seqware accessions found.");
        }

        //TODO: only return one accession, there may be multiple seqware accessions.
        return accessions.get(0);
        //return accessions;

    }

    public static String getWorkflowRunStatusFromOutput(String seqwareExecutionOutput) {

        String regex = "Workflow Run Status\\s*\\|\\s*(.*)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(seqwareExecutionOutput);

        int matches = 0;
        String result = null;
        while (m.find()) {
            matches++;
            result = m.group(1).trim();
        }

        if (matches != 1 || result == null || result.isEmpty()) {
            throw new RuntimeException("Unable to parse SWID");
        }

        return result;

    }

    public static String getFirstMatch(String input, String pattern) throws IOException {
        Pattern p = Pattern.compile(pattern, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(input);

        String result = "";
        if (m.find()) {
            result = m.group(1).trim();
        }

        return result;
    }

}
