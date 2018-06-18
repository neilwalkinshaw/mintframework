/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mint.visualise.dot;

import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.model.Machine;
import mint.model.dfa.TraceDFA;
import mint.model.statepair.OrderedStatePair;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

public class DotGraphWithLabels {
	
	
	
	public static String summaryDotGraph(SimpleMergingState mergeState){
		Machine automaton = mergeState.getCurrent();
		return summaryDotGraph(automaton);
		
	}
	
	public static String summaryDotGraph(Machine<Set<TraceElement>> automaton){
		StringBuilder b = new StringBuilder("digraph Automaton {\n");
		//b.append("  rankdir = LR;\n");
		Collection<Integer> states = automaton.getStates();
		Integer[] stateArray = states.toArray(new Integer[states.size()]);
		for (int i = 0; i<stateArray.length;i++) {
			Integer s = stateArray[i];
			b.append("  ").append(i);
			//b.append(" [label=\"\"");
            b.append(" [label=\""+s+"\"");
			if (automaton.getAutomaton().getAccept(s).equals(TraceDFA.Accept.ACCEPT))
				b.append(",shape=doublecircle");
			else
				b.append(",shape=circle");
			b.append("];\n");
			if (s.equals(automaton.getInitialState())) {
				b.append("  initial [shape=plaintext];\n");
				b.append("  initial -> ").append(i).append("\n");
			}
			Map<OrderedStatePair,Set<String>> destLabels = new HashMap<OrderedStatePair,Set<String>>();
			for (DefaultEdge t : automaton.getAutomaton().getOutgoingTransitions(s)) {
				//if(automaton.getAutomaton().getTransitionData(t).getPayLoad().size()<5)
				//	continue;
				int dest = automaton.getAutomaton().getTransitionTarget(t);
				String l = automaton.getLabel( t);
				Set<String> labs;
				OrderedStatePair pair = new OrderedStatePair(s,dest);
				if(destLabels.get(pair) == null)
					labs = new HashSet<String>();
				else
					labs = destLabels.get(pair);
				labs.add(l);
				destLabels.put(pair, labs);

			}
			appendTransitions(b,destLabels,stateArray);
		}
		b = b.append("}\n");
		return b.toString();
	}
	

	private static void appendTransitions(StringBuilder b,
										  Map<OrderedStatePair, Set<String>> destLabels, Integer[] stateArray) {
		Iterator<OrderedStatePair> pairs = destLabels.keySet().iterator();
		while(pairs.hasNext()){
			OrderedStatePair next = pairs.next();
			int from = getIndexOf(next.getFirstState(), stateArray);
			int to = getIndexOf(next.getSecondState(), stateArray);
			b.append("  ").append(from);
			b.append(" -> ").append(to).append(" [label=\"");
			b.append(getLabels(destLabels.get(next)));
			b.append("\"]\n");
		}
		
	}

	private static String getLabels(Set<String> set) {
		String lab = "";
		for (String string : set) {
			if(!lab.isEmpty())
				lab = lab+"\\n ";
			string = string.replace("\"", "\\\"");
			lab = lab + string;
		}
		return lab;
	}
	
	
	private static int getIndexOf(Integer dest, Integer[] stateArray) {
		for(int i = 0; i<stateArray.length;i++){
			Integer s = stateArray[i];
			if(s.equals(dest))
				return i;
		}
		return -1;
	}


}
