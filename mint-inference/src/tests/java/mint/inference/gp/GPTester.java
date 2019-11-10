package mint.inference.gp;

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

import junit.framework.Assert;
import mint.inference.evo.GPConfiguration;
import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.nonterminals.booleans.AndBooleanOperator;
import mint.inference.gp.tree.nonterminals.booleans.BooleanNonTerminal;
import mint.inference.gp.tree.nonterminals.booleans.EQArithOperator;
import mint.inference.gp.tree.nonterminals.booleans.EQBooleanOperator;
import mint.inference.gp.tree.nonterminals.booleans.GTBooleanDoublesOperator;
import mint.inference.gp.tree.nonterminals.booleans.LTBooleanDoublesOperator;
import mint.inference.gp.tree.nonterminals.booleans.OrBooleanOperator;
import mint.inference.gp.tree.nonterminals.doubles.AddDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.CosDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.DivideDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.DoubleNonTerminal;
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
import mint.tracedata.types.VariableAssignment;

public class GPTester {

	@Before
	public void setUp() throws Exception {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}

	@Test
	public void testMedDepthAll() {
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
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("a"), false));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("b"), false));
		// doubleTerms.add(new DoubleVariableAssignmentTerminal(new
		// DoubleVariableAssignment("2",2.0), true));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("1", 1D), true));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("0", 0.5D), true));
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
		BooleanVariableAssignmentTerminal trueterm = new BooleanVariableAssignmentTerminal(truevar, true);
		VariableAssignment<Boolean> falsevar = new BooleanVariableAssignment("false", false);
		BooleanVariableAssignmentTerminal falseterm = new BooleanVariableAssignmentTerminal(falsevar, true);
		boolTerms.add(trueterm);
		boolTerms.add(falseterm);
		gpGenerator.setBooleanTerminals(boolTerms);

		SingleOutputGP gp = new SingleOutputGP(gpGenerator, generateTrainingSet(60),
				new GPConfiguration(60, 0.5, 0.4, 5, 7), false);

		System.out.println(gp.evolve(100));

	}

	private DoubleNonTerminal createConstrained(DoubleVariableAssignment var, DoubleNonTerminal dnt) {
		DoubleVariableAssignment dv = new DoubleVariableAssignment(var.getName(), var.getMin(), var.getMax());
		dnt.setResVar(dv);
		return dnt;
	}

	@Test
	public void testLimitedAll() {
		DoubleVariableAssignment constrained = new DoubleVariableAssignment("constrained", -1000D, 1000D);
		constrained.setEnforcing(true);
		Generator gpGenerator = new Generator(new Random(0));

		List<NonTerminal<?>> doubleNonTerms = new ArrayList<NonTerminal<?>>();
		doubleNonTerms.add(createConstrained(constrained, new AddDoublesOperator()));
		doubleNonTerms.add(createConstrained(constrained, new SubtractDoublesOperator()));
		doubleNonTerms.add(createConstrained(constrained, new MultiplyDoublesOperator()));
		doubleNonTerms.add(createConstrained(constrained, new DivideDoublesOperator()));
		doubleNonTerms.add(createConstrained(constrained, new PwrDoublesOperator()));
		doubleNonTerms.add(new IfThenElseOperator());
		// doubleNonTerms.add(createConstrained(constrained,new CosDoublesOperator()));
		doubleNonTerms.add(createConstrained(constrained, new ExpDoublesOperator()));
		doubleNonTerms.add(createConstrained(constrained, new LogDoublesOperator()));
		gpGenerator.setDoubleFunctions(doubleNonTerms);

		List<VariableTerminal<?>> doubleTerms = new ArrayList<VariableTerminal<?>>();
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("a"), false));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("b"), false));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("2", 2.0), true));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("1", 1D), true));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("0", 0D), true));
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
		BooleanVariableAssignmentTerminal trueterm = new BooleanVariableAssignmentTerminal(truevar, true);
		VariableAssignment<Boolean> falsevar = new BooleanVariableAssignment("false", false);
		BooleanVariableAssignmentTerminal falseterm = new BooleanVariableAssignmentTerminal(falsevar, true);
		boolTerms.add(trueterm);
		boolTerms.add(falseterm);
		gpGenerator.setBooleanTerminals(boolTerms);

		SingleOutputGP gp = new SingleOutputGP(gpGenerator, generateTrainingSet(100),
				new GPConfiguration(200, 0.9, 0.1, 5, 20), false);

		System.out.println(gp.evolve(400));

	}

	@Test
	public void testBooleanGP() {
		Generator gpGenerator = new Generator(new Random(0));

		List<NonTerminal<?>> doubleNonTerms = new ArrayList<NonTerminal<?>>();
		doubleNonTerms.add(new AddDoublesOperator());
		doubleNonTerms.add(new SubtractDoublesOperator());
		doubleNonTerms.add(new MultiplyDoublesOperator());
		doubleNonTerms.add(new IfThenElseOperator());
		// doubleNonTerms.add(new WriteAuxOperator());
		gpGenerator.setDoubleFunctions(doubleNonTerms);

		List<VariableTerminal<?>> doubleTerms = new ArrayList<VariableTerminal<?>>();
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("a"), false));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("b"), false));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("1", 1.0), true));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("0.5", 0.5), true));
		// doubleTerms.add(new ReadAuxTerminal());
		gpGenerator.setDoubleTerminals(doubleTerms);

		List<NonTerminal<?>> intNonTerms = new ArrayList<NonTerminal<?>>();
		// intNonTerms.add(new CastIntegersOperator());
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
		BooleanVariableAssignmentTerminal trueterm = new BooleanVariableAssignmentTerminal(truevar, true);
		VariableAssignment<Boolean> falsevar = new BooleanVariableAssignment("false", false);
		BooleanVariableAssignmentTerminal falseterm = new BooleanVariableAssignmentTerminal(falsevar, true);
		boolTerms.add(trueterm);
		boolTerms.add(falseterm);
		gpGenerator.setBooleanTerminals(boolTerms);

		SingleOutputGP gp = new SingleOutputGP(gpGenerator, generateBooleanTrainingSet(50),
				new GPConfiguration(100, 0.95, 0.05, 7, 10));

		System.out.println(gp.evolve(20));

	}

	private MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> generateTrainingSet(int size) {
		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> trainingSet = new HashSetValuedHashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();
		Random r = new Random(0);
		for (int i = 0; i < size; i++) {
			List<VariableAssignment<?>> inputs = new ArrayList();
			DoubleVariableAssignment a = new DoubleVariableAssignment("a", r.nextDouble());
			DoubleVariableAssignment b = new DoubleVariableAssignment("b", r.nextDouble());
			inputs.add(a);
			inputs.add(b);

			DoubleVariableAssignment c = new DoubleVariableAssignment("c", simpleFunction(a.getValue(), b.getValue()));
			trainingSet.put(inputs, c);
		}
		return trainingSet;
	}

	private MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> generateBooleanTrainingSet(int size) {
		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> trainingSet = new HashSetValuedHashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();
		Random r = new Random(0);
		for (int i = 0; i < size; i++) {
			List<VariableAssignment<?>> inputs = new ArrayList();
			DoubleVariableAssignment a = new DoubleVariableAssignment("a", r.nextDouble());
			DoubleVariableAssignment b = new DoubleVariableAssignment("b", r.nextDouble());
			inputs.add(a);
			inputs.add(b);
			BooleanVariableAssignment c = new BooleanVariableAssignment("c", (a.getValue() > b.getValue()));
			trainingSet.put(inputs, c);
		}
		return trainingSet;
	}

	@Test
	public void testIfThenElse() throws InterruptedException {
		VariableAssignment<Double> var = new DoubleVariableAssignment("i", 200D);
		VariableAssignment<Double> add = new DoubleVariableAssignment("j", 0D);
		VariableAssignment<Double> one = new DoubleVariableAssignment("one", 1D);

		DoubleVariableAssignmentTerminal varvar = new DoubleVariableAssignmentTerminal(var, true);
		DoubleVariableAssignmentTerminal addvar = new DoubleVariableAssignmentTerminal(add, true);
		DoubleVariableAssignmentTerminal incvar = new DoubleVariableAssignmentTerminal(one, false);

		BooleanNonTerminal intvar = new EQBooleanOperator(addvar, new SubtractDoublesOperator(incvar, incvar));

		NonTerminal<DoubleVariableAssignment> adder = new AddDoublesOperator(addvar, incvar);

		IfThenElseOperator ro = new IfThenElseOperator(intvar, varvar, incvar);
		VariableAssignment<?> result = ro.evaluate();
		Assert.assertEquals(result.toString(), "i=200.0");
	}

	private double simpleFunction(double a, double b) {
		return (3 * a + 2 * b + 1);
	}

}
