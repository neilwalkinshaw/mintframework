package mint.testgen.stateless.gp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.log4j.Logger;

import mint.inference.evo.AbstractEvo;
import mint.inference.evo.GPConfiguration;
import mint.inference.gp.Generator;
import mint.inference.gp.Normaliser;
import mint.inference.gp.SingleOutputGP;
import mint.inference.gp.tree.Node;
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
import mint.inference.gp.tree.nonterminals.doubles.MultiplyDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.SubtractDoublesOperator;
import mint.inference.gp.tree.nonterminals.integers.IfThenElseIntegerOperator;
import mint.inference.gp.tree.terminals.BooleanVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.DoubleVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.IntegerVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.testgen.stateless.TestGenerator;
import mint.testgen.stateless.gp.qbc.BooleanOutputQuerySelector;
import mint.testgen.stateless.gp.qbc.DoubleOutputQuerySelector;
import mint.testgen.stateless.gp.qbc.ListOutputQuerySelector;
import mint.testgen.stateless.gp.qbc.NGramOutputQuerySelector;
import mint.testgen.stateless.gp.qbc.QBC;
import mint.testgen.stateless.gp.qbc.QuerySelector;
import mint.testgen.stateless.runners.execution.TestRunner;
import mint.tracedata.TestIO;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.ListVariableAssignment;
import mint.tracedata.types.NumberVariableAssignment;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 02/06/15.
 */
public class GPTestRunner extends TestRunner {

	private final static Logger LOGGER = Logger.getLogger(GPTestRunner.class.getName());

	protected Node<?> lastRun = null;
	protected String label = "LBT";

	protected AbstractEvo latestGP = null;

	/**
	 *
	 */
	public GPTestRunner(String setupFile, String testPlan) {

		super(setupFile, testPlan);

	}

	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * Infers a GP tree, and generates a test set from it by symbolic execution.
	 */
	@Override
	public List<TestIO> generateTests() {
		List<TestIO> toInfer = null;
		try {
			//
			if (testInputs.isEmpty())
				LOGGER.error("No test inputs to infer from ...");
			QuerySelector selector = createQuerySelector();
			TestGenerator tester = new QBC(command, params, latestGP, testInputs, selector);

			// TestGenerator tester = new ClusteringQBC(command,params,latestGP,
			// testInputs);

			toInfer = tester.generateTestCases();

		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("FAILURE: " + e);

		}
		return toInfer;
	}

	protected QuerySelector createQuerySelector() {
		if (testOutputs.get(0).getVals().size() > 1) {
			return new ListOutputQuerySelector(params, command);
		} else {
			VariableAssignment<?> output = testOutputs.get(0).getVals().get(0);
			if (output instanceof NumberVariableAssignment)
				return new DoubleOutputQuerySelector(params, command);
			else if (output instanceof StringVariableAssignment)
				return new NGramOutputQuerySelector(params, command);
			else if (output instanceof BooleanVariableAssignment)
				return new BooleanOutputQuerySelector(params, command);
		}
		LOGGER.error("Cannot create tests for unrecognised output type.");
		return null;
	}

	private boolean isList(List<TestIO> outputs) {
		if (outputs.isEmpty())
			return false;
		TestIO firstElement = outputs.get(0);
		if (firstElement.getVals().size() > 1)
			return true;
		else
			return false;
	}

	protected Node<?> infer(List<TestIO> testInputs, List<TestIO> testOutputs) {
		LOGGER.debug("Inferring model");
		Normaliser normaliser = new Normaliser(testOutputs);
		testOutputs = normaliser.getNormalised();
//		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> trainingSet = new HashSetValuedHashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();

		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> evals;
		if (isList(testOutputs)) {
			evals = getMultiEvals(testInputs, testOutputs);
		} else {
			evals = getSingleEvals(testInputs, testOutputs);
		}

		Generator gpGenerator = new Generator(rand);

		List<NonTerminal<?>> doubleNonTerms = new ArrayList<NonTerminal<?>>();
		doubleNonTerms.add(new AddDoublesOperator());
		doubleNonTerms.add(new SubtractDoublesOperator());
		doubleNonTerms.add(new MultiplyDoublesOperator());
		// doubleNonTerms.add(new CastDoublesOperator());
		doubleNonTerms.add(new DivideDoublesOperator());
		// doubleNonTerms.add(new PwrDoublesOperator());
		// doubleNonTerms.add(new ForWhileOperator());
		doubleNonTerms.add(new IfThenElseOperator());
		// doubleNonTerms.add(new WriteAuxOperator());
		doubleNonTerms.add(new CosDoublesOperator());
		doubleNonTerms.add(new ExpDoublesOperator());
		// doubleNonTerms.add(new LogDoublesOperator());
		gpGenerator.setDoubleFunctions(doubleNonTerms);

		List<VariableTerminal<?>> doubleTerms = generateTerms(testInputs);
		// doubleTerms.add(new ReadAuxTerminal());
		gpGenerator.setDoubleTerminals(doubleTerms);

		List<VariableTerminal<?>> intTerms = generateIntTerms(testInputs);
		gpGenerator.setIntegerTerminals(intTerms);

		List<NonTerminal<?>> boolNonTerms = new ArrayList<NonTerminal<?>>();
		boolNonTerms.add(new AndBooleanOperator());
		boolNonTerms.add(new OrBooleanOperator());
		boolNonTerms.add(new LTBooleanDoublesOperator());
		boolNonTerms.add(new GTBooleanDoublesOperator());
		boolNonTerms.add(new EQBooleanOperator());
		boolNonTerms.add(new EQArithOperator());
		// boolNonTerms.add(new EQStringOperator());
		gpGenerator.setBooleanFunctions(boolNonTerms);

		List<NonTerminal<?>> intNonTerms = new ArrayList<NonTerminal<?>>();
		// intNonTerms.add(new CastIntegersOperator());
		intNonTerms.add(new IfThenElseIntegerOperator());
		gpGenerator.setIntegerFunctions(intNonTerms);

		List<VariableTerminal<?>> boolTerms = new ArrayList<VariableTerminal<?>>();
		VariableAssignment<Boolean> truevar = new BooleanVariableAssignment("truez", true);
		BooleanVariableAssignmentTerminal trueterm = new BooleanVariableAssignmentTerminal(truevar, true, false);
		VariableAssignment<Boolean> falsevar = new BooleanVariableAssignment("falsez", false);
		BooleanVariableAssignmentTerminal falseterm = new BooleanVariableAssignmentTerminal(falsevar, true, false);
		boolTerms.add(trueterm);
		boolTerms.add(falseterm);
		gpGenerator.setBooleanTerminals(boolTerms);

		gpGenerator.setListLength(listLength);

		// SingleOutputGP gp = new SingleOutputGP(gpGenerator, evals,new
		// GPConfiguration(300,0.9,0.1,8,4));
		// SingleOutputGP gp = new SingleOutputGP(gpGenerator, evals,new
		// GPConfiguration(600,0.9,0.1,5,10)); //DEFAULT
		// MultiOutputGP gp = new MultiOutputGP(gpGenerator, evals,new
		// GPConfiguration(600,0.8,0.2,15,6));
		SingleOutputGP gp = new SingleOutputGP(gpGenerator, evals, new GPConfiguration(150, 0.6, 0.4, 6, 7), true); // gauss

		/*
		 * if(lastRun!=null) { Collection<Chromosome> seed = new HashSet<Chromosome>();
		 * seed.add(lastRun); gp.setSeeds(seed); }
		 */
		Node<?> evolved = (Node<?>) gp.evolve(60);
		lastRun = evolved;
		latestGP = gp;
		return evolved;
	}

	private List<VariableTerminal<?>> generateTerms(List<TestIO> testInputs) {
		List<VariableTerminal<?>> doubleTerms = new ArrayList<VariableTerminal<?>>();
		TestIO input = testInputs.get(0);

		for (VariableAssignment<?> var : input.getVals()) {
			if (var.typeString().equals(":D")) {
				DoubleVariableAssignment dvar = new DoubleVariableAssignment(var.getName());
				dvar.setParameter(true);
				doubleTerms.add(new DoubleVariableAssignmentTerminal(dvar, false, false));
			}

		}
		DoubleVariableAssignment dvar = new DoubleVariableAssignment("randA", 10D);
		dvar.setParameter(false);
		dvar.setMax(20D);
		dvar.setMin(-20D);
		DoubleVariableAssignment dvar2 = new DoubleVariableAssignment("randB", 1D);
		dvar2.setParameter(false);
		dvar2.setMax(20D);
		dvar2.setMin(-20D);
		DoubleVariableAssignment dvar3 = new DoubleVariableAssignment("randC", 0.1D);
		dvar3.setParameter(false);
		dvar3.setMax(20D);
		dvar3.setMin(-20D);
		doubleTerms.add(new DoubleVariableAssignmentTerminal(dvar, true, false));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(dvar2, true, false));
		doubleTerms.add(new DoubleVariableAssignmentTerminal(dvar3, true, false));
		return doubleTerms;
	}

	private List<VariableTerminal<?>> generateIntTerms(List<TestIO> testInputs) {
		List<VariableTerminal<?>> intTerms = new ArrayList<VariableTerminal<?>>();
		/*
		 * TestIO input = testInputs.get(0);
		 * 
		 * for(VariableAssignment<?> var : input.getVals()){
		 * if(var.typeString().equals(":I")) { IntegerVariableAssignment iv = new
		 * IntegerVariableAssignment(var.getName()); iv.setParameter(true);
		 * intTerms.add(new IntegerVariableAssignmentTerminal(iv, false)); } }
		 * IntegerVariableAssignment dvar = new IntegerVariableAssignment("randA",-10);
		 * dvar.setParameter(false); dvar.setMax(20); dvar.setMin(-20);
		 * /*IntegerVariableAssignment dvar2 = new IntegerVariableAssignment("randB",0);
		 * dvar2.setParameter(false); dvar2.setMax(20); dvar2.setMin(-20);
		 * IntegerVariableAssignment dvar3 = new IntegerVariableAssignment("randC",10);
		 * dvar3.setParameter(false); dvar3.setMax(20); dvar3.setMin(-20);
		 * //doubleTerms.add(new DoubleVariableAssignmentTerminal(new
		 * DoubleVariableAssignment("0.1",0.1D), true));
		 */
		Map<String, Integer> consts = IntegerVariableAssignment.getConstMap();
		for (String i : IntegerVariableAssignment.getConstMap().keySet()) {
			IntegerVariableAssignment var = new IntegerVariableAssignment(i, consts.get(i));
			intTerms.add(new IntegerVariableAssignmentTerminal(var, true, false));
		}
		// intTerms.add(new IntegerVariableAssignmentTerminal(dvar2, true));
		// intTerms.add(new IntegerVariableAssignmentTerminal(dvar3, true));
		return intTerms;
	}

	private MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> getSingleEvals(List<TestIO> testInputs,
			List<TestIO> testOutputs) {
		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> io = new HashSetValuedHashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();
		double floor = 0D;
		for (TestIO output : testOutputs) {
			if (!output.isValid())
				continue;
			VariableAssignment<?> firstOutput = output.getVals().get(0);
			// If Double, need to find lowest value to calculate lower-bound for invalid
			// value.
			if (firstOutput.typeString().equals(":D")) {
				double out = (Double) output.getVals().get(0).getValue();
				if (out < floor)
					floor = out;
			}
		}
		Double invalid = floor - floor;
		for (int i = 0; i < testInputs.size(); i++) {
			TestIO output = testOutputs.get(i);
			if (output.isValid()) {
				io.put(testInputs.get(i).getVals(), testOutputs.get(i).getVals().get(0));
			} else {
				testOutputs.get(i).getVals().get(0).setStringValue(invalid.toString());
				io.put(testInputs.get(i).getVals(), testOutputs.get(i).getVals().get(0));
			}
		}
		return io;
	}

	private MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> getMultiEvals(List<TestIO> testInputs,
			List<TestIO> testOutputs) {
		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> io = new HashSetValuedHashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();
		for (int i = 0; i < testInputs.size(); i++) {
			TestIO output = testOutputs.get(i);

			ListVariableAssignment out = new ListVariableAssignment("output");
			out.setValue(output.getVals());
			io.put(testInputs.get(i).getVals(), out);

		}
		return io;
	}

}
