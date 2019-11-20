package mint.inference.gp.tree.nonterminals.lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import mint.inference.evo.GPConfiguration;
import mint.inference.gp.Generator;
import mint.inference.gp.SingleOutputGP;
import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.nonterminals.booleans.AndBooleanOperator;
import mint.inference.gp.tree.nonterminals.booleans.EQArithOperator;
import mint.inference.gp.tree.nonterminals.booleans.EQBooleanOperator;
import mint.inference.gp.tree.nonterminals.booleans.GTBooleanDoublesOperator;
import mint.inference.gp.tree.nonterminals.booleans.LTBooleanDoublesOperator;
import mint.inference.gp.tree.nonterminals.booleans.OrBooleanOperator;
import mint.inference.gp.tree.nonterminals.doubles.AddDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.CosDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.DivideDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.ExpDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.IfThenElseOperator;
import mint.inference.gp.tree.nonterminals.doubles.LogDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.MultiplyDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.PwrDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.SubtractDoublesOperator;
import mint.inference.gp.tree.terminals.BooleanVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.DoubleVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.ListVariableAssignment;
import mint.tracedata.types.VariableAssignment;

public class RootDoubleListNonTerminalTester {

	@Before
	public void setUp() throws Exception {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}

	@Test
	public void testGPList() throws InterruptedException {
		Generator gpGenerator = new Generator(new Random(0));

		List<NonTerminal<?>> doubleNonTerms = new ArrayList<NonTerminal<?>>();
		doubleNonTerms.add(new AddDoublesOperator());
		doubleNonTerms.add(new SubtractDoublesOperator());
		doubleNonTerms.add(new MultiplyDoublesOperator());
		doubleNonTerms.add(new DivideDoublesOperator());
		doubleNonTerms.add(new PwrDoublesOperator());
		doubleNonTerms.add(new IfThenElseOperator());
		doubleNonTerms.add(new CosDoublesOperator());
		doubleNonTerms.add(new ExpDoublesOperator());
		doubleNonTerms.add(new LogDoublesOperator());
		gpGenerator.setDoubleFunctions(doubleNonTerms);

		List<VariableTerminal<?>> doubleTerms = new ArrayList<VariableTerminal<?>>();
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("a"), false, false));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("b"), false, false));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("2", 2.0), true, false));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("1", 1D), true, false));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("0", 0D), true, false));
		gpGenerator.setDoubleTerminals(doubleTerms);

		List<NonTerminal<?>> intNonTerms = new ArrayList<NonTerminal<?>>();
		gpGenerator.setIntegerFunctions(intNonTerms);

		List<NonTerminal<?>> boolNonTerms = new ArrayList<NonTerminal<?>>();
		boolNonTerms.add(new AndBooleanOperator());
		boolNonTerms.add(new OrBooleanOperator());
		boolNonTerms.add(new LTBooleanDoublesOperator());
		boolNonTerms.add(new GTBooleanDoublesOperator());
		boolNonTerms.add(new EQBooleanOperator());
		boolNonTerms.add(new EQArithOperator());
		gpGenerator.setBooleanFunctions(boolNonTerms);

		List<VariableTerminal<?>> boolTerms = new ArrayList<VariableTerminal<?>>();
		VariableAssignment<Boolean> truevar = new BooleanVariableAssignment("true", true);
		BooleanVariableAssignmentTerminal trueterm = new BooleanVariableAssignmentTerminal(truevar, true, false);
		VariableAssignment<Boolean> falsevar = new BooleanVariableAssignment("false", false);
		BooleanVariableAssignmentTerminal falseterm = new BooleanVariableAssignmentTerminal(falsevar, true, false);
		boolTerms.add(trueterm);
		boolTerms.add(falseterm);
		gpGenerator.setBooleanTerminals(boolTerms);

		SingleOutputGP gp = new SingleOutputGP(gpGenerator, generateTrainingSet(50),
				new GPConfiguration(100, 0.95, 0.05, 8, 7), false);

		System.out.println(gp.evolve(30));

	}

	private MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> generateTrainingSet(int size) {
		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> trainingSet = new HashSetValuedHashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();
		Random r = new Random(0);
		for (int i = 0; i < size; i++) {
			List<VariableAssignment<?>> inputs = new ArrayList<VariableAssignment<?>>();
			DoubleVariableAssignment a = new DoubleVariableAssignment("a", r.nextDouble());
			DoubleVariableAssignment b = new DoubleVariableAssignment("b", r.nextDouble());
			inputs.add(a);
			inputs.add(b);

			ListVariableAssignment c = new ListVariableAssignment("c", simpleFunction(a.getValue(), b.getValue()));
			trainingSet.put(inputs, c);
		}
		return trainingSet;
	}

	private List<Double> simpleFunction(Double value, Double value1) {
		List<Double> training = new ArrayList<Double>();
		for (int i = 0; i < 5; i++) {
			training.add((value + value1 + i));
		}
		return training;
	}

}
