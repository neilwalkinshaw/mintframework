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


import java.util.List;

public abstract class Expression {
	
	boolean negated = false;
			
	protected static double sum(List<Double> list){
		double sum = 0;
		for (Double d : list) {
			sum+= d;
		
		}
		return sum;
	}
	
	public void setNegated(boolean negated){
		this.negated = negated;
	}
	
	public boolean isNegated(){
		return negated;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (negated ? 1231 : 1237);
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
		Expression other = (Expression) obj;
		if (negated != other.negated)
			return false;
		return true;
	}
	
	

}
