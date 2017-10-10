package ca.on.oicr.pde.deciders;

import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HelloWorldDeciderTest {

    public HelloWorldDeciderTest() {

    }

    @Test
    public void checkDeciderMetaType(){
        HelloWorldDecider d = new HelloWorldDecider();
        d.setParams(Arrays.asList(""));
        d.parse_parameters();
        d.init();
        Assert.assertEquals(d.getMetaType(), Arrays.asList("application/bam"));
    }
    
}