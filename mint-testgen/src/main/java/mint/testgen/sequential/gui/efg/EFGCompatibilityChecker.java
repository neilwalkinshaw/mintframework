package mint.testgen.sequential.gui.efg;

import mint.Configuration;
import mint.inference.efsm.mergingstate.RedBlueMergingState;
import mint.inference.efsm.scoring.RedBlueScorer;
import mint.inference.efsm.scoring.scoreComputation.ComputeScore;
import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.statepair.OrderedStatePair;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by neilwalkinshaw on 12/09/2017.
 */
public class EFGCompatibilityChecker<T extends RedBlueMergingState<Machine>> extends RedBlueScorer<T>{

    protected DirectedPseudograph<String,DefaultEdge> efg;

    protected List<Integer> states;

    public EFGCompatibilityChecker( DirectedPseudograph<String,DefaultEdge> efg){
        super(Configuration.getInstance().K,new ComputeScore());
        this.efg = efg;

    }

    public int getScore(T rms,OrderedStatePair sp) {

        if(!compatible(rms,sp))
            return -1;
        else{
            return super.getScore(rms,sp);
        }
    }

    @Override
    public boolean compatible(T rms, OrderedStatePair pair){
        return compatible(pair.getFirstState(),pair.getSecondState(),(PayloadMachine)rms.getCurrent());
    }

    private boolean compatible(Integer a, Integer b, PayloadMachine inferred) {
        Collection<String> subsequentA = new HashSet<String>();
        Collection<String> subsequentB = new HashSet<String>();
        Collection<String> incomingEFG = new HashSet<String>();
        Collection<String> subsequentEFG = new HashSet<String>();
        Collection<DefaultEdge> outgoingA = inferred.getAutomaton().getOutgoingTransitions(a);
        Collection<DefaultEdge> outgoingB = inferred.getAutomaton().getOutgoingTransitions(b);
        Collection<DefaultEdge> incomingA = inferred.getAutomaton().getIncomingTransitions(a);
        Collection<DefaultEdge> incomingB = inferred.getAutomaton().getIncomingTransitions(b);
        for(DefaultEdge de : outgoingA){
            Set<TraceElement> payload = inferred.getAutomaton().getTransitionData(de).getPayLoad();
            for(TraceElement ta : payload){
                subsequentA.add(ta.getName());
            }
        }
        for(DefaultEdge de : outgoingB){
            Set<TraceElement> payload = inferred.getAutomaton().getTransitionData(de).getPayLoad();
            for(TraceElement ta : payload){
                subsequentB.add(ta.getName());
            }
        }


        for(DefaultEdge de : incomingA){
            Set<TraceElement> payload = inferred.getAutomaton().getTransitionData(de).getPayLoad();
            for(TraceElement ta : payload){
                incomingEFG.add(ta.getName());
            }
        }
        for(DefaultEdge de : incomingB){
            Set<TraceElement> payload = inferred.getAutomaton().getTransitionData(de).getPayLoad();
            for(TraceElement ta : payload){
                incomingEFG.add(ta.getName());
            }
        }

        for(String vertex : incomingEFG){
            if(efg.containsVertex(vertex)) {
                for (DefaultEdge de : efg.outgoingEdgesOf(vertex)) {
                    subsequentEFG.add(efg.getEdgeTarget(de));
                }
            }
            else
                System.out.println("EFG Does not contain "+vertex);
        }
        if(subsequentEFG.containsAll(subsequentA) && subsequentEFG.containsAll(subsequentB))
            return true;
        else
            return false;
    }


}
