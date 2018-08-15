package ca.on.oicr.pde.deciders.readgroups;

import com.google.common.collect.Streams;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * Configures an OptionParser with read-groups-generator options and then uses can be used to return a configured ReadGroupsGenerator.
 *
 * @author mlaszloffy
 */
public class ReadGroupsOptionParser {

    private OptionSpec<String> readGroupGeneratorOpt;
    private final OptionParser parser;
    private final ReadGroupsGeneratorBuilder defaultReadGroupsGeneratorBuilder;

    /**
     * Constructs a ReadGroupsOptionParser with DefaultOicrReadGroupsGenerator being the default ReadGroupsGenerator.
     *
     * @param parser the OptionParser to configure with ReadGroupsGenerator options
     */
    public ReadGroupsOptionParser(OptionParser parser) {
        this(parser, new DefaultOicrReadGroupsGenerator.Builder());
    }

    /**
     * Constructs a ReadGroupsOptionParser with the provided ReadGroupsGeneratorBuilder being the default ReadGroupsGenerator.
     *
     * @param parser
     * @param defaultReadGroupsGeneratorBuilder
     */
    public ReadGroupsOptionParser(OptionParser parser, ReadGroupsGeneratorBuilder defaultReadGroupsGeneratorBuilder) {
        this.parser = parser;

        //this is required to allow read groups options to be parsed iteratively - it will be set back to false after init
        parser.allowsUnrecognizedOptions();

        this.defaultReadGroupsGeneratorBuilder = defaultReadGroupsGeneratorBuilder;
    }

    /**
     * Returns the ReadGroupGenerator that is specified in the command line args - or the default ReadGroupGenerator if none is provided.
     *
     * The ReadGroupGenerator is configured using the command line arguments too.
     *
     * @param args command line arguments to be parsed to determine the ReadGroupGenerator and required configuration
     * @return the ReadGroupsGenerator that was specified in the args
     * @throws IllegalArgumentException if the ReadGroupGenerator in the args can not be found
     * @throws OptionException if args is missing required ReadGroupGenerator configuration
     */
    public ReadGroupsGenerator getReadGroupGenerator(String... args) throws IllegalArgumentException, OptionException {

        //load all available ReadGroupsGeneratorBuilder
        ServiceLoader<ReadGroupsGeneratorBuilder> loader = ServiceLoader.load(ReadGroupsGeneratorBuilder.class);
        Map<String, ReadGroupsGeneratorBuilder> readGroupGenerators = Streams.stream(loader).collect(Collectors.toMap(i -> i.getName(), i -> i));
        if (readGroupGenerators.isEmpty()) {
            throw new RuntimeException("No read group generators found");
        }

        //display all available ReadGroupsGeneratorBuilder as a command line option
        readGroupGeneratorOpt = parser.accepts("read-groups-generator",
                "Specify the read groups generator to be used: [" + readGroupGenerators.keySet().stream().collect(Collectors.joining(",")) + "]")
                .withRequiredArg().ofType(String.class).defaultsTo(defaultReadGroupsGeneratorBuilder.getName());

        //get the specified ReadGroupsGeneratorBuilder from the cli parser - or the default if none is provided
        OptionSet options = parser.parse(args);
        Map<String, OptionSpec<String>> opts = new HashMap<>();
        ReadGroupsGeneratorBuilder readGroupsGeneratorBuilder = readGroupGenerators.get(options.valueOf(readGroupGeneratorOpt));
        if (readGroupsGeneratorBuilder == null) {
            throw new IllegalArgumentException(options.valueOf(readGroupGeneratorOpt) + " is not a supported read groups generator");
        }

        //setup the ReadGroupsGeneratorBuilder specific configuration as command line options
        for (Map.Entry<String, String> e : readGroupsGeneratorBuilder.getConfiguration().entrySet()) {
            opts.put(e.getKey(), parser.accepts(e.getKey(), "Required by " + readGroupsGeneratorBuilder.getName())
                    .withRequiredArg().ofType(String.class).required());
        }

        //get the ReadGroupsGeneratorBuilder configuration from args
        options = parser.parse(args);
        Map<String, String> optionValues = new HashMap<>();
        for (Map.Entry<String, OptionSpec<String>> e : opts.entrySet()) {
            optionValues.put(e.getKey(), options.valueOf(e.getValue()));
        }

        //check if there are any other options that have not been handled
        try {
            //OptionParser doesn't allow toggling this back to false
            FieldUtils.writeField(parser, "allowsUnrecognizedOptions", Boolean.FALSE, true);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
        parser.parse(args);

        //configure the ReadGroupsGeneratorBuilder and build the ReadGroupsGenerator
        readGroupsGeneratorBuilder.setConfiguration(optionValues);
        return readGroupsGeneratorBuilder.build();
    }

}
