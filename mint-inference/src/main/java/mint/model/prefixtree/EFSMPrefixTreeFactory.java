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
import mint.model.WekaGuardMachineDecorator;
import mint.model.dfa.TraceDFA;
import mint.model.walk.EFSMAnalysis;
import mint.model.walk.SimpleMachineAnalysis;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import weka.classifiers.Classifier;
import weka.core.Instance;

import java.util.HashMap;
import java.util.Map;

public class EFSMPrefixTreeFactory extends PrefixTreeFactory<WekaGuardMachineDecorator> {

	/**
	 * Generates the EFSM prefix tree, where the EFSM is represented by the WekaGuardMachineDecorator.
	 * 
	 * 
	 * @param kernel
	 * @param modelMap
	 * @param elementsToInstances
	 */
			
	public EFSMPrefixTreeFactory(Machine kernel, Map<String,Classifier> modelMap, HashMap<TraceElement,Instance> elementsToInstances) {
		super(kernel);
		machine.setElementsToInstances(elementsToInstances);
		machine.setModelMap(modelMap);
	}
	
	
	@Override
	public WekaGuardMachineDecorator createPrefixTree(TraceSet traces) {
		buildMachine(traces.getNeg(), false);
		buildMachine(traces.getPos(), true);
		//assert(consistent(m,dataMap));
		machine.getAutomaton().setAccept(machine.getInitialState(), TraceDFA.Accept.ACCEPT);
		return machine;
	}

	@Override
	protected SimpleMachineAnalysis<WekaGuardMachineDecorator> getAnalysis() {
		return new EFSMAnalysis(machine);
	}

	@Override
	protected WekaGuardMachineDecorator initMachine(Machine kernel) {
		Configuration configuration = Configuration.getInstance();
		return new WekaGuardMachineDecorator(kernel,configuration.DATA);
	}


	
		

	

	
		
	
	
	



}
