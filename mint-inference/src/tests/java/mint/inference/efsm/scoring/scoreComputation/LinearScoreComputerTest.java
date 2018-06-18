package mint.inference.efsm.scoring.scoreComputation;

import junit.framework.Assert;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

public class LinearScoreComputerTest {
	


	@Before
	public void setUp() throws Exception {

	}


	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void bfsAnnotateTest() {
		TraceDFA automaton = new TraceDFA();

		int a = automaton.addState();
		int b = automaton.addState();
		int c = automaton.addState();
		int d = automaton.addState();
		automaton.addTransition(a, b, new TransitionData("a",null));
		automaton.addTransition(a, c, new TransitionData("b",null));
		automaton.addTransition(b, c, new TransitionData("a",null));
		automaton.addTransition(c, d, new TransitionData("a",null));
		automaton.setInitialState(a);
		Machine m = new PayloadMachine();
		m.setAutomaton(automaton);
		SimpleMergingState sms = new SimpleMergingState(m);
		LinearScoreComputer lsc = new LinearScoreComputer(sms,null, true, new HashMap<Integer, Integer>());
		lsc.computeStateDepths(sms);
		Assert.assertTrue(lsc.stateDepths.get(a)==0);
		Assert.assertTrue(lsc.stateDepths.get(b)==1);
		Assert.assertTrue(lsc.stateDepths.get(c)==1);
		Assert.assertTrue(lsc.stateDepths.get(d)==2);
	}
	
	
}
