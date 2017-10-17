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

    private final Map<String, String> config;
    private final Metadata metadata;

    public PDEPluginRunner() {
        this(ConfigTools.getSettings());
    }

    public PDEPluginRunner(Map<String, String> config) {
        super();
        this.config = config;
        this.metadata = MetadataFactory.get(config);
    }
    
        public PDEPluginRunner(Map<String, String> config, Metadata metadata) {
        super();
        this.config = config;
        this.metadata = metadata;
    }

    public ReturnValue runPlugin(List<String> args) {
        return runPlugin(args.toArray(new String[args.size()]));
    }

    public ReturnValue runPlugin(PluginInterface instance, List<String> params) {

        //args after "--"
        instance.setParams(params);

        //seqware settings
        instance.setConfig(config);

        //metadata connection
        instance.setMetadata(metadata);

        //execute the plugin
        ReturnValue parseRv = instance.parse_parameters();
        if (!Arrays.asList(ReturnValue.SUCCESS).contains(parseRv.getExitStatus())) {
            throw new PluginRunException(instance.getClass().getName() + " parse_parameters failed with exit code = " + parseRv.getExitStatus());
        }

        ReturnValue initRv = instance.init();
        if (!Arrays.asList(ReturnValue.SUCCESS).contains(initRv.getExitStatus())) {
            throw new PluginRunException(instance.getClass().getName() + " init failed with exit code = " + initRv.getExitStatus());
        }

        ReturnValue testRv = instance.do_test();
        if (!Arrays.asList(ReturnValue.SUCCESS, ReturnValue.NOTIMPLEMENTED).contains(testRv.getExitStatus())) {
            throw new PluginRunException(instance.getClass().getName() + " do_test failed with exit code = " + testRv.getExitStatus());
        }

        ReturnValue runRv = instance.do_run();
        if (!Arrays.asList(ReturnValue.SUCCESS, ReturnValue.QUEUED).contains(runRv.getExitStatus())) {
            throw new PluginRunException(instance.getClass().getName() + " do_run failed with exit code = " + runRv.getExitStatus());
        }

        ReturnValue cleanRv = instance.clean_up();
        if (!Arrays.asList(ReturnValue.SUCCESS, ReturnValue.NOTIMPLEMENTED).contains(cleanRv.getExitStatus())) {
            throw new PluginRunException(instance.getClass().getName() + " clean_up failed with exit code = " + cleanRv.getExitStatus());
        }

        return runRv;
    }

    public ReturnValue runPlugin(String[] args) {

        OptionParser parser = new OptionParser();
        NonOptionArgumentSpec<String> nonOptionSpec = parser.nonOptions();
        parser.acceptsAll(Arrays.asList("plugin", "p")).withRequiredArg();

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

        return runPlugin(plugin, options.valuesOf(nonOptionSpec));
    }

    public class PluginRunException extends RuntimeException {

        public PluginRunException(String errorMessage) {
            super(errorMessage);
        }
    }

}
