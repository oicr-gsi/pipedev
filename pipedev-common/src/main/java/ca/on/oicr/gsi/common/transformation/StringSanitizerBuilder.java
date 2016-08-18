package ca.on.oicr.gsi.common.transformation;

import com.google.common.base.Function;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author mlaszloffy
 */
public class StringSanitizerBuilder {
    
    private final List<String> searchList;
    private final List<String> replacementList;

    public StringSanitizerBuilder() {
        searchList = new ArrayList<>();
        replacementList = new ArrayList<>();
    }

    public void add(String searchString, String replacementString) {
        searchList.add(searchString);
        replacementList.add(replacementString);
    }

    public Function<String, String> build() {
        final String[] searchArr = searchList.toArray(new String[0]);
        final String[] replacementArr = replacementList.toArray(new String[0]);
        return new Function<String, String>() {
            @Override
            public String apply(String s) {
                return StringUtils.replaceEach(s, searchArr, replacementArr);
            }
        };
    }
    
}
