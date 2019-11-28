package mint.inference.gp.tree;

import mint.inference.gp.tree.nonterminals.booleans.AndBooleanOperator;
import mint.inference.gp.tree.nonterminals.booleans.EQBooleanOperator;
import mint.inference.gp.tree.nonterminals.booleans.EQStringOperator;
import mint.inference.gp.tree.nonterminals.booleans.GTBooleanDoublesOperator;
import mint.inference.gp.tree.nonterminals.booleans.GTBooleanIntegersOperator;
import mint.inference.gp.tree.nonterminals.booleans.LTBooleanDoublesOperator;
import mint.inference.gp.tree.nonterminals.booleans.LTBooleanIntegersOperator;
import mint.inference.gp.tree.nonterminals.booleans.NotBooleanOperator;
import mint.inference.gp.tree.nonterminals.booleans.OrBooleanOperator;
import mint.inference.gp.tree.nonterminals.doubles.AddDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.CastDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.CosDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.DivideDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.ExpDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.IfThenElseOperator;
import mint.inference.gp.tree.nonterminals.doubles.LogDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.MultiplyDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.PwrDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.SubtractDoublesOperator;
import mint.inference.gp.tree.nonterminals.integers.CastIntegersOperator;
import mint.inference.gp.tree.nonterminals.integers.IfThenElseIntegerOperator;
import mint.inference.gp.tree.nonterminals.integers.IntegerNonTerminal;
import mint.inference.gp.tree.nonterminals.lists.RootListNonTerminal;
import mint.inference.gp.tree.nonterminals.strings.StringNonTerminal;
import mint.inference.gp.tree.terminals.BooleanVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.DoubleVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.IntegerVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.StringVariableAssignmentTerminal;

/**
 * Created by neilwalkinshaw on 27/05/15.
 */
public interface NodeVisitor {

	boolean visitEnter(EQBooleanOperator eqBooleanOperator);

	boolean visitExit(EQBooleanOperator eqBooleanOperator) throws InterruptedException;

	boolean visitEnter(AndBooleanOperator andBooleanOperator);

	boolean visitExit(AndBooleanOperator andBooleanOperator);

	boolean visitEnter(GTBooleanDoublesOperator gtBooleanDoublesOperator);

	boolean visitExit(GTBooleanDoublesOperator gtBooleanDoublesOperator);

	boolean visitEnter(LTBooleanDoublesOperator ltBooleanDoublesOperator);

	boolean visitExit(LTBooleanDoublesOperator ltBooleanDoublesOperator);

	boolean visitEnter(OrBooleanOperator orBooleanOperator);

	boolean visitExit(OrBooleanOperator orBooleanOperator);

	boolean visitEnter(CastIntegersOperator integerNonTerminal);

	boolean visitExit(CastIntegersOperator integerNonTerminal);

	boolean visitEnter(IntegerNonTerminal integerNonTerminal);

	boolean visitExit(IntegerNonTerminal integerNonTerminal);

	boolean visitEnter(StringNonTerminal stringNonTerminal);

	boolean visitExit(StringNonTerminal stringNonTerminal);

	boolean visitEnter(IfThenElseOperator ifThenElseOperator);

	boolean visitExit(IfThenElseOperator ifThenElseOperator);

	boolean visitEnter(DivideDoublesOperator divideDoublesOperator);

	boolean visitExit(DivideDoublesOperator divideDoublesOperator);

	boolean visitEnter(AddDoublesOperator addDoublesOperator);

	boolean visitExit(AddDoublesOperator addDoublesOperator);

	boolean visitEnter(CastDoublesOperator castDoublesOperator);

	boolean visitExit(CastDoublesOperator castDoublesOperator);

	boolean visitEnter(CosDoublesOperator cosDoublesOperator);

	boolean visitExit(CosDoublesOperator cosDoublesOperator);

	boolean visitEnter(LogDoublesOperator cosDoublesOperator);

	boolean visitExit(LogDoublesOperator cosDoublesOperator);

	boolean visitEnter(MultiplyDoublesOperator multiplyDoublesOperator);

	boolean visitExit(MultiplyDoublesOperator multiplyDoublesOperator);

	boolean visitEnter(PwrDoublesOperator pwrDoublesOperator);

	boolean visitExit(PwrDoublesOperator pwrDoublesOperator);

	boolean visitEnter(ExpDoublesOperator expDoublesOperator);

	boolean visitExit(ExpDoublesOperator expDoublesOperator);

	boolean visitEnter(SubtractDoublesOperator subtractDoublesOperator);

	boolean visitExit(SubtractDoublesOperator subtractDoublesOperator);

	boolean visitEnter(BooleanVariableAssignmentTerminal booleanVariableAssignmentTerminal);

	boolean visitExit(BooleanVariableAssignmentTerminal booleanVariableAssignmentTerminal);

	void visitEnter(DoubleVariableAssignmentTerminal doubleVariableAssignmentTerminal);

	boolean visitExit(DoubleVariableAssignmentTerminal doubleVariableAssignmentTerminal);

	void visitEnter(StringVariableAssignmentTerminal stringVariableAssignmentTerminal);

	boolean visitExit(StringVariableAssignmentTerminal stringVariableAssignmentTerminal);

	void visitEnter(IntegerVariableAssignmentTerminal integerVariableAssignmentTerminal);

	boolean visitExit(IntegerVariableAssignmentTerminal integerVariableAssignmentTerminal);

	boolean visitEnter(EQStringOperator eqStringOperator);

	boolean visitExit(EQStringOperator eqStringOperator);

	boolean visitEnter(RootListNonTerminal rootListNonTerminal);

	boolean visitExit(RootListNonTerminal rootListNonTerminal);

	boolean visitEnter(IfThenElseIntegerOperator ifThenElseIntegerOperator);

	boolean visitExit(IfThenElseIntegerOperator ifThenElseIntegerOperator);

	boolean visitEnter(GTBooleanIntegersOperator gtBooleanIntegersOperator);

	boolean visitExit(GTBooleanIntegersOperator gtBooleanIntegersOperator);

	boolean visitEnter(LTBooleanIntegersOperator ltBooleanIntegersOperator);

	boolean visitExit(LTBooleanIntegersOperator ltBooleanIntegersOperator);

	boolean visitEnter(NotBooleanOperator notBooleanOperator);

	boolean visitExit(NotBooleanOperator notBooleanOperator);

}
