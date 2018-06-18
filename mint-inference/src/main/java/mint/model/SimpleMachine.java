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
import java.util.Vector;

/*
 * Basic implementation of Machine, simple DFA without guards.
 */

public abstract class SimpleMachine<T> extends AbstractMachine<T> implements Machine<T> {
	
	protected TraceDFA<T> automaton;

	public SimpleMachine() {
		automaton = new TraceDFA<T>();
		
	}

	/* (non-Javadoc)
	 * @see org.bitbucket.efsmtool.model.Machine#getLabel(org.jgrapht.graph.DefaultEdge)
	 */
	@Override
	public String getLabel(DefaultEdge de){
		TransitionData<T> td = automaton.getTransitionData(de);
		return td.getLabel();
	}
	

	/* (non-Javadoc)
	 * @see org.bitbucket.efsmtool.model.Machine#setAutomaton(org.bitbucket.efsmtool.model.dfa.TraceDFA)
	 */
	@Override
	public void setAutomaton(TraceDFA<T> set) {
		automaton = set;
	}

	/* (non-Javadoc)
	 * @see org.bitbucket.efsmtool.model.Machine#getStates()
	 */
	@Override
	public Collection<Integer> getStates() {
		return automaton.getStates();
	}

	/* (non-Javadoc)
	 * @see org.bitbucket.efsmtool.model.Machine#getAutomaton()
	 */
	@Override
	public TraceDFA<T> getAutomaton() {
		return automaton;
	}

	/* (non-Javadoc)
	 * @see org.bitbucket.efsmtool.model.Machine#getInitialState()
	 */
	@Override
	public Integer getInitialState() {
		return automaton.getInitialState();
	}

    /**
     * Merges the pair of states by rerouting all outgoing transitions from the second
     * state so that they leave the first, and by rerouting all incoming states to the second
     * state so that they enter the first.
     * Could lead to non-deterministic states.
     * @param pair
     * @param s
     */
	@Override
	public void merge(StatePair pair, SimpleMergingState<?> s) {
	
		assert(automaton.containsState(pair.getSecondState()));
		assert(automaton.compatible(pair.getFirstState(),pair.getSecondState()));
		rerouteAllOutgoingTransitions(pair.getSecondState(),pair.getFirstState());
        rerouteAllIncomingTransitions(pair.getSecondState(),pair.getFirstState(),s);
		if(automaton.getAccept(pair.getFirstState())== TraceDFA.Accept.UNDEFINED)
			automaton.setAccept(pair.getFirstState(),automaton.getAccept(pair.getSecondState()));
        automaton.removeState(pair.getSecondState());
        assert(automaton.containsState(automaton.getInitialState()));

    }

	/**
	 * Reroute incoming transitions from one state to another.
	 * @param from
	 * @param to
	 * @return 
	 */
	protected void rerouteAllIncomingTransitions(Integer from, Integer to,
												 SimpleMergingState<?> s) {
				assert(!from.equals(to));
				Set<DefaultEdge> incoming = automaton.getIncomingTransitions(from);
				Vector<DefaultEdge> toRem = new Vector<DefaultEdge>();
				toRem.addAll(incoming);
                int incomingBeforeAdd = automaton.getIncomingTransitions(to).size(); //only used for assertion.
				for (int i = 0; i<toRem.size(); i++) {
					DefaultEdge transition = toRem.get(i);
					Integer source = automaton.getTransitionSource(transition);
					TransitionData<T> data = automaton.getTransitionData(transition);
					automaton.removeTransition(transition);
					automaton.addTransition(source, to, data);
				}
				assert(automaton.getIncomingTransitions(from).isEmpty());
                assert(automaton.getIncomingTransitions(to).size() == incomingBeforeAdd + toRem.size());
			}

	/**
	 * Reroute outgoing transitions from one state to another.
	 * @param from
	 * @param to
	 * @return 
	 */
	protected void rerouteAllOutgoingTransitions(Integer from, Integer to) {
		assert(!from.equals(to));
		Set<DefaultEdge> workList = new HashSet<DefaultEdge>();
		for (DefaultEdge t : automaton.getOutgoingTransitions(from)) {
			Integer target = automaton.getTransitionTarget(t);
			TransitionData<T> data = automaton.getTransitionData(t);
			workList.add(t);
			if(!from.equals(to))
				automaton.addTransition(to, target, data);
		}
		for (DefaultEdge defaultEdge : workList) {
			automaton.removeTransition(defaultEdge);
		}
		assert(automaton.getOutgoingTransitions(from).isEmpty());
	}

	
	/* (non-Javadoc)
	 * @see org.bitbucket.efsmtool.model.Machine#postProcess()
	 */
	@Override
	public void postProcess(){
		
	}

    @Override
    public Collection<DefaultEdge> findCompatible(Set<DefaultEdge> possibleTransitions, TraceElement element) {
        HashSet<DefaultEdge> comp = new HashSet<DefaultEdge>();
        for(DefaultEdge de : possibleTransitions){
            if(compatible(element,de))
                comp.add(de);
        }
        return comp;
    }

    /* (non-Javadoc)
     * @see org.bitbucket.efsmtool.model.Machine#postProcessMerge()
     */
	@Override
	public void postProcessMerge(){
		
	}

	@Override
	public boolean compatible(TraceElement td,
			DefaultEdge transitionB) {
		return td.getName().equals(automaton.getTransitionData(transitionB).getLabel());
	}
	

}
