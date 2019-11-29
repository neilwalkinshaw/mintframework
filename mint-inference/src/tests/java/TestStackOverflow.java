import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import mint.inference.gp.tree.nonterminals.booleans.BooleanNonTerminal;
import mint.inference.gp.tree.nonterminals.booleans.EQBooleanOperator;
import mint.inference.gp.tree.nonterminals.doubles.IfThenElseOperator;
import mint.inference.gp.tree.nonterminals.doubles.SubtractDoublesOperator;
import mint.inference.gp.tree.terminals.DoubleVariableAssignmentTerminal;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.VariableAssignment;

public class TestStackOverflow {

	public static void main(String[] args) throws InterruptedException {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);

		VariableAssignment<Double> var = new DoubleVariableAssignment("i", 200D);
		VariableAssignment<Double> add = new DoubleVariableAssignment("j", 0D);
		VariableAssignment<Double> one = new DoubleVariableAssignment("1", 1D);

		DoubleVariableAssignmentTerminal varvar = new DoubleVariableAssignmentTerminal(var, true, false);
		DoubleVariableAssignmentTerminal addvar = new DoubleVariableAssignmentTerminal(add, true, false);
		DoubleVariableAssignmentTerminal incvar = new DoubleVariableAssignmentTerminal(one, false, false);

		SubtractDoublesOperator sub = new SubtractDoublesOperator(incvar, incvar);
		BooleanNonTerminal intvar = new EQBooleanOperator(addvar, sub);

		IfThenElseOperator ro = new IfThenElseOperator(intvar, varvar, incvar);
		VariableAssignment<?> result = ro.evaluate();
		System.out.println(ro);
		System.out.println(sub.evaluate());
		System.out.println(result);
		Double res = ((Double) sub.getChild(0).evaluate().getValue() - (Double) sub.getChild(1).evaluate().getValue());

	}

}
