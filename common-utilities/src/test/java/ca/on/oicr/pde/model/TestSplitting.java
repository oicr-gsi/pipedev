package ca.on.oicr.pde.model;

import com.google.common.base.Splitter;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestSplitting {

    @Test
    public void TestSplitting() {

        String attrs = "sample.geo_template_type=Illumina PE Library Seq;sample.geo_tissue_type=C;sample.geo_template_id=24055;sample.geo_library_source_template_type=WG;sample.geo_run_id_and_position_and_slot=1179_4_1,1179_2_1,1179_1_1,1179_3_1;sample.geo_reaction_id=7938,7940,7941,7939;sample.geo_tissue_origin=Pa";

        Map x = Splitter.on(";").omitEmptyStrings().withKeyValueSeparator("=").split(attrs);

        System.out.println(x);

        Assert.assertTrue(true);

    }

    @Test
    public void TestSplitting2() {

        String attrs = "sample.geo_template_type=;sample.geo_tissue_type=C;sample.geo_template_id=24055;sample.geo_library_source_template_type=WG;sample.geo_run_id_and_position_and_slot=1179_4_1,1179_2_1,1179_1_1,1179_3_1;sample.geo_reaction_id=7938,7940,7941,7939;sample.geo_tissue_origin=Pa";

        Map x = Splitter.on(";").omitEmptyStrings().withKeyValueSeparator("=").split(attrs);

        System.out.println(x);

        Assert.assertTrue(true);

    }

    @Test
    public void TestSplitting3() {

        String attrs = "";
        Map x = Splitter.on(";").omitEmptyStrings().withKeyValueSeparator("=").split(attrs);

        System.out.println(x);

        Assert.assertTrue(true);

    }

}
