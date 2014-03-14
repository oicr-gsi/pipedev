package ca.on.oicr.pde.deciders;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import net.sourceforge.seqware.common.hibernate.FindAllTheFiles;
import net.sourceforge.seqware.common.module.FileMetadata;
import net.sourceforge.seqware.common.module.ReturnValue;
import net.sourceforge.seqware.common.util.Log;
import net.sourceforge.seqware.pipeline.deciders.BasicDecider;

/**
 * <p> An implementation of SeqWare's BasicDecider that simplifies the logic in
 * BasicDecider and provides OICR-specific functionality. OicrDecider and
 * accessory classes have a number of functions that depend on metadata
 * originating from the Geospiza LIMS and conventions defined in-house. This
 * document explains some of these assumptions and also lays out the methods to
 * use this class, since it differs significantly from BasicDecider. </p>
 *
 * <p> Most of the benefit of BasicDecider is in additional methods that hide
 * the low-level details from the implementor. You no longer need detailed
 * knowledge about the formatting of Geospiza attributes in order to implement a
 * decider, for example. </p>
 *
 * <h1>Arguments</h1> <ul> <li>--check-file-exists : Only launch on the file if
 * the file exists. By default, SeqWare will launch on any file path, because it
 * can take advantage of remote files in S3 or ftp. At OICR, it is unlikely that
 * we will use these remote paths (the cluster is not internet accessible
 * anyway) and we occasionally have files that are listed in the metadata
 * database that do not exist. <li>--skip-status-check : If enabled, will skip
 * the check for the status of the workflow run. By default, we do not include
 * files from failed workflow runs. This flag turns that checking off. This flag
 * has no effect on sequencer runs, lanes, IUSes or any downstream products
 * marked with a 'skip' flag. </ul>
 *
 * <h1>Extending OicrDecider</h1>
 *
 * <p> OicrDecider should be extended in order to take advantage of the expanded
 * arguments and simplified implementation. See each method for more details
 * about implementation. All overridden methods should call super.method() in
 * order get the full benefit of this class. </p>
 *
 * <p>The following is a brief guide to the methods available when extending
 * OicrDecider. These methods are called in the order presented below. Note that
 * not all methods need to be overridden in order to make a functioning decider;
 * those highly recommended are marked with a *.</p>
 *
 *
 * <ol>
 *
 * <li>*{@link #OicrDecider() Constructor} : Add all new arguments to the
 * decider in this method.
 *
 * <li>*{@link #init()} : the first method where the command-line arguments are
 * available to store, parse, test and store. Return a {@link ReturnValue} with
 * a non success exit status if an argument is incorrect.
 *
 * <li> {@link #separateFiles(List, String)} : override this method to modify
 * the list of files (i.e. ReturnValues) before they are split into different
 * groups.
 *
 * <li> {@link #handleGroupByAttribute(String)} : override this method to change
 * how files are grouped together. The String passed into the method is the
 * unmodified group identifier (the value retrieved from a file with the group
 * set by {@link #setGroupBy(Group, boolean)}. You can use this method to modify
 * it as desired. Files with the identical String returned from this method will
 * be grouped and run together in one workflow run.
 *
 * <li> *checkFileDetails: Override one of these methods to filter files based
 * on their metadata information.
 *
 * <ul><li>The BasicDecider {@link #checkFileDetails(ReturnValue, FileMetadata)}
 * method uses {@link ReturnValue} and {@link FileMetadata}. {@code ReturnValue}
 * has a list of attributes that contain the metadata for the file in question.
 * These attributes can be retrieved using {@link FindAllTheFiles.Header}
 * objects. A {@code ReturnValue} represents one processing event in a previous
 * workflow run and thereby can represent more than one file. The accompanying {@code FileMetadata}
 * is the specific file that the method is evaluating at that time.
 * Lims-specific attributes are somewhat difficult to retrieve with this system.
 *
 * <li>The OicrDecider {@link #checkFileDetails(FileAttributes) } method uses {@link FileAttributes}
 * instead, which simplifies the mechanism to retrieve commonly used metadata
 * (e.g. donor, library sample name, sequencer run name) and Geospiza
 * LIMS-specific attributes. See {@link FileAttributes} for more information.
 * </ul>
 *
 * <li> {@link #doFinalCheck(String,String)} : parse a group of files that will
 * be run together and do a final check to confirm that they should launch.
 *
 * <li> *Customize each workflow run : Override one of the following methods to
 * appropriately parameterize each workflow run using the files involved.
 *
 * <ul><li>The BasicDecider {@link #modifyIniFile(String, String)} has you
 * return a Map which represents the INI file for the workflow.
 *
 * <li>The OicrDecider {@link #customizeRun(WorkflowRun)} hides the process of
 * creating an INI file and instead allows you to configure a {@link WorkflowRun}
 * object. The intention of this method was to a) provide easy access to file
 * metadata and b) simplify the creation of a one-step workflow by providing
 * convenience methods to specify the script, memory, and params. New properties
 * for the INI are added to the WorkflowRun.</ul>
 *
 * </ol>
 *
 *
 * @author Pipeline Development and Evaluation (pde@lists.oicr.on.ca)
 */
public class OicrDecider extends BasicDecider {

    private ReturnValue rv = new ReturnValue();
    private Group groupBy = null;
    protected Map<String, FileAttributes> files;
    private int numberOfFilesPerGroup = Integer.MIN_VALUE;
    private List<String> requiredParams = new ArrayList<String>();
    private static final String [][] readMateFlags = {{"_R1_","1_sequence.txt",".1.fastq"},{"_R2_","2_sequence.txt",".2.fastq"}};
    public static final int MATE_UNDEF = 0;
    public static final int MATE_1 = 1;
    public static final int MATE_2 = 2;

    /**
     * <p>Sets up the decider arguments and global variables. Any arguments
     * intended to be used on the command line should be added using either The
     * constructor runs before the arguments are parsed from the command
     * line.SeqWare uses the JOpt package to parse command line parameters.
     * OicrDecider builds on top of that. There are several mechanisms you can
     * use to pull arguments from the command line.<p>
     *
     * <ul><li>Flags: You add can flags to the decider by using
     * {@link #parser parser}{@code .accepts(String argument, String description)}
     * and retrieve them in later methods using {@link #options options}{@code .has(argument)}.
     * <li>Parameters: If your argument takes a parameter, you can use the
     * convenience method {@link #defineArgument(String, String, boolean) defineArgument}
     * and retrieve the argument later with {@link #getArgument(String) getArgument}
     * </ul>
     *
     */
    public OicrDecider() {
        super();
        parser.acceptsAll(Arrays.asList("check-file-exists", "cf"), "Optional: only launch on the file if the file exists");
        parser.accepts("skip-status-check", "Optional: If enabled will skip the check for the status of the sequencer run/lane/IUS/workflow run");
        parser.acceptsAll(Arrays.asList("help", "h"), "Prints this help message");
        defineArgument("output-path", "The absolute path of the directory to put the final file(s) (workflow output-prefix option).", false);
        defineArgument("output-folder", "The relative path to put the final result(s) (workflow output-dir option).", false);
        files = new HashMap<String, FileAttributes>();
    }

    /**
     * Define an argument for the Decider to use on the command line.
     *
     * @param command the argument to use. Single letters will use -, and more
     * characters will use --
     * @param description the description of the argument to give when giving
     * help
     * @param required whether or not the argument is required for the decider
     * to function. The presence of this argument is tested in the {@link #init() init}
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
     *
     * {@inheritDoc}
     *
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
     * Initializes the decider using arguments on the command line. Any
     * arguments defined as 'required' by {@link #defineArgument(java.lang.String, java.lang.String, boolean)
     * } are tested for in this method.
     *
     * @return {@link ReturnValue} with an appropriate exit status.
     */
    @Override
    public ReturnValue init() {
        if (options.has("help")) {
            System.err.println(get_syntax());
            ret.setExitStatus(ReturnValue.RETURNEDHELPMSG);
            return ret;
        }
        ret = super.init();
        //Sanity checking for required parameters
        for (String option : requiredParams) {
            if (!options.has(option)) {
                Log.warn("Required argument missing: --" + option);
                ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
                System.out.println(get_syntax());
            }
        }

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkFileDetails(ReturnValue returnValue, FileMetadata fm) {
        Boolean toReturn = super.checkFileDetails(returnValue, fm);
        // if the preliminary checks failed, ther
        if (!toReturn) {
            return toReturn;
        }
        Log.debug("CHECK FILE DETAILS:" + fm);

        if (this.options.has("check-file-exists")) {
            if (!new File(fm.getFilePath()).exists()) {
                Log.warn("File not found:" + fm.getFilePath());
                return false;
            }
        }
        
        // SEQWARE-1809, PDE-474 ensure that deciders only use input from completed workflow runs
        FileAttributes attributes = new FileAttributes(returnValue, fm);
        String status = returnValue.getAttribute(FindAllTheFiles.WORKFLOW_RUN_STATUS);
        if (status == null || !status.equals("completed")){
            return false;
        }
        if (!options.has("skip-status-check")) {
            for (Iterator<String> it = attributes.iterator(); it.hasNext();) {
                String next = it.next();
                if (next.contains("Status")) {
                    if (attributes.getOtherAttribute(next).equals("failed")) {
                        Log.debug("Skipping file because the workflow run status is failed" + fm.getFilePath());
                        return false;
                    }
                }

            }
        }
        if (!checkFileDetails(attributes)) {
            return false;
        }
        files.put(fm.getFilePath(), attributes);
        return toReturn;
    }

    protected boolean checkFileDetails(FileAttributes attributes) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String handleGroupByAttribute(String attribute) {
        if (groupBy == Group.DONOR) {
            String[] parents = attribute.split(":");
            return parents[parents.length - 1];
        }
        return super.handleGroupByAttribute(attribute);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ReturnValue doFinalCheck(String commaSeparatedFilePaths, String commaSeparatedParentAccessions) {
        ReturnValue toReturn = rv;
        rv = new ReturnValue();
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> modifyIniFile(String commaSeparatedFilePaths, String commaSeparatedParentAccessions) {
        Map<String, String> iniFile = super.modifyIniFile(commaSeparatedFilePaths, commaSeparatedParentAccessions);

        String[] paths = commaSeparatedFilePaths.split(",");
        FileAttributes[] attributes = new FileAttributes[paths.length];
        for (int i = 0; i < paths.length; i++) {
            attributes[i] = files.get(paths[i]);
        }

        WorkflowRun run = new WorkflowRun(iniFile, attributes);
        run.addProperty("output_prefix", getArgument("output-path").isEmpty() ? "" : (getArgument("output-path").endsWith("/") ? getArgument("output-path") : getArgument("output-path").concat("/")), "./");
        run.addProperty("output_dir", getArgument("output-folder"), "seqware-results");
//        run.addProperty("output_path", getArgument("output-path"), "NA");
//        if(!getArgument("output-path").isEmpty()){
//            run.setManualOutput(true);
//        }
        ret = customizeRun(run);

        iniFile = run.getIniFile();

        return iniFile;
    }

    public ReturnValue customizeRun(WorkflowRun run) {
        Log.debug("Number of files: " + run.getFiles().length);
        if (numberOfFilesPerGroup != Integer.MIN_VALUE) {
            if (run.getFiles().length != numberOfFilesPerGroup) {
                Log.debug("Invalid number of files: " + run.getFiles().length + ":" + run.getFiles()[0]);
                rv.setExitStatus(ReturnValue.INVALIDFILE);
            }
        }
        return rv;
    }

    /**
     * Sets how to group files together. When a group is chosen, all of the
     * files that have been generated for that group will be grouped together
     * and then filtered by meta-type. Grouping is either specific according to
     * distinct identifier or general by name.
     *
     * <p>Specific grouping is achieved by setting 'groupSimilar' to false. The
     * unique identifier will be used for all groups. For example: if you use
     * Group.STUDY, all of the files produced by a specific study will be
     * included in one workflow run, but if there is another study by the same
     * name, it will not be grouped together.</p>
     *
     * <p>In contrast, general grouping is achieved by setting 'groupSimilar' to
     * true. The name of the entity will be used to group items together.
     * Continuing the example above, all of the studies with a particular name
     * will be grouped together. Depending on the Group, the decider may have
     * different behavior.</p>
     *
     * <p>When 'groupSimilar' is toggled on, the categories will group as
     * follows:</p>
     *
     * <ul> <li> Group.STUDY : Files produced by studies with the same name.
     * <li> Group.EXPERIMENT : experiments with the same name. <li> Group.DONOR
     * : Files produced from a single donor. A donor 'name' in this instance is
     * an identifier like ABCD_0001. This is a common grouping strategy since a
     * single donor may participate in different studies with different analysis
     * types (e.g. RNA or DNA) <li> Group.LIBRARY : Files produced from a single
     * donor library. This flag is often unnecessary because it's unlikely that
     * the same library name will exist in more than one study. <li>
     * Group.BARCODE : Files produced with a particular barcode at sequencing
     * time. This is not recommended except in QC workflows. <li> Group.LANE :
     * Files produced by a particular lane number. This is not recommended
     * except in QC workflows. <li> Group.SEQUENCER_RUN : Files produced by
     * sequencer runs with the same name. <li> Group.FILE : Files with the same
     * absolute file path </ul>
     *
     *
     * @param groupBy one of Group.STUDY, Group.EXPERIMENT, Group.DONOR,
     * Group.LIBRARY, Group.BARCODE, Group.LANE, Group.SEQUENCER_RUN, Group.FILE
     * @param groupSimilar whether or not to group items with identical names
     */
    public void setGroupBy(Group groupBy, boolean groupSimilar) {
        this.groupBy = groupBy;
        if (groupSimilar) {
            this.setGroupingStrategy(groupBy.getNameHeader());
        } else {
            this.setGroupingStrategy(groupBy.getSwaHeader());
        }
    }

    public int getNumberOfFilesPerGroup() {
        return numberOfFilesPerGroup;
    }

    public void setNumberOfFilesPerGroup(int expectedNumberOfFiles) {
        this.numberOfFilesPerGroup = expectedNumberOfFiles;
    }

    /**
     * Escape special characters in a string to html code. Takes a string as
     * input and converts all characters not contained in the regex [0-9A-Za-z
     * _-] to their corresponding html code.
     *
     * @param input string to be escaped
     * @return the input string with special characters escaped
     */
    public String escapeString(String input) {

        //don't escape characters defined by the regex
        Pattern allowedUnescapedCharactersRegex = Pattern.compile("[0-9A-Za-z _-]");

        //prefix/suffix used to create html code
        String escapedCharacterPrefix = "&#";
        String escapedCharacterSuffix = ";";

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            if (allowedUnescapedCharactersRegex.matcher(input.subSequence(i, i + 1)).find()) {
                sb.append(input.charAt(i));
            } else {
                sb.append(escapedCharacterPrefix);
                sb.append(input.codePointAt(i));
                sb.append(escapedCharacterSuffix);
            }
        }

        return sb.toString();
    }

    /**
     * <p>Creates a new file name from the input files given to it. The new
     * filename contains, in order: the donor, the tissue origin, the tissue
     * type, the library type, the library size, the library template type, and
     * the IUS ids. Duplicate values are only listed once, and multiple values
     * are separated by hyphens.</p>
     *
     * <p>E.g. Two files ABCD_0001_Ly_R_420_EX.bam and ABCD_0001_Pa_P_369_EX.bam
     * produce combined file name
     * ABCD_0001_Ly-Pa_P-R_369-420_EX_ius12345-12346.</p>
     *
     * <p>This method uses the metadata associated with the file, not the
     * filename itself, in order to determine these values.</p>
     *
     * @param inputFiles the list of files from WorkflowRun.getFiles();
     * @see WorkflowRun#getFiles()
     * @return a String representing the combined filename
     */
    protected String getCombinedFileName(FileAttributes[] inputFiles) {
        SortedSet<String> donor = null, origin = null, tType = null, lType = null, lSize = null, lTemplate = null, ius = null;
        for (FileAttributes fileAtt : inputFiles) {
            donor = addToSet(donor, fileAtt.getDonor());
            origin = addToSet(origin, fileAtt.getLimsValue(Lims.TISSUE_ORIGIN));
            tType = addToSet(tType, fileAtt.getLimsValue(Lims.TISSUE_TYPE));
            lType = addToSet(lType, fileAtt.getLimsValue(Lims.LIBRARY_TYPE));
            lSize = addToSet(lSize, fileAtt.getLimsValue(Lims.LIBRARY_SIZE));
            lTemplate = addToSet(lTemplate, fileAtt.getLimsValue(Lims.LIBRARY_TEMPLATE_TYPE));
            ius = addToSet(ius, fileAtt.getOtherAttribute(FindAllTheFiles.Header.IUS_SWA));
        }

        StringBuilder root = new StringBuilder();
        root.append(appendAll(donor, "_"));
        root.append(appendAll(origin, "_"));
        root.append(appendAll(tType, "_"));
        root.append(appendAll(lType, "_"));
        root.append(appendAll(lSize, "_"));
        root.append(appendAll(lTemplate, "_"));
        root.append("ius").append(appendAll(ius, ""));

        return root.toString();
    }

    private String appendAll(SortedSet<String> set, String end) {
        String s = "";
        for (Iterator<String> i = set.iterator(); i.hasNext();) {
            if (!s.isEmpty()) {
                s += "-";
            }
            s += i.next();
        }
        if (!s.isEmpty()) {
            s += end;
        }
        return s;
    }

    private SortedSet<String> addToSet(SortedSet<String> set, String value) {
        if (set == null) {
            set = new TreeSet<String>();
        }
        if (value != null) {
            set.add(value);
        }
        return set;
    }

    /*
     * A utility function that seraches for patterns in a file path
     * in order to identify mate in paired-end sequencing data
     * (or report that mate info is unavailable if no pattern detected)
     */
    private static int idMate (String filePath) {
        int [] indexes = {0, 1};
        int [] mates   = {OicrDecider.MATE_1, OicrDecider.MATE_2};
        for (int i : indexes) {
          for (int j = 0; j < readMateFlags[i].length; j++) {
           if (filePath.contains(readMateFlags[i][j])) {
            return mates[i];
           }
          }
        }
        return OicrDecider.MATE_UNDEF;
    }
}
