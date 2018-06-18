/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package mint.model;

import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.model.dfa.TraceDFA;
import mint.model.statepair.StatePair;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;

import java.util.Collection;
import java.util.Set;

public class MachineDecorator extends AbstractMachine<Set<TraceElement>> implements Machine<Set<TraceElement>> {
	
	protected Machine<Set<TraceElement>> component;
	
	
	
	public MachineDecorator(Machine decorated){
		component = decorated;
	}


	@Override
	public String getLabel(DefaultEdge de) {
		return component.getLabel(de);
	}

	@Override
	public void setAutomaton(TraceDFA<Set<TraceElement>> set) {
		component.setAutomaton(set);

	}

	@Override
	public Collection<Integer> getStates() {
		return component.getStates();
	}

	@Override
	public TraceDFA<Set<TraceElement>> getAutomaton() {
		return component.getAutomaton();
	}

	@Override
	public double getProbability(DefaultEdge transition) {
		return component.getProbability(transition);
	}

	@Override
	public Integer getInitialState() {
		return component.getInitialState();
	}

	@Override
	public void merge(StatePair pair, SimpleMergingState<?> s) {
		component.merge(pair, s);

	}

	@Override
	public DefaultEdge mergeTransitions(Integer source, DefaultEdge a,
										DefaultEdge b) {
		return component.mergeTransitions(source, a, b);
	}

	@Override
	public boolean compatible(DefaultEdge transitionA, DefaultEdge transitionB) {
		return component.compatible(transitionA, transitionB);
	}

	@Override
	public void postProcess() {
		component.postProcess();

	}

    @Override
    public Collection<DefaultEdge> findCompatible(Set<DefaultEdge> possibleTransitions, TraceElement element) {
        return component.findCompatible(possibleTransitions,element);
    }

    @Override
	public void postProcessMerge() {
		component.postProcessMerge();

	}

	@Override
	public boolean compatible(TraceElement td,
			DefaultEdge transitionB) {
		return component.compatible(td, transitionB);
	}



}
