package mint.model.soa;


import mint.model.Machine;
import mint.model.MachineDecorator;
import mint.model.PayloadMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.walk.MachineAnalysis;
import mint.model.walk.SimpleMachineAnalysis;
import mint.model.walk.WalkResult;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

public class ProbabilisticMachineDecorator extends MachineDecorator {

    protected Map<DefaultEdge,SubjectiveOpinion> soaMap;
    protected TraceSet traces;
    protected MachineAnalysis ma;
    protected double confidenceThreshold = Double.MAX_VALUE;

    private final static Logger LOGGER = Logger.getLogger(ProbabilisticMachineDecorator.class.getName());


    public ProbabilisticMachineDecorator(Machine decorated, TraceSet traces, double confidenceThreshold) {
        super(decorated);
        soaMap=new HashMap<>();
        ma = new SimpleMachineAnalysis(decorated);
        this.traces=traces;
        this.confidenceThreshold=confidenceThreshold;
    }

    public WalkResult walk(List<TraceElement> elements){
        return ma.walk(elements,getInitialState(),new ArrayList<>(),getAutomaton());
    }

    @Override
    public void postProcess() {
        component.postProcess();
        /*Integer sinkState = component.getAutomaton().addState();
        component.getAutomaton().setAccept(sinkState, TraceDFA.Accept.REJECT);
        for(String element : component.getAutomaton().getAlphabet()){
            DefaultEdge added = component.getAutomaton().addTransition(sinkState,sinkState,new TransitionData<>(element,new HashSet<>()));
            SubjectiveOpinion soa = new SubjectiveOpinion(0,0,1,0.5);
            soaMap.put(added,soa);
        }*/
        Iterator<DefaultEdge> edgeIt = component.getAutomaton().getTransitions().iterator();
        while(edgeIt.hasNext()){
            DefaultEdge current = edgeIt.next();
            double initialBelief = calculateBelief(current);
            double traceCount = 0;
            for(List<TraceElement> trace: traces.getPos()){
                WalkResult result= ma.walk(trace,getInitialState(),new ArrayList<>(),getAutomaton());
                if(result.getWalk().contains(current))
                    traceCount++;
            }
            for(List<TraceElement> trace: traces.getNeg()){
                WalkResult result= ma.walk(trace,getInitialState(),new ArrayList<>(),getAutomaton());
                if(result.getWalk().contains(current))
                    traceCount++;
            }
            double uncertainty = 0D;
            if(confidenceThreshold>0D) {
                uncertainty = traceCount / Math.min(((double) traces.getPos().size() + (double) traces.getNeg().size()), confidenceThreshold);
                uncertainty = Math.max(0,1-uncertainty);
            }
                else
                uncertainty = 0D;
            double remainingBeliefMass = 1-uncertainty;
            double belief = initialBelief *remainingBeliefMass;
            double disbelief = remainingBeliefMass-belief;
            SubjectiveOpinion soa = new SubjectiveOpinion(belief,disbelief,uncertainty);
            soaMap.put(current,soa);
        }
        /*
        for(Integer state : getAutomaton().getStates()){
            Set<String> alphabet = getAutomaton().getAlphabet();
            Set<String> outgoingAlphabet = new HashSet<>();
            for(DefaultEdge de : getAutomaton().getOutgoingTransitions(state)){
                outgoingAlphabet.add(getAutomaton().getTransitionData(de).getLabel());
            }
            alphabet.removeAll(outgoingAlphabet);
            for(String remaining : alphabet){
                DefaultEdge added = getAutomaton().addTransition(state,sinkState,new TransitionData<>(remaining,new HashSet<>()));
                soaMap.put(added, new SubjectiveOpinion(1,0,0,0.5));
            }
        }
        */
         

    }

    private double calculateBelief(DefaultEdge current) {
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

    public SubjectiveOpinion walkOpinion(WalkResult walk){
        SubjectiveOpinion so = null;
        if(walk.getWalk()==null){ //trace rejected by inferred machine
            so = new SubjectiveOpinion(0,0.5,0.5,0.5);
        }
        for(DefaultEdge de : walk.getWalk()){
            if(so==null) {
                so = soaMap.get(de).clone();
            }
            else{
                so.multiply(soaMap.get(de));
            }
        }
        return so;
    }

}
