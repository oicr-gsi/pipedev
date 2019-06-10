package ca.on.oicr.pde.parsers;

import static ca.on.oicr.pde.parsers.SeqwareOutputParser.getFirstMatch;
import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SeqwareOutputParserTest {

    @Test
    public void getSwidFromOutputTest() {

        String input = "Installing Bundle (Working Directory Only)\n"
                + "Bundle: bundle\n"
                + "Added 'Workflow' (SWID: 999999)\n"
                + "Not a match: SWID= 999999";

        Assert.assertEquals(SeqwareOutputParser.getSwidFromOutput(input), "999999");

    }

//    @Test(expectedExceptions = RuntimeException.class)
//    public void getSwidFromOutputFailTest() {
//
//        String input = "(SWID: 999999)\n(SWID: 199999)";
//        SeqwareOutputParser.getSwidFromOutput(input);
//
//    }
    public void multipleAccessions() {

        String input = "(SWID: 999999)\n(SWID: 1)\n(SWID: 2)\n(SWID: 3)";
        String output = SeqwareOutputParser.getSwidFromOutput(input);

        Assert.assertEquals(output, "999999");

    }

    @Test(expectedExceptions = RuntimeException.class)
    public void noSwidInOutput() {

        SeqwareOutputParser.getSwidFromOutput("No swid test.");

    }

    @Test
    public void getWorkflowRunStatusFromOutputTest() {

        String input = "-[ RECORD 0 ]------------------+----------------------------------------------------------------------------------------------\n"
                + "Workflow                       | Workflow\n"
                + "Workflow Run SWID              | 999999\n"
                + "Workflow Run Status            | running\n";

        Assert.assertEquals(SeqwareOutputParser.getWorkflowRunStatusFromOutput(input), "running");

    }

    @Test(expectedExceptions = RuntimeException.class)
    public void getWorkflowRunStatusFromOutputFailTest() {

        String input = "-[ RECORD 0 ]------------------+----------------------------------------------------------------------------------------------\n"
                + "Workflow                       | Workflow\n"
                + "Workflow Run SWID              | 999999\n"
                + "Workflow Run Status            | running\n"
                + "Workflow Run Status   |          running\n";

        Assert.assertEquals(SeqwareOutputParser.getWorkflowRunStatusFromOutput(input), "running");

    }

    @Test
    public void getFirstMatchTest() throws IOException {
        String reportOutput = "-[ RECORD 0 ]------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------\n"
                + "Workflow                       | workflow 1.0\n"
                + "Workflow Run SWID              | 11965288\n"
                + "Workflow Run Status            | failed\n"
                + "Workflow Run Create Timestamp  | 2018-06-05 14:30:48.743\n"
                + "Workflow Run Host              | no\n"
                + "Workflow Run Working Dir       | /tmp/test\n"
                + "Workflow Run Engine ID         | 0000216-180509095001099-oozie-oozi-W\n"
                + "Library Sample Names           |\n"
                + "Library Sample SWIDs           |\n"
                + "Identity Sample Names          |\n"
                + "Identity Sample SWIDs          |\n"
                + "IUS-LimsKeys                   | []\n"
                + "Input File Meta-Types          |\n"
                + "Input File SWIDs               |\n"
                + "Input File Paths               |\n"
                + "Immediate Input File Meta-Types|\n"
                + "Immediate Input File SWIDs     |\n"
                + "Immediate Input File Paths     |\n"
                + "Output File Meta-Types         |\n"
                + "Output File SWIDs              |\n"
                + "Output File Paths              |\n"
                + "Workflow Run Time              | 3m 7.0s\n"
                + "";

        Assert.assertEquals(getFirstMatch(reportOutput, "^Workflow Run Working Dir\\h*\\|\\h*(.*)$"), "/tmp/test");
        Assert.assertEquals(getFirstMatch(reportOutput, "^Workflow Run Engine ID\\h*\\|\\h*(.*)$"), "0000216-180509095001099-oozie-oozi-W");
        Assert.assertEquals(getFirstMatch(reportOutput, "^Output File Paths\\h*\\|\\h*(.*)$"), "");
    }

}
