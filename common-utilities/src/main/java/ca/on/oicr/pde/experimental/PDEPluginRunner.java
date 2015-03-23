package ca.on.oicr.pde.experimental;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.metadata.MetadataFactory;
import net.sourceforge.seqware.common.module.ReturnValue;
import net.sourceforge.seqware.common.util.configtools.ConfigTools;
import net.sourceforge.seqware.pipeline.plugin.PluginInterface;

/**
 * PDEPluginRunner is an alternate implementation of {@link net.sourceforge.seqware.pipeline.runner.PluginRunner} and returns to "do_run"
 * ReturnValue.
 *
 * The SeqWare implementation of PluginRunner is intended for running plugins from the command line. This implementation's purpose is to run
 * plugins and return results within a java program.
 */
public class PDEPluginRunner {

    private final Map config;
    private final Metadata meta;
    private final OptionParser parser;
    private NonOptionArgumentSpec<String> nonOptionSpec;

    public PDEPluginRunner() {
        this(ConfigTools.getSettings());
    }

    public PDEPluginRunner(Map config) {
        super();
        this.config = config;
        this.meta = MetadataFactory.get(config);
        this.parser = new OptionParser();
        this.parser.acceptsAll(Arrays.asList("plugin", "p")).withRequiredArg();
        this.nonOptionSpec = parser.nonOptions();
    }

    public ReturnValue runPlugin(List<String> args) {
        return runPlugin(args.toArray(new String[args.size()]));
    }

    public ReturnValue runPlugin(String[] args) {

        //get options
        OptionSet options = parser.parse(args);

        //get plugin
        String pluginName = (String) options.valueOf("plugin");
        PluginInterface plugin;
        try {
            plugin = (PluginInterface) Class.forName(pluginName).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException cnfe) {
            throw new RuntimeException(cnfe);
        }

        //args after "--"
        plugin.setParams(options.valuesOf(nonOptionSpec));

        //seqware settings
        plugin.setConfig(config);

        //metadata connection
        plugin.setMetadata(meta);

        //execute the plugin
        ReturnValue parseRv = plugin.parse_parameters();
        if (!Arrays.asList(ReturnValue.SUCCESS).contains(parseRv.getExitStatus())) {
            throw new PluginRunException(pluginName + " parse_parameters failed with exit code = " + parseRv.getExitStatus());
        }

        ReturnValue initRv = plugin.init();
        if (!Arrays.asList(ReturnValue.SUCCESS).contains(initRv.getExitStatus())) {
            throw new PluginRunException(pluginName + " init failed with exit code = " + initRv.getExitStatus());
        }

        ReturnValue testRv = plugin.do_test();
        if (!Arrays.asList(ReturnValue.SUCCESS, ReturnValue.NOTIMPLEMENTED).contains(testRv.getExitStatus())) {
            throw new PluginRunException(pluginName + " do_test failed with exit code = " + testRv.getExitStatus());
        }

        ReturnValue runRv = plugin.do_run();
        if (!Arrays.asList(ReturnValue.SUCCESS, ReturnValue.QUEUED).contains(runRv.getExitStatus())) {
            throw new PluginRunException(pluginName + " do_run failed with exit code = " + runRv.getExitStatus());
        }

        ReturnValue cleanRv = plugin.clean_up();
        if (!Arrays.asList(ReturnValue.SUCCESS, ReturnValue.NOTIMPLEMENTED).contains(cleanRv.getExitStatus())) {
            throw new PluginRunException(pluginName + " clean_up failed with exit code = " + cleanRv.getExitStatus());
        }

        return runRv;
    }

    public class PluginRunException extends RuntimeException {

        public PluginRunException(String errorMessage) {
            super(errorMessage);
        }
    }

}
