package ca.on.oicr.pde.testing.metadata;

import ca.on.oicr.pde.testing.metadata.base.InMemoryTestContext;
import ca.on.oicr.pde.testing.metadata.base.MergedFiles;

/**
 *
 * @author mlaszloffy
 */
public class MergedFilesTest extends MergedFiles {
    
    public MergedFilesTest(){
        super(new InMemoryTestContext());
    }
    
}