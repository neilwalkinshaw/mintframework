package mint.evaluation;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import mint.Configuration;
import mint.inference.BaseClassifierInference;
import mint.inference.efsm.AbstractMerger;
import mint.inference.efsm.EDSMDataMerger;
import mint.inference.efsm.mergingstate.RedBlueMergingState;
import mint.inference.efsm.scoring.RedBlueScorer;
import mint.inference.efsm.scoring.Scorer;
import mint.inference.efsm.scoring.scoreComputation.ComputeScore;
import mint.inference.evo.GPConfiguration;
import mint.model.GPFunctionMachineDecorator;
import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.WekaGuardMachineDecorator;
import mint.model.prefixtree.EFSMPrefixTreeFactory;
import mint.model.walk.ComputeTransitionWalk;
import mint.model.walk.EFSMAnalysis;
import mint.model.walk.WalkResult;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.readers.TraceReader;
import mint.tracedata.types.VariableAssignment;
import mint.tracedata.types.VariableAssignmentComparator;
import org.jgrapht.graph.DefaultEdge;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by neilwalkinshaw on 20/03/15.
 */
public class TransitionFunctionEvaluator {


    /**
     * Carry out k-folds evaluation.
     */

    private final static Logger LOGGER = Logger.getLogger(TransitionFunctionEvaluator.class.getName());


    private static int tracesDone = 0;
    private static List<String> commonVariables;

    public static void main(String[] args){
        BasicConfigurator.configure();
        Configuration configuration = Configuration.getInstance();
        Logger.getRootLogger().setLevel(Level.DEBUG);
        configuration.SEED = Integer.parseInt(args[2]);
        configuration.K = 1;
        try {
            LOGGER.info("Reading trace files");
            TraceSet posSet = TraceReader.readTraceFile(args[0], configuration.TOKENIZER);
            commonVariables = getCommonVariables(posSet.getPos());

            printVars(args[1]);
            configuration.PREFIX_CLOSED = true;
            LOGGER.info("Running experiment");
            int folds = 10;
            List<Set<List<TraceElement>>> f = computeFolds(folds,posSet);
            assert(f.size() == folds);
            for(int i = 0; i< folds; i++){

                TraceSet testing = new TraceSet(f.get(i));
                TraceSet training = new TraceSet();
                LOGGER.debug("Building training set for fold "+(i+1));
                for(int j = 0; j<folds;j++){
                    if(j==i)
                        continue;
                    training.getPos().addAll(f.get(j));
                }
                LOGGER.debug("Beginning training");
                final long startTime = System.currentTimeMillis();
                final long endTime;
                Machine model = null;
                try {
                    TraceSet ev = new TraceSet();
                    for (List<TraceElement> tes : testing.getPos()) {
                        ev.addPos(tes);
                    }
                    LOGGER.info("Inferring model.");
                    model = learnModel(training,ev);
                    if(model == null)
                        continue;

                    endTime = System.currentTimeMillis();
                    final long duration = endTime - startTime;
                    LOGGER.info("Comparing.");
                    compare(i,ev,model,args[1]);
                    LOGGER.debug("Done comparing");
                    //Score score = score(model,testing);
                    //score.setDuration(duration);
                    //scores.add(score);
                }
                catch(Exception e){
                    LOGGER.error(e.toString());
                    e.printStackTrace();
                    System.exit(0);
                }
            }
            LOGGER.debug("Completed folds");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static void printVars(String arg) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(arg), true)));
            String outString = "Trace, Label, ";
            for(String varName : commonVariables){
                outString+=varName+",";
            }
            for(String varName : commonVariables){
                outString+=varName+"(inferred),";
            }
            out.println(outString);
            out.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void compare(int nr, TraceSet eval, Machine inferred, String logFile) {

            WekaGuardMachineDecorator wlearned = (WekaGuardMachineDecorator) inferred;
            GPFunctionMachineDecorator learned = (GPFunctionMachineDecorator) wlearned.getWrapped();
            EFSMAnalysis<GPFunctionMachineDecorator> analysis = new EFSMAnalysis<GPFunctionMachineDecorator>(learned);


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
                            return;
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

    protected static Machine learnModel(TraceSet pos, TraceSet eval) throws InterruptedException {
        AbstractMerger<?,?> inference = getInference(pos, eval);
        Machine inferred = null;

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<? extends Machine> future = executor.submit(inference);
        executor.shutdown(); // This does not cancel the already-scheduled task.
        try { inferred = future.get(1500, TimeUnit.MINUTES); }
        catch (InterruptedException ie) {
            future.cancel(true);
            LOGGER.error("Inference interrupted.");
        }
        catch (ExecutionException ee) {
            ee.printStackTrace();
            future.cancel(true);

            LOGGER.error("Exception during inference.");
        }
        catch (TimeoutException te) {
            future.cancel(true);
            LOGGER.error("TIMEOUT");
        }
        LOGGER.info("Size: "+inferred.getAutomaton().getStates().size()+", "+inferred.getAutomaton().getTransitions().size());

        return inferred;

    }

    public static AbstractMerger<?, ?> getInference(TraceSet posSet, TraceSet evalSet) {
        AbstractMerger<?,?> inference = null;
        BaseClassifierInference bci = new BaseClassifierInference(posSet,evalSet, Configuration.Data.J48);

        EFSMPrefixTreeFactory tptg = new EFSMPrefixTreeFactory(new GPFunctionMachineDecorator(new PayloadMachine(),2, new GPConfiguration(600,0.9,0.1,8,6),25),bci.getClassifiers(),bci.getElementsToInstances());

        RedBlueMergingState<WekaGuardMachineDecorator> ms = new RedBlueMergingState<WekaGuardMachineDecorator>(tptg.createPrefixTree(posSet));
        Scorer<RedBlueMergingState<WekaGuardMachineDecorator>> scorer  = new RedBlueScorer<RedBlueMergingState<WekaGuardMachineDecorator>>(Configuration.getInstance().K, new ComputeScore());


        inference = new EDSMDataMerger<RedBlueMergingState<WekaGuardMachineDecorator>>(scorer,ms);



        return inference;
    }

    protected static List<Set<List<TraceElement>>> computeFolds(int folds, TraceSet traceset) {
        List<Set<List<TraceElement>>> folded = new ArrayList<Set<List<TraceElement>>>();
        for(int i = 0; i< folds; i++){
            Set<List<TraceElement>> traceSet = new HashSet<List<TraceElement>>();
            folded.add(i,traceSet);
        }
        int counter = 0;
        List<List<TraceElement>> posTraces = new ArrayList<List<TraceElement>>();
        posTraces.addAll(traceset.getPos());
        Configuration c = Configuration.getInstance();
        Collections.shuffle(posTraces, new Random(c.SEED));
        Iterator<List<TraceElement>> traceIt = posTraces.iterator();
        while(traceIt.hasNext()){
            if(counter==folds)
                counter = 0;
            Set<List<TraceElement>> traces = folded.get(counter++);
            traces.add(traceIt.next());
        }
        return folded;
    }


}
