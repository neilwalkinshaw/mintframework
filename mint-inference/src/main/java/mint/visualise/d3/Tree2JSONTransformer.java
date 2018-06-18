/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package mint.visualise.d3;

import mint.model.Machine;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Tree2JSONTransformer {
	
	int endNodes  = -1;
		
	public void buildTree(Machine tree, File output, boolean patricia){
		try {
			FileWriter fw = new FileWriter(output);
			PrintWriter out = new PrintWriter(fw);
			
			JSONObject root = buildTree(tree, patricia);
			root.writeJSONString(out);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public JSONObject buildTree(Machine tree, boolean patricia){
		Map<Integer,Integer> stateWeights = new HashMap<Integer,Integer>();
		stateWeights.put(tree.getInitialState(), 1);
		List<String> label = new ArrayList<String>();
		label.add("#");
		JSONObject root = handle(tree.getInitialState(),tree,stateWeights, label, patricia);
		return root;
	}
	
	private JSONObject handle(Integer node, Machine<Set<TraceElement>> tree, Map<Integer,Integer> stateWeights, List<String> label, boolean patricia){
		Set<DefaultEdge> outgoing = tree.getAutomaton().getOutgoingTransitions(node);
		if(outgoing.isEmpty())
			return leafNode(node,stateWeights.get(node), label);
		else{
			if(outgoing.size() == 1 && patricia){
				DefaultEdge de = outgoing.iterator().next();
				Integer newNode = tree.getAutomaton().getTransitionTarget(de);
				
				int weight = tree.getAutomaton().getTransitionData(de).getPayLoad().size();
				stateWeights.put(newNode, weight);
				int oldWeight = stateWeights.get(node);
				if(weight < oldWeight){
					JSONObject bNode =  branchNode(node,stateWeights,outgoing, tree, label, patricia);
					stateWeights.put(endNodes--, oldWeight);
					JSONArray arr = (JSONArray)bNode.get("children");
					label.add(tree.getLabel(de));
					arr.add(leafNode(endNodes--,oldWeight,label));
					bNode.put("children", arr);
					return bNode;
				}
				else{
					label.add(tree.getLabel(de));
					return handle(newNode,tree,stateWeights,label,patricia);
				}
			}
			return branchNode(node,stateWeights,outgoing, tree, label, patricia);
			
		}
	}
	
	private static String buildSequenceString(List<String> sequence){
		String lab = sequence.get(0);
		if(sequence.size()>1){
			for (String s : sequence.subList(1, sequence.size()-1)) {
				lab += ","+s;
			}
		}
		return lab;
	}

	
	private JSONObject branchNode(Integer current,
								  Map<Integer, Integer> stateWeights, Set<DefaultEdge> outgoing, Machine<Set<TraceElement>> tree, List<String> sequence, boolean patricia) {
		JSONObject node = new JSONObject();
		node.put("name",sequence.get(0));
		node.put("id", current);
		node.put("sequence", buildSequenceString(sequence));
		JSONArray branch = new JSONArray();
		for(DefaultEdge ed: outgoing){
			Integer target = tree.getAutomaton().getTransitionTarget(ed);
			int weight = tree.getAutomaton().getTransitionData(ed).getPayLoad().size();
			stateWeights.put(target, weight);
			int oldWeight = stateWeights.get(current);
			if(weight < oldWeight){
				List<String> leafSequence = new ArrayList<String>();
				leafSequence.add(tree.getLabel(ed));
				branch.add(leafNode(endNodes--,oldWeight,leafSequence));
			}
			List<String> newLabels = new ArrayList<String>();
			newLabels.add(tree.getAutomaton().getTransitionData(ed).getLabel());
			branch.add(handle(target,tree,stateWeights, newLabels, patricia));
		}
		node.put("children", branch);
		return node;
	}

	private JSONObject leafNode(Integer t, Integer size, List<String> label) {
		
		JSONObject node = new JSONObject();
		node.put("size",size);
		node.put("id", t);
		node.put("name",label.get(0));
		node.put("sequence", buildSequenceString(label));
		return node;
	}

}
