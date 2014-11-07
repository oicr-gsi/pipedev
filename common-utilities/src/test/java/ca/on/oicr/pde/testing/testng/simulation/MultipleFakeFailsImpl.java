package ca.on.oicr.pde.testing.testng.simulation;

import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * This is a fake test case that is used by TestCaseReporterTest to test test failures.
 *
 * This class tests:
 *
 * 1) TestCaseReporter reporter class and test failure
 *
 */
@Listeners({ca.on.oicr.pde.testing.testng.TestCaseReporter.class})
public class MultipleFakeFailsImpl implements ITest {

    @Test(groups = {"level1"})
    public void successlTestLevel1_1() {
        Assert.assertTrue(true);
    }

    @Test(groups = {"level2"}, dependsOnGroups = "level1")
    public void successTestLevel2_1() {
        Assert.assertTrue(true);
    }

    @Test(groups = {"level3"}, dependsOnGroups = "level2")
    public void failureTestLevel3_1() {
        Assert.assertTrue(false, "There was a fake failure.\nExtra information regarding the failure.");
    }

    //should be skipped
    @Test(groups = {"level4"}, dependsOnGroups = "level3")
    public void successTestLevel4_1() {
        Assert.assertTrue(true);
    }

    @Override
    public String getTestName() {
        return "Test name";
    }

}
