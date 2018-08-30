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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;

public class DotGraphWithLabels {
	public static String summaryDotGraph(SimpleMergingState mergeState){
		Machine automaton = mergeState.getCurrent();
		return summaryDotGraph(automaton);
	}

	public static void summaryDotGraph(SimpleMergingState mergeState, OutputStream out){
		Machine automaton = mergeState.getCurrent();
		summaryDotGraph(automaton, out);
	}


	public static String summaryDotGraph(Machine<Set<TraceElement>> automaton) {
		ByteArrayOutputStream builder = new ByteArrayOutputStream();
		summaryDotGraph(automaton, builder);
		return builder.toString();
	}

	/**
	 * Writes the objects passed as parameters to the output stream. Could use an
	 * OutputStreamWriter to roughly do the same thing but it was syntactically
	 * more convenient in my opinion.
	 *
	 * @param o: stream
	 * @param s: objects to write
	 */
	private static void write(OutputStream o, Object... s) {
		for (Object obj : s) {
			try { o.write(String.valueOf(obj).getBytes()); } catch (Exception ignored) {}
		}
	}

	public static void summaryDotGraph(Machine<Set<TraceElement>> automaton, OutputStream out) {
		write(out, "digraph Automaton {\n");
		//write(out, "  rankdir = LR;\n");
		Collection<Integer> states = automaton.getStates();
		Integer[] stateArray = states.toArray(new Integer[states.size()]);
		for (int i = 0; i<stateArray.length;i++) {
			Integer s = stateArray[i];
			write(out, "  ", i);
			//b.append(" [label=\"\"");
            write(out, " [label=\"", s, '"');
			if (automaton.getAutomaton().getAccept(s).equals(TraceDFA.Accept.ACCEPT))
				write(out, ",shape=doublecircle");
			else
				write(out, ",shape=circle");
			write(out,"];\n");
			if (s.equals(automaton.getInitialState())) {
				write(out,"  initial [shape=plaintext];\n",
						       "  initial -> ", i, '\n');
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
			appendTransitions(out, destLabels, stateArray);
		}
		write(out,"}\n");
	}
	

	private static void appendTransitions(OutputStream b,
										  Map<OrderedStatePair, Set<String>> destLabels, Integer[] stateArray) {
		Iterator<OrderedStatePair> pairs = destLabels.keySet().iterator();
		while(pairs.hasNext()){
			OrderedStatePair next = pairs.next();
			int from = getIndexOf(next.getFirstState(), stateArray);
			int to = getIndexOf(next.getSecondState(), stateArray);
			write(b ,"  ", from,
					" -> ", to, " [label=\"",
					getLabels(destLabels.get(next)),
					"\"]\n");
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
