package ca.on.oicr.pde.deciders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import ca.on.oicr.gsi.common.transformation.MapStringifier;
import ca.on.oicr.gsi.common.transformation.StringSanitizerBuilder;
import ca.on.oicr.gsi.provenance.AnalysisProvenanceProvider;
import ca.on.oicr.gsi.provenance.DefaultProvenanceClient;
import ca.on.oicr.gsi.provenance.ExtendedProvenanceClient;
import ca.on.oicr.gsi.provenance.FileProvenanceFilter;
import ca.on.oicr.gsi.provenance.LaneProvenanceProvider;
import ca.on.oicr.gsi.provenance.MultiThreadedDefaultProvenanceClient;
import ca.on.oicr.gsi.provenance.PineryProvenanceProvider;
import ca.on.oicr.gsi.provenance.ProviderLoader;
import ca.on.oicr.gsi.provenance.SampleProvenanceProvider;
import ca.on.oicr.gsi.provenance.SeqwareMetadataAnalysisProvenanceProvider;
import ca.on.oicr.gsi.provenance.model.FileProvenance;
import ca.on.oicr.gsi.provenance.model.FileProvenanceFromAnalysisProvenance;
import ca.on.oicr.pde.deciders.configuration.StudyToOutputPathConfig;
import ca.on.oicr.pinery.client.PineryClient;
import java.time.ZonedDateTime;
import joptsimple.OptionSpec;
import net.sourceforge.seqware.common.err.NotFoundException;
import net.sourceforge.seqware.common.hibernate.FindAllTheFiles;
import net.sourceforge.seqware.common.hibernate.FindAllTheFiles.Header;
import net.sourceforge.seqware.common.model.FileProvenanceParam;
import net.sourceforge.seqware.common.model.LimsKey;
import net.sourceforge.seqware.common.module.FileMetadata;
import net.sourceforge.seqware.common.module.ReturnValue;

/**
 * <p>
 * An implementation of SeqWare's BasicDecider that simplifies the logic in
 * BasicDecider and provides OICR-specific functionality. OicrDecider and
 * accessory classes have a number of functions that depend on metadata
 * originating from the Geospiza LIMS and conventions defined in-house. This
 * document explains some of these assumptions and also lays out the methods to
 * use this class, since it differs significantly from BasicDecider.
 * </p>
 *
 * <p>
 * Most of the benefit of BasicDecider is in additional methods that hide the
 * low-level details from the implementor. You no longer need detailed knowledge
 * about the formatting of Geospiza attributes in order to implement a decider,
 * for example.
 * </p>
 *
 * <h1>Arguments</h1>
 * <ul>
 * <li>--check-file-exists : Only launch on the file if the file exists. By
 * default, SeqWare will launch on any file path, because it can take advantage
 * of remote files in S3 or ftp. At OICR, it is unlikely that we will use these
 * remote paths (the cluster is not internet accessible anyway) and we
 * occasionally have files that are listed in the metadata database that do not
 * exist.
 * <li>--skip-status-check : If enabled, will skip the check for the status of
 * the workflow run. By default, we do not include files from failed workflow
 * runs. This flag turns that checking off. This flag has no effect on sequencer
 * runs, lanes, IUSes or any downstream products marked with a 'skip' flag.
 * </ul>
 *
 * <h1>Extending OicrDecider</h1>
 *
 * <p>
 * OicrDecider should be extended in order to take advantage of the expanded
 * arguments and simplified implementation. See each method for more details
 * about implementation. All overridden methods should call super.method() in
 * order get the full benefit of this class.
 * </p>
 *
 * <p>
 * The following is a brief guide to the methods available when extending
 * OicrDecider. These methods are called in the order presented below. Note that
 * not all methods need to be overridden in order to make a functioning decider;
 * those highly recommended are marked with a *.
 * </p>
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
 * <li>{@link #separateFiles(List, String)} : override this method to modify the
 * list of files (i.e. ReturnValues) before they are split into different
 * groups.
 *
 * <li>{@link #handleGroupByAttribute(String)} : override this method to change
 * how files are grouped together. The String passed into the method is the
 * unmodified group identifier (the value retrieved from a file with the group
 * set by {@link #setGroupBy(Group, boolean)}. You can use this method to modify
 * it as desired. Files with the identical String returned from this method will
 * be grouped and run together in one workflow run.
 *
 * <li>*checkFileDetails: Override one of these methods to filter files based on
 * their metadata information.
 *
 * <ul>
 * <li>The BasicDecider {@link #checkFileDetails(ReturnValue, FileMetadata)}
 * method uses {@link ReturnValue} and {@link FileMetadata}. {@code ReturnValue}
 * has a list of attributes that contain the metadata for the file in question.
 * These attributes can be retrieved using {@link FindAllTheFiles.Header}
 * objects. A {@code ReturnValue} represents one processing event in a previous
 * workflow run and thereby can represent more than one file. The accompanying
 * {@code FileMetadata} is the specific file that the method is evaluating at
 * that time. Lims-specific attributes are somewhat difficult to retrieve with
 * this system.
 *
 * <li>The OicrDecider {@link #checkFileDetails(FileAttributes) } method uses
 * {@link FileAttributes} instead, which simplifies the mechanism to retrieve
 * commonly used metadata (e.g. donor, library sample name, sequencer run name)
 * and Geospiza LIMS-specific attributes. See {@link FileAttributes} for more
 * information.
 * </ul>
 *
 * <li>{@link #doFinalCheck(String,String)} : parse a group of files that will
 * be run together and do a final check to confirm that they should launch.
 *
 * <li>*Customize each workflow run : Override one of the following methods to
 * appropriately parameterize each workflow run using the files involved.
 *
 * <ul>
 * <li>The BasicDecider {@link #modifyIniFile(String, String)} has you return a
 * Map which represents the INI file for the workflow.
 *
 * <li>The OicrDecider {@link #customizeRun(WorkflowRun)} hides the process of
 * creating an INI file and instead allows you to configure a
 * {@link WorkflowRun} object. The intention of this method was to a) provide
 * easy access to file metadata and b) simplify the creation of a one-step
 * workflow by providing convenience methods to specify the script, memory, and
 * params. New properties for the INI are added to the WorkflowRun.
 * </ul>
 *
 * </ol>
 *
 *
 * @author Pipeline Development and Evaluation (pde@lists.oicr.on.ca)
 */
public class OicrDecider extends BasicDecider {

	private final Logger log = LogManager.getLogger(OicrDecider.class);
	private Group groupBy = null;
	protected Map<String, FileAttributes> files;
	private int numberOfFilesPerGroup = Integer.MIN_VALUE;
	private List<String> requiredParams = new ArrayList<>();
        private static final String[][] READ_MATE_FLAGS = {{"_R1_", "1_sequence.txt", ".1.fastq", "_R1.fastq"},
            {"_R2_", "2_sequence.txt", ".2.fastq", "_R2.fastq"}};
	public static final int MATE_UNDEF = 0;
	public static final int MATE_1 = 1;
	public static final int MATE_2 = 2;
	protected Date afterDate = null;
	protected Date beforeDate = null;
	private SimpleDateFormat format;
	private WorkflowRun run;
	private boolean isFailed = false;
	protected ExtendedProvenanceClient provenanceClient;
	private final Function<String, String> stringSanitizer;
	private final Function<String, String> ssForAttributes;
	protected StudyToOutputPathConfig studyToOutputPathConfig = null;
	private EnumMap<FileProvenanceFilter, Set<String>> includeFilters = new EnumMap<>(FileProvenanceFilter.class);
	private EnumMap<FileProvenanceFilter, Set<String>> excludeFilters = new EnumMap<>(FileProvenanceFilter.class);
	private final EnumMap<FileProvenanceFilter, OptionSpec<String>> includeFilterOpts;
	private final EnumMap<FileProvenanceFilter, OptionSpec<String>> excludeFilterOpts;
	protected WorkflowRun currentWorkflowRun;

	/**
	 * <p>
	 * Sets up the decider arguments and global variables. Any arguments intended to
	 * be used on the command line should be added using either The constructor runs
	 * before the arguments are parsed from the command line.SeqWare uses the JOpt
	 * package to parse command line parameters. OicrDecider builds on top of that.
	 * There are several mechanisms you can use to pull arguments from the command
	 * line.
	 * <p>
	 *
	 * <ul>
	 * <li>Flags: You add can flags to the decider by using {@link #parser
	 * parser}{@code .accepts(String argument, String description)} and retrieve
	 * them in later methods using {@link #options options}{@code .has(argument)}.
	 * <li>Parameters: If your argument takes a parameter, you can use the
	 * convenience method {@link #defineArgument(String, String, boolean)
	 * defineArgument} and retrieve the argument later with
	 * {@link #getArgument(String) getArgument}
	 * </ul>
	 *
	 */
	public OicrDecider() {
		super();

		parser.acceptsAll(Arrays.asList("check-file-exists", "cf"),
				"Optional: only launch on the file if the file exists");
		parser.accepts("skip-status-check",
				"Optional: If enabled will skip the check for the status of the sequencer run/lane/IUS/workflow run");
		parser.acceptsAll(Arrays.asList("help", "h"), "Prints this help message");
		parser.accepts("verbose", "Enable debug logging");
		defineArgument("output-path",
				"The absolute path of the directory to put the final file(s) (workflow output-prefix option).", false);
		defineArgument("study-to-output-path-csv",
				"The absolulte path to the \"Study To Output Path\" CSV file that defines the workflow \"output-prefix\" (where the workflow output files should be written to)",
				false);
		defineArgument("output-folder", "The relative path to put the final result(s) (workflow output-dir option).",
				false);
		defineArgument("after-date",
				"Optional: Format YYYY-MM-DD. Only run on files that have been modified after a certain date, not inclusive.",
				false);
		defineArgument("before-date",
				"Optional: Format YYYY-MM-DD. Only run on files that have been modified before a certain date, not inclusive.",
				false);
		defineArgument("pinery-url",
				"The Pinery URL that should be used to get SampleProvenance LIMS metadata (eg, http://localhost:8080).",
				false);
		defineArgument("provenance-settings", "Path to provenance settings file.", false);
		format = new SimpleDateFormat("yyyy-MM-dd");

		// functions used for converting file provenance objects to map data structure
		StringSanitizerBuilder ssbForFields = new StringSanitizerBuilder();
		// ssbForFields.add("\t", "\u2300");
		ssbForFields.add(";", "\u2300");
		ssbForFields.add("=", "\u2300");
		ssbForFields.add("&", "\u2300");
		ssbForFields.add(" ", "_");
		stringSanitizer = ssbForFields.build()::apply;

		StringSanitizerBuilder ssbForAttributes = new StringSanitizerBuilder();
		// ssbForAttributes.add("\t", " ");
		ssbForAttributes.add(";", "\u2300");
		ssbForAttributes.add("=", "\u2300");
		ssbForAttributes.add("&", "\u2300");
		ssForAttributes = ssbForAttributes.build()::apply;

		includeFilterOpts = new EnumMap<>(FileProvenanceFilter.class);
		excludeFilterOpts = new EnumMap<>(FileProvenanceFilter.class);
		for (FileProvenanceFilter filter : getSupportedFilters()) {
			includeFilterOpts.put(filter, parser.accepts("include-" + filter.toString()).withRequiredArg()
					.withValuesSeparatedBy(",").ofType(String.class));
			excludeFilterOpts.put(filter, parser.accepts("exclude-" + filter.toString()).withRequiredArg()
					.withValuesSeparatedBy(",").ofType(String.class));
		}
	}

	protected static FileProvenanceFilter[] getSupportedFilters() {
		return FileProvenanceFilter.values();
	}

	public OicrDecider(ExtendedProvenanceClient provenanceClient) {
		this();
		this.provenanceClient = provenanceClient;
	}

	public void setProvenanceClient(ExtendedProvenanceClient provenanceClient) {
		this.provenanceClient = provenanceClient;
	}

	/**
	 * Define an argument for the Decider to use on the command line.
	 *
	 * @param command
	 *            the argument to use. Single letters will use -, and more
	 *            characters will use --
	 * @param description
	 *            the description of the argument to give when giving help
	 * @param required
	 *            whether or not the argument is required for the decider to
	 *            function. The presence of this argument is tested in the
	 *            {@link #init() init} method, which will throw an exception if the
	 *            argument is not present.
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
	 * presence of your argument using {@link #options
	 * options}{@code .has(argument)} and retrieve it using {@link #options
	 * options}{@code .valueOf(argument))}
	 *
	 *
	 * {@inheritDoc}
	 *
	 *
	 * @param arg
	 *            an argument previously defined by 'defineArgument'.
	 *
	 * @return the value provided on the command line, or empty string if none
	 *         provided.
	 */
	protected String getArgument(String arg) {
		Object o = options.valueOf(arg);
		if (o == null || o.toString().isEmpty()) {
			log.debug("Command line argument is not available: " + arg);
			return "";
		} else {
			return o.toString();
		}
	}

	/**
	 * Initializes the decider using arguments on the command line. Any arguments
	 * defined as 'required' by
	 * {@link #defineArgument(java.lang.String, java.lang.String, boolean) } are
	 * tested for in this method.
	 *
	 * @return {@link ReturnValue} with an appropriate exit status.
	 */
	@Override
	public ReturnValue init() {
		ReturnValue ret = new ReturnValue();
		
		if (this.options.has("verbose")) {
			// log4j logging configuration
			org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("ca.on.oicr");
			logger.setLevel(org.apache.log4j.Level.DEBUG);
			logger.removeAllAppenders();
			logger.addAppender(new org.apache.log4j.ConsoleAppender(
					new org.apache.log4j.PatternLayout("%p [%d{yyyy/MM/dd HH:mm:ss}] | %m%n")));

			// log4j2 logging configuration
			Configurator.setRootLevel(org.apache.logging.log4j.Level.INFO);
			Configurator.setLevel("ca.on.oicr", org.apache.logging.log4j.Level.DEBUG);
		}

		if (provenanceClient == null && options.has("provenance-settings")) {
			ProviderLoader providerLoader;
			try {
				providerLoader = new ProviderLoader(Paths.get(options.valueOf("provenance-settings").toString()));
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}

			MultiThreadedDefaultProvenanceClient provenanceClientImpl = new MultiThreadedDefaultProvenanceClient();
			for (Entry<String, AnalysisProvenanceProvider> e : providerLoader.getAnalysisProvenanceProviders()
					.entrySet()) {
				provenanceClientImpl.registerAnalysisProvenanceProvider(e.getKey(), e.getValue());
			}
			for (Entry<String, LaneProvenanceProvider> e : providerLoader.getLaneProvenanceProviders().entrySet()) {
				provenanceClientImpl.registerLaneProvenanceProvider(e.getKey(), e.getValue());
			}
			for (Entry<String, SampleProvenanceProvider> e : providerLoader.getSampleProvenanceProviders().entrySet()) {
				provenanceClientImpl.registerSampleProvenanceProvider(e.getKey(), e.getValue());
			}

			provenanceClient = provenanceClientImpl;
		}

		// initialize collections
		files = new HashMap<>();

		if (options.has("help")) {
			System.err.println(get_syntax());
			ret.setExitStatus(ReturnValue.RETURNEDHELPMSG);
			return ret;
		}
		ret = super.init();
		// Sanity checking for required parameters
		for (String option : requiredParams) {
			if (!options.has(option)) {
				log.warn("Required argument missing: --" + option);
				ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
				System.out.println(get_syntax());
			}
		}

		if (options.has("after-date")) {
			String dateString = options.valueOf("after-date").toString();
			try {
				afterDate = format.parse(dateString);
			} catch (ParseException e) {
				log.error("After Date should be in the format: " + format.toPattern(), e);
				ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
			}
		}
		if (options.has("before-date")) {
			String dateString = options.valueOf("before-date").toString();
			try {
				beforeDate = format.parse(dateString);
			} catch (ParseException e) {
				log.error("Before Date should be in the format: " + format.toPattern(), e);
				ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
			}
		}
		if (options.has("pinery-url")) {
			PineryClient pineryClient = new PineryClient(options.valueOf("pinery-url").toString(), false);
			PineryProvenanceProvider pineryProvenanceProvider = new PineryProvenanceProvider(pineryClient);
			DefaultProvenanceClient dpc = new DefaultProvenanceClient();
			dpc.registerAnalysisProvenanceProvider("seqware", new SeqwareMetadataAnalysisProvenanceProvider(metadata));
			dpc.registerSampleProvenanceProvider("pinery", pineryProvenanceProvider);
			dpc.registerLaneProvenanceProvider("pinery", pineryProvenanceProvider);
			provenanceClient = dpc;
		}

		if (options.has("study-to-output-path-csv")) {
			if (options.has("output-path")) {
				log.error("Use \"study-to-output-path-csv\" or \"output-path\" - not both");
				ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
			}

			try {
				studyToOutputPathConfig = new StudyToOutputPathConfig(
						options.valueOf("study-to-output-path-csv").toString());
			} catch (IOException ex) {
				log.error("\"study-to-output-path-csv\" is not accessible");
				ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
			}
		}

		includeFilters = new EnumMap<>(FileProvenanceFilter.class);
		for (Entry<FileProvenanceFilter, OptionSpec<String>> e : includeFilterOpts.entrySet()) {
			if (options.has(e.getValue())) {
				includeFilters.put(e.getKey(), ImmutableSet.copyOf(options.valuesOf(e.getValue())));
			}
		}

		excludeFilters = new EnumMap<>(FileProvenanceFilter.class);
		for (Entry<FileProvenanceFilter, OptionSpec<String>> e : excludeFilterOpts.entrySet()) {
			if (options.has(e.getValue())) {
				excludeFilters.put(e.getKey(), ImmutableSet.copyOf(options.valuesOf(e.getValue())));
			}
		}

		// to be enabled at a later date
		// if (!(options.has("--all") ^ !includeFilters.isEmpty() ^
		// !excludeFilters.isEmpty())) {
		// log.error("--all or a combination the following include/exclude filters
		// should be specified ["
		// + Joiner.on(",").join(Lists.transform(Arrays.asList(getSupportedFilters()),
		// Functions.toStringFunction()))
		// + "].");
		// ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
		// }
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
		log.debug("CHECK FILE DETAILS:" + fm);

		if (this.options.has("check-file-exists")) {
			if (!new File(fm.getFilePath()).exists()) {
				log.warn("File not found:" + fm.getFilePath());
				return false;
			}
		}

		// SEQWARE-1809, PDE-474 ensure that deciders only use input from completed
		// workflow runs
		FileAttributes attributes = new FileAttributes(returnValue, fm);
		String workflowRunStatus = returnValue.getAttribute(FindAllTheFiles.WORKFLOW_RUN_STATUS);
		if (workflowRunStatus == null || !workflowRunStatus.equals("completed")) {
			return false;
		}

		// check if record should be skipped
		boolean skip = Boolean.parseBoolean(returnValue.getAttribute("Skip"));
		if (skip) {
			log.debug("Ignoring file because the file provenance record is skip status is set to true "
					+ fm.getFilePath());
			return false;
		}

		// check if record's provenance status is okay
		FileProvenance.Status provenanceStatus = FileProvenance.Status.valueOf(returnValue.getAttribute("Status"));
		if (!FileProvenance.Status.OKAY.equals(provenanceStatus)) {
			log.warn("Ignoring file because the file provenance record status is [" + provenanceStatus.toString() + "] "
					+ fm.getFilePath());
			return false;
		}

		if (!options.has("skip-status-check")) {
			for (String attribute : attributes) {
				if (attribute.contains("Status")) {
					if (attributes.getOtherAttribute(attribute).equals("failed")) {
						log.debug("Ignoring file because the workflow run status is failed " + fm.getFilePath());
						return false;
					}
				}
			}
		}
		String dateString = attributes.getOtherAttribute(FindAllTheFiles.Header.PROCESSING_DATE);
		if (options.has("after-date") && !isAfterDate(dateString, afterDate)) {
			log.debug(
					"File was processed before the after-date " + afterDate.toString() + " : " + attributes.getPath());
			return false;
		}
		if (options.has("before-date") && !isBeforeDate(dateString, beforeDate)) {
			log.debug(
					"File was processed after the before-date " + beforeDate.toString() + " : " + attributes.getPath());
			return false;
		}
		if (!checkFileDetails(attributes)) {
			return false;
		}
		files.put(fm.getFilePath(), attributes);
		return toReturn;
	}

	/**
	 * Helper method that determines if a date string is after a reference date.
	 *
	 * @param dateString
	 *            the date to check against a reference date
	 * @param afterDate
	 *            the reference date
	 *
	 * @return true if dateString is after "afterDate", false otherwise
	 */
	public boolean isAfterDate(String dateString, Date afterDate) {

		try {
			Date fileDate = format.parse(dateString);
			if (fileDate.after(afterDate)) {
				return true;
			}
		} catch (ParseException e) {
			log.error("File date is not in the format: " + format.toPattern(), e);
		}
		return false;
	}

	/**
	 * Helper method that determines if a date string is before a reference date.
	 *
	 * @param dateString
	 *            the date to check against a reference date
	 * @param beforeDate
	 *            the reference date
	 *
	 * @return true if dateString is before "beforeDate", false otherwise
	 */
	public boolean isBeforeDate(String dateString, Date beforeDate) {

		try {
			Date fileDate = format.parse(dateString);
			if (fileDate.before(beforeDate)) {
				return true;
			}
		} catch (ParseException e) {
			log.error("File date is not in the format: " + format.toPattern(), e);
		}
		return false;
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
		ReturnValue r = new ReturnValue();
		r.setExitStatus(ReturnValue.SUCCESS);

		String[] paths = commaSeparatedFilePaths.split(",");
		FileAttributes[] attributes = new FileAttributes[paths.length];
		for (int i = 0; i < paths.length; i++) {
			attributes[i] = files.get(paths[i]);
		}

		// create a new workflow run (with a blank set of ini properties) for each final
		// check
		run = new WorkflowRun(null, attributes);
		currentWorkflowRun = run;

		// Common decider ini modifications
		run.addProperty(getCommonIniProperties());

		if (studyToOutputPathConfig != null) {
			Set<String> outputPrefixes = new HashSet<>();
			for (FileAttributes fa : attributes) {
				String studyTitle = fa.getStudy();
				if (studyTitle == null || studyTitle.isEmpty()) {
					log.warn("Blank study title for workflow run - expected one study title.\n" + "Files = ["
							+ Joiner.on(",\n").join(attributes) + "]");
					r.setExitStatus(ReturnValue.INVALIDFILE);
					return r;
				} else {
					try {
						outputPrefixes.add(studyToOutputPathConfig.getOutputPathForStudy(fa.getStudy()));
					} catch (NotFoundException nfe) {
						log.error("Study title [" + studyTitle + "] was not found in study-to-output-path-csv");
						r.setExitStatus(ReturnValue.INVALIDFILE);
						return r;
					}
				}
			}

			if (outputPrefixes.size() == 1) {
				run.addProperty("output_prefix", Iterables.getOnlyElement(outputPrefixes));
			} else {
				log.error("[" + outputPrefixes.size()
						+ "] output prefixes found for workflow run - expected one output prefix");
				r.setExitStatus(ReturnValue.INVALIDFILE);
				return r;
			}
		}

		// Check for expected number of input files
		log.debug("Number of files: " + run.getFiles().length);
		if (numberOfFilesPerGroup != Integer.MIN_VALUE) {
			if (run.getFiles().length != numberOfFilesPerGroup) {
				log.debug("Invalid number of files: " + run.getFiles().length + ":" + run.getFiles()[0]);
				r.setExitStatus(ReturnValue.INVALIDFILE);
				return r;
			}
		}

		return r;
	}

	/**
	 * OicrDecider's implementation of getSwidsToLinkWorkflowRunTo() clones the
	 * input IusLimsKey object(s)
	 *
	 * @param iusLimsKeySwids
	 *
	 * @return the IUS accessions of the cloned IusLimsKeys, null if there was an
	 *         error when building the list
	 *
	 * @throws java.lang.Exception
	 *             if LimsKey cannot be found
	 */
	@Override
	protected Set<String> getSwidsToLinkWorkflowRunTo(Set<String> iusLimsKeySwids) throws Exception {
		Map<String, String> inputIusToOutputIus = new HashMap<>();
		Set<String> outputIusLimsKeys;

		if (getMetadataWriteback()) {
			// TODO: each file provenance record already has its associated IusLimsKey(s)
			// refactor this implementation to use file provenance rather than getting the
			// LimsKey(s) again from seqware
			// refactor this implementation to clone all LimsKeys in one seqware
			// call/transaction

			// Get all the LimsKey(s) info from seqware
			SetMultimap<ComparableLimsKey, String> limsKeyToIusMap = HashMultimap.create();
			Set<ComparableLimsKey> limsKeys = new HashSet<>();
			for (String swid : iusLimsKeySwids) {
				LimsKey limsKey = metadata.getLimsKeyFrom(Integer.parseInt(swid));
				if (limsKey == null) {
					throw new Exception("No LimsKey found for SWID = [" + swid + "]");
				}
                                //create a new LimsKey object that only uses provider+id+version+lastModified for equality
                                ComparableLimsKey comparableLimsKey = new ComparableLimsKey(limsKey);
				limsKeys.add(comparableLimsKey);
				limsKeyToIusMap.put(comparableLimsKey, swid);
			}

			// Clone the upstream LimsKeys and update the input IUS to new/output IUS map
			if (isDryRunMode()) {
				// populate inputIusToOutputIusForCurrentWorkflowRun blank output IUS
				for (String inputIUS : iusLimsKeySwids) {
					inputIusToOutputIus.put(inputIUS, null);
				}

				// dry run mode enabled - do nothing
				outputIusLimsKeys = Collections.emptySet();
			} else {
				Set<String> newIusSwids = new HashSet<>();
				for (ComparableLimsKey limsKey : limsKeys) {
					// Create a LimsKey -> create IUS -> link LimsKey to IUS
					Integer newLimsKeySwid = metadata.addLimsKey(limsKey.getProvider(), limsKey.getId(),
							limsKey.getVersion(), limsKey.getLastModified());
					Integer newIusSwid = metadata.addIUS(newLimsKeySwid, false);
					newIusSwids.add(newIusSwid.toString());

					// map all input iuses to the new ius
					for (String inputIus : limsKeyToIusMap.get(limsKey)) {
						if (inputIusToOutputIus.put(inputIus, newIusSwid.toString()) != null) {
							throw new Exception("Input IUS SWID [" + inputIus + "] maps to multiple output iuses");
						}
					}
				}

				outputIusLimsKeys = newIusSwids;
			}
		} else {
			// populate inputIusToOutputIusForCurrentWorkflowRun blank output IUS
			for (String inputIUS : iusLimsKeySwids) {
				inputIusToOutputIus.put(inputIUS, null);
			}

			// metadata writeback disabled - do nothing
			outputIusLimsKeys = Collections.emptySet();
		}

		// set the input IUS-LimsKey to output IUS-LimsKey map for the current workflow
		// run
		currentWorkflowRun.setInputIusToOutputIus(inputIusToOutputIus);

		// set the ouput IUS list to link the current workflow run
		// note: this set is currently not used - rather BasicDecider uses the set
		// returned by this method
		List<Integer> outputIusAsInt = new ArrayList<>();
		for (String ius : outputIusLimsKeys) {
			outputIusAsInt.add(Integer.parseInt(ius));
		}
		currentWorkflowRun.setIusSwidsToLinkWorkflowRunTo(outputIusAsInt);

		return outputIusLimsKeys;
	}

	@Deprecated
	public ReturnValue customizeRun(WorkflowRun run) {

		return new ReturnValue();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Map<String, String> modifyIniFile(String commaSeparatedFilePaths, String commaSeparatedParentAccessions) {

		// Get default ini file
		run.addProperty(super.modifyIniFile(commaSeparatedFilePaths, commaSeparatedParentAccessions));

		// Set the final return value to non-zero as the return value from customizeRun
		// does not actually affect the run state.
		ReturnValue ignoredReturnValue = customizeRun(run);
		if (ignoredReturnValue.getExitStatus() != ReturnValue.SUCCESS) {
			log.error(
					"This decider is using customizeRun to abort workflow runs - this functionality is not supported.  Please submit a bug.");
			setFinalStatusToFailed();
		}

		return run.getIniFile();
	}

	protected Map<String, String> getCommonIniProperties() {
		Map<String, String> commonIniProperties = new HashMap<>();

		String outputPath = "./";
		if (options.has("output-path")) {
			outputPath = getArgument("output-path");
			if (outputPath.endsWith("/")) {
				// do nothing
			} else {
				outputPath += "/";
			}
		}
		commonIniProperties.put("output_prefix", outputPath);

		String outputDir = "seqware-results";
		if (options.has("output-folder")) {
			outputDir = getArgument("output-folder");
		}
		commonIniProperties.put("output_dir", outputDir);

		return commonIniProperties;
	}

	@Override
	public ReturnValue do_summary() {
		ReturnValue rv = super.do_summary();

		// decider run failed somewhere, but some how the decider did not terminate -
		// set the exit code to non-zero
		if (isFailed) {
			rv.setReturnValue(ReturnValue.FAILURE);
		}
		return rv;
	}

	private void setFinalStatusToFailed() {
		isFailed = true;
	}

	/**
	 * Sets how to group files together. When a group is chosen, all of the files
	 * that have been generated for that group will be grouped together and then
	 * filtered by meta-type. Grouping is either specific according to distinct
	 * identifier or general by name.
	 *
	 * <p>
	 * Specific grouping is achieved by setting 'groupSimilar' to false. The unique
	 * identifier will be used for all groups. For example: if you use Group.STUDY,
	 * all of the files produced by a specific study will be included in one
	 * workflow run, but if there is another study by the same name, it will not be
	 * grouped together.
	 * </p>
	 *
	 * <p>
	 * In contrast, general grouping is achieved by setting 'groupSimilar' to true.
	 * The name of the entity will be used to group items together. Continuing the
	 * example above, all of the studies with a particular name will be grouped
	 * together. Depending on the Group, the decider may have different behavior.
	 * </p>
	 *
	 * <p>
	 * When 'groupSimilar' is toggled on, the categories will group as follows:
	 * </p>
	 *
	 * <ul>
	 * <li>Group.STUDY : Files produced by studies with the same name.
	 * <li>Group.EXPERIMENT : experiments with the same name.
	 * <li>Group.DONOR : Files produced from a single donor. A donor 'name' in this
	 * instance is an identifier like ABCD_0001. This is a common grouping strategy
	 * since a single donor may participate in different studies with different
	 * analysis types (e.g. RNA or DNA)
	 * <li>Group.LIBRARY : Files produced from a single donor library. This flag is
	 * often unnecessary because it's unlikely that the same library name will exist
	 * in more than one study.
	 * <li>Group.BARCODE : Files produced with a particular barcode at sequencing
	 * time. This is not recommended except in QC workflows.
	 * <li>Group.LANE : Files produced by a particular lane number. This is not
	 * recommended except in QC workflows.
	 * <li>Group.SEQUENCER_RUN : Files produced by sequencer runs with the same
	 * name.
	 * <li>Group.FILE : Files with the same absolute file path
	 * </ul>
	 *
	 *
	 * @param groupBy
	 *            one of Group.STUDY, Group.EXPERIMENT, Group.DONOR, Group.LIBRARY,
	 *            Group.BARCODE, Group.LANE, Group.SEQUENCER_RUN, Group.FILE
	 * @param groupSimilar
	 *            whether or not to group items with identical names
	 */
	public void setGroupBy(Group groupBy, boolean groupSimilar) {
		this.groupBy = groupBy;
		if (groupSimilar) {
			this.setGroupingStrategy(groupBy.getNameHeader());
		} else {
			this.setGroupingStrategy(groupBy.getSwaHeader());
		}
	}

	/**
	 * Get the OicrDecider Group (a simplified version of "FindAllTheFiles" header
	 * fields)
	 *
	 * @return the OicrDecider grouping strategy (Group) or null if not set
	 *
	 * @see Group
	 * @see FindAllTheFiles.Header
	 * @see OicrDecider#setGroupBy(Group, boolean)
	 * @see BasicDecider#getGroupingStrategy()
	 */
	public Group getGroupBy() {
		return this.groupBy;
	}

	/**
	 * Get the number of expected files per workflow run group.
	 *
	 * @return number of files, or Integer.MIN_VALUE if not set.
	 */
	public int getNumberOfFilesPerGroup() {
		return numberOfFilesPerGroup;
	}

	/**
	 * Set the number of expected files per workflow run group. This is used by
	 * {@link #doFinalCheck(java.lang.String, java.lang.String)} to determine if the
	 * workflow run input file set is valid.
	 *
	 * @param expectedNumberOfFiles
	 */
	public void setNumberOfFilesPerGroup(int expectedNumberOfFiles) {
		this.numberOfFilesPerGroup = expectedNumberOfFiles;
	}

	/**
	 * Escape special characters in a string to html code. Takes a string as input
	 * and converts all characters not contained in the regex [0-9A-Za-z _-] to
	 * their corresponding html code.
	 *
	 * @param input
	 *            string to be escaped
	 *
	 * @return the input string with special characters escaped
	 */
	public String escapeString(String input) {

		// don't escape characters defined by the regex
		Pattern allowedUnescapedCharactersRegex = Pattern.compile("[0-9A-Za-z _-]");

		// prefix/suffix used to create html code
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
	 * <p>
	 * Creates a new file name from the input files given to it. The new filename
	 * contains, in order: the donor, the tissue origin, the tissue type, the
	 * library type, the library size, the library template type, the group id, and
	 * the IUS ids. Duplicate values are only listed once, and multiple values are
	 * separated by hyphens.
	 * </p>
	 *
	 * <p>
	 * E.g. Two files ABCD_0001_Ly_R_420_EX.bam and ABCD_0001_Pa_P_369_EX.bam
	 * produce combined file name ABCD_0001_Ly-Pa_P-R_369-420_EX_ius12345-12346.
	 * </p>
	 *
	 * <p>
	 * This method uses the metadata associated with the file, not the filename
	 * itself, in order to determine these values.
	 * </p>
	 *
	 * @param inputFiles
	 *            the list of files from WorkflowRun.getFiles();
	 *
	 * @see WorkflowRun#getFiles()
	 * @return a String representing the combined filename
	 */
	protected String getCombinedFileName(FileAttributes[] inputFiles) {
		SortedSet<String> donor = null, origin = null, tType = null, lType = null, lSize = null, lTemplate = null,
				lGroupid = null, ius = null;
		for (FileAttributes fileAtt : inputFiles) {
			donor = addToSet(donor, fileAtt.getDonor());
			origin = addToSet(origin, fileAtt.getLimsValue(Lims.TISSUE_ORIGIN));
			tType = addToSet(tType, fileAtt.getLimsValue(Lims.TISSUE_TYPE));
			lType = addToSet(lType, fileAtt.getLimsValue(Lims.LIBRARY_TYPE));
			lSize = addToSet(lSize, fileAtt.getLimsValue(Lims.LIBRARY_SIZE));
			lTemplate = addToSet(lTemplate, fileAtt.getLimsValue(Lims.LIBRARY_TEMPLATE_TYPE));
			lGroupid = addToSet(lGroupid, fileAtt.getLimsValue(Lims.GROUP_ID));
			ius = addToSet(ius, fileAtt.getOtherAttribute(FindAllTheFiles.Header.IUS_SWA));
		}

		StringBuilder root = new StringBuilder();
		root.append(appendAll(donor, "_"));
		root.append(appendAll(origin, "_"));
		root.append(appendAll(tType, "_"));
		root.append(appendAll(lType, "_"));
		root.append(appendAll(lSize, "_"));
		root.append(appendAll(lTemplate, "_"));
		root.append(appendAll(lGroupid, "_"));
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
			set = new TreeSet<>();
		}
		if (value != null) {
			set.add(value);
		}
		return set;
	}

	/**
	 * A utility function that searches for patterns in a file path in order to
	 * identify mate in paired-end sequencing data (or report that mate info is
	 * unavailable if no pattern detected).
	 *
	 * @param filePath
	 *            file path to identify if mate 1 or 2
	 *
	 * @return 1 if mate 1, 2 if mate 2, 0 if not able to find mate value
	 */
	public static int idMate(String filePath) {
            return idMate(filePath, READ_MATE_FLAGS);
        }

        /**
         * A utility function that searches for patterns in a file path in order to
         * identify mate in paired-end sequencing data (or report that mate info is
         * unavailable if no pattern detected).
         *
         * @param filePath
         *                 file path to identify if mate 1 or 2
         * @param readMateFlags double array of read 1 patterns in the first array and read 2 patterns in the second array
         *
         * @return 1 if mate 1, 2 if mate 2, 0 if not able to find mate value
         */
        public static int idMate(String filePath, String[][] readMateFlags) {
            int[] indexes = {0, 1};
            int[] mates = {OicrDecider.MATE_1, OicrDecider.MATE_2};
            for (int i : indexes) {
                for (int j = 0; j < readMateFlags[i].length; j++) {
                    if (filePath.contains(readMateFlags[i][j])) {
                        return mates[i];
                    }
                }
            }
            return OicrDecider.MATE_UNDEF;
        }

	/**
	 * Uses idMate to put read1 and read2 files into order and return the set. Tests
	 * to make sure that the read names as well as the list of reads are sensible.
	 * If it encounters a problem, it returns the same set you passed in originally
	 * along with printing a warning.
	 *
	 * @param files
	 *            input files to be sorted
	 *
	 * @return sorted array of files
	 */
	public FileAttributes[] arrangeFastqs(FileAttributes[] files) {
		FileAttributes[] inputs = new FileAttributes[files.length];
		for (FileAttributes file : files) {
			int index = idMate(file.getPath()) - 1;
			if (index == (OicrDecider.MATE_UNDEF - 1)) {
				if (files.length > 1) {
					log.warn("Unidentifiable read number! " + file.toString());
				}
				inputs = files;
				break;
			} else if (index >= inputs.length) {
				log.warn(
						"The read number is larger than the amount of reads given for this study. e.g. this is a read 2 but there is only one file.");
				log.warn(index + "is the read number for file " + file);
				inputs = files;
				break;
			} else {
				inputs[index] = file;
			}
		}
		return inputs;
	}

	/**
	 * Gets the key (Lims or FindAllTheFiles.Header) value from the FileAttributes
	 * object, throws a RuntimeException if the value is missing. Use
	 * getOptionalAttribute() if the RuntimeException is undesired.
	 *
	 * @param fa
	 *            FileAttribute object to search through.
	 * @param key
	 *            The FindAllTheFiles.Header or Lims enum key.
	 *
	 * @return A string value for the FileAttribute key requested.
	 *
	 * @throws RuntimeException
	 *             if a value is not set for the key in the FileAttributes object.
	 */
	public String getRequiredAttribute(FileAttributes fa, Enum<?> key) {
		String value = getAttribute(fa, key);
		if (value == null) {
			throw new RuntimeException(
					String.format("File swid = [%s] is missing a required metadata attribute = [%s].",
							fa.getOtherAttribute(FindAllTheFiles.Header.FILE_SWA), key));
		}
		return value;
	}

	/**
	 * Gets the key (Lims or FindAllTheFiles.Header) value from the FileAttributes
	 * object, returns empty string if the value is missing. Use
	 * getRequiredAttribute() if the "empty string on missing value" is undesired.
	 *
	 * @param fa
	 *            FileAttribute object to search through.
	 * @param key
	 *            The FindAllTheFiles.Header or Lims enum key.
	 *
	 * @return A string value for the requested FileAttribute key or empty string if
	 *         the key value is null.
	 *
	 */
	public String getOptionalAttribute(FileAttributes fa, Enum<?> key) {
		String value = getAttribute(fa, key);
		if (value == null) {
			value = "";
		}
		return value;
	}

	private String getAttribute(FileAttributes fa, Enum<?> key) {
		String value;
		if (key == null || fa == null) {
			throw new IllegalArgumentException("Null arguments");
		} else if (key instanceof FindAllTheFiles.Header) {
			value = fa.getOtherAttribute((FindAllTheFiles.Header) key);
		} else if (key instanceof Lims) {
			value = fa.getLimsValue((Lims) key);
		} else {
			throw new IllegalArgumentException("Only Header and Lims key type supported");
		}
		return value;
	}

	/**
	 * Builds a standard prefix from a FileAttributes. Uses Lims and
	 * FindAllTheFiles.Header enum keys to build a prefix string from a
	 * FileAttributes object.
	 *
	 * @param fa
	 *            FileAttribute object to build prefix from.
	 *
	 * @return A prefix string with the format
	 *         "SWID_{IUS_SWA}_{SAMPLE_NAME}[_{GROUP_ID}]_{SEQUENCER_RUN_NAME}_{IUS_TAG}_L00{LANE_NUM}_R1_001_"
	 *
	 */
	public String getPrefixFromFileMetadata(FileAttributes fa) {
		StringBuilder sb = new StringBuilder();
		sb.append("SWID").append("_").append(getRequiredAttribute(fa, FindAllTheFiles.Header.IUS_SWA));
		sb.append("_").append(getRequiredAttribute(fa, FindAllTheFiles.Header.SAMPLE_NAME));
		if (getAttribute(fa, Lims.GROUP_ID) != null) {
			sb.append("_").append(getRequiredAttribute(fa, Lims.GROUP_ID));
		}
		sb.append("_").append(getRequiredAttribute(fa, FindAllTheFiles.Header.SEQUENCER_RUN_NAME));
		sb.append("_").append(getRequiredAttribute(fa, FindAllTheFiles.Header.IUS_TAG));
		sb.append("_").append("L00").append(getRequiredAttribute(fa, FindAllTheFiles.Header.LANE_NUM));
		sb.append("_").append("R1");
		sb.append("_").append("001");
		sb.append("_");
		return sb.toString();
	}

	@Override
	protected List<Map<String, String>> getFileProvenanceReport(Map<FileProvenanceParam, List<String>> params) {
		if (provenanceClient != null) {

			// convert all BasicDecider file provenance params to GSI file provenance
			// filters
			// BasicDecider opts are only include filters, so add them only to the include
			// list
			for (Entry<FileProvenanceParam, List<String>> e : params.entrySet()) {
				FileProvenanceFilter filter = FileProvenanceFilter.valueOf(e.getKey().name());
				List<String> values = e.getValue();
				List<String> valuesSplit = new ArrayList<>();
				for (String s : values) {
					valuesSplit.addAll(Splitter.on(",").splitToList(s));
				}
				if (includeFilters.containsKey(filter)) {
					includeFilters.get(filter).addAll(valuesSplit);
				} else {
					includeFilters.put(filter, Sets.newHashSet(valuesSplit));
				}
			}

			// we're only interested in processing analysis that successfully completed
			includeFilters.put(FileProvenanceFilter.processing_status, Sets.newHashSet("success"));
			includeFilters.put(FileProvenanceFilter.workflow_run_status, Sets.newHashSet("completed"));

			// only get the analysis with the metatype of interest
			if (getMetaType() != null && !getMetaType().isEmpty()) {
				includeFilters.put(FileProvenanceFilter.file_meta_type, Sets.newHashSet(getMetaType()));
			}

			if (options.has("verbose")) {
				MapUtils.verbosePrint(System.out, "include filters", includeFilters);
				MapUtils.verbosePrint(System.out, "exclude filters", excludeFilters);
			}

			Joiner joiner = Joiner.on(";").skipNulls();
			final String EMPTY_STRING = "";

			String studyTagPrefix = Header.STUDY_TAG_PREFIX.getTitle();
			String parentSampleTagPrefix = Header.PARENT_SAMPLE_TAG_PREFIX.getTitle();
			String sampleTagPrefix = Header.SAMPLE_TAG_PREFIX.getTitle();
			String sequencerRunTagPrefix = Header.SEQUENCER_RUN_TAG_PREFIX.getTitle();
			String laneTagPrefix = Header.LANE_TAG_PREFIX.getTitle();
			String workflowTagPrefix = "workflow.";
			String workflowRunTagPrefix = "workflow_run.";
			String fileTagPrefix = "file.";
			String iusTagPrefix = "ius.";
			String processingTagPrefix = "processing.";

			List<Map<String, String>> fpList = new ArrayList<>();
			Collection<FileProvenance> fps = provenanceClient.getFileProvenance(includeFilters, excludeFilters);
			for (FileProvenance fp : fps) {
				Map<String, String> f = new HashMap<>();
				f.put("Last Modified", StringUtils.defaultString(fp.getLastModified() == null ? null : fp.getLastModified()
						.withZoneSameInstant(ZoneId.of("America/Toronto")).format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss.SSS"))));
				f.put("Study Title", StringUtils.defaultString(join(fp.getStudyTitles())));
				f.put("Study Attributes",
						StringUtils.defaultString(
								MapStringifier.transform(prefixMapKeys(studyTagPrefix).andThen(ssForAttributes),
										ssForAttributes, fp.getStudyAttributes())));
				f.put("Experiment Name", EMPTY_STRING);
				f.put("Experiment Attributes", EMPTY_STRING);
				f.put("Root Sample Name", StringUtils.defaultString(join(fp.getRootSampleNames())));
				f.put("Parent Sample Name", StringUtils.defaultString(join(fp.getParentSampleNames())));
				f.put("Parent Sample Organism IDs",
						StringUtils.defaultString(joiner.join(fp.getParentSampleOrganismIDs())));
				f.put("Parent Sample Attributes",
						StringUtils.defaultString(
								MapStringifier.transform(prefixMapKeys(parentSampleTagPrefix).andThen(ssForAttributes),
										ssForAttributes, fp.getParentSampleAttributes())));
				f.put("Sample Name", StringUtils.defaultString(join(fp.getSampleNames())));
				f.put("Sample Organism ID", StringUtils.defaultString(join(fp.getSampleOrganismIDs())));
				f.put("Sample Organism Code", StringUtils.defaultString(join(fp.getSampleOrganismCodes())));
				f.put("Sample Attributes",
						StringUtils.defaultString(
								MapStringifier.transform(prefixMapKeys(sampleTagPrefix).andThen(ssForAttributes),
										ssForAttributes, fp.getSampleAttributes())));
				f.put("Sequencer Run Name", StringUtils.defaultString(join(fp.getSequencerRunNames())));
				f.put("Sequencer Run Attributes",
						StringUtils.defaultString(
								MapStringifier.transform(prefixMapKeys(sequencerRunTagPrefix).andThen(ssForAttributes),
										ssForAttributes, fp.getSequencerRunAttributes())));
				f.put("Sequencer Run Platform ID", StringUtils.defaultString(join(fp.getSequencerRunPlatformIDs())));
				f.put("Sequencer Run Platform Name",
						StringUtils.defaultString(joiner.join(fp.getSequencerRunPlatformNames())));
				f.put("Lane Name", StringUtils.defaultString(join(fp.getLaneNames())));
				f.put("Lane Number", StringUtils.defaultString(join(fp.getLaneNumbers())));
				f.put("Lane Attributes",
						StringUtils.defaultString(
								MapStringifier.transform(prefixMapKeys(laneTagPrefix).andThen(ssForAttributes),
										ssForAttributes, fp.getLaneAttributes())));
				f.put("IUS Tag", StringUtils.defaultString(join(fp.getIusTags())));

				f.put("Workflow Name", StringUtils.defaultString(stringSanitizer.apply(fp.getWorkflowName())));
				f.put("Workflow Version", StringUtils.defaultString(stringSanitizer.apply(fp.getWorkflowVersion())));
				f.put("Workflow SWID", StringUtils.defaultString(fp.getWorkflowSWID().toString()));
				f.put("Workflow Attributes",
						StringUtils.defaultString(
								MapStringifier.transform(prefixMapKeys(workflowTagPrefix).andThen(ssForAttributes),
										ssForAttributes, fp.getWorkflowAttributes())));
				f.put("Workflow Run Name", StringUtils.defaultString(stringSanitizer.apply(fp.getWorkflowRunName())));
				f.put("Workflow Run Status",
						StringUtils.defaultString(stringSanitizer.apply(fp.getWorkflowRunStatus())));
				f.put("Workflow Run SWID", StringUtils.defaultString(fp.getWorkflowRunSWID().toString()));
				f.put("Workflow Run Attributes",
						StringUtils.defaultString(
								MapStringifier.transform(prefixMapKeys(workflowRunTagPrefix).andThen(ssForAttributes),
										ssForAttributes, fp.getWorkflowRunAttributes())));
				f.put("Workflow Run Input File SWAs",
						StringUtils.defaultString(joiner.join(fp.getWorkflowRunInputFileSWIDs())));
				f.put("Processing Algorithm",
						StringUtils.defaultString(stringSanitizer.apply(fp.getProcessingAlgorithm())));
				f.put("Processing SWID", StringUtils.defaultString(fp.getProcessingSWID().toString()));
				f.put("Processing Attributes",
						StringUtils.defaultString(
								MapStringifier.transform(prefixMapKeys(processingTagPrefix).andThen(ssForAttributes),
										ssForAttributes, fp.getProcessingAttributes())));
				f.put("Processing Status", StringUtils.defaultString(stringSanitizer.apply(fp.getProcessingStatus())));
				f.put("File Meta-Type", StringUtils.defaultString(stringSanitizer.apply(fp.getFileMetaType())));
				f.put("File SWID", StringUtils.defaultString(fp.getFileSWID().toString()));
				f.put("File Attributes",
						StringUtils.defaultString(
								MapStringifier.transform(prefixMapKeys(fileTagPrefix).andThen(ssForAttributes),
										ssForAttributes, fp.getFileAttributes())));
				f.put("File Path", StringUtils.defaultString(stringSanitizer.apply(fp.getFilePath())));
				f.put("File Md5sum", StringUtils.defaultString(stringSanitizer.apply(fp.getFileMd5sum())));
				f.put("File Size", StringUtils.defaultString(stringSanitizer.apply(fp.getFileSize())));
				f.put("File Description", StringUtils.defaultString(stringSanitizer.apply(fp.getFileDescription())));
				f.put("Path Skip", StringUtils.defaultString(stringSanitizer.apply(fp.getSkip())));
				f.put("Skip", StringUtils.defaultString(stringSanitizer.apply(fp.getSkip())));

				f.put("IUS Attributes", StringUtils.defaultString(MapStringifier.transform(
						prefixMapKeys(iusTagPrefix).andThen(ssForAttributes), ssForAttributes, fp.getIusAttributes())));

				// return ALL associated Ius LimsKeys (as OicrDecider will only create links to
				// LimsKeys that are returned here)
				if (fp instanceof FileProvenanceFromAnalysisProvenance) {
					// This is a workaround until file provenace is not split by sample or lane
					// provenance record and is returned as one
					// object with all associated lims metadata combined.
					f.put("IUS SWID", StringUtils
							.defaultString(join(((FileProvenanceFromAnalysisProvenance) fp).getReleatedIusSWIDs())));
				} else {
					throw new UnsupportedOperationException(fp.getClass().getCanonicalName()
							+ " implementation is not support by OicrDecider."
							+ "If the implementation guarantees getIusSWIDs() returns all associated Ius LimsKey links, support can be added.");
					// f.put("IUS SWID",
					// StringUtils.defaultString(joiner.join(Iterables.transform(((FileProvenanceFromAnalysisProvenance)
					// fp).getIusSWIDs(), stringSanitizer))));
				}

				f.put("Status", fp.getStatus().toString());
				f.put("Status Reason", fp.getStatusReason());

				fpList.add(f);
			}
			return fpList;
		} else {
			return super.getFileProvenanceReport(params);
		}
	}

	private Function<String, String> prefixMapKeys(String prefix) {
		return x -> prefix + x;
	}

	private String join(Collection<String> c) {
		return c.stream().filter(Objects::nonNull).map(stringSanitizer).collect(Collectors.joining(";"));
	}

	@Override
	protected Map<FileProvenanceParam, List<String>> parseOptions() {
		if (provenanceClient != null) {
			Map<FileProvenanceParam, List<String>> map = new EnumMap<>(FileProvenanceParam.class);
			Set<HumanProvenanceFilters> supportedFilters = Sets.newHashSet(HumanProvenanceFilters.STUDY_NAME,
					HumanProvenanceFilters.SAMPLE_NAME, HumanProvenanceFilters.ROOT_SAMPLE_NAME,
					HumanProvenanceFilters.SEQUENCER_RUN_NAME, HumanProvenanceFilters.LANE_NAME);
			if (options.has("all")) {
				/**
				 * nothing special
				 */
			} else {
				for (HumanProvenanceFilters filter : supportedFilters) {
					List<?> optionValues = options.valuesOf(filter.toString());
					if (!optionValues.isEmpty()) {
						map.put(filter.mappedParam,
								new ImmutableList.Builder<String>().addAll((List<String>) optionValues).build());
					}
				}
			}
			return map;
		} else {
			return super.parseOptions();
		}

	}

    private class ComparableLimsKey implements ca.on.oicr.gsi.provenance.model.LimsKey {

        private final String provider;
        private final String id;
        private final String version;
        private final ZonedDateTime lastModified;

        public ComparableLimsKey(LimsKey limsKey) {
            this.provider = limsKey.getProvider();
            this.id = limsKey.getId();
            this.version = limsKey.getVersion();
            this.lastModified = limsKey.getLastModified();
        }

        @Override
        public String getProvider() {
            return provider;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getVersion() {
            return version;
        }

        @Override
        public ZonedDateTime getLastModified() {
            return lastModified;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + Objects.hashCode(this.provider);
            hash = 67 * hash + Objects.hashCode(this.id);
            hash = 67 * hash + Objects.hashCode(this.version);
            hash = 67 * hash + Objects.hashCode(this.lastModified);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ComparableLimsKey other = (ComparableLimsKey) obj;
            if (!Objects.equals(this.provider, other.provider)) {
                return false;
            }
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            if (!Objects.equals(this.version, other.version)) {
                return false;
            }
            if (!Objects.equals(this.lastModified, other.lastModified)) {
                return false;
            }
            return true;
        }
    }

}
