package mint.model.walk.probabilistic;

import mint.evaluation.kfolds.Experiment;
import mint.model.Machine;
import mint.model.dfa.TraceDFA;
import mint.model.walk.SimpleMachineAnalysis;
import mint.model.walk.WalkResult;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

public abstract class ParameterisableProbabilisticMachineAnalysis<AType,BType> extends SimpleMachineAnalysis<Machine> {

    private final static Logger LOGGER = Logger.getLogger(ParameterisableProbabilisticMachineAnalysis.class.getName());


    /**
     * For conditional probabilities, what should the prefix length be leading to a given state?
     */
    protected int prefixLimit;

    protected TraceSet traces;

    /**
     * mapping from elements A to sets of sequences B (in b).
     */
    protected Map<AType, Integer> aCount;
    protected Map<AType, Double> pA;

    protected Map<ABCombination,Integer> bGivenACount;
    protected Map<ABCombination,Double> pBGivenA;

    protected Map<BType,Integer> bCount;
    protected Map<BType,Double> pB;

    public ParameterisableProbabilisticMachineAnalysis(Machine m, int prefixLimit, TraceSet traces) {
        super(m);
        this.traces = traces;
        this.prefixLimit = prefixLimit;
        aCount = new HashMap<>();
        bCount = new HashMap<>();
        bGivenACount = new HashMap<>();
        pA = new HashMap<>();
        pB = new HashMap<>();
        pBGivenA = new HashMap<>();
        buildPrefixes(); //done
        calculateProbabilities();
    }



    public Map<String,Double> conditionalDistribution(List<TraceElement> trace){
        WalkResult walk = walk(trace);
        Integer state = walk.getTarget();
        List<String> alphabet = new ArrayList<>();
        alphabet.addAll(machine.getAutomaton().getAlphabet());
        List prefix = new ArrayList();
        prefix.addAll(trace.subList(trace.size()-Math.min(trace.size(),prefixLimit),trace.size()));
        Map<String,Double> conditionalDist = new HashMap<>();
        for(String el : alphabet){
            AType sep = createA(state,el);
            while(!validPrefix(state,prefix)){
                prefix.remove(0);
            }
            BType b = createB(prefix,state);
            conditionalDist.put(el,pAGivenB(sep,b));

        }
        normalise(conditionalDist);
        return conditionalDist;
    }

    private void normalise(Map<String, Double> conditionalDist) {
        double total = 0D;
        for(Double v : conditionalDist.values()){
            total +=v;
        }
        for(String element : conditionalDist.keySet()){
            conditionalDist.put(element,conditionalDist.get(element)/total);
        }
    }

    public Map<String,Double> probabilityDistribution(List<TraceElement> trace){
        WalkResult walk = walk(trace);
        Integer state = walk.getTarget();
        List<String> alphabet = new ArrayList<>();
        alphabet.addAll(machine.getAutomaton().getAlphabet());
        List prefix = new ArrayList();
        prefix.addAll(trace.subList(trace.size()-Math.min(trace.size(),prefixLimit),trace.size()));
        Map<String,Double> conditionalDist = new HashMap<>();
        Collection<DefaultEdge> outgoing = machine.getAutomaton().getOutgoingTransitions(state);
        for(DefaultEdge og : outgoing){
            String label = machine.getLabel(og);
            AType sep = createA(state,label);
            Double val = pA.get(sep);
            if(val !=null)
                conditionalDist.put(label,val);
        }
        Set<String> remaining = new HashSet<>();
        remaining.addAll(machine.getAutomaton().getAlphabet());
        remaining.removeAll(conditionalDist.keySet());
        for(String rem : remaining){
            conditionalDist.put(rem,0D);
        }
        return conditionalDist;
    }

    public boolean validPrefix(Integer state, List b){
        if(b.isEmpty())
            return true;
        BType prefix = createB(b,state);
        return pB.containsKey(prefix);
    }

    public double pAGivenB(AType a, BType prefix)  {
        ABCombination bGivenA = new ABCombination(a,prefix);
        if(!pA.containsKey(a))
            return 0;
        double pa = pA.get(a);
        if(!pB.containsKey(prefix))
            return pa;
        if(!pBGivenA.containsKey(bGivenA))
            return 0;
        double pbgivena = pBGivenA.get(bGivenA);
        double intersection = pa * pbgivena;
        double pb = pB.get(prefix);
        double result = intersection / pb;
        if(result>1){
            LOGGER.debug(">0 probability: P(B|A)="+pbgivena+", P(A)="+pa+", P(B)="+pb);
        }
        return result;
    }

    private void calculateProbabilities() {
        for(AType sep : aCount.keySet()){
            double count = (double)aCount.get(sep);
            pA.put(sep,count/totalA(sep));
        }
        for(ABCombination le : bGivenACount.keySet()){
            double count = (double) bGivenACount.get(le);
            double aC = (double)aCount.get(le.a);
            pBGivenA.put(le,count/aC);
        }
        for(BType le : bCount.keySet()){
            double count = (double) bCount.get(le);
            double total = totalB(le);
            pB.put(le,count/total);
        }
    }

    /**
     * What do we count as the denominator for computing the probability
     * of some occurrence A?
     * @return
     */
    protected  double totalA(AType a){
        double total = 0D;
        for(List<TraceElement> sequence : traces.getPos()){
            total+=sequence.size();
        }
        return total;
    }

    /**
     * What do we count as the denominator for computing the probability
     * of some occurrence B?
     *
     * @return
     */
    protected double totalB(BType b){
        double total = 0D;
        for(List<TraceElement> sequence : traces.getPos()){
            total+=sequence.size();
        }
        return total;
    }



    private void buildPrefixes() {
        for(List<TraceElement> trace : traces.getPos()){
            process(trace);
        }
        /*for(List<TraceElement> trace : traces.getNeg()){
            process(trace.subList(0,trace.size()-1));
        }*/
    }

    protected void process(List<TraceElement> trace) {
        WalkResult wr = walk(trace);
        List<DefaultEdge> walk = wr.getWalk();
        for(int i = 0; i<trace.size(); i++){
            TraceElement current = trace.get(i);
            DefaultEdge de = walk.get(i);
            Integer state = machine.getAutomaton().getTransitionSource(de);
            Integer aCount = 0;
            AType a = createA(state,current.getName());
            if(this.aCount.containsKey(a))
                aCount = this.aCount.get(a);

            this.aCount.put(a,aCount+1);
            int j = i-prefixLimit;
            if(j<0)
                continue;
            List<String> prefix = new ArrayList<>();
            for(TraceElement el :trace.subList(j, i) ){
                prefix.add(el.getName());
            }

            addPrefix(a, prefix, state);
        }
    }

    protected abstract AType createA(Integer state, String name);


    private void addPrefix(AType a, List prefix, Integer state) {

        BType bPrefix = createB(prefix,state);
        Integer bCount = 0;
        if(this.bCount.containsKey(bPrefix))
            bCount = this.bCount.get(bPrefix);
        this.bCount.put(bPrefix,bCount+1);

        ABCombination le = new ABCombination(a,bPrefix);
        Integer bga = 0;
        if(bGivenACount.containsKey(le)){
            bga = bGivenACount.get(le);
        }
        bGivenACount.put(le,bga+1);

    }

    protected abstract BType createB(List prefix, Integer state);


    public double walkProbability(List<TraceElement> in){
        double result = 1D;
        WalkResult walk = walk(in);
        if(walk.getWalk().size()<in.size() || walk.isAccept().equals(TraceDFA.Accept.REJECT))
            return 0D;
        for(DefaultEdge de : walk.getWalk()){
            result = result * machine.getProbability(de);
        }
        return result;
    }


    public double walkConditionalProbability(List<TraceElement> in){
        double result = 1D;
        WalkResult walk = walk(in);
        if(walk.getWalk().size()<in.size() || walk.isAccept().equals(TraceDFA.Accept.REJECT))
            return 0D;
        DefaultEdge de = null;
        AType a = null;
        BType b = null;
        for(int i = 0; i<walk.getWalk().size(); i++){
            de = walk.getWalk().get(i);
            Integer state = machine.getAutomaton().getTransitionSource(de);
            String current = machine.getLabel(de);
            a = createA(state,current);
            int j = Math.min(i,prefixLimit);
            if(j>0) {
                List<String> prefix = new ArrayList<>();
                for (DefaultEdge el : walk.getWalk().subList(i - j, i)) {
                    prefix.add(machine.getLabel(el));
                }
                b = createB(prefix, state);
                double aGivenB = pAGivenB(a, b);
                result = result * aGivenB;

            }
            else{
                if(pA.containsKey(a))
                    result = result * pA.get(a);
                else
                    result = 0;
            }
        }
        return result;
    }

    private class ABCombination{
        AType a;
        BType b;

        public ABCombination(AType a, BType b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ParameterisableProbabilisticMachineAnalysis.ABCombination)) return false;
            ABCombination that = (ABCombination) o;
            return a.equals(that.a) && b.equals(that.b);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b);
        }
    }

}
