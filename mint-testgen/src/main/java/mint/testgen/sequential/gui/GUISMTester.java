package mint.testgen.sequential.gui;

import mint.Configuration;
import mint.evaluation.coberturaAnalysis.CoverageComparison;
import mint.inference.InferenceBuilder;
import mint.inference.efsm.AbstractMerger;
import mint.model.Machine;
import mint.testgen.sequential.AbstractSMTester;
import mint.testgen.sequential.LowestAverageWeightTransitionCoverageTester;
import mint.testgen.sequential.TestGenerator;
import mint.testgen.sequential.gui.efg.EFGCompatibilityChecker;
import mint.testgen.sequential.gui.efg.EFGDFSTester;
import mint.testgen.sequential.gui.efg.EFGReader;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;
import mint.visualise.dot.DotGraphWithLabels;
import org.apache.commons.cli.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;

import java.io.*;
import java.util.*;

/**
 * Created by neilwalkinshaw on 12/07/2017.
 */
public class GUISMTester extends AbstractSMTester {

    protected static Map<String,String> ops = new HashMap<String, String>();

    protected File testDir;
    protected static String testExecutionCommand;
    protected String workingDir;
    protected int testCounter = 0;
    protected DirectedPseudograph<String,DefaultEdge> efg = null;
    protected List<Double> seqLengths;


    public void setEFG( DirectedPseudograph<String,DefaultEdge> efg){
        this.efg = efg;
    }

    private final static Logger LOGGER = Logger.getLogger(GUISMTester.class.getName());


    public GUISMTester(InferenceBuilder ib, TraceSet tr, String testDir, String testExecutionCommand, String workingDir) {
        super(ib, tr);
        this.testDir = new File(testDir);
        this.testExecutionCommand = testExecutionCommand;
        this.workingDir = workingDir;
        this.seqLengths = new ArrayList<Double>();
    }

    public List<Double> getSeqLengths(){
        return seqLengths;
    }

    @Override
    protected TraceSet runTests(int iteration, List<List<TraceElement>> testSet) {
        TraceSet ts = new TraceSet();
        try {

            File testSubdir = new File(testDir.getCanonicalPath()+File.separator+Integer.toString(iteration));
            LOGGER.debug(testSubdir.getCanonicalPath());
            File reportSubdir = new File(testSubdir.getCanonicalPath()+File.separator+"reports");
            checkAndMake(testDir);
            checkAndMake(testSubdir);
            checkAndMake(reportSubdir);

            for(List<TraceElement> test : testSet){
                writeTest(test,testSubdir);
            }
            if(!testSet.isEmpty()) {
                execute(testSubdir);
                ts = populateTraces(reportSubdir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        seqLengths.add(averageLength(ts));
        return ts;
    }

    private Double averageLength(TraceSet ts) {
        double total = 0;
        for(List pos : ts.getPos()){
            total += pos.size();
        }
        for(List neg : ts.getNeg()){
            total += neg.size();
        }
        return total / (ts.getNeg().size() + ts.getPos().size());
    }


    protected static void checkAndMake(File testDir) {
        if(!testDir.exists())
            testDir.mkdir();
    }

    private void execute(File testSubdir) throws InterruptedException, IOException {
        ProcessBuilder pb = new ProcessBuilder(testExecutionCommand, testSubdir.getCanonicalPath());
        File workingDir = new File(this.workingDir);
        pb.directory(workingDir);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process p = pb.start();
        p.waitFor();
        p.destroy();

    }


    protected static void writeTest(List<TraceElement> test, File testSubdir) {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<TestCase>\n";
        String sequence = "";
        for(TraceElement te : test){
            sequence+=te.getName()+",";
            content +="<Step>\n<EventId>"+te.getName()+"</EventId>\n<ReachingStep>true</ReachingStep>\n</Step>\n";
        }
        content+="</TestCase>";
        String fileName = testSubdir.getName()+testSubdir.listFiles().length+".tst";
        try {
            FileWriter fw = new FileWriter(testSubdir+File.separator+fileName);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
            fw.close();
            fw = new FileWriter(testSubdir+File.separator+"sequence");
            bw = new BufferedWriter(fw);
            bw.write(sequence);
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static TraceSet populateTraces(File logDir) {
        TraceSet traces = new TraceSet();
        File[] files = logDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".log");
            }
        });
        for(File f : files){
            List<TraceElement> trace = new ArrayList<TraceElement>();
            boolean passed = false;
            try{
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {
                    if(line.contains("BEGIN STEP"))
                        recordStep(br,trace);
                    if(line.contains("END TESTCASE"))
                        passed = getOutcome(br);
                }
                if(passed){
                    traces.addPos(trace);
                }
                else
                    traces.addNeg(trace);
            }
            catch(IOException e){
                LOGGER.error("Problem with log file");
            }
        }
        return traces;
    }

    private static boolean getOutcome(BufferedReader br) throws IOException {
        boolean passed = false;
        String line = null;
        while ((line = br.readLine()) != null) {
            if(line.startsWith("Pass status    :")){
                String status = line.substring(16);
                status = status.trim();
                if(status.equals("-2"))
                    passed = false;
                else
                    passed = true;
            }
        }
        return passed;
    }

    private static void recordStep(BufferedReader br, List<TraceElement> trace) throws IOException {
        String line;
        List<String> lines = new ArrayList<String>();
        while ((line = br.readLine())!=null) {
            if(line.contains("END STEP")) {
                break;
            }
            else{
                lines.add(line);
            }
        }
        TraceElement newElement = getTraceElement(lines);
        trace.add(newElement);
        if(trace.size()>1){
            TraceElement previous = trace.get(trace.size()-2);
            previous.setNext(newElement);
        }

    }

    private static TraceElement getTraceElement(List<String> lines) {
        String name = getName(lines);
        Collection<VariableAssignment<?>> vars = getVars(lines);
        TraceElement te = new SimpleTraceElement(name,vars);
        return te;
    }

    private static Collection<VariableAssignment<?>> getVars(List<String> lines) {
        Collection<VariableAssignment<?>> vars = new HashSet<VariableAssignment<?>>();
        VariableAssignment<String> window = new StringVariableAssignment("window");
        VariableAssignment<String> widget = new StringVariableAssignment("widget");
        for(String line : lines){
            String trimmed = line.trim();
            if(trimmed.startsWith("+ Window Title = ")){
                String windowName = trimmed.substring(16);
                window.setValue(windowName.trim());
            }
            else if(trimmed.startsWith("+ Widget Title = ")){
                String widgetName = trimmed.substring(16);
                widget.setValue(widgetName.trim());
            }
        }
        vars.add(window);
        vars.add(widget);
        return vars;
    }

    private static String getName(List<String> lines) {
        boolean found = false;
        String name = "";
        for(String line : lines){
            found = true;
            if(line.startsWith("Executing Step EventID")){
                name = line.substring(line.lastIndexOf(" "));
                name.trim();
                break;
            }
        }
        if(!found){
            LOGGER.error("Did not find event name for trace step.");
        }
        return name.trim();
    }

    public static void main(String[] args) throws IOException {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ALL);
        parseCommandLine(args);
        //File f = new File(ops.get("initial"));
        //TraceSet base = populateTraces(f);
        int reps = 300;
        String label = ops.get("label");
        if(label == null){
            label = "Summary";
        }

        File efg = new File(ops.get("efg"));
        EFGReader efgReader = new EFGReader();
        efgReader.readFile(efg);
        DirectedPseudograph<String,DefaultEdge> graph = efgReader.getGraph();
        Set<String> init = efgReader.getInitNodes();
        EFGDFSTester globTester = new EFGDFSTester(graph,init,false);

        TraceSet base = globTester.getTests(false);

        InferenceBuilder ib = new InferenceBuilder(Configuration.getInstance());
        TraceSet initial = new TraceSet();
        initial.getPos().addAll(base.getPos());
        initial.getNeg().addAll(base.getNeg());

        LowestAverageWeightTransitionCoverageTester tester = new LowestAverageWeightTransitionCoverageTester(initial);
        //for(int i = 0; i< 5; i++) {
            int i = Integer.parseInt(ops.get("seed"));
            Configuration.getInstance().SEED = i;
            File testRoot = new File(ops.get("testRoot"));
            testRoot.mkdir();
            long current = System.currentTimeMillis();
            GUISMTester guiSM = new GUISMTester(ib, new TraceSet(), ops.get("testRoot") + File.separator + Integer.toString(i), ops.get("command"), ops.get("workingDir"));
            guiSM.setEFG(graph);
            guiSM.setTestGenerator(tester);
            guiSM.run(Integer.parseInt(ops.get("interval")), reps);
            long mlTimes = System.currentTimeMillis() - current;
            List<Double> mlSeqs = guiSM.getSeqLengths();

            File testRoot_rand = new File(ops.get("testRoot") + "_rand");
            testRoot_rand.mkdir();
            tester.setRandom(true);
            current = System.currentTimeMillis();
            guiSM = new GUISMTester(ib, new TraceSet(), ops.get("testRoot") + "_rand" + File.separator + Integer.toString(i), ops.get("command"), ops.get("workingDir"));
            guiSM.setTestGenerator(tester);
            guiSM.run(Integer.parseInt(ops.get("interval")), reps);
            long randTimes = System.currentTimeMillis() - current;
            List<Double> randSeqs = guiSM.getSeqLengths();
            CoverageComparison cc = new CoverageComparison(ops.get("testRoot")+File.separator+Integer.toString(i),ops.get("testRoot")+"_rand"+File.separator+Integer.toString(i),reps);

            summarise(mlSeqs,randSeqs,cc.getCovered_A(),cc.getCovered_B(),cc.getTotLines(),mlTimes,randTimes,label+"-sd"+Integer.toString(i)+"-k"+Configuration.getInstance().K+".csv");

        //}

    }

    private static void summarise(List<Double> lengthsA, List<Double> lengthsB, List<Collection<String>> covered_a, List<Collection<String>> covered_b, double tot_lines, long mlTimes, long randTimes, String fileName) {
        LOGGER.debug("Summarising");
        File out = new File(fileName);
        FileWriter fw;

        try {
            if(!out.exists())
                out.createNewFile();
            fw = new FileWriter(out,true);
            for(int i = 0; i<covered_a.size(); i++){
                double lengthA = lengthsA.get(i);
                double lengthB = lengthsB.get(i);
                Collection<String> a = covered_a.get(i);
                Collection<String> b = covered_b.get(i);
                Collection<String> shared= new HashSet<String>();
                shared.addAll(a);
                shared.retainAll(b);
                //System.out.println(a.size()+","+
                //        b.size()+","+
                //        shared.size()+","+tot_lines);
                fw.write(lengthA+","+lengthB+","+a.size()+","+
                        b.size()+","+
                        shared.size()+","+tot_lines+","+mlTimes+","+randTimes+"\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected TestGenerator getTestGenerator(){

        LowestAverageWeightTransitionCoverageTester lawt  = (LowestAverageWeightTransitionCoverageTester)generator;
        lawt.setTraces(traces);

        return generator;
    }

    @SuppressWarnings("static-access")
    public static void parseCommandLine(String[] args) {
        Options options = new Options();
        Option label = OptionBuilder.withArgName("label").hasArg().withDescription("Label for csv file with results").create("label");
        Option initial = OptionBuilder.withArgName("initial").hasArg().withDescription("Initial test sets").create("initial");
        Option interval = OptionBuilder.withArgName("interval").hasArg().withDescription("Interval").create("interval");
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
        options.addOption(label);
        options.addOption(workingDir);
        options.addOption(interval);
        options.addOption(command);
        options.addOption(initial);
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
                ops.put("efg",String.valueOf((line.getOptionValue("efg"))));
            if(line.hasOption("label"))
                ops.put("label",String.valueOf((line.getOptionValue("label"))));
            if(line.hasOption("interval"))
                ops.put("interval",String.valueOf((line.getOptionValue("interval"))));
            if(line.hasOption("initial"))
                ops.put("initial",String.valueOf((line.getOptionValue("initial"))));
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

            if (line.hasOption("seed")) {
                ops.put("seed",String.valueOf(line.getOptionValue("seed")));
                configuration.SEED = Integer.valueOf(line.getOptionValue("seed"));
            }

        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ExperimentDriver", options);
        }

    }

    protected Machine inferMachine(){
        AbstractMerger merger = ib.getInference(traces);
        if(efg!=null) {
            merger.setScorer(new EFGCompatibilityChecker(efg));
        }
        Machine m = merger.infer();
        try {
            String mDir = testDir+File.separator+"models";
            File machineDir = new File(testDir+File.separator+"models");
            checkAndMake(testDir);
            checkAndMake(machineDir);
            File machineFile = new File(mDir+File.separator+"machine_"+Integer.toString(this.testCounter));
            FileOutputStream os = new FileOutputStream(machineFile);
            DotGraphWithLabels.summaryDotGraph(m, os);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return m;
    }

    @Override
    protected List<List<TraceElement>> getTestSet(int perIteration) {
        Machine m = null;
        if(!(traces.getPos().isEmpty() && traces.getNeg().isEmpty()))
            m = inferMachine();
        return getTestGenerator().generateTests(perIteration,m);
    }


}
