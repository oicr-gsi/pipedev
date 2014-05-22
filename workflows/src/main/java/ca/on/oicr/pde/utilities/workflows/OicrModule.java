package ca.on.oicr.pde.utilities.workflows;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.sourceforge.seqware.common.module.ReturnValue;
import net.sourceforge.seqware.common.util.Log;
import net.sourceforge.seqware.pipeline.module.Module;
import net.sourceforge.seqware.pipeline.module.ModuleInterface;
import org.openide.util.lookup.ServiceProvider;

/**
 * A wrapper around SeqWare's module system to hide some of the ugly parts of
 * module development. For more information on module development, see the
 * SeqWare documentation at <a
 * href="http://seqware.github.io/docs/6-pipeline/writing_modules/">Writing
 * Modules</a>.
 *
 * @author Morgan Taschuk, Brian O'Connor
 */
@ServiceProvider(service = ModuleInterface.class)
public class OicrModule extends Module {

    private ReturnValue ret;
    private OptionSet options = null;
    protected OptionParser parser;
    private List<String> requiredParams = new ArrayList<String>();

    public OicrModule() {
        super();
        parser = new OptionParser();
        parser.accepts("help", "Print this help information");
        ret = new ReturnValue();
    }

    /**
     * Define an argument for the Module to use on the command line.
     *
     * @param command the argument to use. Single letters will use -, and more
     * characters will use --
     * @param description the description of the argument to give when giving
     * help
     * @param required whether or not the argument is required for the module to
     * function. The presence of this argument is tested in the {@link #init() init}
     * method, which will throw an exception if the argument is not present.
     */
    protected final void defineArgument(String command, String description, boolean required) {
        parser.accepts(command, description).withRequiredArg();
        if (required) {
            requiredParams.add(command);
        }
    }

    /**
     * Get the argument provided on the command line. If the argument was not
     * provided, this method will return an empty String. You can test for the
     * presence of your argument using  {@link #options options}{@code .has(argument)}
     * and retrieve it using {@link #options options}{@code .valueOf(argument))}
     *
     * {@inheritDoc}
     *
     * @param arg an argument previously defined by 'defineArgument'.
     * @return the value provided on the command line, or empty string if none
     * provided.
     */
    protected String getArgument(String arg) {
        Object o = options.valueOf(arg);
        if (o == null || o.toString().isEmpty()) {
            Log.debug("Command line argument is not available: " + arg);
            return "";
        } else {
            return o.toString();
        }
    }

    /**
     * getOptionParser is an internal method to parse command line args.
     *
     * @return OptionParser this is used to get command line options
     */
    @Override
    protected OptionParser getOptionParser() {
        return (parser);
    }

    /**
     * A method used to return the syntax for this module
     *
     * @return a string describing the syntax
     */
    @Override
    public String get_syntax() {
        StringWriter output = new StringWriter();
        try {
            parser.printHelpOn(output);
            return (output.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return (e.getMessage());
        }
    }

    /**
     * The init method is where you put any code needed to setup your module.
     * Here I set some basic information in the ReturnValue object which will
     * eventually populate the "processing" table in seqware_meta_db. I also
     * create a temporary directory using the FileTools object.
     *
     * init is optional
     *
     * @return A ReturnValue object that contains information about the status
     * of init
     */
    @Override
    public ReturnValue init() {
        try {
            // The parameters object is actually an ArrayList of Strings created
            // by splitting the command line options by space. JOpt expects a String[]
            options = parser.parse(this.getParameters().toArray(new String[0]));

            // you can write to "stdout" or "stderr" which will be persisted back to the DB
            ret.setStdout(ret.getStdout() + "Output: " + (String) options.valueOf("output-file") + "\n");

        } catch (OptionException e) {
            e.printStackTrace();
            Log.error(e.getMessage(), e);
            ret.setStderr(e.getMessage());
            ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
        }

        if (options.has("help")) {
            System.err.println(get_syntax());
            ret.setStderr("Requested help information");
            ret.setExitStatus(ReturnValue.RETURNEDHELPMSG);
            return ret;
        }

        ret = super.init();
        //Sanity checking for required parameters
        for (String option : requiredParams) {
            if (!options.has(option)) {
                Log.warn("Required argument missing: --" + option);
                ret.setStderr("Required argument missing: --" + option);
                ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
            }
        }
        // now return the ReturnValue
        return (ret);

    }

    /**
     * Verifies that the parameters make sense.
     *
     * @return a ReturnValue object
     */
    @Override
    public ReturnValue do_verify_parameters() {
        return ret;
    }

    /**
     * Ensures that the input files exist and also do validation of the input
     * files. There is some overlap between this method and
     * do_verify_parameters. This one is more focused on validating files,
     * making sure web services are up, DBs can be connected to etc. While
     * do_verify_parameters is primarily used to validate that the minimal
     * parameters are passed in. The overlap between these two methods is at the
     * discretion of the developer
     *
     * @return a ReturnValue object
     */
    @Override
    public ReturnValue do_verify_input() {
        return (ret);
    }

    /**
     * Test the programs you're calling here by running them on a "known good"
     * test dataset and then compare the new answer with the previous known good
     * answer. Other forms of testing could be encapsulated here as well.
     *
     * @return a ReturnValue object
     */
    @Override
    public ReturnValue do_test() {

        // notice the use of "NOTIMPLEMENTED", this signifies that we simply 
        // aren't doing this step. It's better than just saying SUCCESS
        ReturnValue ret = new ReturnValue();
        ret.setExitStatus(ReturnValue.NOTIMPLEMENTED);

        // not much to do, just return
        return (ret);
    }

    /**
     * This is the core of a module. While some modules may be written in pure
     * Java or use various third-party Java APIs, the vast majority of modules
     * will use this method to make calls out to the shell (typically the BASH
     * shell in Linux) and use that shell to execute various commands. In an
     * ideal world this would never happen, we would all write out code with a
     * language-agnostic, network-aware API (e.g. thrift, SOAP, etc). But until
     * that day comes most programs in bioinformatics are command line tools (or
     * websites). So the heart of the module is it acts as a way for us to treat
     * the disparate tools as well-behaved modules that present a standard
     * interface and report back their metadata in well-defined ways. That's,
     * ultimately, what this object and, in particular this method, are all
     * about.
     *
     * There are other alternatives out there, such as Galaxy, that may provide
     * an XML syntax for accomplishing much of the same thing. For example, they
     * make disparate tools appear to function the same because the
     * inputs/outputs are all described using a standardized language. We chose
     * Java because it was more expressive than XML as a module running
     * descriptor. But clearly there are a lot of ways to solve this problem.
     * The key concern, though, is that a module should present very clear
     * inputs and outputs based, whenever possible, on standardized file types.
     * This makes it easy to use modules in novel workflows, rearranging them as
     * needed. Make every effort to make your modules self-contained and robust!
     *
     * @return a ReturnValue object
     */
    @Override
    public ReturnValue do_run() {
        return ret;
    }

    /**
     * Check to make sure the output was created correctly.
     *
     * @return a ReturnValue object
     */
    @Override
    public ReturnValue do_verify_output() {
        return ret;
    }

    /**
     * Clean up files that aren't needed after this module finishes.
     *
     * clean_up is optional
     */
    @Override
    public ReturnValue clean_up() {
        return (ret);
    }
}
