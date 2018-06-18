package mint.inference.evo.pfsm.matrix;

import org.apache.log4j.Logger;
import mint.evaluation.kfolds.NGram;
import mint.inference.gp.fitness.Fitness;
import mint.model.PayloadMachine;
import mint.model.ProbabilisticMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.walk.probabilistic.ProbabilisticMachineAnalysis;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neilwalkinshaw on 18/05/2016.
 */
public class PFSMFitness extends Fitness {

    protected MergingTable toEvaluate;
    protected NGram<String> ngrams;
    protected List<Double> targetDist;
    protected List<List<TraceElement>> training;

    private final static Logger LOGGER = Logger.getLogger(PFSMFitness.class.getName());


    public PFSMFitness(MergingTable toEvaluate, List<List<TraceElement>> t, NGram ngrams, List<Double> targetDist){
        this.targetDist = targetDist;
        this.ngrams = ngrams;
        this.toEvaluate = toEvaluate;
        this.training = new ArrayList<List<TraceElement>>();
        this.training.addAll(t);
        normalise(targetDist);
    }

    @Override
    public Double call() throws InterruptedException {
        ProbabilisticMachine sm = buildMachine();
        double fitness = (double) sm.getStates().size();


        if(sm.getStates().size() > 800) {
            return fitness;
        }
        ProbabilisticMachineAnalysis pma = new ProbabilisticMachineAnalysis(sm);
        List<Double> dist = pma.getDistribution(ngrams.getNgrams());
        normalise(dist);
        double divergence = KLDivergencee(targetDist, dist);
        return (divergence);
    }

    private ProbabilisticMachine buildMachine() {

        PayloadMachine payM = toEvaluate.getMergedMachine();

        ProbabilisticMachine pm = new ProbabilisticMachine();
        TraceDFA<Double> automaton = pm.getAutomaton();

        for(DefaultEdge payEdge : payM.getAutomaton().getTransitions()){
            Double payload = (double)payM.getAutomaton().getTransitionData(payEdge).getPayLoad().size();
            TransitionData dt = new TransitionData(payM.getAutomaton().getTransitionData(payEdge).getLabel(),payload);
            if(!automaton.containsState(payM.getAutomaton().getTransitionSource(payEdge)))
                automaton.addState(payM.getAutomaton().getTransitionSource(payEdge));
            if(!automaton.containsState(payM.getAutomaton().getTransitionTarget(payEdge)))
                automaton.addState(payM.getAutomaton().getTransitionTarget(payEdge));
            automaton.addTransition(payM.getAutomaton().getTransitionSource(payEdge),
                    payM.getAutomaton().getTransitionTarget(payEdge), dt);
        }
        return pm;
    }


    public static double KLDivergencee(List<Double> p, List<Double> q) {
        Double sum = 0D;
        for (int j = 0; j < p.size(); j++) {
            if (p.get(j) == 0) {
                if (q.get(j) == 0)
                    continue;
                p.set(j, 0.0000001);
            }
            if (q.get(j) == 0)
                q.set(j, 0.0000001);
            sum += p.get(j) * Math.log(p.get(j) / q.get(j));
        }
        return Math.abs(sum);
    }

    private void normalise(List<Double> machineCoords) {
        double total = 0D;
        for(int i = 0; i< machineCoords.size(); i++){
            total+=machineCoords.get(i);
        }
        if(total>0D) {
            for (int i = 0; i < machineCoords.size(); i++) {
                machineCoords.set(i, machineCoords.get(i) / total);
            }
        }
    }



}
