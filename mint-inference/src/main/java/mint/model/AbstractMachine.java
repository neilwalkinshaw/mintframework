package mint.model;

import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.statepair.StatePair;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractMachine<T> implements Machine<T> {

	public int numNonDeterministic(Integer s){
		Set<DefaultEdge> dets = new HashSet<DefaultEdge>();
		for(DefaultEdge outgoingA: getAutomaton().getOutgoingTransitions(s)){
			for(DefaultEdge outgoingB: getAutomaton().getOutgoingTransitions(s)){
				if(outgoingA.equals(outgoingB))
					continue;
				TransitionData<T> a = getAutomaton().getTransitionData(outgoingA);
				TransitionData<T> b = getAutomaton().getTransitionData(outgoingB);
				if(!a.getLabel().equals(b.getLabel()))
					continue;
				if(compatible(outgoingA,outgoingB)){
					dets.add(outgoingA);
					dets.add(outgoingB);
				}
					
			}			
		}
		return dets.size();
	}
	
	
	public boolean isDeterministic(){
		boolean ret = true;
		for(Integer s : getStates()){
			int nondet = numNonDeterministic(s);
			if(nondet>0){
				ret = false;
			}
		}
		return ret;
	}
	
	

	@Override
	public abstract String getLabel(DefaultEdge de);

	@Override
	public abstract void setAutomaton(TraceDFA<T> set);

	@Override
	public abstract Collection<Integer> getStates();

	@Override
	public abstract TraceDFA<T> getAutomaton() ;

	@Override
	public abstract Integer getInitialState();

	@Override
	public abstract void merge(StatePair pair, SimpleMergingState<?> s) ;

	@Override
	public abstract DefaultEdge mergeTransitions(Integer source, DefaultEdge a,
												 DefaultEdge b);

	@Override
	public abstract boolean compatible(DefaultEdge transitionA, DefaultEdge transitionB) ;

	@Override
	public abstract boolean compatible(TraceElement td, DefaultEdge transitionB);

	@Override
	public abstract void postProcess();

}
