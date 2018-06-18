package mint.inference.efsm.scoring.scoreComputation;

import mint.Configuration;
import mint.inference.BaseClassifierInference;
import mint.inference.efsm.mergingstate.RedBlueMergingState;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.inference.efsm.scoring.RedBlueScorer;
import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.prefixtree.EFSMPrefixTreeFactory;
import mint.model.prefixtree.PrefixTreeFactory;
import mint.model.statepair.OrderedStatePair;
import mint.tracedata.TraceSet;
import mint.tracedata.readers.TraceReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;

public class ScoreTest {
	
	Machine m;
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
	public void kTailtest() {
		TraceDFA automaton = new TraceDFA();
		
		int a = automaton.addState();
		int b = automaton.addState();
		int c = automaton.addState();
		int d = automaton.addState();
		int e = automaton.addState();
		int f = automaton.addState();
		automaton.addTransition(a, b, new TransitionData("1",null));
		automaton.addTransition(a, c, new TransitionData("2",null));
		automaton.addTransition(b, c, new TransitionData("2",null));
		automaton.addTransition(d, e, new TransitionData("1",null));
		automaton.addTransition(d, f, new TransitionData("2",null));
		automaton.addTransition(e, f, new TransitionData("2",null));
		m = new PayloadMachine();
		m.setAutomaton(automaton);
		
		OrderedStatePair osp = new OrderedStatePair(a,d);
        ComputeScore kts = new KTailsScorecomputer(0);
        SimpleMergingState sms = new SimpleMergingState(m);
        kts = kts.newInstance(sms,osp);
		Score score = kts.recurseScore(osp);
		assertEquals(2,score.getPrimaryScore());
	}
	
	@Test
	public void kTailSimpleTest1() {
		TraceDFA automaton = new TraceDFA();
		
		int a = automaton.addState();
		int b = automaton.addState();
		int c = automaton.addState();
		int d = automaton.addState();
		automaton.addTransition(a, b, new TransitionData("1",null));
		automaton.addTransition(c, d, new TransitionData("1",null));
		m = new PayloadMachine();
		m.setAutomaton(automaton);
		
		OrderedStatePair osp = new OrderedStatePair(a,c);
        ComputeScore kts = new KTailsScorecomputer(0);
        SimpleMergingState sms = new SimpleMergingState(m);
        kts = kts.newInstance(sms,osp);
        Score score = kts.recurseScore(osp);
		assertEquals(1,score.getPrimaryScore());
	}
	
	@Test
	public void kTailSimpleTest2() {
		TraceDFA automaton = new TraceDFA();
		
		int a = automaton.addState();
		int b = automaton.addState();
		int c = automaton.addState();
		int d = automaton.addState();
		int e = automaton.addState();
		automaton.addState();
		automaton.addTransition(a, b, new TransitionData("1",null));
		automaton.addTransition(b, c, new TransitionData("2",null));
		automaton.addTransition(b, a, new TransitionData("1",null));
		automaton.addTransition(c, d, new TransitionData("1",null));
		automaton.addTransition(d, e, new TransitionData("2",null));
		automaton.addTransition(d, c, new TransitionData("1",null));
		automaton.addTransition(e, c, new TransitionData("2",null));
		automaton.addTransition(c, a, new TransitionData("2",null));
		m = new PayloadMachine();
		m.setAutomaton(automaton);
		
		OrderedStatePair osp = new OrderedStatePair(a,c);
        ComputeScore kts = new KTailsScorecomputer(0);
        SimpleMergingState sms = new SimpleMergingState(m);
        kts = kts.newInstance(sms,osp);
        Score score = kts.recurseScore(osp);
		assertEquals(2,score.getPrimaryScore());
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
		
		
		RedBlueMergingState ms = new RedBlueMergingState(tree);
		OrderedStatePair os = new OrderedStatePair(1,6);
		int score = rbs.getScore(ms, os);
		System.out.println(score);
		

	}
	
	
}
