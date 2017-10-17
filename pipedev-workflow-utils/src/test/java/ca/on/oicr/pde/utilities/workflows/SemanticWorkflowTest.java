/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.oicr.pde.utilities.workflows;

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

    private final SemanticWorkflow instance;
    private static final String PIZZA = "PIZZA";
    private static final String FAKE = "SHMITZA";
    private static final Map<String, Set<String>> TERMS;
    private static final Map<String, Set<String>> LABELS;
    private static final String CONFIG;

    
    static {
        CONFIG=ClassLoader.getSystemClassLoader().getResource("ontologies.conf").getFile();
        TERMS = new HashMap<>();
        TERMS.put(PIZZA, new HashSet<>(Arrays.asList("AnchoviesTopping", "FourCheesesTopping")));
        LABELS = new HashMap<>();
        LABELS.put(PIZZA,new HashSet<>(Arrays.asList("CoberturaDeAnchovies", "CoberturaQuatroQueijos")));
    }

    public SemanticWorkflowTest() {
        this.instance = new SemanticWorkflow(CONFIG) {
            @Override
            protected Map<String, Set<String>> getTerms() {
                Map<String, Set<String>> myTerms = new HashMap<>();
                myTerms.putAll(TERMS);
                myTerms.putAll(LABELS);

                return myTerms;
            }

            @Override
            public void buildWorkflow() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        Map<String, String> configs = instance.getConfigs();
        configs.put("test_files", "/u/me/in1.txt,/u/me/in2.txt");

        instance.setConfigs(configs);
        instance.configureOntologyManager(CONFIG);
    }

    @Test
    public void testAttachCVlabelsRegistered() {
        SqwFile[] twoFiles = instance.provisionInputFiles("test_files");
        for (SqwFile f : twoFiles) {
            for (String label: LABELS.get(PIZZA)) {
                System.out.println("Adding "+ label);
                instance.attachCVlabels(f, PIZZA, label);
            }
            Assert.assertEquals(f.getAnnotations().size(), 1, "File does not have the right number of CV term annotations:");
            for (String annot : f.getAnnotations().keySet()){
                System.out.println(f.toString() + " has tag "+annot + " > " + f.getAnnotations().get(annot));
            }
        }

        for (SqwFile fa : twoFiles) {
            String attachedTerm = fa.getAnnotations().get(SemanticWorkflow.CVTERM_TAG);
            System.out.println("THING "+ attachedTerm);
            for (String term: TERMS.get(PIZZA)) {
                Assert.assertTrue(attachedTerm.contains(term), "Terms on file "+fa+" don't contain "+term);
            }
        }
    }

    @Test
    public void testAttachCVtermsUnRegistered() {
        SqwFile[] twoFiles = instance.provisionInputFiles("test_files");
        for (SqwFile f : twoFiles) {
            instance.attachCVlabels(f, PIZZA, "SubmarineSandwich");
        }

        String attachedTerm = twoFiles[0].getAnnotations().get(SemanticWorkflow.CVTERM_TAG);
        Assert.assertEquals(attachedTerm, null);
    }

    @Test
    public void testAttachCVtermsInvalid() {
        SqwFile[] twoFiles = instance.provisionInputFiles("test_files");
        for (SqwFile f : twoFiles) {
            instance.attachCVlabels(f, FAKE, "Invalid Description");
        }

        String attachedTerm = twoFiles[0].getAnnotations().get(SemanticWorkflow.CVTERM_TAG);
        Assert.assertEquals(attachedTerm, null);
    }

}
