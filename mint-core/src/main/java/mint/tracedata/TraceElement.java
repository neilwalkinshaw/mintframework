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

import java.util.Set;

public interface TraceElement {
	
	public String getName();

	public Set<VariableAssignment<?>> getData();
	
	public TraceElement getNext();
	
	public void setNext(TraceElement e);
	
	public int getID();
	
	public String typeString();
	
	public boolean isInput();
	
	public TraceElement copy();

}
