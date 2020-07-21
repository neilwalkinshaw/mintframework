package mint.inference.evo.pfsm.apacheGA;

import org.apache.commons.math3.genetics.AbstractListChromosome;
import org.apache.commons.math3.genetics.InvalidRepresentationException;
import org.apache.log4j.Logger;
import mint.evaluation.kfolds.NGram;
import mint.inference.efsm.EDSMMerger;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.ProbabilisticMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.prefixtree.FSMPrefixTreeFactory;
import mint.model.prefixtree.PrefixTreeFactory;
import mint.model.statepair.OrderedStatePair;
import mint.model.statepair.StatePair;
import mint.model.statepair.StatePairComparator;
import mint.model.walk.probabilistic.ProbabilisticMachineAnalysis;
import mint.tracedata.TraceSet;
import org.jgrapht.graph.DefaultEdge;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by neilwalkinshaw on 19/05/2016.
 */
public class AGPMergingTable extends AbstractListChromosome<StatePair> implements Callable<Double> {

    private final static Logger LOGGER = Logger.getLogger(AGPMergingTable.class.getName());


    protected int id;


    protected String latest;

    protected TraceSet base;

    protected NGram nGram;
    protected List<Double> targetDist;
    protected double lastFitness = -1000000;
    protected boolean done;



    public AGPMergingTable(TraceSet prefixTree, NGram ngrams, List<Double> targetDist, List<StatePair> representation) throws InvalidRepresentationException {
        super(representation);
        done = false;
        base = prefixTree;
        this.nGram = ngrams;
        this.targetDist= targetDist;
    }

    public List<StatePair> getList(){
        return getRepresentation();
    }


    protected PayloadMachine getTree(){
        PrefixTreeFactory<?> tptg;
        tptg = new FSMPrefixTreeFactory(new PayloadMachine());
        PayloadMachine tree = (PayloadMachine)tptg.createPrefixTree(base);
        return tree;
    }

    public void sortMerges(List<StatePair> merges){
        SimpleMergingState<PayloadMachine> sm = new SimpleMergingState<PayloadMachine>(getTree());
        Collections.sort(merges, new StatePairComparator(sm));
    }

    public PayloadMachine getMergedMachine(){
        MergeTrackingState<PayloadMachine> sm = new MergeTrackingState<PayloadMachine>(getTree());
        EDSMMerger<PayloadMachine,SimpleMergingState<PayloadMachine>> merger = new EDSMMerger<PayloadMachine, SimpleMergingState<PayloadMachine>>(null,sm);

        for(StatePair sp : getRepresentation()){
            TraceDFA cloned = getTree().getAutomaton().clone();
            if(Thread.currentThread().isInterrupted()){
                return null;
            }
            Machine current = sm.getCurrent();
            int firstState = sm.getMerged(sp.getFirstState());
            int secondState= sm.getMerged(sp.getSecondState());
            if(current.getStates().contains(firstState) && current.getStates().contains(secondState)){
                OrderedStatePair osp = new OrderedStatePair(firstState,secondState);
                if(current.getInitialState().equals(secondState)) {
                    osp = new OrderedStatePair(secondState, firstState);
                }


                boolean merged = merger.merge(osp);
                if(merged){
                    //assert(!sm.getCurrent().getStates().contains(osp.getSecondState()));
                    sm.addConfirmedSuccessfulPair(osp);
                    sm.setMerged(secondState);
                    sm.confirmTemps();
                }
                else{
                    sm.addConfirmedFailedPair(osp);
                    sm.getCurrent().setAutomaton(cloned);
                }
                sm.clearTemps();

            }


        }
        latest = sm.getCurrent().getStates().size()+" states "+getRepresentation().size()+" merges";
        return sm.getCurrent();
    }



    @Override
    public double fitness() {
        if(!done) {
            done=true;
            lastFitness = -1000000;
            ProbabilisticMachine sm = buildMachine();
            if(sm == null){
                return lastFitness;
            }
            double fitness = (double) sm.getStates().size();


            if (sm.getStates().size() > 1000) {
                fitness =  -fitness;
            }
            else {
                ProbabilisticMachineAnalysis pma = new ProbabilisticMachineAnalysis(sm);
                List<Double> dist = pma.getNGramDistribution(nGram.getNgrams());
                normalise(dist);
                double divergence = KLDivergencee(targetDist, dist);
                fitness = -divergence;
            }
            lastFitness = fitness;
            return (lastFitness);
        }
        else
            return lastFitness;
    }

    private ProbabilisticMachine buildMachine() {

        PayloadMachine payM = getMergedMachine();
        if(Thread.currentThread().isInterrupted()){
            return null;
        }
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

    @Override
    protected void checkValidity(List<StatePair> list) throws InvalidRepresentationException {

    }

    @Override
    public AbstractListChromosome<StatePair> newFixedLengthChromosome(List<StatePair> list) {
        AGPMergingTable newTable = new AGPMergingTable(base,nGram,targetDist,list);
        return newTable;
    }

    @Override
    public Double call() throws Exception {
        return fitness();
    }

    public String toString() {
        return String.format("(f=%s %s)", new Object[]{lastFitness, this.getRepresentation()});
    }
}
