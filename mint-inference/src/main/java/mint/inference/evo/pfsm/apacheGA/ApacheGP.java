package mint.inference.evo.pfsm.apacheGA;

import org.apache.commons.math3.genetics.*;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.log4j.Logger;
import mint.evaluation.kfolds.NGram;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.inference.evo.GPConfiguration;
import mint.model.PayloadMachine;
import mint.model.prefixtree.FSMPrefixTreeFactory;
import mint.model.prefixtree.PrefixTreeFactory;
import mint.model.statepair.StatePair;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by neilwalkinshaw on 26/05/2016.
 */
public class ApacheGP {

    private final static Logger LOGGER = Logger.getLogger(ApacheGP.class.getName());


    protected GeneticAlgorithm ga;
    protected ThreadedElitisticListPopulation population;
    StoppingCondition stopCond;
    GPConfiguration config;
    PayloadMachine tree;

    public ApacheGP(GPConfiguration config, int generations, TraceSet traces, int chromSize){


        PrefixTreeFactory<?> tptg;
        tptg = new FSMPrefixTreeFactory(new PayloadMachine());
        tree = (PayloadMachine)tptg.createPrefixTree(traces);

        this.config = config;
        ga = new GA(
                new OnePointCrossover<StatePair>(),
                config.getCrossOver(),
                new AGPMutationPolicy(new SimpleMergingState(tree)),
                config.getMutation(),
                new ThreadedTournamentSelection(config.getTournamentSize())
        );

        population = getInitialPopulation(traces, chromSize);
        stopCond = new FixedGenerationCount(generations);

    }

    private Collection<String> getAlphabet(List<List<TraceElement>> training) {
        Collection<String> alphabet = new HashSet<String>();
        for(List<TraceElement> list : training){
            for(TraceElement el : list){
                alphabet.add(el.getName());
            }
        }
        return alphabet;
    }

    private List<Double> getTestCoords(List<List<TraceElement>> pos, List<List<String>> ngrams) {
        List<Double> dist = new ArrayList<Double>();
        for(List<String> ngram : ngrams){
            Double ngramTot = 0D;
            for(List<TraceElement> trace : pos){
                for(int i = 0; i < trace.size(); i++){
                    boolean matched = false;
                    for(int j = 0; j< ngram.size(); j++){
                        if(i+j >= trace.size()) {
                            matched = false;
                            break;
                        }
                        String traceEl = trace.get(i+j).getName();
                        if(!ngram.get(j).equals(traceEl)) {
                            matched = false;
                            break;
                        }
                        else
                            matched = true;
                    }
                    if(matched)
                        ngramTot++;
                }
            }
            dist.add(ngramTot);
        }
        return dist;
    }

    private ThreadedElitisticListPopulation getInitialPopulation(TraceSet traces, int total) {
        ThreadedElitisticListPopulation population = new ThreadedElitisticListPopulation(config.getPopulationSize(),0.05);
        Collection<String> alphabet = getAlphabet(traces.getPos());
        NGram ngrams = new NGram(alphabet,3);
        List<Double> targetDist = getTestCoords(traces.getPos(),ngrams.getNgrams());
        for(int j = 0; j<config.getPopulationSize(); j++){
            AGPMergingTable mt = new AGPMergingTable(traces,ngrams,targetDist,randomMerges(total));
            population.addChromosome(mt);
        }
        return population;
    }

    private List<StatePair> randomMerges(int merges) {
        List<StatePair> pairs = new ArrayList<StatePair>();
        RandomGenerator rg = GA.getRandomGenerator();
        List<Integer> states = new ArrayList<Integer>();
        states.addAll(tree.getStates());
        for(int j = 0; j<merges; j++) {
            /*List<Integer> pool = new ArrayList<Integer>();
            pool.addAll(tree.getStates());
            int jInd = rg.nextInt(pool.size());
            Integer jState = pool.get(jInd);
            Collection<Integer> reachables = tree.getAutomaton().nodesReachableFrom(jState);
            pool.removeAll(reachables);
            if(pool.isEmpty())
                continue;
            int iInd = rg.nextInt(pool.size());
            int iState = pool.get(iInd);*/
            Integer iState = states.get(rg.nextInt(states.size()));
            Integer jState = states.get(rg.nextInt(states.size()));
            pairs.add(new StatePair(iState,jState));
        }
        //LOGGER.debug("computed merges");
        return pairs;
    }


    public PayloadMachine evolve(){
        // run the algorithm
        Population finalPopulation = ga.evolve(population, stopCond);

        // best chromosome from the final population
        AGPMergingTable bestFinal = (AGPMergingTable)finalPopulation.getFittestChromosome();

        return bestFinal.getMergedMachine();
    }
}
