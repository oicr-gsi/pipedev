package ca.on.oicr.pde.testing.metadata;

import ca.on.oicr.pde.testing.metadata.base.RegressionStudy;
import ca.on.oicr.pde.testing.metadata.base.WebserviceTestContext;

/**
 *
 * @author mlaszloffy
 */
public class RegressionStudyIT extends RegressionStudy {

    public RegressionStudyIT() {
        super(new WebserviceTestContext());

        numberOfThreads = 200;
    }

}
