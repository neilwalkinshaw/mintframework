package mint.testgen.sequential.gui.efg;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import mint.Configuration;
import mint.evaluation.coberturaAnalysis.CoverageComparison;
import mint.inference.InferenceBuilder;
import mint.testgen.sequential.gui.GUISMTester;
import mint.testgen.sequential.gui.efg.EFGDFSTester;
import mint.testgen.sequential.gui.efg.EFGReader;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by neilwalkinshaw on 01/09/2017.
 */
public class EFGGuiTester extends GUISMTester {

    protected static int initCounter = 0;

    public EFGGuiTester(InferenceBuilder ib, TraceSet tr, String testDir, String testExecutionCommand, String workingDir) {
        super(ib, tr, testDir, testExecutionCommand, workingDir);
    }

    public static void main(String[] args) throws IOException {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ALL);
        parseCommandLine(args);

        File f = new File(ops.get("initial"));

        EFGReader efgReader = new EFGReader();
        efgReader.readFile(f);
        DirectedPseudograph<String,DefaultEdge> graph = efgReader.getGraph();
        Set<String> init = efgReader.getInitNodes();
        EFGDFSTester tester = new EFGDFSTester(graph,init,true);

        //TraceSet base = runPreliminaryTests(tester.getTests(true).getPos());
        TraceSet base = populateTraces(f);

        InferenceBuilder ib = new InferenceBuilder(Configuration.getInstance());
        for(int i = 0; i<1; i++) {
            TraceSet initial = new TraceSet();
            /*initial.getPos().addAll(base.getPos());
            initial.getNeg().addAll(base.getNeg());
            Configuration.getInstance().SEED=i;
            File testRoot = new File(ops.get("testRoot"));
            testRoot.mkdir();
            GUISMTester guiSM = new GUISMTester(ib, initial, ops.get("testRoot")+File.separator+Integer.toString(i), ops.get("command"), ops.get("workingDir"));
            guiSM.run(5,30);
            */
            File testRoot_rand = new File(ops.get("testRoot")+ "_rand");
            testRoot_rand.mkdir();
            initial = new TraceSet();
            initial.getPos().addAll(base.getPos());
            initial.getNeg().addAll(base.getNeg());
            GUISMTester guiSM = new GUISMTester(ib, initial, ops.get("testRoot") + "_rand"+File.separator+Integer.toString(i), ops.get("command"), ops.get("workingDir"));
            //guiSM.setTarget(200);
            guiSM.setTestGenerator(tester);
            //guiSM.run();
            guiSM.run(5,30);
            //CoverageComparison cc = new CoverageComparison(ops.get("testRoot")+File.separator+Integer.toString(i),ops.get("testRoot")+"_rand"+File.separator+Integer.toString(i));
            //FileUtils.deleteDirectory(testRoot);
            //FileUtils.deleteDirectory(testRoot_rand);
        }
    }

    private static TraceSet runPreliminaryTests(List<List<TraceElement>> tests){
        TraceSet ts = new TraceSet();
        try {
            File tDir = new File(ops.get("testRoot"));
            File testSubdir = new File(tDir.getCanonicalPath()+File.separator+"init");
            File reportSubdir = new File(testSubdir.getCanonicalPath()+File.separator+"reports");
            checkAndMake(tDir);
            checkAndMake(testSubdir);
            checkAndMake(reportSubdir);
            for(List<TraceElement> test : tests){
                writeTest(test,testSubdir);
            }
            if(!tests.isEmpty()) {
                execute(testSubdir);
                ts = populateTraces(reportSubdir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ts;
    }

    private static void execute(File testSubdir) throws InterruptedException, IOException {
        ProcessBuilder pb = new ProcessBuilder(ops.get("command"), testSubdir.getCanonicalPath());
        File workingDir = new File(ops.get("workingDir"));
        pb.directory(workingDir);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process p = pb.start();
        p.waitFor();

    }

    @SuppressWarnings("static-access")
    public static void parseCommandLine(String[] args) {
        Options options = new Options();
        Option efg = OptionBuilder.withArgName("efg").hasArg().withDescription("Event-Flow Graph (EFG)").create("efg");
        Option testRoot = OptionBuilder.withArgName("testRoot").hasArg().withDescription("Root directory for test sets").create("testRoot");
        Option command = OptionBuilder.withArgName("command").hasArg().withDescription("Test execution command").create("command");
        Option workingDir = OptionBuilder.withArgName("workingDir").hasArg().withDescription("Working directory").create("workingDir");
        Option help = new Option("help", "print this message");
        Option algorithm = OptionBuilder.withArgName("algorithm").hasArg().withDescription("J48, JRIP, NaiveBayes, AdaBoostDiscrete").create("algorithm");
        Option data = OptionBuilder.withArgName("data").hasArg().withDescription("use variable data for inference or not").create("data");
        Option k = OptionBuilder.withArgName("k").hasArg().withDescription("minimum length of overlapping outgoing paths for a merge").create("k");
        Option wekaOptions = OptionBuilder.withArgName("wekaOptions").hasArgs().withDescription("WEKA options for specific learning algorithms (See WEKA documentation)").create("wekaOptions");
        wekaOptions.setArgs(6);
        Option visualise = OptionBuilder.withArgName("visualise").hasArg().withDescription("How to output your EFSM - either `text' or `graphical'.").create("visualise");
        Option daikon = OptionBuilder.withArgName("daikon").withDescription("Generate Daikon invariants for transitions").create("daikon");
        Option strategy = OptionBuilder.withArgName("strategy").hasArg().withDescription("redblue,gktails,noloops,ktails").create("strategy");
        Option gp = OptionBuilder.withArgName("gp").withDescription("Use GP to infer transition functions.").create("gp");
        Option carefulDet = OptionBuilder.withArgName("carefulDet").withDescription("Determinize to prevent overgeneralisation.").create("carefulDet");
        Option seed = OptionBuilder.withArgName("seed").hasArg().withDescription("Random seed").create("seed");
        options.addOption(workingDir);
        options.addOption(command);
        options.addOption(efg);
        options.addOption(testRoot);
        options.addOption(help);
        options.addOption(daikon);
        options.addOption(algorithm);
        options.addOption(k);
        options.addOption(data);
        options.addOption(wekaOptions);
        options.addOption(visualise);
        options.addOption(strategy);
        options.addOption(gp);
        options.addOption(carefulDet);
        options.addOption(seed);
        // create the parser
        CommandLineParser parser = new GnuParser();
        Configuration configuration = Configuration.getInstance();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("help") ) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("GUISMTester", options);
            }
            if(line.hasOption("efg"))
                ops.put("initial",String.valueOf((line.getOptionValue("efg"))));
            if(line.hasOption("testRoot"))
                ops.put("testRoot",String.valueOf((line.getOptionValue("testRoot"))));
            if(line.hasOption("command"))
                ops.put("command",String.valueOf((line.getOptionValue("command"))));
            if(line.hasOption("workingDir"))
                ops.put("workingDir",String.valueOf((line.getOptionValue("workingDir"))));
            configuration.PREFIX_CLOSED = true;
            if (line.hasOption("data"))
                configuration.DATA = Boolean.valueOf(line.getOptionValue("data"));
            if (line.hasOption("algorithm"))
                configuration.ALGORITHM = Configuration.Data.valueOf(line.getOptionValue("algorithm"));
            if (line.hasOption("k"))
                configuration.K = Integer.valueOf(line.getOptionValue("k"));
            if (line.hasOption("daikon"))
                configuration.DAIKON = true;
            if (line.hasOption("gp"))
                configuration.GP = true;
            if (line.hasOption("carefulDet"))
                configuration.CAREFUL_DETERMINIZATION = true;
            if (line.hasOption("wekaOptions")){
                String[] opt = line.getOptionValues("wekaOptions");
                configuration.WEKA_OPTIONS =opt;
            }
            if (line.hasOption("visualise"))
                configuration.VIS = Configuration.Visualise.valueOf(line.getOptionValue("visualise"));
            if (line.hasOption("strategy"))
                configuration.STRATEGY = Configuration.Strategy.valueOf(line.getOptionValue("strategy"));

            if (line.hasOption("seed"))
                configuration.SEED = Integer.valueOf(line.getOptionValue("seed"));

        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ExperimentDriver", options);
        }

    }

}
