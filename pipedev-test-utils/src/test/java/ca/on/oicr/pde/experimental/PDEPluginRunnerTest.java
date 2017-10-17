package ca.on.oicr.pde.experimental;

import java.io.IOException;
import java.util.Collections;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.metadata.MetadataFactory;
import net.sourceforge.seqware.common.module.ReturnValue;
import net.sourceforge.seqware.pipeline.plugins.HelloWorld;
import org.powermock.reflect.Whitebox;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mlaszloffy
 */
public class PDEPluginRunnerTest {

    public PDEPluginRunnerTest() {
    }

    @Test
    public void testPDEPluginRunner() throws IOException {
        Metadata expectedMetadata = MetadataFactory.getNoOp();
        PDEPluginRunner pr = new PDEPluginRunner(Collections.EMPTY_MAP, expectedMetadata);
        HelloWorld hw = new HelloWorld();
        ReturnValue rv = pr.runPlugin(hw, Collections.<String>emptyList());
        Assert.assertEquals(rv.getExitStatus(), ReturnValue.SUCCESS);

        //probe plugin object as necessary
        Assert.assertEquals(Whitebox.getInternalState(hw, "metadata"), expectedMetadata);
    }

}
