package mint.evaluation.kfolds;

import org.apache.log4j.Logger;
import mint.Configuration;
import mint.model.Machine;
import mint.model.ProbabilisticMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.walk.probabilistic.ProbabilisticMachineAnalysis;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

/**
 * Created by neilwalkinshaw on 07/05/2016.
 */
public class ProbabilisticExperiment extends Experiment {

    private final static Logger LOGGER = Logger.getLogger(ProbabilisticExperiment.class.getName());


    public ProbabilisticExperiment(String name, Random r, Collection<List<TraceElement>> trace, int folds, Configuration.Data algo, int seed, int tail, boolean data, Configuration.Strategy strategy) {
        super(name, r, trace, null, folds, algo, seed, tail, data, strategy);
    }

    private void setConfiguration() {
        Configuration config = Configuration.getInstance();
        config.ALGORITHM = algo;
        config.SEED = seed;
        config.K = tail;
        config.DATA = data;
        config.STRATEGY = strategy;

    }

    @Override
    public List<Result> call() {
        LOGGER.info("Running experiment for:"+name+","+algo.toString()+","+seed+","+data);
        setConfiguration();
        List<Set<List<TraceElement>>> f = computeFolds(folds);
        List<Double> scores = new ArrayList<Double>();
        List<Double> states = new ArrayList<Double>();
        List<Double> transitions = new ArrayList<Double>();
        for(int i = 0; i< folds; i++){
            TraceSet testing = new TraceSet(f.get(i));
            TraceSet training = new TraceSet();
            for(int j = 0; j<folds;j++){
                if(j==i)
                    continue;
                training.getPos().addAll(f.get(j));
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
        final Object res = new SimpleResult(name,algo.toString(),seed,tail,data,meanStates,meanTransitions,strategy,meanScore);
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
        ProbabilisticMachineAnalysis pma = new ProbabilisticMachineAnalysis(buildProbabilisticMachine(model));
        List<Double> dist = pma.getNGramDistribution(ngrams);
        return dist;
    }

    protected ProbabilisticMachine buildProbabilisticMachine(Machine<?> m) {
        ProbabilisticMachine pm = new ProbabilisticMachine();
        TraceDFA<?> automaton = m.getAutomaton();
        for(DefaultEdge de : automaton.getTransitions()){
            pm.getAutomaton().addState(automaton.getTransitionSource(de));
            pm.getAutomaton().addState(automaton.getTransitionTarget(de));
            pm.getAutomaton().addTransition(automaton.getTransitionSource(de),automaton.getTransitionTarget(de),
                    new TransitionData<Double>(automaton.getTransitionData(de).getLabel(),m.getProbability(de)));

        }
        return pm;
    }


}
