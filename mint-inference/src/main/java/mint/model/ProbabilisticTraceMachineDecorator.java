package mint.model;

import mint.model.Machine;
import mint.model.MachineDecorator;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.walk.MachineAnalysis;
import mint.model.walk.SimpleMachineAnalysis;
import mint.model.walk.WalkResult;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A weighted DFA, associated with a trace set. Intended to be used as a basis for computing probabilities (or
 * subjective opinions) attributed to states or transitions.
 */

public class ProbabilisticTraceMachineDecorator extends MachineDecorator {

    protected TraceSet traces;

    protected double confidenceThreshold = Double.MAX_VALUE;

    protected MachineAnalysis ma;
    protected boolean oneWeightPerTrace;


    public ProbabilisticTraceMachineDecorator(Machine decorated, TraceSet traces, double conf, boolean oneWeightPerTrace) {
        super(decorated);
        this.traces=traces;
        this.confidenceThreshold=conf;
        this.oneWeightPerTrace = oneWeightPerTrace;
        ma = new SimpleMachineAnalysis(decorated);
        addWeights();
    }

    public TraceSet getTraces(){
        return traces;
    }

    public WalkResult walk(List<TraceElement> elements){
        return ma.walk(elements,getInitialState(),new ArrayList<>(),getAutomaton());
    }

    /**
     * Add weights to all transitions for a given set of traces.
     */
    private void addWeights(){
        for(List<TraceElement> trace: traces.getPos()){
            WalkResult result= ma.walk(trace,getInitialState(),new ArrayList<>(),getAutomaton());
            HashSet<DefaultEdge> done = new HashSet<>();
            List<DefaultEdge> walkEdges = result.getWalk();
            for(int i = 0; i<walkEdges.size(); i++){
                DefaultEdge current= walkEdges.get(i);
                if(done.contains(current) & oneWeightPerTrace)
                    continue;
                else {
                    Set<TraceElement> pl = component.getAutomaton().getTransitionData(current).getPayLoad();
                    pl.add(trace.get(i));
                    done.add(current);
                }
            }
        }
        for(List<TraceElement> trace: traces.getNeg()){
            HashSet<DefaultEdge> done = new HashSet<>();
            WalkResult result= ma.walk(trace,getInitialState(),new ArrayList<>(),getAutomaton());
            List<DefaultEdge> walkEdges = result.getWalk();
            for(int i = 0; i<walkEdges.size(); i++){
                DefaultEdge current = walkEdges.get(i);
                if(done.contains(current) & oneWeightPerTrace)
                    continue;
                else {
                    Set<TraceElement> pl = component.getAutomaton().getTransitionData(current).getPayLoad();
                    pl.add(trace.get(i));
                    done.add(current);
                }
            }
        }
    }

    /**
     * Calculates the proportion of "weight" for the current transition, with respect to the
     * other outgoing transitions from this state. Could be interpreted as a probability; the
     * equivalent values for all transitions from the same state would sum to 1.
     *
     * @param current
     * @return
     */
    protected double calculateBelief(DefaultEdge current) {
        TraceDFA<Set<TraceElement>> automaton = getAutomaton();
        Integer source = automaton.getTransitionSource(current);
        double total = 0D;
        for(DefaultEdge outgoing : automaton.getOutgoingTransitions(source)){
            TransitionData<Set<TraceElement>> td =automaton.getTransitionData(outgoing);
            total = total + td.getPayLoad().size();
        }
        TransitionData<Set<TraceElement>> td =automaton.getTransitionData(current);
        double trans=td.getPayLoad().size();
        if(total == 0D)
            return 0D;
        else
            return trans / total;
    }

}
