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

import mint.model.Machine;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;

import java.util.Set;


/*
 * Determines whether a sequence of TraceElement objects is possible, but extends MachineAnalysis
 * by taking data state into account (for transitions with data guards).
 * Relies on WEKA classifiers to determine next transition
 */

public class EFSMAnalysis<T extends Machine<Set<TraceElement>>> extends SimpleMachineAnalysis<T> {

	
	
	
	public EFSMAnalysis(T m) {
		super(m);
	}


    /**
     * This will go through the set of transitions, and will return
     * the first transition that is compatible (in terms of data).
     *
     * @param transitions
     * @param current
     * @return
     */
	protected DefaultEdge chooseTransition(
			Set<DefaultEdge> transitions, TraceElement current, boolean isLast) {
        if(isLast){
            return super.chooseTransition(transitions,current,isLast); //data doesn't matter now.
        }
		assert(!transitions.isEmpty());
        if(transitions.size() == 1)
            return transitions.iterator().next();
		DefaultEdge ret = null;
		for(DefaultEdge de: transitions){
			if(machine.compatible(current, de)){
				ret = de;		
				break;
			}
		}
		
		return ret;
		
		
	}


}
