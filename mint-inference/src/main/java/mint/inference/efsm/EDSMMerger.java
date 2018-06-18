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

import org.apache.log4j.Logger;
import mint.Configuration;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.inference.efsm.scoring.Scorer;
import mint.model.Machine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.statepair.OrderedStatePair;
import mint.tracedata.IOTraceElement;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Merges states without taking data into account to produce a conventional FSM.
 * @author neilwalkinshaw
 *
 * @param <S>
 * @param <T>
 */

public class EDSMMerger <S extends Machine, T extends SimpleMergingState<S>>  extends AbstractMerger<S,T>{
	
	protected boolean consistentIOSequencing = false;
    protected boolean carefulDet = false;


    final static Logger LOGGER = Logger.getLogger(EDSMMerger.class.getName());


    public EDSMMerger(Scorer<T> scorer, T state){
		super(scorer, state);
		
		Configuration c = Configuration.getInstance();
        carefulDet = c.CAREFUL_DETERMINIZATION;
		consistentIOSequencing = c.CONSISTENT_RETURNS;
		initSize = state.getCurrent().getStates().size();

		assert(state.getCurrent().isDeterministic());
	}

	
	public void setConsistentIOSequencing(boolean ioSeq){
		consistentIOSequencing = ioSeq;
	}
	
	protected boolean simpleMerge(OrderedStatePair sp){
		if(!sp.getFirstState().equals(sp.getSecondState()))
			state.getCurrent().merge(sp,state);
		else{
			return true; //already merged.
		}
		boolean merged = mergeTransitionsToSameState(sp.getFirstState(), state);
		if(merged){
			state.addTempSuccessfulPair(sp);
			return merged;
		}
        return false;

	}


	/*
	 * Recursive merge and determinize function
	 */
	@Override
	public boolean merge(OrderedStatePair sp){
		if(Thread.currentThread().isInterrupted()){
			return false;
		}
		//firstStates.add(sp.getFirstState());
		state.registerMerge(sp);
		TraceDFA automaton = state.getCurrent().getAutomaton();
		if(!state.allowed(sp))
			return false;
        if(!simpleMerge(sp)){
			return false;
		}				
		if(!(consistent(sp))){
			return false;
		}
		assert(automaton.containsState(sp.getFirstState()));
        assert(state.getCurrent().getAutomaton().containsState(state.getCurrent().getInitialState()));

        boolean merged  = determinise(sp.getFirstState());

        if(!merged){
			return false;
		}

		return true; 
	}


    /**
     * A post-processing step to a merge. Once the merge has happened, there can be outgoing
     * transitions from the merged state, where identical transitions go to the same state.
     * These are merged here.
     * @param root
     * @param currentState
     * @return
     */
	protected boolean mergeTransitionsToSameState(Integer root, T currentState) {
		TraceDFA automaton = state.getCurrent().getAutomaton();
		Set<DefaultEdge> outgoing = automaton.getOutgoingTransitions(root);

        for (DefaultEdge anOutgoing : outgoing) {
			if(Thread.currentThread().isInterrupted()){
				return false;
			}
			Iterator<DefaultEdge> innerIt = outgoing.iterator();
            TransitionData<Set<TraceElement>> outerData = automaton.getTransitionData(anOutgoing);
            while (innerIt.hasNext()) {
				if(Thread.currentThread().isInterrupted()){
					return false;
				}
				DefaultEdge inner = innerIt.next();
                TransitionData<Set<TraceElement>> innerData = automaton.getTransitionData(inner);

                if (anOutgoing.equals(inner))
                    continue;
                if (!outerData.getLabel().equals(innerData.getLabel()))
                    continue;
                if (!state.getCurrent().compatible(inner, anOutgoing))
                    continue;
                assert (outerData.getLabel().equals(innerData.getLabel()));
                if (automaton.getTransitionTarget(anOutgoing).equals(automaton.getTransitionTarget(inner))) {
                    currentState.getCurrent().mergeTransitions(root, anOutgoing, inner);
                    return mergeTransitionsToSameState(root, currentState);
                }
            }
        }

		return true;
	}

	



	
	
	/*
	 * The term "determinise" is possibly misleading. This function tries to determinise in the EFSM sense; transitions may
	 * still have the same outgoing labels, but may lead to different states because the functions attached to these labels 
	 * produce different outcomes for the data values attached to each transition.
	 */
	protected boolean determinise(Integer from) {

		Set<OrderedStatePair> detMerges = determinisePhase1(from);
		if(Thread.currentThread().isInterrupted()){
			return false;
		}
		Iterator<OrderedStatePair> detMergeIt = detMerges.iterator();
		while(detMergeIt.hasNext()) {



			OrderedStatePair op = detMergeIt.next();
			op = state.getMergedEquivalent(op);
			if(op.getFirstState().equals(op.getSecondState()))
				return false; //NO?
            /*
            Check whether a determinisation-merge would be invalid according to the scorer used
            This check is only carried out if the Configuration.CAREFUL_DETERMINIZATION is true.
             */
            if(carefulDet) {
                if (!scorer.compatible(state, op))
                    return false;
            }

            /*If both states belong to firstStates, then one of them will inevitably be removed, causing the whole
            merge sequence to fail, so we terminate it.
             */
            //if(firstStates.contains(op.getFirstState()) && firstStates.contains(op.getSecondState()))
            //    return false;

            /*
            If only the second state is a first-state, then we can prevent the invalidation of the merge sequence by
            simply swapping the states to be merged.
             */
            //if(firstStates.contains(op.getSecondState())) {
            //    if(op.getFirstState().equals(state.getCurrent().getInitialState()))
            //        return false;
            //    op.reverse();
            //}

            /*
            Prevent determinization from swallowing up the initial state..
             */
            if(state.getCurrent().getInitialState().equals(op.getSecondState())) {
                //if(firstStates.contains(op.getFirstState()))
                //    return false;
                op.reverse();
            }

			//try merge------
			//assert(state.getCurrent().getAutomaton().getStates().contains(from));
			boolean merged = merge(op);
			if(!merged){
				return false;
			}


			/*
			This if-condition should never evaluate to true, since introducing the firstState tracker.
			 */
			if(!state.getCurrent().getAutomaton().containsState(from)){
            //    LOGGER.debug("Root node swallowed up.");
                return true; //all finished
            }

            detMerges = determinisePhase1(from);
			if(Thread.currentThread().isInterrupted()){
				return false;
			}
			detMergeIt = detMerges.iterator();	
		}
		return true;
	}

	
	

	/**
	 * Return the set of all non-deterministic transitions that are immediately outgoing from the root state.
	 * @param root
	 */
	private Set<OrderedStatePair> determinisePhase1(Integer root) {
		if(Thread.currentThread().isInterrupted()){
			return null;
		}
		Set<OrderedStatePair> detMerges = new HashSet<OrderedStatePair>();
		TraceDFA automaton = state.getCurrent().getAutomaton();
		assert(automaton.containsState(root));
		Set<DefaultEdge> outgoing = automaton.getOutgoingTransitions(root);
		assert(state.getCurrent().getStates().contains(root));

        for (DefaultEdge anOutgoing : outgoing) {
            Iterator<DefaultEdge> innerIt = outgoing.iterator();
            DefaultEdge outer = anOutgoing;
            TransitionData<Set<TraceElement>> outerData = automaton.getTransitionData(outer);

            while (innerIt.hasNext()) {
                DefaultEdge inner = innerIt.next();
                TransitionData<Set<TraceElement>> innerData = automaton.getTransitionData(inner);
                if (inner == outer)
                    continue;
                if (!state.getCurrent().compatible(inner, outer))
                    continue;
                assert (innerData.getLabel().equals(outerData.getLabel()));
                if (automaton.getTransitionTarget(inner).equals(root))
                    continue;

				//-- BEGIN Heuristic to set state with <=1 outgoing transitions as second state
				Integer outerTarget = automaton.getTransitionTarget(outer);
				Integer innerTarget = automaton.getTransitionTarget(inner);
				OrderedStatePair op;
				if(automaton.getOutgoingTransitions(outerTarget).size()<=1)
					op = new OrderedStatePair(innerTarget, outerTarget);
				else
					op = new OrderedStatePair(outerTarget, innerTarget);
				//-- END

                if (automaton.getTransitionTarget(inner).equals(automaton.getTransitionTarget(outer))) { //going to the same state - we can merge them here!
                    state.getCurrent().mergeTransitions(root, outer, inner);
                    return determinisePhase1(root);
                }


                detMerges.add(op);

            }

        }
		return detMerges;
	}

	@Override
	protected boolean consistent(OrderedStatePair p) {
		if(!consistentIOSequencing)
			return true;
		Integer first = p.getFirstState();
		TraceDFA automaton = state.getCurrent().getAutomaton();
		Set<DefaultEdge> incoming = automaton.getIncomingTransitions(first);
		Set<DefaultEdge> outgoing = automaton.getOutgoingTransitions(first);
		boolean init = false;
		if(automaton.getInitialState().equals(first)) //initial state.
			init = true;
		if(containsIO(automaton,incoming,false))
			if(containsIO(automaton,outgoing,false))
				return false; //two returns cannot happen after each other.
		if(init){
			if(containsIO(automaton,outgoing,false))
				return false; //a trace cannot start with a return.
		}
		return true;
	}
	
	private boolean containsIO(TraceDFA a, Set<DefaultEdge> edges, boolean input){
		for(DefaultEdge de:edges){
			TransitionData<Set<TraceElement>> transData = a.getTransitionData(de);
			for(TraceElement te:transData.getPayLoad()){
				if(!(te instanceof IOTraceElement))
					continue;
				IOTraceElement iot = (IOTraceElement)te;
				if(iot.isInput() == input)
					return true;
			}
		}
		return false;
	}

	
}
