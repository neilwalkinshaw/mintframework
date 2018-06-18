package mint.model;

import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.model.dfa.TraceDFA;
import mint.model.statepair.StatePair;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;

import java.util.Collection;
import java.util.Set;

public interface Machine<T> {
	
	boolean isDeterministic();

	String getLabel(DefaultEdge de);

	void setAutomaton(TraceDFA<T> set);

	Collection<Integer> getStates();

	TraceDFA<T> getAutomaton();

	public double getProbability(DefaultEdge transition);

	Integer getInitialState();

	void merge(StatePair pair, SimpleMergingState<?> s);

	DefaultEdge mergeTransitions(Integer source, DefaultEdge a,
								 DefaultEdge b);

	/**
	 * Check that the labels of the two transitions are the same, otherwise treat them 
	 * as different.
	 */
	boolean compatible(DefaultEdge transitionA,
					   DefaultEdge transitionB);
	
	/**
	 * Check whether a trace-element is compatible with a given edge.
	 */
	boolean compatible(TraceElement td,
					   DefaultEdge transitionB);

	void postProcessMerge();
	
	void postProcess();

    /**
    * Reduces the set of possibleTransitions to those where the attached *data* values
    * predict the same outcome in the relevant classifier as the passed element.
    */
    Collection<DefaultEdge> findCompatible(
			Set<DefaultEdge> possibleTransitions, TraceElement element);

}