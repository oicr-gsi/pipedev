package ca.on.oicr.pde.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FileProvenanceReportRecordTest {

    @Test
    public void transformAttributeStringToMap_normal() {

        String attributeString = "key1=value1;key2=value2;key3=value1&value2&value3;key4=value1&value2";

        //transformAttributeStringToMap produces an ordered map
        Map<String, Set<String>> expected = new TreeMap<String, Set<String>>();
        expected.put("key1", new TreeSet(Arrays.asList("value1")));
        expected.put("key2", new TreeSet(Arrays.asList("value2")));
        expected.put("key3", new TreeSet(Arrays.asList("value1", "value2", "value3")));
        expected.put("key4", new TreeSet(Arrays.asList("value1", "value2")));

        Map<String, Set<String>> actual = FileProvenanceReportRecord.transformAttributeStringToMap(attributeString, ";", "=", "&");

        Assert.assertEquals(actual, expected, "Parsing the attribute string failed to produce the expected map representation");

    }

    @Test
    public void transformAttributeStringToMap_duplicatesAndNulls() {

        String attributeString = "key1=;;;;;;;;;;;key1=value1&&value2;key2=value3&value1&value2&&&&&&&&&&&&&;key2=value3&value4;key3=&&&&&&&&&&&;;;;";

        //transformAttributeStringToMap produces an ordered map
        Map<String, Set<String>> expected = new TreeMap<String, Set<String>>();
        expected.put("key1", new TreeSet(Arrays.asList("value1", "value2")));
        expected.put("key2", new TreeSet(Arrays.asList("value1", "value2", "value3", "value4")));
        expected.put("key3", Collections.EMPTY_SET);

        Map<String, Set<String>> actual = FileProvenanceReportRecord.transformAttributeStringToMap(attributeString, ";", "=", "&");

        Assert.assertEquals(actual, expected, "Parsing the attribute string failed to produce the expected map representation");

    }

}
