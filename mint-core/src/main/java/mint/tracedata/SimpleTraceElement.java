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

import java.util.*;

/*
 * Corresponds to a trace instance (e.g. execution of a method). The 
 * data attributes include the name of the method, some unique ID, the data configuration at this point,
 * and a pointer to the next trace instance.
 */

public class SimpleTraceElement implements TraceElement {
	
	protected final String name;
	protected final int id;
	protected Set<VariableAssignment<?>> data;
	protected TraceElement next;
	
	public static int idCounter = 0;
	
	public TraceElement getNext() {
		return next;
	}

	public void setNext(TraceElement next) {
		this.next = next;
	}

	public SimpleTraceElement(String name, VariableAssignment<?>[] data){
		if(name!=null)
			this.name = name;
		else
			this.name = "";
		this.data = new HashSet<VariableAssignment<?>>();
		for (VariableAssignment<?> variableAssignment : data) {
			this.data.add(variableAssignment);
		}
		this.id = idCounter++;
		this.next = null;
	}
	
	public SimpleTraceElement(String name, Collection<VariableAssignment<?>> data){
		if(name!=null)
			this.name = name;
		else
			this.name = "";
		this.data = new HashSet<VariableAssignment<?>>();
		for (VariableAssignment<?> variableAssignment : data) {
			this.data.add(variableAssignment);
		}		this.id = idCounter++;
		this.next = null;
	}
	
	public int getID(){
		return id;
	}
	
	
	public String toString(){
		String ret = name;
		for (VariableAssignment<?> v : data) {
			if(v!=null)
				ret = ret + " "+v.toString();
			else
				ret = ret + "null";
		}
		return ret;
	}
	
	public String typeString(){
		String ret = name;
		for (VariableAssignment<?> v : data) {
			if(v!=null)
				ret = ret + " "+v.typeString();
			else
				ret = ret + "null";
		}
		return ret;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Set<VariableAssignment<?>> getData() {
		return data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleTraceElement other = (SimpleTraceElement) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public boolean isInput() {
		return true;
	}

	@Override
	public TraceElement copy() {
		List<VariableAssignment<?>> copiedData = new ArrayList<VariableAssignment<?>>();
		for(VariableAssignment<?> var : data){
			copiedData.add(var.copy());
		}
		TraceElement copiedElement = new SimpleTraceElement(name,copiedData);
		copiedElement.setNext(next);
		return copiedElement;
	}

	

}
