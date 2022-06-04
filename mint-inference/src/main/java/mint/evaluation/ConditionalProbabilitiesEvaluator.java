package mint.evaluation;

import mint.Configuration;
import mint.evaluation.kfolds.CPResult;
import mint.evaluation.kfolds.Experiment;
import mint.model.LatentProbabilitiesMachine;
import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.dfa.reader.DotReader;
import mint.model.prefixtree.FSMPrefixTreeFactory;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.types.VariableAssignment;
import org.apache.log4j.BasicConfigurator;
import org.jgrapht.graph.DefaultEdge;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.FileSystems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ConditionalProbabilitiesEvaluator {

    static String name, referenceMachine, numTraces, prefixLimit, folds, seed;
    static int noChangeLimit = 1000;

    public static void main(String[] args){
        name = args[0];
        referenceMachine = args[1];
        numTraces = args[2];
        prefixLimit = args[3];
        String initial = args[4];
        folds = args[5];
        seed = args[6];
        run(name,referenceMachine,numTraces,initial, prefixLimit, Integer.parseInt(folds), seed);
    }

    private static void run(String label, String referenceMachine, String numtraces, String initial, String prefixLimit, Integer folds, String seed){
        BasicConfigurator.configure();
        Configuration configuration = Configuration.getInstance();
        configuration.DATA = false;
        configuration.PREFIX_CLOSED = true;

        DotReader dr = new DotReader(FileSystems.getDefault().getPath(referenceMachine),initial);
        dr.setRemoveOutput(false);
        Machine dfa = dr.getImported();
        dfa.getAutomaton().completeWithRejects();
        LatentProbabilitiesMachine pdfa = createProbabilisticMachine(dfa);

        int targetSize = Integer.parseInt(numtraces);
        TraceSet traces = createTraces3(pdfa,targetSize,targetSize);
        Collections.shuffle(traces.getPos());
        Collections.shuffle(traces.getNeg());
        while(traces.getPos().size()>targetSize){
            traces.getPos().remove(0);
        }
        while(traces.getNeg().size()>targetSize){
            traces.getNeg().remove(0);
        }

        runExperiment(label, configuration, traces, pdfa, Integer.parseInt(prefixLimit), folds, Integer.parseInt(seed));
    }

    private static void runExperiment(String label, Configuration configuration, TraceSet traces, LatentProbabilitiesMachine pdfa, int prefixLimit, int folds, int seed) {
        List<List> results = new ArrayList<List>();
        Experiment probEx = new ConditionalProbabilitiesExperiment(label, traces, pdfa, prefixLimit, folds, seed, numTraces);
        results.add(probEx.call());
        for(int i = 0; i< results.size();i++){
            List<Object> outcomes = results.get(i);
            output(outcomes);
        }
    }

    static void output(List res) {
        FileWriter fWriter = null;
        BufferedWriter writer = null;
        String fileName = name + "_" + numTraces +"_" +prefixLimit+"_"+folds+"_"+seed;
        try {
            fWriter = new FileWriter(fileName+".csv",true);
            writer = new BufferedWriter(fWriter);

            for (Object result : res) {
                CPResult cpr = (CPResult) result;
                writer.append(cpr.toString());
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static LatentProbabilitiesMachine createProbabilisticMachine(Machine dfa) {
        LatentProbabilitiesMachine pdfa = new LatentProbabilitiesMachine();
        pdfa.setAutomaton(dfa.getAutomaton());
        pdfa.addRandomLatentDependencies();
        for(Integer state : pdfa.getAutomaton().getStates()){
            int numOutgoing = pdfa.getAutomaton().getOutgoingTransitions(state).size();
            double[] distribution = createDistribution(numOutgoing);
            int i = 0;
            for(DefaultEdge edge : pdfa.getAutomaton().getOutgoingTransitions(state)){
                pdfa.getAutomaton().getTransitionData(edge).setPayLoad(distribution[i]);
                i++;
            }
        }
        return pdfa;
    }

    public static TraceSet createTraces2(Machine pdfa, int posNum, int negNum) {


        FSMPrefixTreeFactory prefixTreeFactory = new FSMPrefixTreeFactory(new PayloadMachine());

        int targetDepth = pdfa.getAutomaton().getDepth()+5;
        int posTraces = 0;
        Random rand = new Random(Configuration.getInstance().SEED);
        int newPosTraces = 0;
        int attempts = 0;
        while(posTraces < posNum && attempts < noChangeLimit){
            List<DefaultEdge> walk = pdfa.getAutomaton().randomAcceptingWalk(targetDepth,rand);
            List<TraceElement> trace = new ArrayList<>();
            for (int i = 0; i < walk.size(); i++) {
                DefaultEdge de = walk.get(i);

                String label = pdfa.getAutomaton().getTransitionData(de).getLabel();
                trace.add(new SimpleTraceElement(label, new VariableAssignment[]{}));
            }
            prefixTreeFactory.addSequence(trace,true);
            newPosTraces = prefixTreeFactory.numSequences(true);
            if(newPosTraces - posTraces==0)
                attempts++;
            else {
                attempts = 0;
                posTraces = newPosTraces;
            }
        }


        int negTraces = 0;

        while(negTraces<negNum){

            List<DefaultEdge> walk = null;
            while(walk == null){
                walk = pdfa.getAutomaton().randomRejectingWalk(targetDepth,rand);
            }
            List<TraceElement> trace = new ArrayList<>();
            for (int i = 0; i < walk.size(); i++) {
                DefaultEdge de = walk.get(i);

                String label = pdfa.getAutomaton().getTransitionData(de).getLabel();
                trace.add(new SimpleTraceElement(label, new VariableAssignment[]{}));
            }
            prefixTreeFactory.addSequence(trace,false);
            negTraces = prefixTreeFactory.numSequences(false);
        }

        return prefixTreeFactory.getTraces();


    }

    public static TraceSet createTraces3(LatentProbabilitiesMachine pdfa, int posNum, int negNum) {


        FSMPrefixTreeFactory prefixTreeFactory = new FSMPrefixTreeFactory(new PayloadMachine());

        int targetDepth = pdfa.getAutomaton().getDepth()+5;
        int posTraces = 0;
        Random rand = new Random(Configuration.getInstance().SEED);
        while(posTraces < posNum ){
            List<DefaultEdge> walk = pdfa.randomWalk(targetDepth,rand, true);
            List<TraceElement> trace = new ArrayList<>();
            for (int i = 0; i < walk.size(); i++) {
                DefaultEdge de = walk.get(i);

                String label = pdfa.getAutomaton().getTransitionData(de).getLabel();
                trace.add(new SimpleTraceElement(label, new VariableAssignment[]{}));
            }
            prefixTreeFactory.addSequence(trace,true);
            posTraces++;
        }


        int negTraces = 0;

        while(negTraces<negNum){

            List<DefaultEdge> walk = null;
            while(walk == null){
                walk = pdfa.randomWalk(targetDepth,rand, false);
            }
            List<TraceElement> trace = new ArrayList<>();
            for (int i = 0; i < walk.size(); i++) {
                DefaultEdge de = walk.get(i);

                String label = pdfa.getAutomaton().getTransitionData(de).getLabel();
                trace.add(new SimpleTraceElement(label, new VariableAssignment[]{}));
            }
            prefixTreeFactory.addSequence(trace,false);
            negTraces++;
        }

        return prefixTreeFactory.getTraces();


    }


    private static double[] createDistribution(int numOutgoing) {
        double[] dist = new double[numOutgoing];
        Random rand = new Random(Configuration.getInstance().SEED);
        for(int i = 0; i<numOutgoing; i++){
            dist[i] = rand.nextDouble();
        }
        dist = normalise(dist);
        return dist;
    }

    private static double[] normalise(double[] dist) {
        double total = 0;
        for(int i = 0; i<dist.length; i++){
            total+=dist[i];
        }
        for(int i = 0; i<dist.length; i++){
            dist[i] = dist[i]/total;
        }
        return dist;
    }



}
