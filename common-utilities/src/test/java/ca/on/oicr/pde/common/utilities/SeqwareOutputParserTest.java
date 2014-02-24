package ca.on.oicr.pde.common.utilities;

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

    @Test(expectedExceptions = RuntimeException.class)
    public void getSwidFromOutputFailTest() {

        String input = "(SWID: 999999)\n(SWID: 199999)";
        SeqwareOutputParser.getSwidFromOutput(input);

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
    
}
