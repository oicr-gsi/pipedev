package ca.on.oicr.pde.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeqwareOutputParser {

    public static String getSwidFromOutput(String seqwareExecutionOutput) {

        String regex = "SWID: (\\d*)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(seqwareExecutionOutput);

        int matches = 0;
        String result = null;
        while (m.find()) {
            matches++;
            result = m.group(1).trim();
        }

        if (matches != 1 || result == null || result.isEmpty()) {
            throw new RuntimeException("Invalid SWID state.");
        }

        return result;

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

}
