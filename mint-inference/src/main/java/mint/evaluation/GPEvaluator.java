package mint.evaluation;

import mint.Configuration;
import mint.app.Mint;
import mint.inference.InferenceBuilder;
import mint.inference.efsm.AbstractMerger;
import mint.model.GPFunctionMachineDecorator;
import mint.model.Machine;
import mint.model.WekaGuardMachineDecorator;
import mint.model.walk.ComputeTransitionWalk;
import mint.model.walk.EFSMAnalysis;
import mint.model.walk.WalkResult;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.readers.TraceReader;
import mint.tracedata.types.VariableAssignment;
import mint.tracedata.types.VariableAssignmentComparator;
import mint.visualise.dot.DotGraphWithLabels;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class GPEvaluator {

    private final static Logger LOGGER = Logger.getLogger(GPEvaluator.class.getName());
    private static List<String> commonVariables;

    public static void main(String[] args) throws IOException {

        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();

        Configuration config = Configuration.getInstance();

        config.ALGORITHM = Configuration.Data.J48;
        config.SEED = 0;
        config.K =0;
        config.DATA = true;
        config.STRATEGY = Configuration.Strategy.redblue;
        config.GP = true;
        config.PREFIX_CLOSED = true;

        processDirectory(args[0],config);
        //run(testSet, trainingSet, config);


    }

    public static void run(String testSet, String trainingSet, Configuration config, String label) throws IOException {
        for(int i = 0; i<30; i++) {
            Configuration.getInstance().SEED=i;
            InferenceBuilder ib = new InferenceBuilder(config);

            TraceSet training = TraceReader.readTraceFile(trainingSet, config.TOKENIZER);

            commonVariables = getCommonVariables(training.getPos());

            AbstractMerger inference = ib.getInference(training);
            long startTime = System.currentTimeMillis();

            Machine inferred = inference.infer();
            long finishTime=System.currentTimeMillis();

            TraceSet test = TraceReader.readTraceFile(testSet, config.TOKENIZER);

            compare(i, test, inferred, label+"_s"+i+".csv", finishTime-startTime);
        }
    }

    private static TraceSet shift(TraceSet test) {
        TraceSet shifted = new TraceSet();
        for(List<TraceElement> trace: test.getPos()){
            ArrayList<TraceElement> newTrace = new ArrayList<TraceElement>();
            SimpleTraceElement recent = null;
            for(int i = 0; i<trace.size(); i++){
                TraceElement element = trace.get(i);
                Set<VariableAssignment<?>> data = null;
                if(i>0){
                    data = trace.get(i-1).getData();
                }
                else
                    data = trace.get(i).getData();
                SimpleTraceElement newElement = new SimpleTraceElement(element.getName(),data);
                if(recent!=null){
                    recent.setNext(newElement);
                }
                recent = newElement;
                newTrace.add(newElement);
            }
            shifted.addPos(newTrace);
        }
        return shifted;
    }

    private static void processDirectory(String dir, Configuration config) throws IOException {
         File directory = new File(dir);
         File[] subs = directory.listFiles();
         for(int i = 0; i<subs.length; i++){
             File sub = subs[i];
             File training = findFileWithName(sub,"original-train");
             File testing = findFileWithName(sub,"original-test");
             run(testing.getAbsolutePath(),training.getAbsolutePath(),config,sub.getName());
         }

    }

    private static File findFileWithName(File sub, String target) {
        File[] subs = sub.listFiles();
        for(int i = 0; i<subs.length; i++){
            File current = subs[i];
            if(current.getName().contains(target))
                return current;
        }
        return null;
    }

    private static void compare(int nr, TraceSet eval, Machine inferred, String logFile, long time) {

        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
            out.println(time+" ms");
            out.println(inferred.getAutomaton().getStates().size() + " states");
            out.println(inferred.getAutomaton().getTransitions().size() + " transitions");
            out.println(DotGraphWithLabels.summaryDotGraph(inferred));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        WekaGuardMachineDecorator wlearned = (WekaGuardMachineDecorator) inferred;
        GPFunctionMachineDecorator learned = (GPFunctionMachineDecorator) wlearned.getWrapped();
        EFSMAnalysis<GPFunctionMachineDecorator> analysis = new EFSMAnalysis<GPFunctionMachineDecorator>(learned);

        int tracesDone= 0;
        for(List<TraceElement> e : eval.getPos()) {
            try {
                List<TraceElement> elements = new ArrayList<TraceElement>();
                elements.addAll(e);
                WalkResult wr = analysis.walk(e, learned.getInitialState(), new ArrayList<DefaultEdge>(), learned.getAutomaton());
                while(wr.getWalk() == null && elements.size()>1){
                    elements.remove(elements.size()-1);
                    wr = analysis.walk(elements, learned.getInitialState(), new ArrayList<DefaultEdge>(), learned.getAutomaton());
                }
                if (wr.getWalk() != null) {
                    ComputeTransitionWalk ctw = new ComputeTransitionWalk(learned, wr, commonVariables);

                    tracesDone++;
                    Collection<VariableAssignment<?>> predicted = ctw.compute(tracesDone, elements, logFile);
                    if (elements.size() < 2) {
                        continue;
                    }
                    Collection<VariableAssignment<?>> actual = elements.get(elements.size() - 1).getData();
                    LOGGER.info(nr + ": Predicted: " + varString(predicted) + ",Actual: " + varString(actual));

                } else {
                    Collection<VariableAssignment<?>> actual = elements.get(elements.size() - 1).getData();
                    LOGGER.info(nr + ": Predicted: NA" + ",Actual: " + varString(actual));
                }
            }
            catch(Exception ex){
                ex.printStackTrace();
                LOGGER.error(ex.toString());
            }
        }
    }

    protected static String varString(Collection<VariableAssignment<?>> vars){
        String retString = "";
        List<VariableAssignment<?>> assignmentList = new ArrayList<VariableAssignment<?>>();
        assignmentList.addAll(vars);
        Collections.sort(assignmentList,new VariableAssignmentComparator());
        for(VariableAssignment<?> var : assignmentList){
            retString+= var.toString()+" ";
        }
        return retString.trim();
    }

    private static List<String> getCommonVariables(List<List<TraceElement>> pos) {
        Set<String> commonVariables = new HashSet<String>();
        for(List<TraceElement> e : pos) {
            for(TraceElement el : e) {

                for (VariableAssignment var : el.getData()) {
                    commonVariables.add(var.getName());
                }
            }

        }
        List<String> vars = new ArrayList<String>();
        vars.addAll(commonVariables);
        Collections.sort(vars);
        return vars;
    }

}
