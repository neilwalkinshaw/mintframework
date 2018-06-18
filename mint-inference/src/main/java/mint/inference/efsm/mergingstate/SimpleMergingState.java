/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mint.inference.efsm.mergingstate;

import mint.model.Machine;
import mint.model.statepair.OrderedStatePair;
import mint.model.statepair.StatePair;

import java.util.HashSet;
import java.util.Set;

public class SimpleMergingState <T extends Machine> implements MergingState{

	protected T current;
	protected Set<StatePair> failed, succeeded;
	protected Set<StatePair> tempSucceeded;
	protected MergeTracker mt = new MergeTracker();


    protected StatePair newStatePair(StatePair sp){
        return new StatePair(sp.getFirstState(),sp.getSecondState());
    }
	
	public void setMerged(Integer state){}

	/**
	 * Does the current state allow the state pair sp to be merged?
	 * @param sp
	 * @return
	 */
	public boolean allowed(StatePair sp){
        sp = newStatePair(sp);
		if(sp.getSecondState() == current.getInitialState())
			return false;
		if(failed.contains(sp))
			return false;
		else if(!getCurrent().getAutomaton().compatible(sp.getFirstState(),sp.getSecondState())){
			return false;
		}
		else
			return true;
	}
	
	/**
	 * Clear the temporary state, set only to confirmed
	 * state.
	 */
	public void clearTemps(){
		tempSucceeded.clear();
		mt.clear();
	}
	
	/**
	 * Add the confirmed failed pair sp.
	 */
	public void addConfirmedFailedPair(StatePair sp){
        sp = newStatePair(sp);
        failed.add(sp);
	}

	
	/**
	 * Add the hypothesised successful pair sp.
	 */
	public void addTempSuccessfulPair(StatePair sp){
        sp = newStatePair(sp);
		tempSucceeded.add(new StatePair(sp.getFirstState(),sp.getSecondState()));
	}
	
	/**
	 * Return the confirmed successful pairs.
	 * @return
	 */
	public int getConfirmedSuccessfulPairs(){
	return succeeded.size();
	}

	
	public boolean alreadyAttempted(StatePair sp){
        sp = newStatePair(sp);
        boolean alreadyAttempted = failed.contains(sp);
        if(alreadyAttempted)
            return true;
		else
            return succeeded.contains(sp);
	}

	public SimpleMergingState(T current) {
		this.current = current;
		failed = new HashSet<StatePair>();
		tempSucceeded = new HashSet<StatePair>();
        succeeded = new HashSet<StatePair>();
	}
	
	

	public T getCurrent() {
		return current;
	}	
	
	public void setCurrent(T m) {
		current = m;
	}

    /**
     * Add the confirmed successful pair sp.
     */
    public void addConfirmedSuccessfulPair(StatePair sp){
        succeeded.add(sp);
    }

	@Override
	public void confirmTemps() {
		for(StatePair sp: tempSucceeded){
            addConfirmedSuccessfulPair(sp);
        }
		mt.clear();
	}

	public double numTemps(){
		return tempSucceeded.size();
	}

	@Override
	public void postProcess() {
		
	}

	public void registerMerge(OrderedStatePair sp){
		mt.registerMerge(sp);
	}

	@Override
	public OrderedStatePair getMergedEquivalent(OrderedStatePair sp) {
		return mt.getMergedEquivalent(sp);
	}

}
