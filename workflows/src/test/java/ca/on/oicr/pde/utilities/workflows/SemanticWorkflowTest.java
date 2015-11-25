/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.oicr.pde.utilities.workflows;

import ca.on.oicr.pde.tools.common.GSIOntologyManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.sourceforge.seqware.pipeline.workflowV2.model.SqwFile;
import org.junit.Test;
import org.testng.Assert;

/**
 *
 * @author pruzanov
 */
public class SemanticWorkflowTest {

    private SemanticWorkflow instance;
    private static final String EDAM = "EDAM";
    private static final String PIZZA = "PIZZA";
    private static final String FAKE = "SHMITZA";
    private static final Map<String, Set<String>> testTerms;

    static {
        testTerms = new HashMap<String, Set<String>>();
        testTerms.put(EDAM, new HashSet<String>(Arrays.asList("SAM", "BAM", "Nucleic acid sequence alignment")));
    }

    public SemanticWorkflowTest() {
        this.instance = new SemanticWorkflow() {

            @Override
            Map<String, Set<String>> getTerms() {
                Map<String, Set<String>> myTerms = new HashMap<String, Set<String>>();
                myTerms.putAll(testTerms);
                return myTerms;
            }

            @Override
            public void buildWorkflow() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };

        Map<String, String> configs = instance.getConfigs();
        configs.put("test_files", "/u/me/in1.txt,/u/me/in2.txt");

        instance.setConfigs(configs);
        instance.configureOntologyManager(GSIOntologyManager.DEFAULT_CONFIG);
    }

    @Test
    public void testAttachCVtermsRegistered() {
        SqwFile[] twoFiles = instance.provisionInputFiles("test_files");
        for (SqwFile f : twoFiles) {
            instance.attachCVterms(f, EDAM, "BAM");
            instance.attachCVterms(f, EDAM, "Nucleic acid sequence alignment");
        }

        for (SqwFile fa : twoFiles) {
            String attachedTerm = fa.getAnnotations().get(SemanticWorkflow.CVTERM_TAG);
            Assert.assertTrue(attachedTerm.contains(EDAM));
            Assert.assertTrue(attachedTerm.contains("format_2572"));
            Assert.assertTrue(attachedTerm.contains("topic_0740"));
        }
    }

    @Test
    public void testAttachCVtermsUnRegistered() {
        SqwFile[] twoFiles = instance.provisionInputFiles("test_files");
        for (SqwFile f : twoFiles) {
            instance.attachCVterms(f, PIZZA, "PizzaTemperada");
        }

        String attachedTerm = twoFiles[0].getAnnotations().get(SemanticWorkflow.CVTERM_TAG);
        Assert.assertEquals(attachedTerm, null);
    }

    @Test
    public void testAttachCVtermsInvalid() {
        SqwFile[] twoFiles = instance.provisionInputFiles("test_files");
        for (SqwFile f : twoFiles) {
            instance.attachCVterms(f, FAKE, "Invalid Description");
        }

        String attachedTerm = twoFiles[0].getAnnotations().get(SemanticWorkflow.CVTERM_TAG);
        Assert.assertEquals(attachedTerm, null);
    }

}
