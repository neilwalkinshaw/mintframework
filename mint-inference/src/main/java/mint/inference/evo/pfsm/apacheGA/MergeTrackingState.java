package mint.inference.evo.pfsm.apacheGA;

import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.model.Machine;
import mint.model.statepair.StatePair;
import org.jgrapht.DirectedGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Collection;

/**
 * Created by neilwalkinshaw on 02/06/2016.
 */
public class MergeTrackingState<T extends Machine> extends SimpleMergingState<T> {

    DirectedGraph<Integer,DefaultEdge> mergeGraph;

    public MergeTrackingState(T current) {
        super(current);
        mergeGraph = new DirectedAcyclicGraph<Integer, DefaultEdge>(DefaultEdge.class);
    }



    public void addConfirmedSuccessfulPair(StatePair sp){
        super.addConfirmedSuccessfulPair(sp);
        addMerged(sp);

    }

    private void addMerged(StatePair sp) {
        mergeGraph.addVertex(sp.getFirstState());
       // assert(!mergeGraph.containsVertex(sp.getSecondState()));
        mergeGraph.addVertex(sp.getSecondState());
        mergeGraph.addEdge(sp.getSecondState(),sp.getFirstState());
    }

    /**
     * Return the state into which state has ultimately been merged.
     * @param state
     * @return
     */
    public Integer getMerged(Integer state){
        if(!mergeGraph.containsVertex(state))
            return state;
        Collection<DefaultEdge> outgoing = mergeGraph.outgoingEdgesOf(state);
        if(outgoing.isEmpty())
            return state;
        DefaultEdge first = outgoing.iterator().next();
        return getMerged(mergeGraph.getEdgeTarget(first));
    }


}
