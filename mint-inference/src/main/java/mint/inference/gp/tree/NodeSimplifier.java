package mint.inference.gp.tree;

import java.util.Arrays;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.RealExpr;
import com.microsoft.z3.SeqExpr;

import mint.inference.gp.tree.nonterminals.booleans.AndBooleanOperator;
import mint.inference.gp.tree.nonterminals.booleans.GTBooleanDoublesOperator;
import mint.inference.gp.tree.nonterminals.booleans.GTBooleanIntegersOperator;
import mint.inference.gp.tree.nonterminals.booleans.LTBooleanDoublesOperator;
import mint.inference.gp.tree.nonterminals.booleans.LTBooleanIntegersOperator;
import mint.inference.gp.tree.nonterminals.booleans.NotBooleanOperator;
import mint.inference.gp.tree.nonterminals.booleans.OrBooleanOperator;
import mint.inference.gp.tree.nonterminals.doubles.AddDoublesOperator;
import mint.inference.gp.tree.nonterminals.doubles.SubtractDoublesOperator;
import mint.inference.gp.tree.nonterminals.integers.AddIntegersOperator;
import mint.inference.gp.tree.nonterminals.integers.MultiplyIntegersOperator;
import mint.inference.gp.tree.nonterminals.integers.SubtractIntegersOperator;
import mint.inference.gp.tree.terminals.BooleanVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.DoubleVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.IntegerVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.StringVariableAssignmentTerminal;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.StringVariableAssignment;

public class NodeSimplifier {

	private static Node<?> makeBinary(Expr[] e, NonTerminal<?> f) {
		if (e.length == 2) {
			f.addChild(fromZ3(e[0]));
			f.addChild(fromZ3(e[1]).copy());
			return f;
		} else {
			NonTerminal<?> fPrime = (NonTerminal<?>) f.copy();
			fPrime.clearChildren();
			Node<?> right = makeBinary(Arrays.copyOfRange(e, 1, e.length), fPrime);
			f.addChild(fromZ3(e[0]));
			f.addChild(right.copy());
			return f;
		}
	}

	@SuppressWarnings("unchecked")
	public static Node<IntegerVariableAssignment> fromZ3(IntExpr exp) {
		if (exp.isAdd()) {
			return (Node<IntegerVariableAssignment>) makeBinary(exp.getArgs(), new AddIntegersOperator());
		}
		if (exp.isSub()) {
			return (Node<IntegerVariableAssignment>) makeBinary(exp.getArgs(), new SubtractIntegersOperator());
		}
		if (exp.isMul()) {
			return (Node<IntegerVariableAssignment>) makeBinary(exp.getArgs(), new MultiplyIntegersOperator());
		}
		if (exp.isIntNum()) {
			IntegerVariableAssignment num = new IntegerVariableAssignment(exp.toString(),
					Integer.valueOf(exp.toString()));
			return new IntegerVariableAssignmentTerminal(num, true, false);
		}
		if (exp.isConst()) {
			IntegerVariableAssignment num = new IntegerVariableAssignment(exp.toString().replace("latent", ""));
			return new IntegerVariableAssignmentTerminal(num, false, exp.toString().startsWith("latent"));
		}
		throw new IllegalArgumentException("Could not convert from Z3 expression " + exp);
	}

	@SuppressWarnings("unchecked")
	public static Node<DoubleVariableAssignment> fromZ3(RealExpr exp) {
		if (exp.isAdd()) {
			return (Node<DoubleVariableAssignment>) makeBinary(exp.getArgs(), new AddDoublesOperator());
		}
		if (exp.isSub()) {
			return (Node<DoubleVariableAssignment>) makeBinary(exp.getArgs(), new SubtractDoublesOperator());
		}
		if (exp.isMul() && Double.valueOf(exp.getArgs()[0].toString()) == -1 && exp.getArgs().length == 2) {
			DoubleVariableAssignment zero = new DoubleVariableAssignment("0", 0D);
			Node<DoubleVariableAssignment> c2 = fromZ3((RealExpr) exp.getArgs()[1]);
			return new SubtractDoublesOperator(new DoubleVariableAssignmentTerminal(zero, true, false), c2);
		}
		if (exp.isConst()) {
			DoubleVariableAssignment num = new DoubleVariableAssignment(exp.toString().replace("latent", ""));
			// This sets all "r" variable names to be latent
			return new DoubleVariableAssignmentTerminal(num, false, exp.toString().startsWith("latent"));
		}
		if (exp.isRatNum()) {
			DoubleVariableAssignment num = new DoubleVariableAssignment(exp.toString(), Double.valueOf(exp.toString()));
			return new DoubleVariableAssignmentTerminal(num, true, false);
		}
		throw new IllegalArgumentException("Could not convert from Z3 expression " + exp);
	}

	@SuppressWarnings("unchecked")
	public static Node<BooleanVariableAssignment> fromZ3(BoolExpr exp) {
		if (exp.isAnd()) {
			return (Node<BooleanVariableAssignment>) makeBinary(exp.getArgs(), new AndBooleanOperator());
		}
		if (exp.isOr()) {
			return (Node<BooleanVariableAssignment>) makeBinary(exp.getArgs(), new OrBooleanOperator());
		}
		if (exp.isNot()) {
			Node<?> child = fromZ3(exp.getArgs()[0]);
			// This simplifies double negation which might not always get dealt with because
			// of the GE and LE thing
			if (child instanceof NotBooleanOperator) {
				return (Node<BooleanVariableAssignment>) (((NotBooleanOperator) child).getChild(0));
			}
			return new NotBooleanOperator(fromZ3(exp.getArgs()[0]));
		}
		// x <= y == ¬ (x > y)
		if (exp.isLE()) {
			if (exp.getArgs()[0].isInt())
				return new NotBooleanOperator(
						new GTBooleanIntegersOperator((Node<IntegerVariableAssignment>) fromZ3(exp.getArgs()[0]),
								(Node<IntegerVariableAssignment>) fromZ3(exp.getArgs()[1])));
			else
				return new NotBooleanOperator(
						new GTBooleanDoublesOperator((Node<DoubleVariableAssignment>) fromZ3(exp.getArgs()[0]),
								(Node<DoubleVariableAssignment>) fromZ3(exp.getArgs()[1])));
		}
		if (exp.isLT()) {
			if (exp.getArgs()[0].isInt())
				return new LTBooleanIntegersOperator((Node<IntegerVariableAssignment>) fromZ3(exp.getArgs()[0]),
						(Node<IntegerVariableAssignment>) fromZ3(exp.getArgs()[1]));
			else
				return new LTBooleanDoublesOperator((Node<DoubleVariableAssignment>) fromZ3(exp.getArgs()[0]),
						(Node<DoubleVariableAssignment>) fromZ3(exp.getArgs()[1]));
		}
		// x >= y == ¬(x < y)
		if (exp.isGE()) {
			if (exp.getArgs()[0].isInt())
				return new NotBooleanOperator(
						new LTBooleanIntegersOperator((Node<IntegerVariableAssignment>) fromZ3(exp.getArgs()[0]),
								(Node<IntegerVariableAssignment>) fromZ3(exp.getArgs()[1])));
			else
				return new NotBooleanOperator(
						new LTBooleanDoublesOperator((Node<DoubleVariableAssignment>) fromZ3(exp.getArgs()[0]),
								(Node<DoubleVariableAssignment>) fromZ3(exp.getArgs()[1])));
		}
		if (exp.isGT()) {
			if (exp.getArgs()[0].isInt())
				return new GTBooleanIntegersOperator((Node<IntegerVariableAssignment>) fromZ3(exp.getArgs()[0]),
						(Node<IntegerVariableAssignment>) fromZ3(exp.getArgs()[1]));
			else
				return new GTBooleanDoublesOperator((Node<DoubleVariableAssignment>) fromZ3(exp.getArgs()[0]),
						(Node<DoubleVariableAssignment>) fromZ3(exp.getArgs()[1]));
		}
		if (exp.isConst()) {
			BooleanVariableAssignment num = new BooleanVariableAssignment(exp.toString(),
					Boolean.valueOf(exp.toString()));
			return new BooleanVariableAssignmentTerminal(num, true, false);
		}
		throw new IllegalArgumentException("Could not convert from Z3 expression " + exp);
	}

	public static Node<StringVariableAssignment> fromZ3(SeqExpr exp) {
		if (exp.isConst() && exp.getFuncDecl().getName().toString().equals("String")) {
			return new StringVariableAssignmentTerminal(exp.getString());
		}
		if (exp.isConst()) {
			String vname = exp.getFuncDecl().getName().toString().replace("latent", "");
			// This sets all "r" variable names to be latent
			return new StringVariableAssignmentTerminal(new StringVariableAssignment(vname), false,
					exp.getFuncDecl().getName().toString().startsWith("latent"));
		}
		throw new IllegalArgumentException("Could not convert from Z3 expression " + exp);
	}

	public static Node<?> fromZ3(Expr exp) {
		if (exp instanceof BoolExpr)
			return fromZ3((BoolExpr) exp);
		if (exp instanceof IntExpr)
			return fromZ3((IntExpr) exp);
		if (exp instanceof RealExpr)
			return fromZ3((RealExpr) exp);
		if (exp instanceof SeqExpr) {
			return fromZ3((SeqExpr) exp);
		}
		throw new IllegalArgumentException("Could not convert from Z3 expression " + exp);
	}

}
