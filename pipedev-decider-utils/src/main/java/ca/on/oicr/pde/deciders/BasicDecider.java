/*
 * Copyright (C) 2012 SeqWare
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.on.oicr.pde.deciders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.seqware.common.model.ProcessingStatus;
import io.seqware.common.model.WorkflowRunStatus;
import io.seqware.pipeline.plugins.WorkflowScheduler;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import net.sourceforge.seqware.common.hibernate.FindAllTheFiles;
import net.sourceforge.seqware.common.hibernate.FindAllTheFiles.Header;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.model.FileProvenanceParam;
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.common.model.WorkflowRun;
import net.sourceforge.seqware.common.module.FileMetadata;
import net.sourceforge.seqware.common.module.ReturnValue;
import net.sourceforge.seqware.common.util.filetools.FileTools;
import net.sourceforge.seqware.common.util.filetools.FileTools.LocalhostPair;
import net.sourceforge.seqware.pipeline.decider.DeciderInterface;
import net.sourceforge.seqware.pipeline.plugin.Plugin;
import net.sourceforge.seqware.pipeline.plugin.PluginInterface;
import net.sourceforge.seqware.pipeline.plugins.fileprovenance.ProvenanceUtility;
import net.sourceforge.seqware.pipeline.runner.PluginRunner;
import net.sourceforge.seqware.pipeline.tools.SetOperations;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.MetaInfServices;

/**
 *
 * @author mtaschuk
 */
@MetaInfServices(PluginInterface.class)
public class BasicDecider extends Plugin implements DeciderInterface {
    private static final Logger LOGGER = LogManager.getLogger(BasicDecider.class);

    private List<Header> header = Lists.newArrayList(Header.FILE_SWA);
    private Set<String> parentWorkflowAccessions = new TreeSet<>();
    private Set<String> workflowAccessionsToCheck = new TreeSet<>();
    private List<String> metaTypes = null;
    private Boolean ignorePreviousRuns = null;
    private Boolean isDryRunMode = null;
    private String workflowAccession = null;
    protected Random random = new Random(System.currentTimeMillis());
    private Boolean metadataWriteback = null;
    private Collection<String> parentAccessionsToRun;
    private Collection<String> filesToRun;
    private Collection<String> workflowParentAccessionsToRun;
    private Collection<Integer> fileSWIDsToRun;
    private List<String> workflowRuns;
    private Set<String> studyReporterOutput;
    private ArrayList<String> iniFiles;
    private Boolean skipStuff = null;
    private int launchMax = Integer.MAX_VALUE, launched = 0;
    private int rerunMax = 5;
    private String host = null;
    protected Set<String> workflowRunAttributeTagFilters = new HashSet<>(Arrays.asList("skip","deleted"));

    protected final NonOptionArgumentSpec<String> nonOptionSpec;
    protected final OptionSpecBuilder ignorePreviousRunsSpec;
    protected final OptionSpecBuilder forceRunAllSpec;
    protected final OptionSpec<String> workflowRunAttributeTagFiltersOpt;
    protected final OptionSpec<Boolean> dryRunOpt;

    private boolean isValidWorkflowRun;


    public BasicDecider() {
        super();
        parser.acceptsAll(Arrays.asList("wf-accession"), "The workflow accession of the workflow").withRequiredArg();

        // configure parameters used to parse provenance report
        ProvenanceUtility.configureFileProvenanceParams(parser);

        parser.acceptsAll(Arrays.asList("group-by"),
                "Optional: Group by one of the headings in FindAllTheFiles. Default: FILE_SWA. One of LANE_SWA or IUS_SWA.")
                .withRequiredArg();
        parser.acceptsAll(Arrays.asList("parent-wf-accessions"),
                "The workflow accessions of the parent workflows, comma-separated with no spaces. May also specify the meta-type.")
                .withRequiredArg();
        this.ignorePreviousRunsSpec = parser.acceptsAll(Arrays.asList("ignore-previous-runs"),
                "Forces the decider to run all matches regardless of whether they've been run before or not");
        parser.acceptsAll(Arrays.asList("meta-types"),
                "The comma-separated meta-type(s) of the files to run this workflow with. Alternatively, use parent-wf-accessions.")
                .withRequiredArg();
        parser.acceptsAll(
                Arrays.asList("check-wf-accessions"),
                "The comma-separated, no spaces, workflow accessions of the workflow that perform the same function (e.g. older versions). Any files that have been processed with these workflows will be skipped.")
                .withRequiredArg();
        this.forceRunAllSpec = parser.acceptsAll(Arrays.asList("force-run-all"),
                "Forces the decider to run all matches regardless of whether they've been run before or not");
        dryRunOpt = parser.acceptsAll(Arrays.asList("dry-run", "test"),
                "Dry-run/test mode. Prints the INI files to standard out and does not submit the workflow.")
                .withOptionalArg().ofType(Boolean.class).defaultsTo(false);
        parser.acceptsAll(Arrays.asList("no-meta-db", "no-metadata"), "Optional: a flag that prevents metadata writeback (which is done "
                + "by default) by the Decider and that is subsequently "
                + "passed to the called workflow which can use it to determine if "
                + "they should write metadata at runtime on the cluster.");
        parser.acceptsAll(Arrays.asList("ignore-skip-flag"),
                "Ignores any 'skip' flags on lanes, IUSes, sequencer runs, samples, etc. Use caution.");
        parser.acceptsAll(Arrays.asList("launch-max"), "The maximum number of jobs to launch at once.").withRequiredArg()
                .defaultsTo("2147483647");
        parser.acceptsAll(Arrays.asList("rerun-max"), "The maximum number of times to re-launch a workflowrun if failed.")
                .withRequiredArg().defaultsTo("5");
        parser.acceptsAll(Arrays.asList("host", "ho"),
                "Used only in combination with --schedule to schedule onto a specific host. If not provided, the default is the local host")
                .withRequiredArg();
        // SEQWARE-1622 - check whether files exist
        parser.acceptsAll(Arrays.asList("check-file-exists", "cf"), "Optional: only launch on the file if the file exists");
        workflowRunAttributeTagFiltersOpt = parser.accepts("workflow-run-annotation-tag-filters",
                "When checking if a workflow run has been processed before, filter out workflow runs annotated with these tags.")
                .withRequiredArg()
                .ofType(String.class)
                .defaultsTo(workflowRunAttributeTagFilters.stream().toArray(String[]::new));
        this.nonOptionSpec = parser.nonOptions(WorkflowScheduler.OVERRIDE_INI_DESC);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String get_description() {
        return "The decider from which all other deciders came";
    }

    @Override
    /**
     * This method is intended to be called AFTER any implementing class's init
     * method.
     */
    public ReturnValue init() {
        ReturnValue ret = new ReturnValue();
        
        //initialize collections
        workflowRuns = new ArrayList<>();
        
        if (!ProvenanceUtility.checkForValidOptions(options)) {
            println("One of the various contraints or '--all' must be specified.");
            println(this.get_syntax());
            ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
        }

        try {
            ResourceBundle rb = PropertyResourceBundle.getBundle("decider");
            String parents = rb.getString("parent-workflow-accessions");
            String checks = rb.getString("check-wf-accessions");
            String wfa = rb.getString("workflow-accession");
            if (wfa != null && !wfa.trim().isEmpty()) {
                this.setWorkflowAccession(wfa);
            }
            if (parents != null && !parents.trim().isEmpty()) {
                List<String> pas = Arrays.asList(parents.split(","));
                this.setParentWorkflowAccessions(new TreeSet<>(pas));
            }
            if (checks != null && !checks.trim().isEmpty()) {
                List<String> cwa = Arrays.asList(checks.split(","));
                this.setWorkflowAccessionsToCheck(new TreeSet<>(cwa));

            }
        } catch (MissingResourceException e) {
            LOGGER.debug("No decider resource found: ", e);
        }

        // Group-by allows you to group processing events based on one characteristic.
        // Normally, this allows you to run on a group of samples (for example, all
        // of the IUS-level BAM files). The default is no grouping, so the workflow
        // will be run independently on every file it finds
        if (options.has("group-by")) {
            String headerString = (String) options.valueOf("group-by");
            try {
                header = Lists.newArrayList(Header.valueOf(headerString));
            } catch (IllegalArgumentException e) {
                LOGGER.error("IllegalArgumentException when grouping", e);
                StringBuilder sb = new StringBuilder();
                sb.append("group-by attribute must be one of the following: \n");
                for (Header h : Header.values()) {
                    sb.append("\t").append(h.name()).append("\n");

                }
                LOGGER.debug(sb.toString());
                ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
            }
        }

        if (options.has("wf-accession")) {
            workflowAccession = (String) options.valueOf("wf-accession");
        } else if (workflowAccession == null) {
            LOGGER.error("Must specify the workflow-accession of the workflow to run");
            ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
        }

        // Check for filtering on the files. Either parent workflow accessions
        // or file meta-types, or both
        boolean hasFilter = false;
        if (options.has("parent-wf-accessions")) {
            String pas = (String) options.valueOf("parent-wf-accessions");
            for (String p : pas.split(",")) {
                parentWorkflowAccessions.add(p.trim());
                hasFilter = true;
            }
        }
        if (options.has("meta-types")) {
            String mt = (String) options.valueOf("meta-types");
            metaTypes = Arrays.asList(mt.split(","));
            hasFilter = true;
        }

        if (!hasFilter && parentWorkflowAccessions.isEmpty() && metaTypes == null) {
            LOGGER.error("You must run a decider with parent-wf-accessions or meta-types (or both).");
            ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
        }

        // Repeat-checking options. if present, check-wf-accessions will check to
        // see if the samples have been run through a particular workflow before.
        // These workflows will not be launched again
        // Optionally you can force the decider to re-run all possibilities in
        // the database with force-run-all.
        if (options.has("check-wf-accessions")) {

            String pas = (String) options.valueOf("check-wf-accessions");
            LOGGER.debug("Pas = " + pas);
            if (pas.contains(",")) {
                for (String p : pas.split(",")) {
                    workflowAccessionsToCheck.add(p.trim());
                }
            } else {
                workflowAccessionsToCheck.add(pas.trim());
            }
            // Separate out this logic
            // workflowAccessionsToCheck.add(workflowAccession);
        }
        ignorePreviousRuns = options.has(this.ignorePreviousRunsSpec) || options.has(this.forceRunAllSpec);

        if (getBooleanFlagOrArgValue(dryRunOpt) || options.has("no-metadata") || options.has("no-meta-db")) {
            // dry run mode turns off all of the submission functions and just prints to debug
            isDryRunMode = true;
            metadataWriteback = false;

            StringWriter writer = new StringWriter();
            try {
                FindAllTheFiles.printHeader(writer, true);
                LOGGER.debug(writer.toString());
            } catch (IOException ex) {
                LOGGER.error("BasicDecider.init IOException",ex);
            }
        } else {
            isDryRunMode = false;
            metadataWriteback = true;
        }

        skipStuff = !options.has("ignore-skip-flag");

        LocalhostPair localhostPair = FileTools.getLocalhost(options);
        String localhost = localhostPair.hostname;
        
        if (options.has("host") || options.has("ho")) {
            host = (String) options.valueOf("host");
        } else {
            host = localhost;
        }
        
        if (localhostPair.returnValue.getExitStatus() != ReturnValue.SUCCESS && host == null) {
            LOGGER.error("Could not determine localhost: Return value " + localhostPair.returnValue.getExitStatus());
            LOGGER.error("Please supply it on the command line with --host");
            ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
        } else if (!host.equals(localhost)) {
            LOGGER.warn("The localhost and the scheduling host are not the same: " + localhost + " and " + host + ". Proceeding anyway.");
        }

        if (options.has("launch-max")) {
            try {
                launchMax = Integer.parseInt(options.valueOf("launch-max").toString());
            } catch (NumberFormatException e) {
                LOGGER.error("The launch-max parameter must be an integer. Unparseable integer: " + options.valueOf("launch-max").toString());
                ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
            }
        }

        if (options.has("rerun-max")) {
            try {
                rerunMax = Integer.parseInt(options.valueOf("rerun-max").toString());
            } catch (NumberFormatException e) {
                LOGGER.error("The rerun-max parameter must be an integer. Unparseable integer: " + options.valueOf("rerun-max").toString());
                ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
            }
        }

        if (workflowAccession == null || "".equals(workflowAccession)) {
            LOGGER.error("The wf-accession must be defined.");
            ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
        }

        if (options.has(workflowRunAttributeTagFiltersOpt)) {
            workflowRunAttributeTagFilters = new HashSet<>(options.valuesOf(workflowRunAttributeTagFiltersOpt));
        }

        return ret;
    }

    @Override
    public ReturnValue do_test() {
        return ReturnValue.featureNotImplemented();
    }

    @Override
    public ReturnValue do_run() {
        if (!metadata.checkClientServerMatchingVersion()) {
            LOGGER.warn("Client version does not match webservice version");
        }
        List<ReturnValue> vals = createListOfRelevantFilePaths();

        //get(0) is for backwards compatiblity for decider that override and expect one header
        //BasicDecider impl of separateFiles uses the header list
        Map<String, List<ReturnValue>> mappedFiles = separateFiles(vals, header.get(0).getTitle());

        return launchWorkflows(mappedFiles);
    }
    
    public List<String> getWorkflowRuns() {
        return Collections.unmodifiableList(workflowRuns);
    }

    private ReturnValue launchWorkflows(Map<String, List<ReturnValue>> mappedFiles) {
        ReturnValue ret = new ReturnValue();
        if (mappedFiles != null) {

            List<Entry<String, List<ReturnValue>>> entryList = new ArrayList<>();
            entryList.addAll(mappedFiles.entrySet());
            Collections.sort(entryList, new ReturnValueProcessingTimeComparator());

            for (Entry<String, List<ReturnValue>> entry : entryList) {
                LOGGER.debug("Considering key:" + entry.getKey());
                for (ReturnValue r : entry.getValue()) {
                    LOGGER.debug("Group contains: " + r.getAttribute(FindAllTheFiles.FILE_SWA));
                }

                parentAccessionsToRun = new HashSet<>();
                filesToRun = new HashSet<>();
                workflowParentAccessionsToRun = new HashSet<>();
                fileSWIDsToRun = new HashSet<>();
                studyReporterOutput = new HashSet<>();

                // for each grouping (e.g. sample), iterate through the files
                List<ReturnValue> files = entry.getValue();
                LOGGER.debug("key:" + entry.getKey() + " consists of " + files.size() + " files");

                for (ReturnValue file : files) {
                    String wfAcc = file.getAttribute(Header.WORKFLOW_SWA.getTitle());
                    LOGGER.debug(Header.WORKFLOW_SWA.getTitle() + ": WF accession is " + wfAcc);

                    // if there is no parent accessions, or if the parent accession is correct
                    // this makes an assumption that if the wfAcc is null then the parentWorkflowAccessions will be empty
                    // and thus we are able to find files of a particular metatype with no wfAcc

                    // check for each file if the metatype is correct (if it exists),
                    // or just add it
                    for (FileMetadata fm : file.getFiles()) {
                        if (metaTypes != null) {
                            if (metaTypes.contains(fm.getMetaType())) {
                                addFileToSets(file, fm, workflowParentAccessionsToRun, parentAccessionsToRun, filesToRun, fileSWIDsToRun);
                            }
                        } else {
                            addFileToSets(file, fm, workflowParentAccessionsToRun, parentAccessionsToRun, filesToRun, fileSWIDsToRun);
                        }
                    }

                }// end iterate through files

                if (!parentAccessionsToRun.isEmpty() && !filesToRun.isEmpty() && !workflowParentAccessionsToRun.isEmpty()) {
                    final String parentAccessionString = commaSeparateMy(parentAccessionsToRun);
                    final String fileString = commaSeparateMy(filesToRun);
                    LOGGER.debug("FileString: " + fileString);
                    // SEQWARE-1773 short-circuit this with forceRunAll to ensure that sample fingerprinting workflow launches
                    if (ignorePreviousRuns) {
                        LOGGER.debug("Ignoring previous runs because --ignore-previous-runs was enabled");
                    }
                    boolean rerun = ignorePreviousRuns || rerunWorkflowRun(filesToRun, fileSWIDsToRun);

                    // SEQWARE-1728 - move creation of ini to launches (and dry run launches) to conserve disk space
                    iniFiles = new ArrayList<>();

                    ReturnValue newRet = this.doFinalCheck(fileString, parentAccessionString);
                    if (newRet.getExitStatus() != ReturnValue.SUCCESS) {
                        LOGGER.warn("Final check failed, aborting run. Return value was: " + newRet.getExitStatus());
                        rerun = false;
                    }

                    // need to reset back to valid, client/subclass may not have reset
                    isValidWorkflowRun = true;

                    // if we're in dry run mode or we don't want to rerun and we don't want to force the re-processing
                    if (isDryRunMode || !rerun) {
                        //TODO: we need to simplify the logic and make it more readable
                        if (rerun) {
                            try {
                                workflowParentAccessionsToRun = getSwidsToLinkWorkflowRunTo(new HashSet<>(workflowParentAccessionsToRun));
                            } catch (Exception e) {
                                LOGGER.error("Error while scheduling workflow run in dry run mode - getSwidsToLinkWorkflowRunTo() failed. "
                                        + "workflowParentAccessionsToRun = " + workflowParentAccessionsToRun.toString(), e);
                                continue;
                            }

                            iniFiles.add(createIniFile(fileString, parentAccessionString));

                            if (!isValidWorkflowRun) {
                                LOGGER.error("Not a valid workflow run - not scheduling.");
                                continue;
                            }

                            LOGGER.info(studyReporterOutput.stream().collect(Collectors.joining("\n\n", "Input file records:\n", "")));
                            LOGGER.debug("NOT RUNNING (but would have ran). dryRunMode=" + isDryRunMode + " or !rerun=" + !rerun);
                            reportLaunch();

                            //keep track of workflow runs to be scheduled
                            workflowRuns.addAll(iniFiles);

                            // SEQWARE-1642 - output to debug only whether a decider would launch
                            ret = do_summary();
                            launched++;
                        } else {
                            for (String line : studyReporterOutput) {
                                LOGGER.debug(line);
                            }
                            LOGGER.debug("NOT RUNNING (and would not have ran). dryRunMode=" + isDryRunMode + " or !rerun=" + !rerun);
                        }
                    } else if (launched < launchMax) {
                        try {
                            workflowParentAccessionsToRun = getSwidsToLinkWorkflowRunTo(new HashSet<>(workflowParentAccessionsToRun));
                        } catch (Exception e) {
                            LOGGER.error("Error while scheduling workflow run - getSwidsToLinkWorkflowRunTo() failed. "
                                    + "workflowParentAccessionsToRun = " + workflowParentAccessionsToRun.toString(), e);
                            continue;
                        }
                        
                        iniFiles.add(createIniFile(fileString, parentAccessionString));

                        if (!isValidWorkflowRun) {
                            LOGGER.error("Not a valid workflow run - not scheduling.");
                            continue;
                        }

                        launched++;
                        // construct the INI and run it
                        for (String line : studyReporterOutput) {
                            LOGGER.debug(line);
                        }
                        
                        //keep track of workflow runs to be scheduled
                        workflowRuns.addAll(iniFiles);
                        
                        LOGGER.debug("Scheduling");
                        // construct the INI and run it
                        ArrayList<String> runArgs = constructCommand();
                        PluginRunner pluginRunner = new PluginRunner();
                        pluginRunner.setConfig(config);
                        pluginRunner.run(runArgs.toArray(new String[runArgs.size()]));
                        
                        LOGGER.debug("Scheduling.");
                        do_summary();

                    }
                    // separate this out so that it is reachable when in dry run mode
                    if (launched >= launchMax) {
                        LOGGER.info("The maximum number of jobs has been scheduled"
                                + ". The next jobs will be launched when the decider runs again.");
                        ret.setExitStatus(ReturnValue.QUEUED);
                        // SEQWARE-1666 - short-circuit and exit when the maximum number of jobs have been launched
                        return ret;
                    }
                } else {
                    LOGGER.debug("Cannot run: parentAccessions: " + parentAccessionsToRun.size() + " filesToRun: " + filesToRun.size()
                            + " workflowParentAccessions: " + workflowParentAccessionsToRun.size());
                }

            }
        } else {
            LOGGER.debug("There are no files");
        }
        return ret;
    }
    
    protected Set<String> getSwidsToLinkWorkflowRunTo(Set<String> swids) throws Exception {
        return swids;
    }

    protected ArrayList<String> constructCommand() {
        ArrayList<String> runArgs = new ArrayList<>();
        runArgs.add("--plugin");
        runArgs.add("io.seqware.pipeline.plugins.WorkflowScheduler");
        runArgs.add("--");
        runArgs.add("--workflow-accession");
        runArgs.add(workflowAccession);
        runArgs.add("--ini-files");
        runArgs.add(commaSeparateMy(iniFiles));
        if (getMetadataWriteback()) {
            Collection<String> fileSWIDs = new ArrayList<>();
            runArgs.add("--" + WorkflowScheduler.INPUT_FILES);
            for (Integer fileSWID : fileSWIDsToRun) {
                fileSWIDs.add(String.valueOf(fileSWID));
            }
            runArgs.add(commaSeparateMy(fileSWIDs));
            runArgs.add("--parent-accessions");
            runArgs.add(commaSeparateMy(parentAccessionsToRun));
            if (!workflowParentAccessionsToRun.isEmpty()) {
                runArgs.add("--link-workflow-run-to-parents");
                runArgs.add(commaSeparateMy(workflowParentAccessionsToRun));
            }
        } else {
            runArgs.add("--no-metadata");
        }
        runArgs.add("--host");
        runArgs.add(host);
        runArgs.add("--");
        for (String s : options.valuesOf(nonOptionSpec)) {
            runArgs.add(s);
        }
        return runArgs;
    }

    /**
     * Returns true only if there are more files to run than have been run on any workflow so far, or if the filesToRun have different
     * filepaths than those that have been run before.
     *
     * @param filesToRun
     * @param fileSWIDs
     * @return
     */
    protected boolean rerunWorkflowRun(final Collection<String> filesToRun, Collection<Integer> fileSWIDs) {

        boolean rerun;
        List<Boolean> failures = new ArrayList<>();
        List<Integer> asList = Arrays.asList(fileSWIDs.toArray(new Integer[fileSWIDs.size()]));
        List<WorkflowRun> runs = produceAccessionListWithFileList(asList);
        rerun = processWorkflowRuns(filesToRun, failures, runs);
        if (!rerun) {
            LOGGER.debug("This workflow has failed to launch based on workflow runs found via direct search");
            return rerun;
        }
        // special case, when rerun max is 0, we still want to launch even if there are 0 failures
        if (failures.isEmpty() && this.rerunMax == 0) {
            return rerun;
        }
        if (failures.size() >= this.rerunMax) {
            LOGGER.debug("This workflow has failed " + rerunMax + " times: not running");
            rerun = false;
        }
        return rerun;
    }

    /**
     * Map a normal status to whether a workflow run completed, failed, or other (submitted, pending, etc.) (the states that we care about
     * for the decider)
     *
     * @param generateStatus
     * @return
     */
    protected PREVIOUS_RUN_STATUS determineStatus(WorkflowRunStatus generateStatus) {
        switch (generateStatus) {
        case completed:
            return PREVIOUS_RUN_STATUS.COMPLETED;
        case failed:
            return PREVIOUS_RUN_STATUS.FAILED;
        default:
            return PREVIOUS_RUN_STATUS.OTHER;
        }
    }

    /**
     * Returns true if the filesToRun are totally contained by the files associated with the files in a given workflowRunAcc
     *
     * @param filesSWIDsHasRun
     * @param filesToRun
     *            the files to check to see if they are contained by the past run
     * @return
     */
    protected boolean isToRunContained(Set<Integer> filesSWIDsHasRun, Collection<String> filesToRun) {
        Set<String> filesHasRun = determineFilePaths(filesSWIDsHasRun);
        LOGGER.info("Files to run: " + StringUtils.join(filesToRun, ','));
        // use set operations to be more explicit about our cases
        Set<String> setToRun = new HashSet<>(filesToRun);
        Set<String> setHasRun = new HashSet<>(filesHasRun);
        return SetOperations.isSuperset(setHasRun, setToRun);
    }

    /**
     * Tests if the files from the workflow run (filesHasRun) are the same as those found in the database (filesToRun). True if the
     * filesToRun has more files than the workflow run. True if the filesToRun and the workflow run have the same number of files but with
     * different filepaths. False if the filesToRun and the workflow run have the same number of files with the same file paths. False and
     * prints an error message if there are more files in the workflow run than in the filesToRun.
     *
     * @param filesSWIDsHasRun
     * @param filesToRun
     * @return
     */
    protected FILE_STATUS compareWorkflowRunFiles(Set<Integer> filesSWIDsHasRun, Collection<String> filesToRun) {
        Set<String> filesHasRun = determineFilePaths(filesSWIDsHasRun);
        LOGGER.info("Files to run: " + StringUtils.join(filesToRun, ','));
        LOGGER.info("Files has run: " + StringUtils.join(filesHasRun, ','));

        // use set operations to be more explicit about our cases
        Set<String> setToRun = new HashSet<>(filesToRun);
        Set<String> setHasRun = new HashSet<>(filesHasRun);
        if (setToRun.equals(setHasRun)) {
            return FILE_STATUS.SAME_FILES;
        }
        if (SetOperations.isSubset(setHasRun, setToRun)) {
            return FILE_STATUS.PAST_SUBSET_OR_INTERSECTION;
        }
        if (SetOperations.isSuperset(setHasRun, setToRun)) {
            return FILE_STATUS.PAST_SUPERSET;
        }
        if (SetOperations.intersection(setToRun, setHasRun).size() > 0) {
            return FILE_STATUS.PAST_SUBSET_OR_INTERSECTION;
        }
        return FILE_STATUS.DISJOINT_SETS;
    }

    private void addFileToSets(ReturnValue file, FileMetadata fm, Collection<String> workflowParentAccessionsToRun,
            Collection<String> parentAccessionsToRun, Collection<String> filesToRun, Collection<Integer> fileToRunSWIDs) {
        if (checkFileDetails(file, fm)) {
            if (skipStuff) {
                for (String key : file.getAttributes().keySet()) {
                    if (key.contains("skip")) {
                        LOGGER.warn("File SWID:" + fm.getDescription() + " path " + fm.getFilePath() + " is skipped: " + key + ">"
                                + file.getAttribute(key));
                        return;
                    }
                }
            }
            if (isDryRunMode) {
                printFileMetadata(file, fm);
            }

            filesToRun.add(fm.getFilePath());
            String fileSWID = file.getAttribute(Header.FILE_SWA.getTitle());
            fileToRunSWIDs.add(Integer.valueOf(fileSWID));
            parentAccessionsToRun.add(file.getAttribute(Header.PROCESSING_SWID.getTitle()));

            String swidString = file.getAttribute(Header.IUS_SWA.getTitle());
            Set<String> swids = Sets.newHashSet(swidString.split(";"));
            if (swids == null || swids.isEmpty()) {
                swidString = file.getAttribute(Header.LANE_SWA.getTitle());
                swids = Sets.newHashSet(swidString.split(";"));
            }
            // seqware-2002 it is possible that both are null if the path goes through sample_processing
            if (swids == null || swids.isEmpty()) {
                return;
            }
            workflowParentAccessionsToRun.addAll(swids);
        }
    }

    protected void printFileMetadata(ReturnValue file, FileMetadata fm) {
        try {
            StringWriter writer = new StringWriter();
            FindAllTheFiles.print(writer, file, true, fm);
            studyReporterOutput.add(writer.getBuffer().toString().trim());
        } catch (IOException ex) {
            LOGGER.error("Error printing file metadata", ex);
        }
    }

    protected String commaSeparateMy(Collection<String> list) {
        return separateMy(list, ",");
    }

    protected String spaceSeparateMy(Collection<String> list) {
        return separateMy(list, " ");
    }

    private String separateMy(Collection<String> list, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            if (sb.length() != 0) {
                sb.append(delimiter);
            }
            sb.append(s);
        }
        return sb.toString();
    }

    private String createIniFile(String commaSeparatedFilePaths, String commaSeparatedParentAccessions) {
        String iniPath = "";

        Workflow wf = metadata.getWorkflow(Integer.parseInt(workflowAccession));
        Map<String, String> iniFileMap = new TreeMap<>(wf.getParameterDefaults());

        iniFileMap.putAll(modifyIniFile(commaSeparatedFilePaths, commaSeparatedParentAccessions));

        PrintWriter writer = null;
        File file = null;
        try {
            file = File.createTempFile("" + random.nextInt(), ".ini");
            writer = new PrintWriter(new FileWriter(file), true);

            for (String key : iniFileMap.keySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(key).append("=").append(iniFileMap.get(key));
                writer.println(sb.toString());
            }

        } catch (IOException ex) {
            LOGGER.error("BasicDecider.createIniFile IOException",ex);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        if (file != null) {
            iniPath = file.getAbsolutePath();
        }
        return iniPath;

    }

    /**
     * Performs any additional checks on the file before adding it to the list of files to incorporate. This method should be extended for
     * future deciders for custom behaviour. You can also pull any details out of the file metadata here.
     *
     * @param returnValue
     *            The ReturnValue representing the Processing event. May have one or more files. The attributes table contains the
     *            information from FindAllTheFiles.Header.
     * @param fm
     *            the particular file that will be added
     * @return true if the file can be added to the list, false otherwise
     */
    protected boolean checkFileDetails(ReturnValue returnValue, FileMetadata fm) {
        if (this.options.has("check-file-exists")) {
            if (!new File(fm.getFilePath()).exists()) {
                LOGGER.warn("File not found:" + fm.getFilePath());
                return false;
            }
        }

        return true;
    }

    protected Map<String, String> modifyIniFile(String commaSeparatedFilePaths, String commaSeparatedParentAccessions) {
        Map<String, String> iniFileMap = new TreeMap<>();
        iniFileMap.put("input_files", commaSeparatedFilePaths);
        return iniFileMap;
    }

    protected String handleGroupByAttribute(String attribute) {
        return attribute;
    }

    /**
     * Deprecated - use {@link #separateFiles(java.util.List, java.util.List) }
     *
     * @param vals
     * @param groupBy
     *
     * @return map of grouped files (wrapped in ReturnValue) with the map key string being an aggregation of the "group by" values
     *
     * @deprecated
     */
    @Deprecated
    public Map<String, List<ReturnValue>> separateFiles(List<ReturnValue> vals, String groupBy) {
        return separateFiles(vals, header);
    }

    // protected
    public Map<String, List<ReturnValue>> separateFiles(List<ReturnValue> vals, List<Header> groupBy) {
        Map<String, List<ReturnValue>> map = new HashMap<>();
        for (ReturnValue r : vals) {
            //iterate through ordered list of headers to group by
            StringBuilder keyBuilder = new StringBuilder();
            for (Header h : groupBy) {
                String subKey = r.getAttributes().get(h.getTitle());
                if (subKey != null) {
                    subKey = handleGroupByAttribute(subKey);
                }
                keyBuilder.append(String.format("[%s=%s] ", h.getTitle(), subKey));
            }
            String key = keyBuilder.toString();
            List<ReturnValue> vs = map.get(key);
            if (vs == null) {
                vs = new ArrayList<>();
            }
            vs.add(r);
            map.put(key, vs);
        }
        return map;
    }

    @Override
    public ReturnValue clean_up() {
        return ReturnValue.featureNotImplemented();
    }

    @Override
    public ReturnValue do_summary() {
        String command = do_summary_command();
        LOGGER.info(command);
        return new ReturnValue();
    }

    public Boolean getForceRunAll() {
        return ignorePreviousRuns;
    }

    public void setForceRunAll(Boolean forceRunAll) {
        this.ignorePreviousRuns = forceRunAll;
    }

    /**
     * Do not use this method - use {@link #getHeadersToGroupBy() }
     *
     * @return single header string - will throw exception if multiple "group by" headers have be set
     *
     * @deprecated
     */
    @Deprecated
    public Header getHeader() {
        return Iterables.getOnlyElement(header);
    }

    /**
     * Do not use this method - use {@link #getHeadersToGroupBy() }
     *
     * @return single header string - will throw exception if multiple "group by" headers have be set
     *
     * @deprecated
     */
    @Deprecated
    public Header getGroupingStrategy() {
        return Iterables.getOnlyElement(header);
    }

    /**
     * Do not use this method - use {@link #setHeadersToGroupBy(java.util.List) }
     *
     * @param header
     *
     * @deprecated
     */
    @Deprecated
    public void setHeader(Header header) {
        this.header = Lists.newArrayList(header);
    }

    /**
     * Do not use this method - use {@link #setHeadersToGroupBy(java.util.List) }
     *
     * @param headers
     *
     * @deprecated
     */
    @Deprecated
    public void setHeader(List<Header> headers) {
        this.header = Lists.newArrayList(headers);
    }

    /**
     * Do not use this method - use {@link #setHeadersToGroupBy(java.util.List) }
     *
     * @param strategy
     *
     * @deprecated
     */
    @Deprecated
    public void setGroupingStrategy(Header strategy) {
        this.header = Lists.newArrayList(strategy);
    }

    /**
     * Get the ordered list of file provenance properties to group by.
     *
     * These "group by" values are used in {@link #separateFiles(java.util.List, java.util.List) } to partition the file set into
     * candidate sets of files that may be used in a workflow run.
     *
     * @return unmodifiable list of {@link net.sourceforge.seqware.common.hibernate.FindAllTheFiles.Header}
     */
    public final List<Header> getHeadersToGroupBy() {
        return Collections.unmodifiableList(header);
    }

    /**
     * Set the file provenance properties to group by - the ordering of the list determines how to group of files.
     *
     * These "group by" values are used in {@link #separateFiles(java.util.List, java.util.List) } to partition the file set into
     * candidate sets of files that may be used in a workflow run.
     *
     * @param headers
     */
    public final void setHeadersToGroupBy(List<Header> headers) {
        this.header = Lists.newArrayList(headers);
    }

    public List<String> getMetaType() {
        return metaTypes;
    }

    public void setMetaType(List<String> metaType) {
        this.metaTypes = metaType;
    }

    public Boolean getMetadataWriteback() {
        return metadataWriteback;
    }

    public void setMetadataWriteback(Boolean metadataWriteback) {
        this.metadataWriteback = metadataWriteback;
    }

    public Set<String> getParentWorkflowAccessions() {
        return parentWorkflowAccessions;
    }

    public void setParentWorkflowAccessions(Set<String> parentWorkflowAccessions) {
        this.parentWorkflowAccessions = parentWorkflowAccessions;
    }

    /**
     * Do not use this method, use one of the following.
     * 1) Use {@link #abortSchedulingOfCurrentWorkflowRun() } to abort scheduling of the current workflow run
     * 2) Use {@link #setDryRunMode(java.lang.Boolean) } to enable "dry run" mode (aka, test mode, no-metadata mode)
     *
     * @param test
     *
     * @deprecated
     */
    @Deprecated
    public void setTest(Boolean test) {
        this.isDryRunMode = test;
    }

    /**
     * Stop the current workflow run (and only the current workflow run) from being scheduled.
     *
     * Calling this method will stop the workflow run - and more importantly, only the current workflowr run, from being executed
     * during {@link #launchWorkflows(java.util.Map) }.
     */
    public void abortSchedulingOfCurrentWorkflowRun() {
        this.isValidWorkflowRun = false;
    }

    public Boolean isDryRunMode() {
        return isDryRunMode;
    }

    public void setDryRunMode(Boolean isDryRunMode) {
        this.isDryRunMode = isDryRunMode;
    }

    public String getWorkflowAccession() {
        return workflowAccession;
    }

    public void setWorkflowAccession(String workflowAccession) {
        this.workflowAccession = workflowAccession;
    }

    public Set<String> getWorkflowAccessionsToCheck() {
        return workflowAccessionsToCheck;
    }

    public void setWorkflowAccessionsToCheck(Set<String> workflowAccessions) {
        this.workflowAccessionsToCheck = workflowAccessions;
    }

    /**
     * allow to user to do the final check and decide to run or cancel the decider e.g. check if all files are present
     *
     * @param commaSeparatedFilePaths
     * @param commaSeparatedParentAccessions
     * @return
     */
    protected ReturnValue doFinalCheck(String commaSeparatedFilePaths, String commaSeparatedParentAccessions) {
        ReturnValue checkReturnValue = new ReturnValue(ReturnValue.SUCCESS);
        return checkReturnValue;
    }

    /**
     * Report an actual launch of a workflow for testing purpose
     *
     * @return false iff we don't actually want to launch
     */
    protected boolean reportLaunch() {
        return true;
    }

    public void setMetaws(Metadata metaws) {
        metadata = metaws;
    }

    /**
     * We now use the guideline that we only count failures when they occur on the same number of files (with the same paths)
     *
     * @param fileStatus
     * @param previousStatus
     * @return
     */
    protected static boolean isCountAsFail(FILE_STATUS fileStatus, PREVIOUS_RUN_STATUS previousStatus) {
        return (fileStatus == FILE_STATUS.SAME_FILES && previousStatus == PREVIOUS_RUN_STATUS.FAILED);
    }

    /**
     * See https://wiki.oicr.on.ca/display/SEQWARE/BasicDecider+logic
     *
     * @param fileStatus
     * @param previousStatus
     * @return
     */
    protected static boolean isDoRerun(FILE_STATUS fileStatus, PREVIOUS_RUN_STATUS previousStatus) {
        LOGGER.info("Considering match with " + fileStatus.name() + " status:" + previousStatus.name());
        boolean strangeCondition = fileStatus == FILE_STATUS.PAST_SUPERSET && previousStatus == PREVIOUS_RUN_STATUS.FAILED;
        if (strangeCondition) {
            LOGGER.warn("****** Workflow run has more files in the past but failed. We will try to re-run, but you should investigate!!!! *******");
        }
        boolean doRerun = true;
        if (fileStatus == FILE_STATUS.PAST_SUBSET_OR_INTERSECTION) {
            doRerun = true;
        } else if (fileStatus == FILE_STATUS.SAME_FILES || fileStatus == FILE_STATUS.PAST_SUPERSET) {
            doRerun = previousStatus == PREVIOUS_RUN_STATUS.FAILED;
        }
        return doRerun;
    }

    private final Predicate<WorkflowRun> workflowRunAttributeFilter = wr -> wr.getAnnotations().stream()
            .noneMatch(attr -> workflowRunAttributeTagFilters.contains(attr.getTag()));

    private List<WorkflowRun> produceAccessionListWithFileList(List<Integer> fileSWIDs) {
        // grab only the workflows in which we are interested
        List<Integer> relevantWorkflows = new ArrayList<>();
        relevantWorkflows.add(Integer.valueOf(this.workflowAccession));
        for (String accession : this.workflowAccessionsToCheck) {
            relevantWorkflows.add(Integer.valueOf(accession));
        }
        // find relevant workflow runs for this group of files
        List<WorkflowRun> wrFiles1 = this.metadata.getWorkflowRunsAssociatedWithInputFiles(fileSWIDs, relevantWorkflows);
        LOGGER.debug("Found " + wrFiles1.size() + " workflow runs via direct search");

        //select workflowRuns that do not have workflow run attribute tags
        List<WorkflowRun> filteredWorkflowRuns = wrFiles1.stream().filter(workflowRunAttributeFilter).collect(Collectors.toList());
        LOGGER.debug("After workflow run attribute filtering " + filteredWorkflowRuns.size() + " relevant workflow runs");
        return filteredWorkflowRuns;
    }

    /**
     * For a given set of file SWIDs in filesToRun, we will count up the number of previous workflow runs that failed and return whether or
     * not we think the workflow should be rerun
     *
     * @param filesToRun
     * @param failures
     * @param previousWorkflowRuns
     * @return
     */
    private boolean processWorkflowRuns(Collection<String> filesToRun, List<Boolean> failures, List<WorkflowRun> previousWorkflowRuns) {
        int count = 0;
        boolean rerun = true;
        for (WorkflowRun previousWorkflowRun : previousWorkflowRuns) {
            count++;
            // only consider previous runs of the same workflow
            if (workflowAccession.equals(previousWorkflowRun.getWorkflowAccession().toString())) {
                FILE_STATUS fileStatus = compareWorkflowRunFiles(previousWorkflowRun.getInputFileAccessions(), filesToRun);
                LOGGER.info("Workflow run " + previousWorkflowRun.getSwAccession() + " has a file status of " + fileStatus);
                PREVIOUS_RUN_STATUS previousStatus = determineStatus(previousWorkflowRun.getStatus());
                LOGGER.info("Workflow run " + previousWorkflowRun.getSwAccession() + " has a status of " + previousStatus);

                boolean countAsFail = isCountAsFail(fileStatus, previousStatus);
                boolean doRerun = isDoRerun(fileStatus, previousStatus);

                if (countAsFail) {
                    LOGGER.info("Workflow run " + previousWorkflowRun.getSwAccession() + " counted as a failure with a file status of "
                            + fileStatus);
                    LOGGER.info("The failing run was workflow_run " + count + "/" + previousWorkflowRuns.size() + " out of "
                            + previousWorkflowRuns.size());
                    failures.add(true);
                }
                if (!doRerun) {
                    LOGGER.info("Workflow run " + previousWorkflowRun.getSwAccession() + " blocking re-run with a status of: "
                            + previousStatus + "  file status of: " + fileStatus);
                    LOGGER.info("The blocking run was workflow_run " + count + "/" + previousWorkflowRuns.size() + " out of "
                            + previousWorkflowRuns.size());
                    rerun = false;
                    break;
                }
            } else if (this.workflowAccessionsToCheck.contains(previousWorkflowRun.getWorkflowAccession().toString())) {
                LOGGER.debug("Workflow run " + previousWorkflowRun.getWorkflowAccession() + " has a workflow "
                        + previousWorkflowRun.getWorkflowAccession() + " on the list of workflow accessions to check");
                // we will check whether all the files to run are contained within the previous run of the workflow, if so we will not
                // re-run
                FILE_STATUS fileStatus = compareWorkflowRunFiles(previousWorkflowRun.getInputFileAccessions(), filesToRun);
                LOGGER.info("Workflow run " + previousWorkflowRun.getSwAccession() + " has a file status of " + fileStatus);
                if (this.isToRunContained(previousWorkflowRun.getInputFileAccessions(), filesToRun)) {
                    LOGGER.info("Previous workflow run contained the all of the files that we want to run");
                    rerun = false;
                }
            } else {
                LOGGER.info("Workflow run " + previousWorkflowRun.getSwAccession() + " was neither a workflow to check nor a previous run of "
                        + workflowAccession + " , ignored");
            }
        }
        return rerun;
    }

    private Set<String> determineFilePaths(Set<Integer> fileSWIDs) {
        Set<String> results = new HashSet<>();
        for (Integer fileSWID : fileSWIDs) {
            net.sourceforge.seqware.common.model.File file = metadata.getFile(fileSWID);
            results.add(file.getFilePath());
        }
        return results;
    }

    private String do_summary_command() {
        StringBuilder command = new StringBuilder();
        // SEQWARE-1612 Change test command to actual jar name
        String seqwareVersion = this.metadata.getClass().getPackage().getImplementationVersion();
        command.append("\njava -jar seqware-distribution-").append(seqwareVersion).append("-full.jar ");
        command.append(spaceSeparateMy(constructCommand()));
        command.append("\n");
        return command.toString();
    }
    
    protected List<Map<String,String>> getFileProvenanceReport(Map<FileProvenanceParam, List<String>> params){
        LOGGER.error("Cannot return file provenance report from BasicDecider.getFileProvenanceReport");
        return Collections.emptyList();
    }
    
    protected Map<FileProvenanceParam, List<String>> parseOptions(){
        return ProvenanceUtility.convertOptionsToMap(options, metadata);
    }

    private List<ReturnValue> createListOfRelevantFilePaths() {

        List<ReturnValue> vals;
        List<Map<String, String>> fileProvenanceReport;
        Map<FileProvenanceParam, List<String>> map = parseOptions();
        if (skipStuff) {
            map.put(FileProvenanceParam.skip, new ImmutableList.Builder<String>().add("false").build());
        }
        map.put(FileProvenanceParam.workflow_run_status, new ImmutableList.Builder<String>().add(WorkflowRunStatus.completed.toString())
                .build());
        map.put(FileProvenanceParam.processing_status, new ImmutableList.Builder<String>().add(ProcessingStatus.success.toString()).build());
        if (this.parentWorkflowAccessions.size() > 0) {
            map.put(FileProvenanceParam.workflow, new ImmutableList.Builder<String>().addAll(this.parentWorkflowAccessions).build());
        }

        fileProvenanceReport = getFileProvenanceReport(map);
        // convert to list of ReturnValues for backwards compatibility
        vals = convertFileProvenanceReport(fileProvenanceReport);
        // consider memory use and GC here
        return vals;
    }

    private List<ReturnValue> convertFileProvenanceReport(List<Map<String, String>> fileProvenanceReport) {
        List<ReturnValue> list = new ArrayList<>();
        for (Map<String, String> map : fileProvenanceReport) {
            ReturnValue row = new ReturnValue();
            row.setAttributes(map);
            list.add(row);
            
            if (map.get(Header.FILE_PATH.getTitle()) != null) {
                // mutate additional rows into a nested FileMetadata object
                FileMetadata fm = new FileMetadata();
                fm.setFilePath(map.get(Header.FILE_PATH.getTitle()));
                fm.setMetaType(map.get(Header.FILE_META_TYPE.getTitle()));
                fm.setDescription(map.get(Header.FILE_DESCRIPTION.getTitle()));
                fm.setMd5sum(map.get(Header.FILE_MD5SUM.getTitle()));
                if (map.containsKey(Header.FILE_SIZE.getTitle())) {
                    if (!map.get(Header.FILE_SIZE.getTitle()).isEmpty()) {
                        fm.setSize(Long.valueOf(map.get(Header.FILE_SIZE.getTitle())));
                    }
                }
                row.setFiles(new ArrayList<>(new ImmutableList.Builder<FileMetadata>().add(fm).build()));
            }
            
            handleAttributes(map, row, Header.STUDY_ATTRIBUTES, Header.STUDY_TAG_PREFIX);
            handleAttributes(map, row, Header.EXPERIMENT_ATTRIBUTES, Header.EXPERIMENT_TAG_PREFIX);
            handleAttributes(map, row, Header.PARENT_SAMPLE_ATTRIBUTES, Header.PARENT_SAMPLE_TAG_PREFIX);
            handleAttributes(map, row, Header.SAMPLE_ATTRIBUTES, Header.SAMPLE_TAG_PREFIX);
            handleAttributes(map, row, Header.IUS_ATTRIBUTES, Header.IUS_TAG_PREFIX);
            handleAttributes(map, row, Header.LANE_ATTRIBUTES, Header.LANE_TAG_PREFIX);
            handleAttributes(map, row, Header.SEQUENCER_RUN_ATTRIBUTES, Header.SEQUENCER_RUN_TAG_PREFIX);
            handleAttributes(map, row, Header.PROCESSING_ATTRIBUTES, Header.PROCESSING_TAG_PREFIX);
            handleAttributes(map, row, Header.FILE_ATTRIBUTES, Header.FILE_TAG_PREFIX);
        }
        return list;
    }

    private void handleAttributes(Map<String, String> map, ReturnValue row, Header headerType, Header headerPrefix) {
        // mutate attributes into expected format from FindAllTheFiles
        String attributes = map.remove(headerType.getTitle());
        if (attributes != null && !attributes.isEmpty()) {
            String[] studyAttrArr = attributes.split(";");
            for (String studyAttr : studyAttrArr) {
                String[] parts = studyAttr.split("=");
                String key = parts[0];
                String value = parts.length > 1 ? parts[1] : null;
                FindAllTheFiles.addAttributeToReturnValue(row, key, value);
            }
        }
    }

    /**
     * These file statuses reflect the discussion at https://wiki.oicr.on.ca/display/SEQWARE/BasicDecider+logic
     */
    protected enum FILE_STATUS {
        /**
         * Two sets of files have no relationship
         */
        DISJOINT_SETS,
        /**
         * Two sets of files partially overlap i.e. intersection and subset (set of files in the past was smaller)
         */
        PAST_SUBSET_OR_INTERSECTION,
        /**
         * the same files are found at the same paths
         */
        SAME_FILES,
        /**
         * The set of files in the past was strictly larger than the current files under consideration
         */
        PAST_SUPERSET
    }

    /**
     * We care about three types of status, an outright fail, other (pending, running, submitted, etc.), and completed
     */
    protected enum PREVIOUS_RUN_STATUS {
        FAILED, OTHER, COMPLETED
    }

    private class ReturnValueProcessingTimeComparator implements Comparator<Entry<String, List<ReturnValue>>> {

        @Override
        public int compare(Entry<String, List<ReturnValue>> t0, Entry<String, List<ReturnValue>> t1) {
            DateFormat formatter = new SimpleDateFormat();
            Integer t0date = latestSWID(t0, formatter);
            Integer t1date = latestSWID(t1, formatter);
            return t1date.compareTo(t0date);
        }

        private Integer latestSWID(Entry<String, List<ReturnValue>> t0, DateFormat formatter) {
            // grab the latest date in each group
            Integer latestSWID = Integer.MIN_VALUE;
            for (ReturnValue t0i : t0.getValue()) {
                Integer currInt = Integer.valueOf(t0i.getAttribute(FindAllTheFiles.FILE_SWA));

                if (currInt != null && currInt > latestSWID) {
                    latestSWID = currInt;
                }
            }
            return latestSWID;
        }

    }

    private boolean getBooleanFlagOrArgValue(OptionSpec<Boolean> param) {
        if (options.has(param)) {
            if (options.hasArgument(param)) {
                //return explicit boolean provided as arg
                return options.valueOf(param);
            } else {
                //only parameter provided - treat as flag
                return true;
            }
        } else {
            //return default
            return options.valueOf(param);
        }
    }
}
