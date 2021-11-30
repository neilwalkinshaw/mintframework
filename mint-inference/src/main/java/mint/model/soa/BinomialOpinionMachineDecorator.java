package mint.model.soa;


import citcom.subjectiveLogic.BinomialOpinion;
import citcom.subjectiveLogic.operators.binomial.BinomialMultiplication;
import mint.model.Machine;
import mint.model.ProbabilisticTraceMachineDecorator;
import mint.model.walk.WalkResult;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

public class BinomialOpinionMachineDecorator extends ProbabilisticTraceMachineDecorator {

    protected Map<DefaultEdge, BinomialOpinion> soaMap;

    /**
     * Count a path that is not in the machine as impossible, as opposed to simply unknown?
     */
    protected boolean strict = true;


    private final static Logger LOGGER = Logger.getLogger(BinomialOpinionMachineDecorator.class.getName());


    public void setStrict(boolean strict){
        this.strict = strict;
    }

    public BinomialOpinionMachineDecorator(Machine decorated, TraceSet traces, double confidenceThreshold, boolean oneWeightPerTrace) {
        super(decorated, traces, confidenceThreshold, oneWeightPerTrace);
        soaMap=new HashMap<>();

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
        double apriori = 1D/(double)component.getAutomaton().getTransitions().size();

        while(edgeIt.hasNext()){
            DefaultEdge current = edgeIt.next();
            double initialBelief = calculateBelief(current);
            double traceCount = 0;
            int processed = 0;
            for(List<TraceElement> trace: traces.getPos()){
                WalkResult result= ma.walk(trace,getInitialState(),new ArrayList<>(),getAutomaton());
                //if(result.getWalk().contains(current))
                 //   traceCount++;
                for(DefaultEdge de : result.getWalk()){
                    if(de.equals(current))
                        traceCount++;
                }
                processed += trace.size();
            }
            for(List<TraceElement> trace: traces.getNeg()){
                WalkResult result= ma.walk(trace,getInitialState(),new ArrayList<>(),getAutomaton());
                //if(result.getWalk().contains(current))
                //    traceCount++;
                for(DefaultEdge de : result.getWalk()){
                    if(de.equals(current))
                        traceCount++;
                }
                processed += trace.size();
            }

            double uncertainty = 0D;
            if(confidenceThreshold>0D) {
                uncertainty = traceCount / Math.min(((double) processed), confidenceThreshold);
                uncertainty = Math.max(0,1-uncertainty);
            }
                else
                uncertainty = 0D;
            double remainingBeliefMass = 1-uncertainty;
            double belief = initialBelief *remainingBeliefMass;
            double disbelief = remainingBeliefMass-belief;
            BinomialOpinion soa = new BinomialOpinion(belief,disbelief,uncertainty,apriori);
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



    public BinomialOpinion walkOpinion(WalkResult walk){
        BinomialOpinion so = null;
        if(walk.getWalk()==null){ //trace rejected by inferred machine
            so = getRejectionOpinion();
        }
        else if(walk.getWalk().size() == 0)
            so = new BinomialOpinion(0,0.5,0.5,0.5);
        for(DefaultEdge de : walk.getWalk()){
            if(so==null) {
                so = soaMap.get(de).clone();
            }
            else{
                BinomialMultiplication multi = new BinomialMultiplication();
                so = multi.apply(so,soaMap.get(de));
            }
        }
        return so;
    }

    private BinomialOpinion getRejectionOpinion() {
        if(strict)
            return new BinomialOpinion(0,1,0,0.5);
        else
            return new BinomialOpinion(0,0,1,0.5);
    }

}
