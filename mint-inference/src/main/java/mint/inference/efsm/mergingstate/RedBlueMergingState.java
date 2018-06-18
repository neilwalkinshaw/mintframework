package mint.inference.efsm.mergingstate;

import org.apache.log4j.Logger;
import mint.model.Machine;
import mint.model.dfa.TraceDFA;
import mint.model.statepair.OrderedStatePair;
import mint.model.statepair.StatePair;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class RedBlueMergingState <T extends Machine> extends SimpleMergingState<T>{

	protected Set<Integer> red, tempBlue, tempRed, toConsolidate;
	final static Logger LOGGER = Logger.getLogger(RedBlueMergingState.class.getName());
	
	public boolean allowed(StatePair sp){
		if(!super.allowed(sp))
			return false;
		else if(tempRed.contains(sp.getSecondState()) && tempRed.contains(sp.getFirstState()))
			return false;
		else return true;
		
	}

	public void addTempSuccessfulPair(StatePair sp){
		super.addTempSuccessfulPair(sp);
		if(tempRed.contains(sp.getSecondState())){
			assert(!tempRed.contains(sp.getFirstState()));
			tempRed.remove(sp.getSecondState());
			tempRed.add(sp.getFirstState());
		}
	}

    protected StatePair newStatePair(StatePair sp){
        return new OrderedStatePair(sp.getFirstState(),sp.getSecondState());
    }
	
	public RedBlueMergingState(T current) {
		super(current);
		this.red = new HashSet<Integer>();
		this.tempRed = new HashSet<Integer>();
		this.tempBlue = new HashSet<Integer>();
		this.toConsolidate = new HashSet<Integer>();
		Integer init = getCurrent().getInitialState();
		red.add(init);
		tempRed.addAll(red);
		calculateBlues();
	}
	
	/**
	 * Add blue state that should be consolidated into a red one.
	 * Required whilst computing eligible merge-pairs.
	 */
	public void addToConsolidate(Integer c){
		assert(tempBlue.contains(c));
		toConsolidate.add(c);
	}
	
	public void clearTemps(){
		super.clearTemps();
		tempBlue.clear();
		tempRed.clear();
		tempRed.addAll(red);
	}
	
	public void confirmTemps(){
		super.confirmTemps();
		consolidateAll(toConsolidate);
		for(Integer r : tempRed){
			assert(current.getStates().contains(r));
		}
		red.clear();
		red.addAll(tempRed);
		calculateBlues();
	}
	
	public void postProcess(){
		consolidateAll(toConsolidate);
		calculateBlues();
	}

	protected void calculateBlues() {
		tempBlue.clear();
		Iterator<Integer> redIt = red.iterator();
		TraceDFA automaton = getCurrent().getAutomaton();
		while(redIt.hasNext()){
			Integer r = redIt.next();
			assert(current.getStates().contains(r));
			Set<DefaultEdge> outgoingTransitions = automaton.getOutgoingTransitions(r);
			for(DefaultEdge t: outgoingTransitions) {
				if(!red.contains(automaton.getTransitionTarget(t)))
						tempBlue.add(automaton.getTransitionTarget(t));
			}
		}
	}
	
	
	/**
	 * Convert all blue states to be consolidated into red ones.
	 * A state gets added to tempRed that is not in the machine.
	 * @param consolidate
	 */
	protected void consolidateAll(Set<Integer> consolidate) {
		for (Integer state : consolidate) {
			if(!getCurrent().getAutomaton().containsState(state))
				continue;
			tempBlue.remove(state);
			tempRed.add(state);
			red.add(state);
		}
		toConsolidate.clear();
	}
	
	public Set<Integer> getReds(){
		return red;
	}
	
	public Set<Integer> getBlues(){
		return tempBlue;
	}
	
	public void setMerged(Integer state){
		assert(!red.contains(state));
		assert(!tempRed.contains(state));
		tempBlue.remove(state);
	}


}
