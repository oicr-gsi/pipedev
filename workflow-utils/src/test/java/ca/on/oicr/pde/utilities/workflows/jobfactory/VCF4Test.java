package ca.on.oicr.pde.utilities.workflows.jobfactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.seqware.pipeline.workflowV2.model.Job;
import net.sourceforge.seqware.pipeline.workflowV2.model.Workflow;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author mtaschuk
 */
public class VCF4Test {

    VCF4 instance;

    @BeforeMethod
    public void setUp() {
        instance = new VCF4(new Workflow());
    }

    /**
     * Test of mergeSort method, of class VCF4.
     */
    @Test
    public void testMergeSort() {
        System.out.println("mergeSort");

        Job result = instance.mergeSort("output.vcf", "input1.vcf", "input2.vcf");

        Matcher matcher = Pattern.compile("merged(.+?).vcf").matcher(result.getCommand().toString());
        matcher.find();
        String mergedFile = matcher.group();
        //String expected = String.format("cp /dev/null %1$s; cat input1.vcf | tr -cd '\\11\\12\\15\\40-\\176' >> %1$s;cat input2.vcf | tr -cd '\\11\\12\\15\\40-\\176' >> %1$s;head -10000 %1$s | grep ^# > output.vcf ; "
//                + "grep -v ^# %1$s | sed 's/chrX/chr23/' | sed 's/chrY/chr24/' "
//                + "| sed 's/chrM/chr25/' | sort -k1.4,1.5n -k2,2n "
//                + "| sed 's/chr23/chrX/' | sed 's/chr24/chrY/' "
//                + "| sed 's/chr25/chrM/' >> output.vcf;",mergedFile);;
        String expected = String.format("cp /dev/null %1$s; cat input1.vcf >> %1$s;cat input2.vcf >> %1$s;head -10000 %1$s | grep ^# > output.vcf ; "
                + "grep -v ^# %1$s | sed 's/chrX/chr23/' | sed 's/chrY/chr24/' "
                + "| sed 's/chrM/chr25/' | sort -k1.4,1.5n -k2,2n "
                + "| sed 's/chr23/chrX/' | sed 's/chr24/chrY/' "
                + "| sed 's/chr25/chrM/' >> output.vcf;", mergedFile);
        System.out.println(expected);
        Assert.assertEquals(result.getCommand().toString(), expected);
    }
}
