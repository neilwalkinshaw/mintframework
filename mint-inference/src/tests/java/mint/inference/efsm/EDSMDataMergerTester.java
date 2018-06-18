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
import mint.inference.BaseClassifierInference;
import mint.inference.efsm.mergingstate.RedBlueMergingState;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.inference.efsm.scoring.RedBlueScorer;
import mint.inference.efsm.scoring.scoreComputation.ComputeScore;
import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.WekaGuardMachineDecorator;
import mint.model.prefixtree.EFSMPrefixTreeFactory;
import mint.model.prefixtree.PrefixTreeFactory;
import mint.model.walk.EFSMAnalysis;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.readers.TraceReader;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;
import mint.visualise.dot.DotGraphWithLabels;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Merges states to produce a state machine (a conventional FSM) - without accommodating data.
 * 
 * @author neilwalkinshaw
 *
 */

public class EDSMDataMergerTester {
	
	File tempFile;
    PrintWriter file;
    Configuration configuration;

	@Before
	public void setUp() throws Exception {
		Configuration.reset();
		configuration = Configuration.getInstance();
		tempFile = File.createTempFile("testtrace", ".txt");
        tempFile.deleteOnExit();
        file = new PrintWriter(tempFile);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSmallBranch() throws IOException, InterruptedException {
		file.println("types");
        file.println("a a:S b:N");
        file.println("b x:S y:N");
        file.println("c x:S y:N");
        file.println("trace");
        file.println("a abc 2");
        file.println("b abc 2");
        file.println("c abc 2");
        file.println("trace");
        file.println("c abc 2");
        file.println("a abc 2");
        file.println("b abc 2");
        file.flush();

        
        configuration.PREFIX_CLOSED = true;
        TraceSet result = TraceReader.readTraceFile(tempFile.toPath().toString(), configuration.TOKENIZER);

        BaseClassifierInference bci = null;
		PrefixTreeFactory<?> tptg;
		bci = new BaseClassifierInference(result, configuration.ALGORITHM);
		tptg = new EFSMPrefixTreeFactory(new PayloadMachine(), bci.getClassifiers(),bci.getElementsToInstances());
		
		Machine tree = tptg.createPrefixTree(result);
		RedBlueScorer rbs = new RedBlueScorer(1, new ComputeScore());
		SimpleMergingState ms = new RedBlueMergingState(tree);
		EDSMDataMerger edsm = new EDSMDataMerger(rbs, ms);
		Machine m = edsm.infer();
		assertTrue(m.getAutomaton().getStates().size() == 2);
		assertTrue(m.getAutomaton().getOutgoingTransitions(m.getInitialState(), "c").size() == 1);
		assertTrue(m.getAutomaton().getOutgoingTransitions(m.getInitialState(), "b").size() == 0);
		assertTrue(m.getAutomaton().getOutgoingTransitions(m.getInitialState(), "a").size() == 1);

	}
	
	@Test
	public void testBigBranch() throws IOException, InterruptedException {
		file.println("types");
        file.println("a a:S b:N");
        file.println("b x:S y:N");
        file.println("c x:S y:N");
        file.println("d x:S y:N");
        file.println("trace");
        file.println("a abc 2");
        file.println("b abc 2");
        file.println("c abc 2");
        file.println("d abc 2");
        file.println("a abc 2");
        file.println("b abc 2");
        file.println("trace");
        file.println("a abc 2");
        file.println("c abc 2");
        file.println("d abc 2");
        file.println("a abc 2");
        file.println("b abc 2");
        file.println("trace");
        file.println("a abc 2");
        file.println("c abc 2");
        file.println("d abc 2");
        file.println("a abc 2");
        file.println("b abc 2");
        file.flush();


        configuration.PREFIX_CLOSED = true;
        TraceSet result = TraceReader.readTraceFile(tempFile.toPath().toString(), configuration.TOKENIZER);

        BaseClassifierInference bci = null;
		PrefixTreeFactory<?> tptg;
		bci = new BaseClassifierInference(result, configuration.ALGORITHM);
		tptg = new EFSMPrefixTreeFactory(new PayloadMachine(), bci.getClassifiers(),bci.getElementsToInstances());
		
		Machine tree = tptg.createPrefixTree(result);
		
		RedBlueScorer rbs = new RedBlueScorer(2, new ComputeScore());
		SimpleMergingState ms = new RedBlueMergingState(tree);
		EDSMDataMerger edsm = new EDSMDataMerger(rbs, ms);
		Machine m = edsm.infer();
		System.out.println(DotGraphWithLabels.summaryDotGraph(m));

	}
	
	@Test
	public void testBigBranchNegTrace() throws IOException, InterruptedException {
		file.println("types");
        file.println("a a:S b:N");
        file.println("b x:S y:N");
        file.println("c x:S y:N");
        file.println("d x:S y:N");
        file.println("trace");
        file.println("a abc 2");
        file.println("b abc 2");
        file.println("c abc 2");
        file.println("d abc 2");
        file.println("a abc 2");
        file.println("b abc 2");
        file.println("trace");
        file.println("a abc 2");
        file.println("c abc 2");
        file.println("d abc 2");
        file.println("a abc 2");
        file.println("b abc 2");
        file.println("trace");
        file.println("a abc 2");
        file.println("c abc 2");
        file.println("d abc 2");
        file.println("a abc 2");
        file.println("b abc 2");
        file.println("negtrace");
        file.println("a abc 2");
        file.println("d abc 2");
        file.flush();


        configuration.PREFIX_CLOSED = true;
        TraceSet result = TraceReader.readTraceFile(tempFile.toPath().toString(), configuration.TOKENIZER);

        BaseClassifierInference bci = null;
		PrefixTreeFactory<WekaGuardMachineDecorator> tptg;
		bci = new BaseClassifierInference(result, configuration.ALGORITHM);
		tptg = new EFSMPrefixTreeFactory(new PayloadMachine(), bci.getClassifiers(),bci.getElementsToInstances());
		
		WekaGuardMachineDecorator tree = tptg.createPrefixTree(result);
		
		RedBlueScorer<RedBlueMergingState<WekaGuardMachineDecorator>> rbs = new RedBlueScorer<RedBlueMergingState<WekaGuardMachineDecorator>>(2, new ComputeScore());
		RedBlueMergingState<WekaGuardMachineDecorator> ms = new RedBlueMergingState<WekaGuardMachineDecorator>(tree);
		EDSMDataMerger<RedBlueMergingState<WekaGuardMachineDecorator>> edsm = new EDSMDataMerger<RedBlueMergingState<WekaGuardMachineDecorator>>(rbs, ms);
		WekaGuardMachineDecorator m = edsm.infer();
		System.out.println(DotGraphWithLabels.summaryDotGraph(m));
		EFSMAnalysis wca = new EFSMAnalysis(m);
		
		//re-create negative trace
		List<TraceElement> trace = new ArrayList<TraceElement>();
		List<VariableAssignment<?>> sVars = new ArrayList<VariableAssignment<?>>();
		StringVariableAssignment a = new StringVariableAssignment("a","abc");
		DoubleVariableAssignment b = new DoubleVariableAssignment("b",2D);
		sVars.add(a);
		sVars.add(b);
		SimpleTraceElement s = new SimpleTraceElement("a", sVars);
		trace.add(s);
		List<VariableAssignment<?>> tVars = new ArrayList<VariableAssignment<?>>();
		a = new StringVariableAssignment("a","abc");
		b = new DoubleVariableAssignment("b",2D);
		tVars.add(a);
		tVars.add(b);
		SimpleTraceElement t = new SimpleTraceElement("a", tVars);
		trace.add(t);
		assertTrue(!wca.walk(trace,true, m.getAutomaton()));
	}

}
