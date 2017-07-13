package ca.on.oicr.gsi.common.transformation;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mlaszloffy
 */
public class FunctionBuilder {
    
    Function<String, String> transformer;

    public FunctionBuilder(Function<String, String> transformer) {
        this.transformer = transformer;
    }

    public Function getFunction() {
        return new Function<Map.Entry<String, Set<String>>, String>() {
            @Override
            public String apply(Map.Entry<String, Set<String>> s) {
                String key = transformer.apply(s.getKey());
                return key + "=" + Joiner.on("&").join(Iterables.transform(s.getValue(), transformer));
            }
        };
    }
// TODO remove me after sonarcloud does its thing
public void junkMethodIsJunk(String unusedParam) throws Exception {
    if (true) {
        throw new Exception("this is garbage");
    }
}    
}
