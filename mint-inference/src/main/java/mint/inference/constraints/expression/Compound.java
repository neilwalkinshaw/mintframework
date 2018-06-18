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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 * A Compound is a (tree-shaped) collection of sub-expressions. Each Compound consists
 * of a set of Atoms that are combined either via an AND or OR relationship.
 */
public class Compound extends Expression{
	
	public enum Rel {AND,OR}
	
	private List<Expression> exps;
	private Rel rel;
	
	public Compound(Rel r){
		exps = new ArrayList<Expression>();
		rel = r;
	}
	
	
	public Compound(List<Expression> e, Rel r){
		exps = e;
		rel = r;
	}
	
	public void add(Expression e){
		exps.add(e);
	}

	public List<Expression> getExps() {
		return exps;
	}

	public Rel getRel() {
		return rel;
	}
	
	public String toString(){
		String s = "";
		Iterator<Expression> expIt = exps.iterator();
		while(expIt.hasNext()){
			Expression e = expIt.next();
			s += "("+e.toString()+")";
			if(expIt.hasNext())
				s += getRelString();
		}
		return s;
	}

	private String getRelString() {
		if(rel == Rel.AND)
			return "&&";
		else
			return "||";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((exps == null) ? 0 : exps.hashCode());
		result = prime * result + ((rel == null) ? 0 : rel.hashCode());
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
		Compound other = (Compound) obj;
		if (exps == null) {
			if (other.exps != null)
				return false;
		} else if (!exps.equals(other.exps))
			return false;
		if (rel != other.rel)
			return false;
		return true;
	}
	
	

}
