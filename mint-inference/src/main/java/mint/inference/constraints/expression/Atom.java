/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mint.inference.constraints.expression;

import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * An Atom represents a single constraint on a variable. It consists of a 
 * VariableAssignment paired with the constraint relation (greater than, smaller than, etc.)
 * 
 * For Boolean variables the relation must only be EQ or NEQ.
 */

public class Atom extends Expression{
		
	public enum Rel {GT,LT,GEQ,LEQ,EQ,NEQ}
	
	private final VariableAssignment<?> va;
	
	private Rel r;
	
	
	public Atom(VariableAssignment<?> v, Rel r){
		if (v instanceof BooleanVariableAssignment) 
			assert((r == Rel.EQ || r == Rel.NEQ));
		this.va = v.copy();
		this.r = r;
	}
	
	public Atom(VariableAssignment<?> v, String r){
		this.va = v.copy();
		setRel(r);
		//if (v instanceof BooleanVariableAssignment)
		//	assert((this.r == Rel.EQ || this.r == Rel.NEQ));
	}
	
	public VariableAssignment<?> getVariableAssignment(){
		return va.copy();
	}


	private void setRel(String rel){
		if (rel.equals("<="))
			r = Rel.LEQ;
		else if(rel.equals(">="))
			r = Rel.GEQ;
		else if(rel.equals("="))
			r = Rel.EQ;
		else if(rel.equals(">"))
			r = Rel.GT;
		else
			r = Rel.LT;
	}

	

	public Rel getR() {
		return r;
	}
	
	public String toString(){
		return va.getName() + getString(r) + va.getValue();
	}

	private String getString(Rel r2) {
		if(r2 == Rel.EQ)
			return "==";
		else if (r2 == Rel.GEQ)
			return ">=";
		else if (r2 == Rel.GT)
			return ">";
		else if (r2 == Rel.LEQ)
			return "<=";
		else if(r2 == Rel.LT)
			return "<";
		else 
			return "!=";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((r == null) ? 0 : r.hashCode());
		result = prime * result + ((va == null) ? 0 : va.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Atom other = (Atom) obj;
		if (r != other.r)
			return false;
		if (va == null) {
			if (other.va != null)
				return false;
		} else if (!va.equals(other.va))
			return false;
		return true;
	}
	
	
	
}
