package mint.tracedata.readers;

import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by neilwalkinshaw on 10/07/2017.
 */
public class GUITARLogsReader {

    private File[] files;
    private TraceSet traces;

    private final static Logger LOGGER = Logger.getLogger(TraceReader.class.getName());


    public TraceSet getTraces(){
        return traces;
    }

    private void populateTraces(String logDir) {
        File logDirectory = new File(logDir);
        files = logDirectory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".log");
            }
        });
        traces = new TraceSet();
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
    }

    private boolean getOutcome(BufferedReader br) throws IOException {
        boolean passed = false;
        String line = null;
        while ((line = br.readLine()) != null) {
            if(line.startsWith("Pass status    :")){
                String status = line.substring(16);
                status = status.trim();
                if(status.equals("0"))
                    passed = true;
                else
                    passed = false;
            }
        }
        return passed;
    }

    private void recordStep(BufferedReader br, List<TraceElement> trace) throws IOException {
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

    private TraceElement getTraceElement(List<String> lines) {
        String name = getName(lines);
        Collection<VariableAssignment<?>> vars = getVars(lines);
        TraceElement te = new SimpleTraceElement(name,vars);
        return te;
    }

    private Collection<VariableAssignment<?>> getVars(List<String> lines) {
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

    private String getName(List<String> lines) {
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
    /*
    @SuppressWarnings("static-access")
    public void parseCommandLine(String[] args) {
        Options options = new Options();
        Option csv = OptionBuilder.withArgName("input").hasArg().withDescription("trace file").create("input");

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

        options.addOption(csv);
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
        // create the parser
        CommandLineParser parser = new GnuParser();
        Configuration configuration = Configuration.getInstance();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("help") || !line.hasOption("input")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("Mint", options);
            }
            if (line.hasOption("input"))
                configuration.INPUT = line.getOptionValue("input");
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

        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ExperimentDriver", options);
        }

    }

    protected static void  infer(TraceSet traces)throws IOException {
        LOGGER.info("Parsing input file");
        Configuration configuration = Configuration.getInstance();
        InferenceBuilder ib = new InferenceBuilder(configuration);
        AbstractMerger<?, ?> inference = ib.getInference(traces);


        Machine output = inference.infer();
        if(configuration.VIS.equals(Configuration.Visualise.text))
            System.out.println(DotGraphWithLabels.summaryDotGraph(inference.getState()));
        else{
            Machine2JSONTransformer trans = new Machine2JSONTransformer();
            trans.buildMachine(output, new File(configuration.TMP_PREFIX+"machine.json"));
        }
        if(output instanceof WekaGuardMachineDecorator && configuration.DATA){
            WekaGuardMachineDecorator wgm = (WekaGuardMachineDecorator) output;
            System.out.println(wgm.modelStrings());
        }


    }

    public static void main(String[] args) throws IOException {
        GUITARLogsReader reader = new GUITARLogsReader();
        reader.parseCommandLine(args);
        reader.populateTraces(Configuration.getInstance().INPUT);
        TraceSet ts = reader.getTraces();
        infer(ts);

    }
*/

}
