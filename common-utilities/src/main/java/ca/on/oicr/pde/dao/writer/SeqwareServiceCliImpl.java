package ca.on.oicr.pde.dao.writer;

import ca.on.oicr.pde.model.Experiment;
import ca.on.oicr.pde.model.Ius;
import ca.on.oicr.pde.model.Lane;
import ca.on.oicr.pde.model.Sample;
import ca.on.oicr.pde.model.SequencerRun;
import ca.on.oicr.pde.model.SeqwareObject;
import ca.on.oicr.pde.model.Study;
import ca.on.oicr.pde.model.Workflow;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.seqware.pipeline.runner.PluginRunner;

/**
 *
 * @author mlaszloffy
 */
public class SeqwareServiceCliImpl implements SeqwareWriteService {

    private final File seqwareSettings;

    private final PrintStream SYSOUT;

    private static AtomicInteger counter = new AtomicInteger(0);

    public SeqwareServiceCliImpl(File seqwareSettings) {
        this.seqwareSettings = seqwareSettings;
        SYSOUT = System.out;
    }

    @Override
    public Experiment createExperiment(String description, String platformId, Study study, String title) {
        List<String> params = new ArrayList(Arrays.asList(
                "--description", sanitize(description),
                "--platform-id", sanitize(platformId),
                "--study-accession", sanitize(study.getSwid()),
                "--title", sanitize(title)
        ));
        //io.seqware.cli.Main.createExperiment
        String swid = callPrivateSeqwareCliMethod("createExperiment", params);
        Experiment.Builder b = new Experiment.Builder();
        b.setSwid(swid);
        return b.build();
    }

    @Override
    public Ius createIus(String barcode, String description, Lane lane, String name, Sample sample, boolean skip) {
        List<String> params = new ArrayList(Arrays.asList(
                "--barcode", sanitize(barcode),
                "--description", sanitize(description),
                "--lane-accession", sanitize(lane.getSwid()),
                "--name", sanitize(name),
                "--sample-accession", sanitize(sample.getSwid()),
                "--skip", Boolean.toString(skip)
        ));
        //io.seqware.cli.Main.createIus
        String swid = callPrivateSeqwareCliMethod("createIus", params);
        Ius.Builder b = new Ius.Builder();
        b.setSwid(swid);
        return b.build();
    }

    @Override
    public Lane createLane(String cycleDescriptor, String description, String laneNumber, String librarySelectionId, String librarySourceId, String libraryStrategyId, String name, SequencerRun sequencerRun, boolean skip, String studyTypeId) {
        List<String> params = new ArrayList(Arrays.asList(
                "--cycle-descriptor", sanitize(cycleDescriptor),
                "--description", sanitize(description),
                "--lane-number", sanitize(laneNumber),
                "--library-selection-accession", sanitize(librarySelectionId),
                "--library-source-accession", sanitize(librarySourceId),
                "--library-strategy-accession", sanitize(libraryStrategyId),
                "--name", sanitize(name),
                "--sequencer-run-accession", sanitize(sequencerRun.getSwid()),
                "--skip", Boolean.toString(skip),
                "--study-type-accession", sanitize(studyTypeId)
        ));
        //io.seqware.cli.Main.createLane
        String swid = callPrivateSeqwareCliMethod("createLane", params);
        Lane.Builder b = new Lane.Builder();
        b.setSwid(swid);
        return b.build();
    }

    @Override
    public Sample createSample(String description, Experiment experiment, String organismId, String title, Sample parentSample) {
        List<String> params = new ArrayList(Arrays.asList(
                "--description", sanitize(description),
                "--experiment-accession", sanitize(experiment.getSwid()),
                "--organism-id", sanitize(organismId),
                "--title", sanitize(title)
        ));
        if (parentSample != null) {
            params.add("--parent-sample-accession");
            params.add(parentSample.getSwid());
        }
        String swid;
        if (parentSample != null) {
            //io.seqware.cli.Main.createSample does not allow us to access additional fields, so Metadata plugin must be called.
            swid = createObject("sample", params);
        } else {
            //io.seqware.cli.Main.createSample
            swid = callPrivateSeqwareCliMethod("createSample", params);
        }
        Sample.Builder b = new Sample.Builder();
        b.setSwid(swid);
        return b.build();
    }

    @Override
    public SequencerRun createSequencerRun(String description, String filePath, String name, boolean isPairedEnd, String platformId, boolean skip) {
        List<String> params = new ArrayList(Arrays.asList(
                "--description", sanitize(description),
                "--file-path", sanitize(filePath),
                "--name", sanitize(name),
                "--paired-end", Boolean.toString(isPairedEnd),
                "--platform-accession", sanitize(platformId),
                "--skip", Boolean.toString(skip)
        ));
        //io.seqware.cli.Main.createSequencerRun
        String swid = callPrivateSeqwareCliMethod("createSequencerRun", params);
        SequencerRun.Builder b = new SequencerRun.Builder();
        b.setSwid(swid);
        return b.build();
    }

    @Override
    public Study createStudy(String accession, String centerName, String centerProjectName, String description, String studyType, String title) {
        List<String> params = new ArrayList(Arrays.asList(
                "--accession", sanitize(accession),
                "--center-name", sanitize(centerName),
                "--center-project-name", sanitize(centerProjectName),
                "--description", sanitize(description),
                "--study-type", sanitize(studyType),
                "--title", sanitize(title)));
        //io.seqware.cli.Main.createStudy
        String swid = callPrivateSeqwareCliMethod("createStudy", params);
        Study.Builder b = new Study.Builder();
        b.setSwid(swid);
        return b.build();
    }

    @Override
    public String createWorkflowRun(Workflow workflow, List<? extends SeqwareObject> parents, List<FileInfo> files) {
        List<String> params = new ArrayList(Arrays.asList(
                "--workflow-accession", sanitize(workflow.getSwid())
        ));

        for (SeqwareObject o : parents) {
            params.add("--parent-accession");
            params.add(sanitize(o.getSwid()));
        }

        for (FileInfo f : files) {
            params.add("--file");
            params.add(f.getType() + "::" + f.getMetaType() + "::" + f.getFilePath());
        }

        //io.seqware.cli.Main.createSequencerRun
        return callPrivateSeqwareCliMethod("createWorkflowRun", params);
    }

    @Override
    public Workflow createWorkflow(String name, String version, String description) {
        Workflow.Builder w = new Workflow.Builder();
        w.setName(name);
        w.setVersion(version);

        List<String> params = new ArrayList(Arrays.asList(
                "--name", sanitize(name),
                "--version", sanitize(version),
                "--description", sanitize(description)
        ));
        //io.seqware.cli.Main.createWorkflow
        w.setSwid(callPrivateSeqwareCliMethod("createWorkflow", params));

        return w.build();
    }

    @Override
    public void annotate(SeqwareObject o, String key, String value) {
        List<String> params = new ArrayList(Arrays.asList(
                o.getTableName(),
                "--accession", sanitize(o.getSwid()),
                "--key", sanitize(key),
                "--val", sanitize(value)
        ));
        //io.seqware.cli.Main.annotate
        callPrivateSeqwareCliMethod("annotate", params);
    }

    @Override
    public void updateFileReport() {
        List<String> params = new ArrayList(Arrays.asList(
                "refresh"
        ));
        //io.seqware.cli.Main.files
        callPrivateSeqwareCliMethod("files", params);
    }

//    @Override
//    public void annotate(String objectType, String id, String key, String value) {
//        List<String> params = new ArrayList(Arrays.asList(
//                objectType,
//                "--accession", sanitize(id),
//                "--key", sanitize(key),
//                "--val", sanitize(value)
//        ));
//        //io.seqware.cli.Main.annotate
//        callPrivateSeqwareCliMethod("annotate", params);
//    }
    private final Pattern seqwareSwidRegex = Pattern.compile("SWID: (\\d*)");

    private String createObject(String objectType, List<String> params) {
        List<String> args = new ArrayList<String>();
        args.add("--plugin");
        args.add("net.sourceforge.seqware.pipeline.plugins.Metadata");
        args.add("--");
        args.add("--table");
        args.add(objectType);
        args.add("--create");
        for (String s : params) {
            if (s == null) {
                args.remove(args.size() - 1);
            } else if (s.startsWith("--")) {
                args.add("--field");
                args.add(s.substring(2).replace("-", "_") + "::");
            } else {
                args.add(args.remove(args.size() - 1).concat(sanitize(s)));
            }
        }

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        PrintStream p = new PrintStream(b);

        synchronized (SYSOUT) {
            System.setOut(p);
            System.setProperty("SEQWARE_SETTINGS", seqwareSettings.getAbsolutePath());
            PluginRunner.main(args.toArray(new String[args.size()]));
            System.out.flush();
            System.setOut(SYSOUT);
        }
        Matcher ma = seqwareSwidRegex.matcher(b.toString());

        ma.find();

        return ma.group(1).trim();
    }

    private String callPrivateSeqwareCliMethod(String methodName, List<String> params) {
        String r = null;
        try {
            System.setProperty("SEQWARE_SETTINGS", seqwareSettings.getAbsolutePath());
            Method m = io.seqware.cli.Main.class.getDeclaredMethod(methodName, List.class);
            m.setAccessible(true);
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            PrintStream p = new PrintStream(b);

            //Need to exclusive access to system.out
            synchronized (SYSOUT) {
                System.setOut(p);

                m.invoke(null, params);

                System.out.flush();
                //System.err.flush();

                System.setOut(SYSOUT);
            }
            Matcher ma = seqwareSwidRegex.matcher(b.toString());

            if (ma.find()) {
                r = ma.group(1).trim();
            } else {
                r = null;
            }
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(SeqwareServiceCliImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(SeqwareServiceCliImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(SeqwareServiceCliImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(SeqwareServiceCliImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(SeqwareServiceCliImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return r;
    }

    private String sanitize(String input) {
        if (input == null) {
            return " ";
        }
        return input;
    }

    @Override
    public void annotate(SeqwareObject o, boolean skip, String reason) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
