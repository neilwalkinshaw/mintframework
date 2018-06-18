/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013,2014 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package mint.inference.efsm;

import mint.Configuration;
import mint.inference.efsm.mergingstate.RedBlueMergingState;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.inference.efsm.scoring.Scorer;
import mint.model.DaikonMachineDecorator;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.statepair.OrderedStatePair;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;

import java.util.Iterator;
import java.util.Set;

/**
 * Merges states according to the GK-Tails algorithm by Lorenzoli et al (ICSE 2008). By default implements strong subsumption (see Lorenzoli et al. ICSE 2008). Need to use the -ktail flag and supply a value for the -k flag
 * to apply weak subsumption. Does not implement equivalence relation.
 * 
 * @author neilwalkinshaw
 *
 */


public class GKTailMerger extends AbstractMerger<DaikonMachineDecorator,RedBlueMergingState<DaikonMachineDecorator>> {

	
	
	public GKTailMerger(Scorer<RedBlueMergingState<DaikonMachineDecorator>> scorer, RedBlueMergingState<DaikonMachineDecorator> state) {
		super(scorer,state);
		Configuration configuration = Configuration.getInstance();
		DaikonMachineDecorator dmd = new DaikonMachineDecorator(state.getCurrent(),configuration.MINDAIKON, false);
		state.setCurrent(dmd);
	}
	
	/*
	 * GK-Tail does not check anything *after* a merge, so this function simply returns true.
	 * (non-Javadoc)
	 * @see org.bitbucket.efsmtool.inference.efsm.AbstractRedBlueMerger#consistent(org.bitbucket.efsmtool.model.StatePair)
	 */
	@Override
	protected boolean consistent(OrderedStatePair p) {
		return true;
	}

	protected boolean simpleMerge(OrderedStatePair sp){
		if(!sp.getFirstState().equals(sp.getSecondState()))
			state.getCurrent().merge(sp,state);
		return mergeTransitionsToSameState(sp.getFirstState(), state);
	}
	
	
	protected boolean mergeTransitionsToSameState(Integer root, SimpleMergingState<DaikonMachineDecorator> currentState) {
		TraceDFA automaton = state.getCurrent().getAutomaton();
		Set<DefaultEdge> outgoing = automaton.getOutgoingTransitions(root);
		DaikonMachineDecorator daikonDec = (DaikonMachineDecorator)state.getCurrent();
		Iterator<DefaultEdge> outerIt = outgoing.iterator();
		
		while(outerIt.hasNext()){
			Iterator<DefaultEdge> innerIt = outgoing.iterator();
			DefaultEdge outer = outerIt.next();
			TransitionData<Set<TraceElement>> outerData = automaton.getTransitionData(outer);
			while(innerIt.hasNext()){
				DefaultEdge inner = innerIt.next();
				TransitionData<Set<TraceElement>> innerData = automaton.getTransitionData(inner);

				if(outer.equals(inner))
					continue;
				if(!outerData.getLabel().equals(innerData.getLabel()))
					continue;
				if(!state.getCurrent().compatible(inner,outer))
					continue;
				if(automaton.getTransitionTarget(outer).equals(automaton.getTransitionTarget(inner)) ){
					if(!daikonDec.constraintCompatible(inner, outer))
						return false;
					currentState.getCurrent().mergeTransitions(root, outer, inner); 
					return mergeTransitionsToSameState(root, currentState);
				}
			}
		}

		return true;
	}

	@Override
	protected boolean merge(OrderedStatePair p) {
		if(!state.allowed(p))
			return false;
		
		if(!simpleMerge(p)){
			return false;
		}				
			if(!(consistent(p))){
				return false;
			}

		state.addTempSuccessfulPair(p);


        /* The following code is commented out because it does not exactly match the
        procedure described in the ICSE paper (though the following code would no doubt
         improve the accuracy.
         */
		/*try{
			state.getCurrent().postProcessMerge();
		}
		catch(Exception e){
			System.out.println("not enough points ...");
		}*/
		return true; 
	}

}
