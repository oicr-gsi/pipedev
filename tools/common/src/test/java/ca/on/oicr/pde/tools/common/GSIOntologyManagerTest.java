/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.oicr.pde.tools.common;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Test for GSIOntologyManager
 * initializes the Manager and tests 4 public methods with data from EDAM
 */
public class GSIOntologyManagerTest {
    
    public GSIOntologyManagerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of hasTerm method, of class GSIOntologyManager.
     */
    @org.junit.Test
    public void testHasTerm() {
        System.out.println("Testing hasTerm with EDAM data");
        String term = "format_2573";
        String ontID = "EDAM";
        GSIOntologyManager instance = new GSIOntologyManager(null);
        boolean expResult = true;
        boolean result = instance.hasTerm(term, ontID);
        assertEquals(expResult, result);
        
        term = "balh_3452";
        expResult = false;
        result = instance.hasTerm(term, ontID);
        assertEquals(expResult, result);
    }

    /**
     * Test of hasLabel method, of class GSIOntologyManager.
     */
    @org.junit.Test
    public void testHasLabel() {
        System.out.println("Testing hasLabel with EDAM data");
        String label = "SAM";
        String ontID = "EDAM";
        GSIOntologyManager instance = new GSIOntologyManager(null);
        boolean expResult = true;
        boolean result = instance.hasLabel(label, ontID);
        assertEquals(expResult, result);
        
        label = "Bar Foo";
        expResult = false;
        result = instance.hasLabel(label, ontID);
        assertEquals(expResult, result);
    }

    /**
     * Test of termToLabel method, of class GSIOntologyManager.
     */
    @org.junit.Test
    public void testTermToLabel() {
        System.out.println("Testing termToLabel with EDAM data");
        String ontID = "EDAM";
        String term = "format_2573";
        GSIOntologyManager instance = new GSIOntologyManager(null);
        String expResult = "SAM";
        String result = instance.termToLabel(ontID, term);
        assertEquals(expResult, result);

    }

    /**
     * Test of labelToTerm method, of class GSIOntologyManager.
     */
    @org.junit.Test
    public void testLabelToTerm() {
        System.out.println("Testing labelToTerm with EDAM data");
        String ontID = "EDAM";
        String label = "BAM";
        GSIOntologyManager instance = new GSIOntologyManager(null);
        String expResult = "format_2572";
        String result = instance.labelToTerm(ontID, label);
        assertEquals(expResult, result);

    }
    
}
