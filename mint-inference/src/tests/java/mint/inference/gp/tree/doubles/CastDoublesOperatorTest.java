package mint.inference.gp.tree.doubles;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import mint.inference.gp.tree.nonterminals.doubles.CastDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.CosDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.ExpDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.LogDoublesOperator;
import mint.inference.gp.tree.nonterminals.integers.CastIntegersOperator;
import mint.inference.gp.tree.terminals.DoubleVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.IntegerVariableAssignmentTerminal;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.IntegerVariableAssignment;

public class CastDoublesOperatorTest {

	@Before
	public void setUp() throws Exception {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}

	@Test
	public void testCastDouble() throws InterruptedException {

		DoubleVariableAssignment iv = new DoubleVariableAssignment("x");
		iv.setParameter(false);
		iv.setStringValue("-1.0");
		DoubleVariableAssignmentTerminal ivt = new DoubleVariableAssignmentTerminal(iv, false, false);
		LogDoublesOperator ldo = new LogDoublesOperator();
		ldo.addChild(ivt);
		ExpDoublesOperator expo = new ExpDoublesOperator();
		expo.addChild(ldo);
		CastIntegersOperator cio = new CastIntegersOperator();
		cio.addChild(expo);
		CastDoublesOperator dio = new CastDoublesOperator();
		dio.addChild(cio);
		CosDoublesOperator cosio = new CosDoublesOperator();
		cosio.addChild(dio);
		System.out.println(cosio.evaluate());

	}

	@Test
	public void testCastInteger() throws InterruptedException {

		IntegerVariableAssignment iv = new IntegerVariableAssignment("x");
		iv.setParameter(false);
		iv.setStringValue("-200");
		IntegerVariableAssignmentTerminal ivt = new IntegerVariableAssignmentTerminal(iv, false, false);
		CastDoublesOperator cio = new CastDoublesOperator();
		cio.addChild(ivt);
		CastIntegersOperator dio = new CastIntegersOperator();
		dio.addChild(cio);
		System.out.println(cio.evaluate());

	}

}
