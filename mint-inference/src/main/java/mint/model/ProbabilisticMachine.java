package mint.model;

import mint.model.dfa.TransitionData;
import org.jgrapht.graph.DefaultEdge;

/**
 * Created by neilwalkinshaw on 18/05/2016.
 */
public class ProbabilisticMachine extends SimpleMachine<Double> {

    @Override
    public double getProbability(DefaultEdge transition) {
        Integer source = getAutomaton().getTransitionSource(transition);
        double total = 0D;
        for(DefaultEdge outgoing : getAutomaton().getOutgoingTransitions(source)){
            total += getAutomaton().getTransitionData(outgoing).getPayLoad();
        }
        Double prob = getAutomaton().getTransitionData(transition).getPayLoad()/total;
        if(prob.isNaN())
            prob = 0D;
        return prob;
    }

    /* (non-Javadoc)
         * @see org.bitbucket.efsmtool.model.Machine#mergeTransitions(java.lang.Integer, org.jgrapht.graph.DefaultEdge, org.jgrapht.graph.DefaultEdge)
         */
    @Override
    public DefaultEdge mergeTransitions(Integer source, DefaultEdge a, DefaultEdge b) {
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
        TransitionData<Double> aData = automaton.getTransitionData(transitionA);
        TransitionData<Double> bData = automaton.getTransitionData(transitionB);

        if(!aData.getLabel().equals(bData.getLabel()))
            return false;
        else
            return true;
    }
}
