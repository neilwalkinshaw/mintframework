package mint.model;

import mint.model.dfa.TransitionData;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;

import java.util.Set;

/**
 * Created by neilwalkinshaw on 18/05/2016.
 */
public class PayloadMachine extends SimpleMachine<Set<TraceElement>> {

    @Override
    public double getProbability(DefaultEdge transition) {
        Integer source = getAutomaton().getTransitionSource(transition);
        double total = 0D;
        for(DefaultEdge outgoing : getAutomaton().getOutgoingTransitions(source)){
            total += getAutomaton().getTransitionData(outgoing).getPayLoad().size();
        }
        Double prob = getAutomaton().getTransitionData(transition).getPayLoad().size()/total;
        if(prob.isNaN())
            prob = 0D;
        return prob;
    }

    /* (non-Javadoc)
         * @see org.bitbucket.efsmtool.model.Machine#mergeTransitions(java.lang.Integer, org.jgrapht.graph.DefaultEdge, org.jgrapht.graph.DefaultEdge)
         */
    @Override
    public DefaultEdge mergeTransitions(Integer source, DefaultEdge a, DefaultEdge b) {
        Set<TraceElement> payload = automaton.getTransitionData(b).getPayLoad();
        payload.addAll(automaton.getTransitionData(a).getPayLoad());
        automaton.removeTransition(a);
        return b;
    }

    /*
	 * Check that the labels of the two transitions are the same, otherwise treat them
	 * as different.
	 */
	/* (non-Javadoc)
	 * @see org.bitbucket.efsmtool.model.Machine#compatible(org.jgrapht.graph.DefaultEdge, org.jgrapht.graph.DefaultEdge)
	 */
    @Override
    public boolean compatible(DefaultEdge transitionA, DefaultEdge transitionB) {
        TransitionData<Set<TraceElement>> aData = automaton.getTransitionData(transitionA);
        TransitionData<Set<TraceElement>> bData = automaton.getTransitionData(transitionB);

        if(!aData.getLabel().equals(bData.getLabel()))
            return false;
        else
            return true;
    }
}
