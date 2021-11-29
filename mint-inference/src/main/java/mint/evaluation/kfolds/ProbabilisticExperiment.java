package mint.evaluation.kfolds;

import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.apache.log4j.Logger;
import mint.Configuration;
import mint.model.Machine;
import mint.model.RawProbabilisticMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.walk.probabilistic.RawProbabilisticMachineAnalysis;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

/**
 * Created by neilwalkinshaw on 07/05/2016.
 */
public class ProbabilisticExperiment extends Experiment {

    private final static Logger LOGGER = Logger.getLogger(ProbabilisticExperiment.class.getName());


    public ProbabilisticExperiment(String name, Random r, Collection<List<TraceElement>> trace,Collection<List<TraceElement>> negtrace, int folds, Configuration.Data algo, int seed, int tail, boolean data, Configuration.Strategy strategy) {
        super(name, r, trace, negtrace, folds, algo, seed, tail, data, strategy);
    }



    protected void setConfiguration() {
        Configuration config = Configuration.getInstance();
        config.ALGORITHM = algo;
        config.SEED = seed;
        config.K = tail;
        config.DATA = data;
        config.STRATEGY = strategy;

    }

    @Override
    public List<Result> call() {
        LOGGER.info("Running experiment for:"+name+","+algo.toString()+","+seed+","+data+","+strategy);
        setConfiguration();
        List<TraceSet> f = computeFolds(folds);
        List<Double> scores = new ArrayList<Double>();
        List<Double> states = new ArrayList<Double>();
        List<Double> transitions = new ArrayList<Double>();
        for(int i = 0; i< folds; i++){
            TraceSet testing = f.get(i);
            TraceSet training = new TraceSet();
            for(int j = 0; j<folds;j++){
                if(j==i)
                    continue;
                training.getPos().addAll(f.get(j).getPos());
                training.getNeg().addAll(f.get(j).getNeg());
            }


            Machine model = null;
            try {
                TraceSet ev = new TraceSet();
                for (List<TraceElement> tes : testing.getPos()) {
                    ev.addPos(tes);
                }
                eval = ev;
                model = learnModel(training);
                if(model == null)
                    continue;
                states.add((double) model.getStates().size());
                transitions.add((double)model.getAutomaton().transitionCount());
                Double score = score(model,testing);
                scores.add(score);
            }
            catch(Exception e){
                LOGGER.error(e.toString());
                e.printStackTrace();
                System.exit(0);
            }
        }
        double meanScore = calculateMean(scores);
        double meanStates = calculateMean(states);
        double meanTransitions = calculateMean(transitions);
        final SimpleResult res = new SimpleResult(name,algo.toString(),seed,tail,data,meanStates,meanTransitions,strategy,meanScore);
        results.add(res);
        LOGGER.info("Results for:"+name+","+algo.toString()+","+seed+","+data+"\n"+res);

        return results;
    }

    static private Double calculateMean(List<Double> from){
        Double sum = 0D;
        for (Double d : from) {
            sum+=d;
        }
        return sum/from.size();
    }

    protected Double score(Machine model, TraceSet pos) {
        model.getAutomaton().getAlphabet();
        NGram<String> ngramGenerator = new NGram<String>(model.getAutomaton().getAlphabet(),3);
        List<List<String>> ngrams = ngramGenerator.getNgrams();

        List<Double> machineCoords =  getMachineNGramDistribution(model, ngrams);
        List<Double> testCoords = getTestCoords(pos.getPos(),ngrams);

        normalise(machineCoords);
        normalise(testCoords);
        return KLDivergencee(machineCoords,testCoords);
    }

    public static double KLDivergencee(List<Double> p, List<Double> q) {
        double sum = 0D;
        for (int j = 0; j < p.size(); j++) {
            if (p.get(j) == 0)
                p.set(j,0.0000001);
            if (q.get(j) == 0)
                q.set(j, 0.0000001);
            sum += p.get(j) * Math.log(p.get(j) / q.get(j));
        }
        return Math.abs(sum);
    }

    public static double KSStatistic(List<Double> p, List<Double> q) {
        double[] pArray = new double[p.size()];
        double[] qArray = new double[q.size()];
        for(int i = 0; i< p.size(); i++){
            pArray[i] = p.get(i);
        }
        for(int i = 0; i< q.size(); i++){
            qArray[i] = q.get(i);
        }
        KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest();

        double stat = ksTest.kolmogorovSmirnovStatistic(pArray,qArray);
        return stat;
    }

    protected void normalise(List<Double> machineCoords) {
        double total = 0D;
        for(int i = 0; i< machineCoords.size(); i++){
            total+=machineCoords.get(i);
        }
        for(int i = 0; i< machineCoords.size(); i++){
            machineCoords.set(i,machineCoords.get(i)/total);
        }
    }

    private List<Double> getTestCoords(List<List<TraceElement>> pos, List<List<String>> ngrams) {
        List<Double> dist = new ArrayList<Double>();
        for(List<String> ngram : ngrams){
            Double ngramTot = 0D;
            for(List<TraceElement> trace : pos){
                for(int i = 0; i < trace.size(); i++){

                    for(int j = 0; j< ngram.size(); j++){
                        if(i+j >= trace.size())
                            break;
                        String traceEl = trace.get(i+j).getName();
                        if(!ngram.get(j).equals(traceEl))
                            break;
                    }
                    ngramTot++;
                }
            }
            dist.add(ngramTot);
        }
        return dist;
    }

    private List<Double> getMachineNGramDistribution(Machine model, List<List<String>> ngrams) {
        RawProbabilisticMachineAnalysis pma = new RawProbabilisticMachineAnalysis(buildProbabilisticMachine(model));
        List<Double> dist = pma.getNGramDistribution(ngrams);
        return dist;
    }

    protected RawProbabilisticMachine buildProbabilisticMachine(Machine<?> m) {
        RawProbabilisticMachine pm = new RawProbabilisticMachine();
        TraceDFA<?> automaton = m.getAutomaton();
        for(DefaultEdge de : automaton.getTransitions()){
            pm.getAutomaton().addState(automaton.getTransitionSource(de));
            pm.getAutomaton().addState(automaton.getTransitionTarget(de));
            pm.getAutomaton().addTransition(automaton.getTransitionSource(de),automaton.getTransitionTarget(de),
                    new TransitionData<Double>(automaton.getTransitionData(de).getLabel(),m.getProbability(de)));

        }
        pm.getAutomaton().setInitialState(automaton.getInitialState());
        return pm;
    }


}
