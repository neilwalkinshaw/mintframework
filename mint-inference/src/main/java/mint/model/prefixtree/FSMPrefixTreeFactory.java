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

import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.SimpleMachine;
import mint.model.dfa.TraceDFA;
import mint.model.walk.SimpleMachineAnalysis;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;

import java.util.Set;

/**
 * 
 * To generate conventional prefix trees without taking data into account.
 * 
 * @author neilwalkinshaw
 *
 */

public class FSMPrefixTreeFactory extends PrefixTreeFactory<SimpleMachine>{
	
	
	public FSMPrefixTreeFactory(Machine kernel){
		super(kernel);
	}
	
	@Override
	public SimpleMachine createPrefixTree(TraceSet traces) {
		buildMachine(traces.getNeg(), false);
		buildMachine(traces.getPos(), true);
		machine.getAutomaton().setAccept(machine.getInitialState(), TraceDFA.Accept.ACCEPT);
		return machine;
	}

	@Override
	protected SimpleMachineAnalysis<Machine<Set<TraceElement>>> getAnalysis() {
		return new SimpleMachineAnalysis<Machine<Set<TraceElement>>>(machine);
	}

	@Override
	protected SimpleMachine initMachine(Machine kernel) {
		return new PayloadMachine();
	}

}
