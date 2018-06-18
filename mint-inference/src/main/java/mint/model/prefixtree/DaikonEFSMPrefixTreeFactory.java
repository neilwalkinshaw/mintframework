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
import mint.model.DaikonMachineDecorator;
import mint.model.Machine;
import mint.model.dfa.TraceDFA;
import mint.model.walk.SimpleMachineAnalysis;
import mint.tracedata.TraceSet;

/**
 * To create prefix trees with Daikon invariants.
 * 
 * @author neilwalkinshaw
 *
 */

public class DaikonEFSMPrefixTreeFactory extends PrefixTreeFactory<DaikonMachineDecorator> {
			
	public DaikonEFSMPrefixTreeFactory(Machine kernel) {
		super(kernel);
	}
	
	
	
	@Override
	public DaikonMachineDecorator createPrefixTree(TraceSet traces) {
		buildMachine(traces.getNeg(), false);
		buildMachine(traces.getPos(), true);
		machine.getAutomaton().setAccept(machine.getInitialState(), TraceDFA.Accept.ACCEPT);
		//assert(consistent(m,dataMap));
		return machine;
	}

	@Override
	protected SimpleMachineAnalysis<DaikonMachineDecorator> getAnalysis() {
		return new SimpleMachineAnalysis<DaikonMachineDecorator>(machine);
	}

	@Override
	protected DaikonMachineDecorator initMachine(Machine kernel) {
		Configuration configuration = Configuration.getInstance();
		return new DaikonMachineDecorator(kernel, configuration.MINDAIKON, true);

	}


	
		

	

	
		
	
	
	



}
