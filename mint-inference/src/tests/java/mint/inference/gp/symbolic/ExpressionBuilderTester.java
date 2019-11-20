package mint.inference.gp.symbolic;

import org.junit.Before;
import org.junit.Test;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.nonterminals.booleans.BooleanNonTerminal;
import mint.inference.gp.tree.nonterminals.booleans.EQArithOperator;
import mint.inference.gp.tree.nonterminals.booleans.EQBooleanOperator;
import mint.inference.gp.tree.nonterminals.booleans.GTBooleanDoublesOperator;
import mint.inference.gp.tree.nonterminals.booleans.LTBooleanDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.AddDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.IfThenElseOperator;
import mint.inference.gp.tree.nonterminals.doubles.MultiplyDoublesOperator;
import mint.inference.gp.tree.terminals.DoubleVariableAssignmentTerminal;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.VariableAssignment;

public class ExpressionBuilderTester {

	static int constCounter = 0;

	@Before
	public void setUp() throws Exception {

	}

	/**
	 * y = read() y = 2 * y if(y == 12) fail; print ok
	 */
	@Test
	public void testWikipediaSymbexExample() {
		NonTerminal nt = new EQArithOperator(new MultiplyDoublesOperator(getConst(2D), getVar("y")), getConst(12D));
		NonTerminal ifN = new IfThenElseOperator(nt, getConst(5D), getConst(6D));
		ExpressionBuilder eb = new ExpressionBuilder(ifN, new Context());
		for (Expr e : eb.getTargets()) {
			System.out.println(e);
		}
	}

	@Test
	public void testEQIfThenElse() {
		VariableAssignment<Double> var = new DoubleVariableAssignment("i", 200D);
		VariableAssignment<Double> add = new DoubleVariableAssignment("j", 0D);
		VariableAssignment<Double> one = new DoubleVariableAssignment("one", 1D);

		DoubleVariableAssignmentTerminal varvar = new DoubleVariableAssignmentTerminal(var, true, false);
		DoubleVariableAssignmentTerminal j = new DoubleVariableAssignmentTerminal(add, false, false);
		DoubleVariableAssignmentTerminal oneV = new DoubleVariableAssignmentTerminal(one, true, false);

		BooleanNonTerminal intvar = new EQBooleanOperator(j, varvar);

		NonTerminal<DoubleVariableAssignment> adder = new AddDoublesOperator(j, oneV);

		IfThenElseOperator ro = new IfThenElseOperator(intvar, varvar, adder);
		ExpressionBuilder eb = new ExpressionBuilder(ro, new Context());
	}

	@Test
	public void testGTIfThenElse() {
		VariableAssignment<Double> var = new DoubleVariableAssignment("i", 200D);
		VariableAssignment<Double> add = new DoubleVariableAssignment("j", 0D);
		VariableAssignment<Double> one = new DoubleVariableAssignment("one", 1D);

		DoubleVariableAssignmentTerminal varvar = new DoubleVariableAssignmentTerminal(var, true, false);
		DoubleVariableAssignmentTerminal j = new DoubleVariableAssignmentTerminal(add, false, false);
		DoubleVariableAssignmentTerminal oneV = new DoubleVariableAssignmentTerminal(one, true, false);

		BooleanNonTerminal intvar = new GTBooleanDoublesOperator(j, varvar);

		NonTerminal<DoubleVariableAssignment> adder = new AddDoublesOperator(j, oneV);

		IfThenElseOperator ro = new IfThenElseOperator(intvar, varvar, adder);
		ExpressionBuilder eb = new ExpressionBuilder(ro, new Context());
	}

	@Test
	public void testLTIfThenElse() {
		VariableAssignment<Double> var = new DoubleVariableAssignment("i", 0D);
		VariableAssignment<Double> add = new DoubleVariableAssignment("j", 0D);
		VariableAssignment<Double> one = new DoubleVariableAssignment("one", 1D);

		DoubleVariableAssignmentTerminal varvar = new DoubleVariableAssignmentTerminal(var, true, false);
		DoubleVariableAssignmentTerminal j = new DoubleVariableAssignmentTerminal(add, false, false);
		DoubleVariableAssignmentTerminal oneV = new DoubleVariableAssignmentTerminal(one, true, false);

		BooleanNonTerminal intvar = new LTBooleanDoublesOperator(j, varvar);

		NonTerminal<DoubleVariableAssignment> adder = new AddDoublesOperator(j, oneV);

		IfThenElseOperator ro = new IfThenElseOperator(intvar, varvar, adder);
		ExpressionBuilder eb = new ExpressionBuilder(ro, new Context());
	}

	private Node<DoubleVariableAssignment> getConst(double val) {
		DoubleVariableAssignment resVar = new DoubleVariableAssignment("const" + constCounter++, val);
		DoubleVariableAssignmentTerminal term = new DoubleVariableAssignmentTerminal(resVar, true, false);
		return term;
	}

	private Node<DoubleVariableAssignment> getVar(String name) {
		DoubleVariableAssignment resVar = new DoubleVariableAssignment(name);
		DoubleVariableAssignmentTerminal term = new DoubleVariableAssignmentTerminal(resVar, false, false);
		return term;
	}

}
