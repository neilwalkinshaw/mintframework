/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013,2014 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package mint.model.walk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.Terminal;
import mint.inference.gp.tree.nonterminals.doubles.AddDoublesOperator;
import mint.inference.gp.tree.terminals.DoubleVariableAssignmentTerminal;
import mint.model.GPFunctionMachineDecorator;
import mint.model.PayloadMachine;
import mint.model.SimpleMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.VariableAssignment;

public class ComputeTransitionWalkTests {

	@Before
	public void setup() {

	}

	@After
	public void teardown() {
	}

	/**
	 * Test of for a simple, linear prefix tree.
	 */
	@Test
	public void testSimpleAcceptingTrace() throws Exception {

		NonTerminal<DoubleVariableAssignment> adder = new AddDoublesOperator();
		DoubleVariableAssignment x = new DoubleVariableAssignment("x", 0D);
		DoubleVariableAssignment y = new DoubleVariableAssignment("y", 0D);

		Terminal<DoubleVariableAssignment> xTerm = new DoubleVariableAssignmentTerminal(x, false, false);
		Terminal<DoubleVariableAssignment> yTerm = new DoubleVariableAssignmentTerminal(y, false, false);
		adder.addChild(xTerm);
		adder.addChild(yTerm);

		Collection<VariableAssignment<?>> assignments = new HashSet<VariableAssignment<?>>();
		assignments.add(new DoubleVariableAssignment("x", 7D));
		assignments.add(new DoubleVariableAssignment("y", 3D));

		// SET UP MACHINE
		SimpleMachine sm = new PayloadMachine();
		TraceDFA automaton = new TraceDFA();
		int initState = automaton.addState();
		automaton.setInitialState(initState);
		int first = automaton.addState();
		TransitionData<Set<TraceElement>> td = new TransitionData<Set<TraceElement>>("toot", null);
		DefaultEdge e = automaton.addTransition(initState, first, td);
		GPFunctionMachineDecorator gpMachine = new GPFunctionMachineDecorator(sm, 0, null, 0);

		Set<Node<?>> funcs = new HashSet<Node<?>>();
		funcs.add(adder);
		gpMachine.setFunctions(e, funcs);
		gpMachine.setVarToFunction(adder, "x");

		// SET UP WALK
		List<DefaultEdge> walk = new ArrayList<DefaultEdge>();
		walk.add(e);
		WalkResult wr = new WalkResult(first, walk);

		// EXECUTE
		ComputeTransitionWalk compWalk = new ComputeTransitionWalk(gpMachine, wr, null);
		Collection<VariableAssignment<?>> results = compWalk.compute(assignments);
		for (VariableAssignment<?> r : results) {
			System.out.println(r.toString());
		}
	}

	/**
	 * Test of for a simple, linear prefix tree.
	 */
	@Test
	public void testOverlayAcceptingTrace() throws Exception {

		NonTerminal<DoubleVariableAssignment> adder = new AddDoublesOperator();
		DoubleVariableAssignment x = new DoubleVariableAssignment("x", 0D);
		DoubleVariableAssignment y = new DoubleVariableAssignment("y", 0D);

		Terminal<DoubleVariableAssignment> xTerm = new DoubleVariableAssignmentTerminal(x, false, false);
		Terminal<DoubleVariableAssignment> yTerm = new DoubleVariableAssignmentTerminal(y, false, false);
		adder.addChild(xTerm);
		adder.addChild(yTerm);

		Collection<VariableAssignment<?>> assignments = new HashSet<VariableAssignment<?>>();
		DoubleVariableAssignment d1 = new DoubleVariableAssignment("x", 7D);
		d1.setParameter(true);
		assignments.add(d1);
		DoubleVariableAssignment d2 = new DoubleVariableAssignment("y", 3D);
		assignments.add(d2);

		Collection<VariableAssignment<?>> assignments2 = new HashSet<VariableAssignment<?>>();
		DoubleVariableAssignment d3 = new DoubleVariableAssignment("x", 1D);
		d3.setParameter(true);
		assignments2.add(d3);
		DoubleVariableAssignment d4 = new DoubleVariableAssignment("y", 2D);
		assignments2.add(d4);

		TraceElement te1 = new SimpleTraceElement("a", assignments);
		TraceElement te2 = new SimpleTraceElement("a", assignments2);

		List<TraceElement> trace = new ArrayList<TraceElement>();
		trace.add(te1);
		trace.add(te2);

		// SET UP MACHINE
		SimpleMachine sm = new PayloadMachine();
		TraceDFA automaton = new TraceDFA();
		int initState = automaton.addState();
		automaton.setInitialState(initState);
		TransitionData<Set<TraceElement>> td = new TransitionData<Set<TraceElement>>("a", null);
		DefaultEdge e = automaton.addTransition(initState, initState, td);
		GPFunctionMachineDecorator gpMachine = new GPFunctionMachineDecorator(sm, 0, null, 0);

		Set<Node<?>> funcs = new HashSet<Node<?>>();
		funcs.add(adder);
		gpMachine.setFunctions(e, funcs);
		gpMachine.setVarToFunction(adder, "x");

		// SET UP WALK
		List<DefaultEdge> walk = new ArrayList<DefaultEdge>();
		walk.add(e);
		walk.add(e);
		WalkResult wr = new WalkResult(initState, walk);

		// EXECUTE
		ComputeTransitionWalk compWalk = new ComputeTransitionWalk(gpMachine, wr, null);
		Collection<VariableAssignment<?>> results = compWalk.compute(trace);
		for (VariableAssignment<?> r : results) {
			System.out.println(r.toString());
		}
	}

	@Test
	public void MathTest() {
		System.out.println(Double.MAX_VALUE);
		System.out.println(Double.MAX_VALUE + 1D);
	}

}
