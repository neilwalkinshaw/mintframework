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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A simple class to store positive and negative sets of traces.
 * @author neilwalkinshaw
 *
 */
public class TraceSet {
	
	protected List<List<TraceElement>> pos, neg;
	
	public TraceSet(){
		pos = new ArrayList<List<TraceElement>>();
		neg = new ArrayList<List<TraceElement>>();
	}
	
	public TraceSet(Collection<List<TraceElement>> pos2){
        this.pos = new ArrayList<List<TraceElement>>();
        pos.addAll(pos2);
		neg = new ArrayList<List<TraceElement>>();
	}
	
	public void addPos(List<TraceElement> trace){
		pos.add(trace);
	}
	
	public void addNeg(List<TraceElement> trace){
		neg.add(trace);
	}

	public List<List<TraceElement>> getPos() {
		return pos;
	}

	public List<List<TraceElement>> getNeg() {
		return neg;
	}
	
	public int indexOf(List<TraceElement> trace){
        int retIndex =  pos.indexOf(trace);
        if(retIndex<0)
            retIndex = neg.indexOf(trace);
        return retIndex;
    }

}
