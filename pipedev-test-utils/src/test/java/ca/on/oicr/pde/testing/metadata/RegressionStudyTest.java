package ca.on.oicr.pde.testing.metadata;

import ca.on.oicr.pde.testing.metadata.base.InMemoryTestContext;
import ca.on.oicr.pde.testing.metadata.base.RegressionStudy;

/**
 *
 * @author mlaszloffy
 */
public class RegressionStudyTest extends RegressionStudy {
    
  public RegressionStudyTest(){
      super(new InMemoryTestContext());
  }
  
}
