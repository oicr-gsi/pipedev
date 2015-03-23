package ca.on.oicr.pde.model;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
        Map<String, Set<String>> expected = new TreeMap<>();
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
        Map<String, Set<String>> expected = new TreeMap<>();
        expected.put("key1", new TreeSet(Arrays.asList("value1", "value2")));
        expected.put("key2", new TreeSet(Arrays.asList("value1", "value2", "value3", "value4")));
        expected.put("key3", Collections.EMPTY_SET);

        Map<String, Set<String>> actual = FileProvenanceReportRecord.transformAttributeStringToMap(attributeString, ";", "=", "&");

        Assert.assertEquals(actual, expected, "Parsing the attribute string failed to produce the expected map representation");

    }

    @Test
    public void transformMapTest() {
        List<String> keys = Arrays.asList("1", "2", "3");
        String mapString = "parent_key1.1=1;parent_key2.1=2;parent_key1.2=3;parent_key2.2=4;parent_key1.3=5;parent_key2.3=6;parent_key3.1=7&8&9&10&11;"
                + "parent_difficult_key.with_special... characters_and_numbers!.1.2.3.1.3=okay";
        Map<String, Map<String, Set<String>>> actual = FileProvenanceReportRecord.parseFileProvenanceMapStructure(keys, mapString);

        Map<String, Map<String, Set<String>>> expected = new HashMap<>();
        Map x;
        //1
        x = new HashMap<>();
        x.put("key1", Sets.newHashSet("1"));
        x.put("key2", Sets.newHashSet("2"));
        x.put("key3", Sets.newHashSet("7", "8", "9", "10", "11"));
        expected.put("1", x);
        //2
        x = new HashMap<>();
        x.put("key1", Sets.newHashSet("3"));
        x.put("key2", Sets.newHashSet("4"));
        expected.put("2", x);
        //3
        x = new HashMap<>();
        x.put("key1", Sets.newHashSet("5"));
        x.put("key2", Sets.newHashSet("6"));
        x.put("difficult_key.with_special... characters_and_numbers!.1.2.3.1", Sets.newHashSet("okay"));
        expected.put("3", x);

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void transformMapValidMissingDataTest() {
        List<String> keys = Arrays.asList("1", "2", "3");
        String mapString = "";
        Map<String, Map<String, Set<String>>> actual = FileProvenanceReportRecord.parseFileProvenanceMapStructure(keys, mapString);

        Map<String, Map<String, Set<String>>> expected = new HashMap<>();
        expected.put("1", Collections.EMPTY_MAP);
        expected.put("2", Collections.EMPTY_MAP);
        expected.put("3", Collections.EMPTY_MAP);

        Assert.assertEquals(actual, expected);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void transformMapInvalidDataTest() {
        List<String> keys = Arrays.asList("1", "2", "3");
        String mapString = "parent_key1.12=1"; //key 12 does not exist
        FileProvenanceReportRecord.parseFileProvenanceMapStructure(keys, mapString);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void transformMapMalformedDataTest() {
        List<String> keys = Arrays.asList("1", "2", "3");
        String mapString = "parent_key1.1=1;parentt_key2.1=2"; //malformed prefix
        FileProvenanceReportRecord.parseFileProvenanceMapStructure(keys, mapString);
    }

    @Test
    public void transformListTest() {
        List<String> keys = Arrays.asList("1", "2", "3");
        String listString = "33!@#$%^&*()_\n\n\n:33:33";
        Map<String, String> actual = FileProvenanceReportRecord.parseFileProvenanceListStructure(keys, listString);

        Map<String, String> expected = new HashMap<>();
        expected.put("1", "33!@#$%^&*()_\n\n\n");
        expected.put("2", "33");
        expected.put("3", "33");

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void transformListValidMissingDataTest() {
        List<String> keys = Arrays.asList("1", "2", "3");
        String listString = "";
        Map<String, String> actual = FileProvenanceReportRecord.parseFileProvenanceListStructure(keys, listString);

        Map<String, String> expected = new HashMap<>();
        expected.put("1", "");
        expected.put("2", "");
        expected.put("3", "");

        Assert.assertEquals(actual, expected);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void transformListInvalidMissingDataTest() {
        List<String> keys = Arrays.asList("1", "2", "3");
        String listString = "1:2...3"; //only two elements
        FileProvenanceReportRecord.parseFileProvenanceListStructure(keys, listString);
    }

}
