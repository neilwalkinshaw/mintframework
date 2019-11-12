package mint.inference.constraints;

import java.util.HashMap;
import java.util.Map;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.ArrayExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.RatNum;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

import daikon.inv.Invariant;
import daikon.inv.binary.BinaryInvariant;
import daikon.inv.binary.twoScalar.FloatEqual;
import daikon.inv.binary.twoScalar.FloatGreaterEqual;
import daikon.inv.binary.twoScalar.FloatGreaterThan;
import daikon.inv.binary.twoScalar.FloatLessEqual;
import daikon.inv.binary.twoScalar.FloatLessThan;
import daikon.inv.binary.twoScalar.FloatNonEqual;
import daikon.inv.binary.twoScalar.IntEqual;
import daikon.inv.binary.twoScalar.IntGreaterEqual;
import daikon.inv.binary.twoScalar.IntGreaterThan;
import daikon.inv.binary.twoScalar.IntLessEqual;
import daikon.inv.binary.twoScalar.IntLessThan;
import daikon.inv.binary.twoScalar.IntNonEqual;
import daikon.inv.binary.twoScalar.TwoFloat;
import daikon.inv.binary.twoScalar.TwoScalar;
import daikon.inv.binary.twoString.TwoString;
import daikon.inv.unary.scalar.LowerBound;
import daikon.inv.unary.scalar.LowerBoundFloat;
import daikon.inv.unary.scalar.NonZero;
import daikon.inv.unary.scalar.NonZeroFloat;
import daikon.inv.unary.scalar.OneOfFloat;
import daikon.inv.unary.scalar.OneOfScalar;
import daikon.inv.unary.scalar.SingleFloat;
import daikon.inv.unary.scalar.SingleScalar;
import daikon.inv.unary.scalar.UpperBound;
import daikon.inv.unary.scalar.UpperBoundFloat;
import daikon.inv.unary.string.OneOfString;
import daikon.inv.unary.string.SingleString;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;

public class InvariantsToZ3Constraints {

	Map<String, Integer> miniHash;
	Map<String, ArithExpr> variables;
	Context ctx;
	BoolExpr current;

	public InvariantsToZ3Constraints() {
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");
		ctx = new Context(cfg);
		variables = new HashMap<String, ArithExpr>();
		miniHash = new HashMap<String, Integer>();
	}

	private void addConstraint(BoolExpr e) {
		if (current == null)
			current = e;
		else
			current = ctx.mkAnd(new BoolExpr[] { current, e });
	}

	private int hash(String s) {
		if (miniHash.get(s) == null)
			miniHash.put(s, miniHash.size() + 1);
		return miniHash.get(s);
	}

	public void addInvariant(Invariant i) {
		if (i instanceof TwoFloat)
			addBinaryFloatConstraint((TwoFloat) i);
		else if (i instanceof TwoScalar)
			addBinaryScalarConstraint((TwoScalar) i);
		else if (i instanceof TwoString)
			addBinaryStringConstraint((TwoString) i);
		else if (i instanceof SingleFloat)
			addUnaryConstraint((SingleFloat) i);
		else if (i instanceof SingleScalar)
			addUnaryConstraint((SingleScalar) i);
		else if (i instanceof SingleString)
			addUnaryConstraint((SingleString) i);
	}

	public void addBinaryFloatConstraint(TwoFloat inv) {
		ArithExpr var1 = getReal(inv.var1().java_name());
		ArithExpr var2 = getReal(inv.var2().java_name());
		addFunction(inv, var1, var2);
	}

	public void addBinaryScalarConstraint(TwoScalar inv) {
		ArithExpr var1 = getScalar(inv.var1().java_name());
		ArithExpr var2 = getScalar(inv.var2().java_name());
		addFunction(inv, var1, var2);
	}

	public void addBinaryStringConstraint(TwoString inv) {
		ArithExpr var1 = getScalar(inv.var1().java_name());
		ArithExpr var2 = getScalar(inv.var1().java_name());
		addFunction(inv, var1, var2);
	}

	public void addUnaryConstraint(SingleFloat inv) {
		ArithExpr var1 = getReal(inv.var().java_name());
		if (inv instanceof OneOfFloat) {
			OneOfFloat finv = (OneOfFloat) inv;
			addFunction(finv, var1);
		} else if (inv instanceof UpperBoundFloat) {
			UpperBoundFloat finv = (UpperBoundFloat) inv;
			addFunction(finv, var1);
		} else if (inv instanceof NonZeroFloat) {
			NonZeroFloat finv = (NonZeroFloat) inv;
			addFunction(finv, var1);
		} else if (inv instanceof LowerBoundFloat) {
			LowerBoundFloat finv = (LowerBoundFloat) inv;
			addFunction(finv, var1);
		}
	}

	private void addFunction(OneOfFloat inv, Expr var) {
		double[] elts = inv.getElts();
		ArrayExpr set = ctx.mkEmptySet(ctx.mkRealSort());
		for (int i = 0; i < inv.num_elts(); i++) {
			Double d = elts[i];
			RatNum dub = ctx.mkReal(d.toString());
			ctx.mkSetAdd(set, dub);
		}
		addConstraint(ctx.mkSetMembership(var, set));
	}

	private void addFunction(UpperBoundFloat inv, ArithExpr var) {
		Double max = inv.max();
		ArithExpr var1 = ctx.mkReal(max.toString());
		addConstraint(ctx.mkLe(var, var1));

	}

	private void addFunction(LowerBoundFloat inv, ArithExpr var) {
		Double max = inv.min();
		ArithExpr var1 = ctx.mkReal(max.toString());
		addConstraint(ctx.mkGe(var, var1));
	}

	private void addFunction(NonZeroFloat inv, ArithExpr var) {
		addConstraint(ctx.mkNot(ctx.mkEq(var, ctx.mkReal("0.0"))));
	}

	public void addUnaryConstraint(SingleScalar inv) {
		ArithExpr var1 = getScalar(inv.var().java_name());
		if (inv instanceof OneOfScalar) {
			OneOfScalar finv = (OneOfScalar) inv;
			addFunction(finv, var1);
		} else if (inv instanceof UpperBound) {
			UpperBound finv = (UpperBound) inv;
			addFunction(finv, var1);
		} else if (inv instanceof NonZero) {
			NonZero finv = (NonZero) inv;
			addFunction(finv, var1);
		} else if (inv instanceof LowerBound) {
			LowerBound finv = (LowerBound) inv;
			addFunction(finv, var1);
		}
	}

	private void addFunction(OneOfScalar inv, Expr var) {
		long[] elts = inv.getElts();
		ArrayExpr set = ctx.mkEmptySet(ctx.mkIntSort());
		for (int i = 0; i < inv.num_elts(); i++) {
			Long d = elts[i];
			IntNum dub = ctx.mkInt(d.toString());
			ctx.mkSetAdd(set, dub);
		}
		addConstraint(ctx.mkSetMembership(var, set));

	}

	private void addFunction(UpperBound inv, ArithExpr var) {
		Long max = inv.max();
		ArithExpr var1 = ctx.mkInt(max.toString());
		addConstraint(ctx.mkLe(var, var1));

	}

	private void addFunction(LowerBound inv, ArithExpr var) {
		Long min = inv.min();
		ArithExpr var1 = ctx.mkInt(min.toString());
		addConstraint(ctx.mkLe(var, var1));

	}

	private void addFunction(NonZero inv, ArithExpr var) {
		addConstraint(ctx.mkNot(ctx.mkEq(var, ctx.mkInt("0"))));

	}

	public void addUnaryConstraint(SingleString inv) {
		Expr varint = getScalar(inv.var().java_name());
		if (inv instanceof OneOfString) {
			OneOfString finv = (OneOfString) inv;
			addFunction(finv, varint);
		}
	}

	private void addFunction(OneOfString inv, Expr var) {
		String[] elts = inv.getElts();
		ArrayExpr set = ctx.mkEmptySet(ctx.mkIntSort());
		for (int i = 0; i < inv.num_elts(); i++) {
			String s = elts[i];
			if (s == null)
				continue;
			Integer h = hash(s);
			IntNum dub = ctx.mkInt(h.toString());
			ctx.mkSetAdd(set, dub);
		}
		addConstraint(ctx.mkSetMembership(var, set));

	}

	private void addFunction(BinaryInvariant inv, ArithExpr var1, ArithExpr var2) {
		BoolExpr expression = null;
		if (inv instanceof FloatEqual || inv instanceof IntEqual)
			expression = ctx.mkEq(var1, var2);
		else if (inv instanceof FloatGreaterEqual || inv instanceof IntGreaterEqual)
			expression = ctx.mkGe(var1, var2);
		else if (inv instanceof FloatGreaterThan || inv instanceof IntGreaterThan) {
			expression = ctx.mkGt(var1, var2);
		} else if (inv instanceof FloatLessEqual || inv instanceof IntLessEqual) {
			expression = ctx.mkLe(var1, var2);
		} else if (inv instanceof FloatLessThan || inv instanceof IntLessThan) {
			expression = ctx.mkLt(var1, var2);
		} else if (inv instanceof FloatNonEqual || inv instanceof IntNonEqual) {
			expression = ctx.mkNot(ctx.mkEq(var1, var2));
		}
		addConstraint(expression);
	}

	public void addVariableAssignment(String varName, Boolean i) {
		// TODO
	}

	public void addVariableAssignment(String varName, Double i) {
		ArithExpr var = getScalar(varName);
		addConstraint(ctx.mkEq(var, ctx.mkReal(i.toString())));
	}

	public void addVariableAssignment(String varName, String i) {
		ArithExpr var = getScalar(varName);
		Integer h = hash(i);
		addConstraint(ctx.mkEq(var, ctx.mkInt(h.toString())));
	}

	public void addVariableAssignment(VariableAssignment<?> va) {
		if (va instanceof BooleanVariableAssignment) {
			BooleanVariableAssignment b = (BooleanVariableAssignment) va;
			addVariableAssignment(va.getName(), b.getValue());
		} else if (va instanceof StringVariableAssignment) {
			StringVariableAssignment s = (StringVariableAssignment) va;
			addVariableAssignment(va.getName(), s.getValue());
		} else if (va instanceof DoubleVariableAssignment) {
			DoubleVariableAssignment d = (DoubleVariableAssignment) va;
			addVariableAssignment(va.getName(), d.getValue());
		}
	}

	public boolean solve() {
		Solver s = ctx.mkSolver();
		Status stat = s.check();
		return stat == Status.SATISFIABLE;
	}

	private ArithExpr getScalar(String var1) {
		ArithExpr var = null;
		if (variables.containsKey(var1))
			var = variables.get(var1);
		else {
			var = (ArithExpr) ctx.mkConst(ctx.mkSymbol(var1), ctx.mkIntSort());
			variables.put(var1, var);
		}
		return var;
	}

	private ArithExpr getReal(String var1) {
		ArithExpr var = null;
		if (variables.containsKey(var1))
			var = variables.get(var1);
		else {
			var = (ArithExpr) ctx.mkConst(ctx.mkSymbol(var1), ctx.mkRealSort());
			variables.put(var1, var);
		}
		return var;
	}

}
