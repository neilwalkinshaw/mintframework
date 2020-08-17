/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package mint.model.dfa;

import org.apache.log4j.Logger;
//import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.*;

/*
 * Represententation of a DFA - built on top of the JGraphT DirectedMultiGraph
 */

public class TraceDFA<T> implements Cloneable {

	public enum Accept{ACCEPT,REJECT,UNDEFINED};
	
	private final static Logger LOGGER = Logger.getLogger(TraceDFA.class.getName());

	
	protected DirectedPseudograph<Integer,DefaultEdge> structure;
	protected Map<Integer,Accept> acceptStates;
	protected Map<DefaultEdge,TransitionData<T>> transitions;
	protected Integer initialState;
	
	public TraceDFA(){
		structure = new DirectedPseudograph<Integer,DefaultEdge>(DefaultEdge.class);
		acceptStates = new HashMap<Integer,Accept>();
		initialState = 0;
		acceptStates.put(initialState, Accept.UNDEFINED);
		structure.addVertex(initialState);
		transitions = new HashMap<DefaultEdge,TransitionData<T>>();
	}
	
	public boolean containsState(Integer state){
       return getStates().contains(state);
    }
	
	public TraceDFA(DirectedPseudograph<Integer, DefaultEdge> structure) {
		super();
		this.structure = structure;
		acceptStates = new HashMap<Integer,Accept>();
		transitions = new HashMap<DefaultEdge,TransitionData<T>>();
	}


	public int stateCount(){
		return structure.vertexSet().size();
	}
	
	public int transitionCount(){
		return structure.edgeSet().size();
	}
	
	public Integer getTransitionTarget(DefaultEdge e){
		return structure.getEdgeTarget(e);
	}
	
	public Integer getTransitionSource(DefaultEdge e){
		return structure.getEdgeSource(e);
	}
	
	public Collection<Integer> getStates(){
		return structure.vertexSet();
	}


	public Set<String> getAlphabet(){
        Set<String> alphabet = new HashSet<String>();
        for(TransitionData<?> td : transitions.values()){
            alphabet.add(td.getLabel());
        }
        return alphabet;
    }
	
	public boolean consistentTransitions(){
		
		for (Integer s : getStates()) {
			for (DefaultEdge t : structure.outgoingEdgesOf(s)){
				if(!structure.vertexSet().contains(structure.getEdgeTarget(t)))
					return false;
			}
			
		}
		for(DefaultEdge e: structure.edgeSet()){
			if(!transitions.containsKey(e))
				return false;
		}
		return true;
	}
	
	/**
	 * Returns true if b is reachable from a, false otherwise.
	 */
	public boolean reachableFrom(Integer a, Integer b){
        BreadthFirstIterator<Integer,DefaultEdge> it = new BreadthFirstIterator<Integer, DefaultEdge>(structure,a);
        while(it.hasNext()){
            if(it.next().equals(b))
                return true;
        }
        return false;
		
	}

    /**
     * Returns all nodes reachable from a.
     * @param a
     * @return
     */
    public Collection<Integer> nodesReachableFrom(Integer a){
        return reachableFrom(structure,a);
    }

    private Collection<Integer> reachableFrom(Graph<Integer,DefaultEdge> graph, Integer a) {
        Collection<Integer> set = new HashSet<Integer>();
        BreadthFirstIterator<Integer,DefaultEdge> it = new BreadthFirstIterator<Integer, DefaultEdge>(graph,a);
        while(it.hasNext()){
            set.add(it.next());
        }
        return set;
    }

    /**
     * Returns all nodes that reach a.
     * @param a
     * @return
     */
    public Collection<Integer> nodesThatReach(Integer a){
        EdgeReversedGraph<Integer,DefaultEdge> reversed = new EdgeReversedGraph<Integer,DefaultEdge>(structure);
        return reachableFrom(reversed,a);
    }

	public GraphPath<Integer,DefaultEdge> shortestPath(Integer a, Integer b){
		return DijkstraShortestPath.findPathBetween(structure, a, b);
	}
	
	public boolean consistentStates(){
		
		for (Integer s : getStates()) {
			if(!s.equals(initialState)){
				if(structure.incomingEdgesOf(s).isEmpty()){
					return false;
				}
			}
		}
		return true;
	}

	public Set<DefaultEdge> getIncomingTransitions(Integer vertex){
		return structure.incomingEdgesOf(vertex);
	}
	
	public Set<DefaultEdge> getOutgoingTransitions(Integer vertex){
		return structure.outgoingEdgesOf(vertex);
	}
	
	
	
	public Integer addState(){
		Integer v = stateCount();
		structure.addVertex(v);
		acceptStates.put(v, Accept.UNDEFINED);
		return v;
	}

	public Integer addState(Integer v){
		structure.addVertex(v);
		acceptStates.put(v, Accept.UNDEFINED);
		return v;
	}
	
	public DefaultEdge addTransition(Integer source, Integer target, TransitionData<T> t){
		DefaultEdge edge = structure.addEdge(source, target);
		transitions.put(edge, t);
		assert(t!=null);
        return edge;
	}
	
	
	public Integer getInitialState(){
		return initialState;
	}
	
	public void setInitialState(Integer s){
		initialState = s;
	}

	public void removeState(Integer s) {
		structure.removeVertex(s);
		acceptStates.remove(s);
	}
	
	public void removeTransition(DefaultEdge e){
		structure.removeEdge(e);
	}
	
	public TransitionData<T> getTransitionData(DefaultEdge e){
		//assert(transitions.containsKey(e));
		if(!transitions.containsKey(e)){
			LOGGER.error("could not find transition data for: "+e);
			//assert(transitions.containsKey(e));
			return null;
		}
		TransitionData<T> ret = transitions.get(e);
		//assert(ret!=null);
		return ret;
	}
	
	public void setAccept(Integer state, Accept accept){
		acceptStates.put(state, accept);
	}

    public boolean compatible(Integer stateA, Integer stateB){
        Accept a = getAccept(stateA);
        Accept b = getAccept(stateB);
        if(a.equals(Accept.UNDEFINED) || b.equals(Accept.UNDEFINED))
            return true;
        else return a.equals(b);
    }
	
	public Accept getAccept(Integer state){
		if(!acceptStates.containsKey(state))
			return Accept.UNDEFINED;
		return acceptStates.get(state);
	}

	public Set<DefaultEdge> getOutgoingTransitions(Integer state,
												   String name) {
		Set<DefaultEdge> outgoing = structure.outgoingEdgesOf(state);
		Set<DefaultEdge> ret = new HashSet<DefaultEdge>();
		for (DefaultEdge defaultEdge : outgoing) {
			TransitionData<T> edgeData = getTransitionData(defaultEdge);
			if(edgeData.getLabel().equals(name))
				ret.add(defaultEdge);
		}
		return ret;
	}
	
	public void setTransitionData(Map<DefaultEdge,TransitionData<T>> transData){
		this.transitions = transData;
	}

	public Set<DefaultEdge> getTransitions(){
		return structure.edgeSet();
	}
	
	public TraceDFA clone(){
		DirectedPseudograph<Integer,DefaultEdge> g = new DirectedPseudograph<Integer,DefaultEdge>(DefaultEdge.class);
		for(Integer i: structure.vertexSet()){
			g.addVertex(new Integer(i));
		}
		Map<DefaultEdge,TransitionData<T>> clonedTransData = new HashMap<DefaultEdge,TransitionData<T>>();
		for(DefaultEdge e:structure.edgeSet()){
			TransitionData<T> transData = transitions.get(e);
			Integer from = structure.getEdgeSource(e);
			Integer to = structure.getEdgeTarget(e);
			DefaultEdge newEdge = g.addEdge(from, to);
			clonedTransData.put(newEdge, transData);
		}
		TraceDFA cloned = new TraceDFA(g);
		for(Integer state:acceptStates.keySet()){
			cloned.setAccept(state, acceptStates.get(state));
		}
		cloned.setInitialState(initialState);
		cloned.setTransitionData(clonedTransData);
		return cloned;
	}

	public List<GraphPath<Integer,DefaultEdge>> allPaths(){
		AllDirectedPaths<Integer,DefaultEdge> paths = new AllDirectedPaths<Integer,DefaultEdge>(structure);
		Set<Integer> sourceVertex = new HashSet<Integer>();
		Set<Integer> destinationVertices = new HashSet<Integer>();
		destinationVertices.addAll(getStates());
		sourceVertex.add(initialState);
		return paths.getAllPaths(sourceVertex,destinationVertices,true,20);
	}



	
	public String toString(){
		return getStates().size()+" states, "+structure.edgeSet().size()+" edges";
	}

}
