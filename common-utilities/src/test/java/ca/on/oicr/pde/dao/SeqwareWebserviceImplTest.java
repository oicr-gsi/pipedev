package ca.on.oicr.pde.dao;

import static ca.on.oicr.pde.dao.SeqwareWebserviceImpl.getElementFromXML;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SeqwareWebserviceImplTest {

    public SeqwareWebserviceImplTest() {
    }

    @Test
    public void testInputFileAccessXpath() {

        List<String> expected = Arrays.asList("1","2","3","4");

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
                + "<WorkflowRun>"
                + "<createTimestamp>2014-04-03T11:36:23.409-04:00</createTimestamp>"
                + "<host></host>"
                + "<iniFile></iniFile>"
                + "<inputFileAccessions>1</inputFileAccessions>"
                + "<inputFileAccessions>2</inputFileAccessions>"
                + "<inputFileAccessions>3</inputFileAccessions>"
                + "<inputFileAccessions>4</inputFileAccessions>"
                + "<isHasFile></isHasFile>"
                + "<isSelected></isSelected>"
                + "<ownerUserName></ownerUserName>"
                + "<status></status><"
                + "swAccession></swAccession>"
                + "<template/>"
                + "<updateTimestamp></updateTimestamp>"
                + "<workflowAccession></workflowAccession>"
                + "<workflowEngine></workflowEngine>"
                + "<workflowRunId></workflowRunId>"
                + "</WorkflowRun>";

        List<String> actual = getElementFromXML(IOUtils.toInputStream(xml), "/WorkflowRun/inputFileAccessions/text()", true);

        Assert.assertEquals(actual, expected);

    }

}
