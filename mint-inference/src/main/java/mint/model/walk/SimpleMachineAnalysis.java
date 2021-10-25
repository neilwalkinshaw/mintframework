/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package mint.model.walk;

import mint.Configuration;
import mint.model.Machine;
import mint.model.dfa.TraceDFA;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

/*
 * Convenience methods for determining whether a sequence (given in the form of a sequence of TraceElement objects)
 * is possible in the machine or not.
 */

public class SimpleMachineAnalysis<T extends Machine> extends MachineAnalysis<T>{
	

	protected Set<DefaultEdge> transitionsCovered;

	public SimpleMachineAnalysis(T m) {
		this.machine = m;
		transitionsCovered = new HashSet<DefaultEdge>();
	}
	

	public static Set<DefaultEdge> step(Integer currentState, String label, TraceDFA<?> a) {
        assert(a.containsState(currentState));
		Set<DefaultEdge> outgoing = new HashSet<DefaultEdge>();
		for(DefaultEdge t: a.getOutgoingTransitions(currentState)){
			if(a.getTransitionData(t).getLabel().equals(label))
				outgoing.add(t);
		}
		return outgoing;
	}


	/*
	 * Which of the possibleTransitions are compatible with element?
	 * possibleTransitions is a set of edges that share the same label as element.
	 * In this case, there is no data to distinguish which edges are incompatible,
	 * so they are all treated as compatible.
	 */
	public Collection<DefaultEdge> getCompatible(Set<DefaultEdge> possibleTransitions, TraceElement element) {
        return machine.findCompatible(possibleTransitions, element);
	}
	
	public WalkResult getState(List<TraceElement> s, TraceDFA automaton){
		List<TraceElement> copy = new ArrayList<TraceElement>();
		copy.addAll(s);
		return walk(copy,machine.getInitialState(), new Stack<DefaultEdge>(),automaton);
	}
	
	/*
	 * Is the list s accepted in the automaton? resetCoverage determines whether the transitions
	 * covered by the processed transitions are incorporated.
	 */
	public boolean walk(List<TraceElement> s, boolean resetCoverage, TraceDFA automaton){
		//List<TraceElement> copy = new ArrayList<TraceElement>();
		//copy.addAll(s);
		if(resetCoverage)
			transitionsCovered = new HashSet<DefaultEdge>();
		WalkResult walk = walk(s);
		if(walk.getWalk()!=null)
			transitionsCovered.addAll(walk.getWalk());
		if(walk.getWalk().size()<s.size())
			return false;
		return walk.isAccept(automaton) == TraceDFA.Accept.ACCEPT;
	}

	/*
	 * Is the list s accepted in the automaton? resetCoverage determines whether the transitions
	 * covered by the processed transitions are incorporated.
	 */
	public TraceDFA.Accept walkAccept(List<TraceElement> s, boolean resetCoverage, TraceDFA automaton){
		//List<TraceElement> copy = new ArrayList<TraceElement>();
		//copy.addAll(s);
		if(resetCoverage)
			transitionsCovered = new HashSet<DefaultEdge>();
		WalkResult walk = walk(s);
		TraceDFA.Accept result = TraceDFA.Accept.UNDEFINED;
		if(walk.getWalk()!=null) {
			transitionsCovered.addAll(walk.getWalk());
			result = walk.isAccept(automaton);
		}
		return result;
	}



	protected WalkResult walk(List<TraceElement> in){
		return walk(in,machine.getInitialState(), new Stack<DefaultEdge>(), machine.getAutomaton());
	}
	
	public WalkResult walk(List<TraceElement> in,
						   Integer initialState,
						   List<DefaultEdge> soFar, TraceDFA automaton) {
        List<TraceElement> s = new ArrayList<TraceElement>();
        s.addAll(in);
		if(s.isEmpty()) {
            return new WalkResult(initialState, soFar,automaton.getAccept(initialState));
        }
		if(automaton.getAccept(initialState).equals(TraceDFA.Accept.REJECT) && Configuration.getInstance().PREFIX_CLOSED){
			return new WalkResult(initialState,soFar,TraceDFA.Accept.REJECT);
		}
		TraceElement current = s.get(0);
		Set<DefaultEdge> transitions = automaton.getOutgoingTransitions(initialState, current.getName());
		if(transitions.size()==0) {
            return new WalkResult(initialState, soFar, TraceDFA.Accept.UNDEFINED);
        }
		DefaultEdge next = chooseTransition(transitions,current,s.size()==1);
		if(next == null) {
            return new WalkResult(initialState, soFar, TraceDFA.Accept.UNDEFINED);
        }
		s.remove(0);
		Integer dest = automaton.getTransitionTarget(next);

		soFar.add(next);
		transitionsCovered.add(next);
		return walk(s,dest,soFar,automaton);
	}


	public double getProportionTransitionsCovered(TraceDFA automaton){
		double covered = (double)transitionsCovered.size();
		double machineSize = (double)automaton.transitionCount();
		return covered/machineSize;
	}
	
	public int getNumberTransitionsCovered(){
		return transitionsCovered.size();
	}

	protected DefaultEdge chooseTransition(Set<DefaultEdge> transitions,
			TraceElement current, boolean isLast) {
		return transitions.iterator().next();
	}

	

	public void walk(Set<List<TraceElement>> testSet, TraceDFA automaton){
		transitionsCovered = new HashSet<DefaultEdge>();
		for (List<TraceElement> test : testSet) {
			walk(test,false,automaton);
		}
	}
	
	public void resetCoverage(){
		transitionsCovered = new HashSet<DefaultEdge>();
	}
	

}
