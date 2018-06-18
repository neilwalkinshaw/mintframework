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

import java.util.List;

/**
 * An input or output, with an associated list of values.
 * @author neilwalkinshaw
 *
 */
public class TestIO {

	protected static int IDcounter = 0;

	protected int id;
	protected String name; 
	protected List<VariableAssignment<?>> vals;
	protected boolean input, valid;

    //The valid attribute is intended primarily to refer to outputs, i.e. if an output yielded an Exception, then this can be recorded as such.
	
	public TestIO(String name, List<VariableAssignment<?>> vals) {
		super();
		this.id = IDcounter;
		IDcounter++;
		this.name = name;
		this.vals = vals;
		this.input = true;
        this.valid = true;
	}
	
	public TestIO(String name, List<VariableAssignment<?>> vals, boolean isInput) {
		super();
		this.name = name;
		this.id = IDcounter;
		IDcounter++;
		this.vals = vals;
		this.input = isInput;
        this.valid = true;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}

	public List<VariableAssignment<?>> getVals() {
		return vals;
	}

    public VariableAssignment<?> getValWithName(String valName){
        VariableAssignment<?> ret = null;
        for(VariableAssignment<?> val : vals){
            if(val.getName().equals(valName)){
                ret = val;
                break;
            }
        }
        return ret;
    }
	
	public String toString(){
		String ret =  name+"(";
		for (VariableAssignment<?> v : vals) {
			ret+=v.toString()+" ";
		}
		return ret+"),";
	}
	
	
	public boolean isInput(){
		return input;
	}

    public void setValid(boolean valid){
        this.valid = valid;
    }

    public boolean isValid(){
        return valid;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TestIO)) return false;

		TestIO testIO = (TestIO) o;

		if (id != testIO.id) return false;
		if (input != testIO.input) return false;
		if (valid != testIO.valid) return false;
		return name != null ? name.equals(testIO.name) : testIO.name == null;

	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (input ? 1 : 0);
		result = 31 * result + (valid ? 1 : 0);
		return result;
	}
}
