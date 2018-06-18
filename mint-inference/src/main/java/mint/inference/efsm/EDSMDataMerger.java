/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mint.inference.efsm;

import org.apache.log4j.Logger;
import mint.inference.BaseClassifierInference;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.inference.efsm.scoring.Scorer;
import mint.model.WekaGuardMachineDecorator;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.statepair.OrderedStatePair;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Merges states taking data into account to produce an EFSM. 
 * @author neilwalkinshaw
 *
 * @param <T>
 */
public class EDSMDataMerger <T extends SimpleMergingState<WekaGuardMachineDecorator>>  extends EDSMMerger<WekaGuardMachineDecorator, T>{
	
	public EDSMDataMerger(
			Scorer<T> scorer,
			T state) {
		super(scorer, state);
		// TODO Auto-generated constructor stub
	}

	final static Logger LOGGER = Logger.getLogger(EDSMDataMerger.class.getName());
	
	
	

	/*
	 * Check for each transition, if it predicts a particular event, that this event is indeed possible from the 
	 * next state - unless it is an accept state.
	 * Although we are looking at a state pair, this is post-merge - the second state in the state pair should no
	 * longer exist, and we are just looking at the first state %TODO(could be refactored so that the parameter is just the first
	 * state, not the state pair).
	 */
	protected boolean consistent(OrderedStatePair tempPair) {
		if(!super.consistent(tempPair))
			return false;
		Integer first = tempPair.getFirstState();
		TraceDFA automaton = state.getCurrent().getAutomaton();
		Set<DefaultEdge> incoming = automaton.getIncomingTransitions(first);
		Set<DefaultEdge> outgoing = automaton.getOutgoingTransitions(first);
		if(incoming.isEmpty()) //initial state.
			return true;
        if(outgoing.isEmpty())
            return true;
        Set<String> predicted = new HashSet<String>();
		for (DefaultEdge transition : incoming) { // get all incoming transitions
			TransitionData<Set<TraceElement>> transData = automaton.getTransitionData(transition);
			Classifier c = state.getCurrent().getModelMap().get(transData.getLabel());
			if(c == null)
				continue;
			Instances instances = obtainInstancesForTransition(transition);
			if(instances == null)
				continue;

			for (Instance instance : instances) { /// check that predicted "next" transition is in outgoing transitions
				try {
					int classified = (int)c.classifyInstance(instance);
                    String p = instances.classAttribute().value(classified);
                    predicted.add(p);
					if(!containsAtLeastOne(outgoing,p))
						return false;
				} catch (Exception e) {
					LOGGER.error("Classification error in consistency check");
					break;
				}
			}
			
		}
		return true;
	}

	/**
	 * Obtains a set of WEKA instances for the data labels that are attached 
	 * to a given transition.
	 * @param transition
	 * @return
	 */
	protected Instances obtainInstancesForTransition(DefaultEdge transition) {
		Set<Instance> ins = new HashSet<Instance>();
		TraceDFA automaton = state.getCurrent().getAutomaton();
		TransitionData<Set<TraceElement>> transData = automaton.getTransitionData(transition);
		for (TraceElement simpleTraceElement : transData.getPayLoad()) { //get instances for each incoming transition
			Map<TraceElement,Instance> elementsToInstances = state.getCurrent().getElementMap();
			if(!elementsToInstances.containsKey(simpleTraceElement))
				continue;
			Instance i = elementsToInstances.get(simpleTraceElement);
			if(i!=null)
				ins.add(i);
		}
		if(ins.isEmpty())
			return null;
		Instances instances = BaseClassifierInference.makeInstances(ins,transData.getLabel());
		return instances;
	}

	

}
