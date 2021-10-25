package mint.model.soa;

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

public abstract class SOAMachineDecorator extends MachineDecorator {

    protected TraceSet traces;

    protected double confidenceThreshold = Double.MAX_VALUE;

    protected MachineAnalysis ma;



    public SOAMachineDecorator(Machine decorated, TraceSet traces, double conf) {
        super(decorated);
        this.traces=traces;
        this.confidenceThreshold=conf;
        ma = new SimpleMachineAnalysis(decorated);
    }

    public WalkResult walk(List<TraceElement> elements){
        return ma.walk(elements,getInitialState(),new ArrayList<>(),getAutomaton());
    }

    public void addWeights(TraceSet traces, boolean oneWeightPerTrace){
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
