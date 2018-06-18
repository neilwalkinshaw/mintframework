/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package mint.tracedata;

import mint.tracedata.types.VariableAssignment;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Extends TraceSet by, for each TraceElement in an added test, enforcing consistency
 * accross all other trace elements in the existing traces. This is accomplished by
 * ensuring that if a TraceElement contains a variable that does not exist in the other
 * TraceElement, it is added and set to null for the other TraceElement.
 * 
 * This should only be used in cases where there is actually a risk of inconsistent 
 * variables. For example, when recursive Java objects are being traced. It increases
 * the expense of reading-in a trace by orders of magnitude.
 * @author neilwalkinshaw
 *
 */
public class ConsistencyEnforcingTraceSet extends TraceSet {
	
	public void addPos(List<TraceElement> trace){
		for(TraceElement te: trace){
			process(te,pos);
			process(te,neg);
		}
		pos.add(trace);
	}
	
	public void addNeg(List<TraceElement> trace){
		for(TraceElement te: trace){
			process(te,pos);
			process(te,neg);
		}
		neg.add(trace);
	}

	private void process(TraceElement te, Collection<List<TraceElement>> traces) {
		for(List<TraceElement> li:traces){
			for(TraceElement element : li){
				if(!(element.getName().equals(te.getName())))
					continue;
				Set<VariableAssignment<?>> elementVars = element.getData();
				Set<VariableAssignment<?>> teVars = te.getData();
				if(elementVars.size() > teVars.size())
					growTo(te,element);
				else if(teVars.size() > elementVars.size())
					growTo(element,te);
				
			}
		}
		
	}

	/**
	 * Grow the variables in te to reflect the variables in element. Additional
	 * variables will be added as nulls.
	 * 
	 * @param te
	 * @param element
	 */
	protected void growTo(TraceElement te, TraceElement element) {
		Set<VariableAssignment<?>> teVars = te.getData();
		for(VariableAssignment<?> elVar : element.getData()){
			if(!contains(teVars,elVar)){
				VariableAssignment<?> copied = elVar.copy();
				copied.setNull(true);
				teVars.add(copied);
			}
		}
		
	}

	private boolean contains(Set<VariableAssignment<?>> teVars,
			VariableAssignment<?> elVar) {
		for(VariableAssignment<?> var : teVars){
			if(var.getName().equals(elVar.getName()))
				return true;
		}
		return false;
	}
	
	

}
