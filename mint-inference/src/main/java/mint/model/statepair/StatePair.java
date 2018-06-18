/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mint.model.statepair;

public class StatePair {
    
    protected Integer a,b;
    
    public StatePair(Integer a, Integer b){
            this.a = a;
            this.b = b;
    }

    public void setFirstState(Integer a){
        this.a = a;
    }

    public void setSecondState(Integer a){
        this.b = a;
    }
    
    public Integer getFirstState(){
        return a;
	}
	
	public Integer getSecondState(){
		return b;
	}


    
    @Override
    public String toString(){
            return a +", "+b;
    }

    @Override
    public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + a;
            result = prime * result + b;
            return result;
    }

    @Override
    public boolean equals(Object obj) {
            boolean equals = false;
            if(obj instanceof StatePair){
                    StatePair other = (StatePair)obj;
                    if(other.getFirstState().intValue() == getFirstState().intValue()){
                            if(other.getSecondState().intValue() == getSecondState().intValue())
                                    equals = true;
                    }
                    else if(other.getFirstState().intValue() == getSecondState().intValue()){
                    	 if(other.getSecondState().intValue() == getFirstState().intValue())
                             equals = true;
                    }
                   
                            
            }
            return equals;
    }
}
