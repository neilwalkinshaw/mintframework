/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013,2014 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mint.model.prefixtree;

import mint.Configuration;
import mint.model.Machine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.walk.SimpleMachineAnalysis;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.types.VariableAssignment;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

/**
 * A factory for the generation of Prefix Trees (which are instances of Machines) - before merging.
 * 
 * The constructor permits a kernel Machine, so that the Decorator pattern used to build Machine objects
 * is not suppressed.
 *  
 * @author neilwalkinshaw
 *
 * @param <T>
 */
public abstract class PrefixTreeFactory<T extends Machine> extends Observable {

	public abstract T createPrefixTree(TraceSet traces);
	
	protected T machine;
	
	protected PrefixTreeFactory(Machine kernel){
		machine = initMachine(kernel);
	}
	
	protected void setMachine(T m){
		machine = m;
	}
	
	protected void buildMachine(Collection<List<TraceElement>> traces, boolean accept) {
		
		Iterator<List<TraceElement>> traceIt = traces.iterator();
		while(traceIt.hasNext()){
			List<TraceElement> st = traceIt.next();
			addSequence(machine, machine.getInitialState(),st,accept);
		}
	}
	
	protected abstract T initMachine(Machine kernel);

	public  void addSequence(Machine<Set<TraceElement>> m, Integer currentState, List<TraceElement> seq, boolean accept){
		assert(m.getAutomaton().getStates().contains(currentState));
		if(seq.isEmpty())
			return;
		if(Configuration.getInstance().PREFIX_CLOSED){
			if(m.getAutomaton().getAccept(currentState).equals(TraceDFA.Accept.REJECT))
				return;
		}
		TraceElement current = seq.get(0);
		Set<DefaultEdge> outgoing = SimpleMachineAnalysis.step(currentState, current.getName(), m.getAutomaton());
		SimpleMachineAnalysis<?> modelAnalysis = getAnalysis();
        Collection<DefaultEdge> toNext = modelAnalysis.getCompatible(outgoing,current);
		if(toNext.isEmpty()){ //no compatible outgoing transitions to current path
			addNew(m,currentState,seq,accept);//add new from here
		}
		else{
			Iterator<DefaultEdge> transIt = toNext.iterator();
			while(transIt.hasNext()) { 		
				DefaultEdge next = transIt.next();
				
				Set<TraceElement> payload = m.getAutomaton().getTransitionData(next).getPayLoad();
				payload.add(current);
				Integer dest = m.getAutomaton().getTransitionTarget(next);
				assert(!dest.equals(currentState));
				if(seq.size()>1){
					addSequence(m,dest,seq.subList(1, seq.size()),accept);
				}
				else{
					if(accept){
						m.getAutomaton().setAccept(dest, TraceDFA.Accept.ACCEPT);
					}
					else{
						m.getAutomaton().setAccept(dest, TraceDFA.Accept.REJECT);
					}
				}

			}
		}

	}
	
	protected  void addNew(Machine m, Integer currentState, List<TraceElement> elements, boolean accept) {
		Configuration configuration = Configuration.getInstance();
		for(int i = 0; i< elements.size(); i++){
            setChanged();
            notifyObservers(new StateSequence(currentState,elements));
			TraceElement e = elements.get(i);
			Integer newState = m.getAutomaton().addState();
			if(i<elements.size()-1){
				if(configuration.PREFIX_CLOSED){
					m.getAutomaton().setAccept(newState, TraceDFA.Accept.ACCEPT);
				}
				else{
					m.getAutomaton().setAccept(newState, TraceDFA.Accept.UNDEFINED);
				}
			}
			else{
				if(accept){
					m.getAutomaton().setAccept(newState, TraceDFA.Accept.ACCEPT);
				}
				else{
					m.getAutomaton().setAccept(newState,TraceDFA.Accept.REJECT);
				}
			}
			Set<TraceElement> payload = new HashSet<TraceElement>();
			payload.add(e);
			TransitionData<Set<TraceElement>> t = new TransitionData<Set<TraceElement>>(e.getName(),payload);
			m.getAutomaton().addTransition(currentState, newState, t);
			currentState = newState;
		}
	}
	
	protected abstract SimpleMachineAnalysis<?> getAnalysis();

	public int numSequences(boolean accept){
		Collection<Integer> states = machine.getStates();
		int counter = 0;
		for(Integer state : states){
			if(machine.getAutomaton().getOutgoingTransitions(state).isEmpty()){
				if(machine.getAutomaton().getAccept(state) == TraceDFA.Accept.ACCEPT && accept)
					counter ++;
				else if(machine.getAutomaton().getAccept(state) == TraceDFA.Accept.REJECT && !accept)
					counter++;
			}
		}
		return counter;
	}

	public TraceSet getTraces(){
		TraceSet ts = new TraceSet();

		Collection<Integer> states = machine.getStates();

		for(Integer state : states){
			if(machine.getAutomaton().getOutgoingTransitions(state).isEmpty()){
				List<TraceElement> trace = traceBack(state);
				if(machine.getAutomaton().getAccept(state) == TraceDFA.Accept.ACCEPT){
					ts.addPos(trace);
				}
				else if(machine.getAutomaton().getAccept(state) == TraceDFA.Accept.REJECT){
					ts.addNeg(trace);
				}
			}
		}
		return ts;
	}

	private List<TraceElement> traceBack(Integer state) {
		ArrayList<TraceElement> trace = new ArrayList<>();
		while(state != machine.getInitialState()){
			List<DefaultEdge> incoming = new ArrayList<>();
			incoming.addAll(machine.getAutomaton().getIncomingTransitions(state));
			//should always be one and only one if it's a valid prefix tree...
			DefaultEdge inc = incoming.get(0);
			TransitionData payload = machine.getAutomaton().getTransitionData(inc);
			trace.add(0,new SimpleTraceElement(payload.getLabel(), new VariableAssignment[]{}));
			state = machine.getAutomaton().getTransitionSource(inc);
		}
		return trace;
	}


}
