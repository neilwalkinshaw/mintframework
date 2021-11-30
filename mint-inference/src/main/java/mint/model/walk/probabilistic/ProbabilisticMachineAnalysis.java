package mint.model.walk.probabilistic;

import mint.model.Machine;
import mint.model.walk.SimpleMachineAnalysis;
import mint.model.walk.WalkResult;
import mint.tracedata.TraceElement;


import mint.tracedata.TraceSet;
import org.jgrapht.graph.DefaultEdge;


import java.util.*;

public class ProbabilisticMachineAnalysis extends SimpleMachineAnalysis<Machine> {

    /**
     * For conditional probabilities, what should the prefix length be leading to a given state?
     */
    protected int prefixLimit = 0;

    protected TraceSet traces;

    /**
     * mapping from elements A to sets of sequences B (in b).
     */
    protected Map<StateEventPair, Integer> aCount;
    protected Map<StateEventPair, Double> pA;

    protected Map<Integer, Double> stateOccurrances;

    protected Map<ListAndElement<StateEventPair>,Integer> bGivenACount;
    protected Map<ListAndElement<StateEventPair>,Double> pBGivenA;

    protected Map<ListAndElement<Integer>,Integer> bCount;
    protected Map<ListAndElement<Integer>,Double> pB;

    public ProbabilisticMachineAnalysis(Machine m, int prefixLimit, TraceSet traces) {
        super(m);
        this.traces = traces;
        this.prefixLimit = prefixLimit;
        aCount = new HashMap<>();
        bCount = new HashMap<>();
        bGivenACount = new HashMap<>();
        pA = new HashMap<>();
        pB = new HashMap<>();
        pBGivenA = new HashMap<>();
        stateOccurrances = new HashMap<>();
        buildPrefixes();
        calculateProbabilities();
    }

    public double pAGivenB(StateEventPair a, List b){
        ListAndElement bGivenA = new ListAndElement(b,a);
        ListAndElement prefix = new ListAndElement(b,a.getState());
        double pa = pA.get(a);
        double pbgivena = pBGivenA.get(bGivenA);
        double intersection = pa * pbgivena;
        return intersection / pB.get(prefix);
    }

    private void calculateProbabilities() {
        for(StateEventPair sep : aCount.keySet()){
            double count = (double)aCount.get(sep);
            double totalState = stateOccurrances.get(sep.getState());
            pA.put(sep,count/totalState);
        }
        for(ListAndElement<StateEventPair> le : bGivenACount.keySet()){
            double count = (double) bGivenACount.get(le);
            double aC = (double)aCount.get(le.getElement());
            pBGivenA.put(le,count/aC);
        }
        for(ListAndElement<Integer> le : bCount.keySet()){
            double count = (double) bCount.get(le);
            //number of times prefix encountered anywhere?
            double totalB = total(le.getElement());
            pB.put(le,count/totalB);
        }
    }

    private double total(Integer element) {
        double total = 0D;
        for(StateEventPair le : aCount.keySet()){
            if(le.getState() == element)
                total+= aCount.get(le);
        }
        return total;
    }

    private void buildPrefixes() {
        for(List<TraceElement> trace : traces.getPos()){
            process(trace);
        }
        for(List<TraceElement> trace : traces.getNeg()){
            process(trace.subList(0,trace.size()-1));
        }
    }

    protected void process(List<TraceElement> trace) {
        WalkResult wr = walk(trace);
        List<DefaultEdge> walk = wr.getWalk();
        //assert(wr.isAccept(machine.getAutomaton())== TraceDFA.Accept.ACCEPT);
        for(int i = 0; i<trace.size(); i++){
            TraceElement current = trace.get(i);
            DefaultEdge de = walk.get(i);
            Integer state = machine.getAutomaton().getTransitionSource(de);
            incrementState(state);
            Integer aCount = 0;
            StateEventPair sep = new StateEventPair(state, current.getName());
            if(this.aCount.containsKey(sep))
                aCount = this.aCount.get(sep);

            this.aCount.put(sep,aCount+1);
            if(i==0){
                List<TraceElement> prefix = new ArrayList<>();
                addPrefix(current.getName(), prefix, state);
            }else {
                for (int j = i - Math.min(prefixLimit, i); j < i; j++) {
                    List<TraceElement> prefix = new ArrayList<>();
                    prefix.addAll(trace.subList(j, i));
                    addPrefix(current.getName(), prefix, state);
                }
            }

        }
    }

    private void incrementState(Integer transitionTarget) {
        Double stateCount = 0D;
        if(stateOccurrances.containsKey(transitionTarget))
            stateCount = stateOccurrances.get(transitionTarget);
        stateOccurrances.put(transitionTarget,stateCount+1);
    }

    private void addPrefix(String element, List prefix, Integer state) {
        StateEventPair sep = new StateEventPair(state, element);

        ListAndElement<Integer> bPrefix = new ListAndElement(prefix,state);
        Integer bCount = 0;
        if(this.bCount.containsKey(bPrefix))
            bCount = this.bCount.get(bPrefix);
        this.bCount.put(bPrefix,bCount+1);

        ListAndElement<StateEventPair> le = new ListAndElement(prefix, sep);
        Integer bga = 0;
        if(bGivenACount.containsKey(le)){
            bga = bGivenACount.get(le);
        }
        bGivenACount.put(le,bga+1);

    }


    public double walkProbability(List<TraceElement> in){
        double result = 1D;
        WalkResult walk = walk(in);
        for(DefaultEdge de : walk.getWalk()){
            result = result * machine.getProbability(de);
        }
        return result;
    }


}
