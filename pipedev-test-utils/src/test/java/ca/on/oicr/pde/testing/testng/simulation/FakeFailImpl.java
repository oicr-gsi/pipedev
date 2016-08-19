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
public class FakeFailImpl implements ITest{
    
    @Test(groups = {"fake"})
    public void fakeFailure() {
        Assert.assertTrue(false, "There was a fake failure.\nExtra information regarding the failure.");
    }

    @Override
    public String getTestName() {
        return "Test name";
    }
    
}
