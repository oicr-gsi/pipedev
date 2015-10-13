package ca.on.oicr.pde.testing.decider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author mlaszloffy
 */
public class RunTestSuiteDefinition {

    public static RunTestDefinition defaults;
    public Collection<RunTestDefinition> tests;

    public RunTestSuiteDefinition() {
        defaults = new RunTestDefinition();
        tests = new LinkedList<>();
    }

    public RunTestDefinition getDefaults() {
        return defaults;
    }

    public void setDefaults(RunTestDefinition defaults) {
        RunTestSuiteDefinition.defaults = defaults;
    }

    public Collection<RunTestDefinition> getTests() {
        return tests;
    }

    public void setTests(Collection<RunTestDefinition> tests) {
        this.tests = tests;
    }

    @JsonIgnore
    public RunTestDefinition getDeciderRunTestDefinition() {
        return new RunTestDefinition(defaults);
    }

    @JsonIgnore
    public void add(RunTestDefinition d) {
        tests.add(d);
    }

    public static RunTestSuiteDefinition buildFromJson(String json) throws IOException {
        // load defaults definition        
        BeanDeserializer2 modifier = new BeanDeserializer2(RunTestSuiteDefinition.class, "tests"); //disable deserialization of "tests"
        DeserializerFactory dFactory = BeanDeserializerFactory.instance.withDeserializerModifier(modifier);

        InjectableValues.Std iv = new InjectableValues.Std();
        iv.addValue(RunTestDefinition.class, new RunTestDefinition());  //inject blank definition

        ObjectMapper m = new ObjectMapper(null, null, new DefaultDeserializationContext.Impl(dFactory));
        m.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        m.setInjectableValues(iv);
        RunTestSuiteDefinition definitionOnlyWithDefaults = m.readValue(json, RunTestSuiteDefinition.class);

        // load remaining test definitions (and inject defaults)
        iv = new InjectableValues.Std();
        iv.addValue(RunTestDefinition.class, definitionOnlyWithDefaults.getDefaults());

        m = new ObjectMapper();
        m.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        m.setInjectableValues(iv);
        return m.readValue(json, RunTestSuiteDefinition.class);
    }

    private static class BeanDeserializer2 extends BeanDeserializerModifier {

        private final java.lang.Class<?> type;
        private final List<String> ignorables;

        public BeanDeserializer2(java.lang.Class clazz, String... properties) {
            ignorables = new ArrayList<>();
            ignorables.addAll(Arrays.asList(properties));
            this.type = clazz;
        }

        @Override
        public BeanDeserializerBuilder updateBuilder(
                DeserializationConfig config, BeanDescription beanDesc,
                BeanDeserializerBuilder builder) {
            if (!type.equals(beanDesc.getBeanClass())) {
                return builder;
            }

            for (String ignorable : ignorables) {
                builder.addIgnorable(ignorable);
            }

            return builder;
        }

        @Override
        public List<BeanPropertyDefinition> updateProperties(
                DeserializationConfig config, BeanDescription beanDesc,
                List<BeanPropertyDefinition> propDefs) {
            if (!type.equals(beanDesc.getBeanClass())) {
                return propDefs;
            }

            List<BeanPropertyDefinition> newPropDefs = new ArrayList<>();
            for (BeanPropertyDefinition propDef : propDefs) {
                if (!ignorables.contains(propDef.getName())) {
                    newPropDefs.add(propDef);
                }
            }
            return newPropDefs;
        }

    }

}
