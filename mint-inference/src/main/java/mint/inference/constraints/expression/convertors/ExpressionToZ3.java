/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2014 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package mint.inference.constraints.expression.convertors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Model;
import com.microsoft.z3.RatNum;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;
import com.microsoft.z3.enumerations.Z3_lbool;

import mint.inference.constraints.expression.Atom;
import mint.inference.constraints.expression.Compound;
import mint.inference.constraints.expression.Expression;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;

public class ExpressionToZ3 {

	private final static Logger LOGGER = Logger.getLogger(ExpressionToZ3.class.getName());
	private static int counter = 0;
	private int id = 0;
	protected Expression target;
	protected final boolean respectLimits;
	protected Map<String, VariableAssignment<?>> vars;
	private Map<String, Integer> miniHash;
	private Map<Integer, String> unHash;
	private Map<String, Expr> variables;
	private Context ctx;
	private BoolExpr current;

	public ExpressionToZ3(Expression t, boolean respectLimits) {
		id = counter++;
		HashMap<String, String> cfg = new HashMap<String, String>();
		this.respectLimits = respectLimits;
		cfg.put("model", "true");
		try {
			ctx = new Context(cfg);
		} catch (Z3Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		variables = new HashMap<String, Expr>();
		miniHash = new HashMap<String, Integer>();
		unHash = new HashMap<Integer, String>();
		vars = new HashMap<String, VariableAssignment<?>>();
		current = add(t);

	}

	private BoolExpr add(Expression e) {
		BoolExpr toReturn = null;
		if (e instanceof Compound) {
			Compound c = (Compound) e;
			toReturn = add(c);
		} else {
			Atom a = (Atom) e;
			toReturn = add(a);
		}
		if (e.isNegated()) {
			toReturn = ctx.mkNot(toReturn);
		}
		return toReturn;
	}

	private BoolExpr add(Compound e) {
		List<BoolExpr> exprs = new ArrayList<BoolExpr>();

		for (Expression expression : e.getExps()) {
			exprs.add(add(expression));
		}

		if (e.getRel() == Compound.Rel.AND)
			return ctx.mkAnd(exprs.toArray(new BoolExpr[exprs.size()]));
		else
			return ctx.mkOr(exprs.toArray(new BoolExpr[exprs.size()]));
	}

	private BoolExpr add(Atom e) {
		VariableAssignment<?> var = e.getVariableAssignment();
		vars.put(var.getName(), var);
		String varName = var.getName();
		BoolExpr ret = ctx.mkTrue();
		if (var instanceof BooleanVariableAssignment) {
			BooleanVariableAssignment converted = (BooleanVariableAssignment) var;
			Expr z3Var = getBoolean(varName);
			Expr boolVal = ctx.mkBool(converted.getValue());
			switch (e.getR()) {
			case NEQ:
				ret = ctx.mkNot(ctx.mkEq(z3Var, boolVal));
				break;
			case EQ:
				ret = ctx.mkEq(z3Var, boolVal);
				break;
			default:
				LOGGER.error("GEQ / LEQ / GT / LT for Booleans not supported");
				ret = ctx.mkEq(z3Var, boolVal);
				break;
			}
		} else if (var instanceof DoubleVariableAssignment || var instanceof IntegerVariableAssignment
				|| var instanceof StringVariableAssignment) {

			ArithExpr z3Var = null;
			ArithExpr doubVal = null;
			if (var instanceof DoubleVariableAssignment) {
				DoubleVariableAssignment converted = (DoubleVariableAssignment) var;
				z3Var = (ArithExpr) getReal(varName);
				doubVal = ctx.mkReal(converted.getValue().toString());
				if (this.respectLimits) {
					ret = ctx.mkAnd(new BoolExpr[] { ret, ctx.mkLe(z3Var, ctx.mkReal(converted.getMax().toString())),
							ctx.mkGe(z3Var, ctx.mkReal(converted.getMin().toString())) });
				}
			} else if (var instanceof IntegerVariableAssignment) {
				IntegerVariableAssignment converted = (IntegerVariableAssignment) var;
				z3Var = (ArithExpr) getScalar(varName);
				doubVal = ctx.mkInt(converted.getValue().toString());
				if (this.respectLimits) {
					ret = ctx.mkAnd(new BoolExpr[] { ret, ctx.mkLe(z3Var, ctx.mkInt(converted.getMax().toString())),
							ctx.mkGe(z3Var, ctx.mkInt(converted.getMin().toString())) });
				}
			} else if (var instanceof StringVariableAssignment) {
				StringVariableAssignment converted = (StringVariableAssignment) var;
				z3Var = (ArithExpr) getScalar(varName);
				Integer h = hash(converted.getValue());
				doubVal = ctx.mkInt(h);
			}
			switch (e.getR()) {
			case NEQ:
				ret = ctx.mkAnd(new BoolExpr[] { ret, ctx.mkNot(ctx.mkEq(z3Var, doubVal)) });
				break;
			case EQ:
				ret = ctx.mkAnd(new BoolExpr[] { ret, ctx.mkEq(z3Var, doubVal) });
				break;
			case GT:
				ret = ctx.mkAnd(new BoolExpr[] { ret, ctx.mkGt(z3Var, doubVal) });
				break;
			case LT:
				ret = ctx.mkAnd(new BoolExpr[] { ret, ctx.mkLt(z3Var, doubVal) });
				break;
			case GEQ:
				ret = ctx.mkAnd(new BoolExpr[] { ret, ctx.mkGe(z3Var, doubVal) });
				break;
			case LEQ:
				ret = ctx.mkAnd(new BoolExpr[] { ret, ctx.mkLe(z3Var, doubVal) });
				break;
			default:
				LOGGER.error("Unexpected relation for Double Z3 constraint");
				ret = ctx.mkEq(z3Var, doubVal);
				break;
			}
		}
		return ret;
	}

	private int hash(String s) {
		if (miniHash.get(s) == null) {
			int hash = miniHash.size() + 1;
			miniHash.put(s, hash);
			unHash.put(hash, s);
		}
		return miniHash.get(s);
	}

	private String unHash(Integer i) {
		return unHash.get(i);
	}

	private Expr getScalar(String var1) {
		ArithExpr var = null;
		if (variables.containsKey(var1))
			var = (ArithExpr) variables.get(var1);
		else {
			var = (ArithExpr) ctx.mkConst(ctx.mkSymbol(var1), ctx.mkIntSort());
			variables.put(var1, var);
		}
		return var;
	}

	private Expr getReal(String var1) {
		ArithExpr var = null;
		if (variables.containsKey(var1))
			var = (ArithExpr) variables.get(var1);
		else {
			var = (ArithExpr) ctx.mkConst(ctx.mkSymbol(var1), ctx.mkRealSort());
			variables.put(var1, var);
		}
		return var;
	}

	private Expr getBoolean(String var1) {
		BoolExpr var = null;
		if (variables.containsKey(var1))
			var = (BoolExpr) variables.get(var1);
		else {
			var = (BoolExpr) ctx.mkConst(ctx.mkSymbol(var1), ctx.mkBoolSort());
			variables.put(var1, var);
		}
		return var;
	}

	public Collection<VariableAssignment<?>> getVars() {
		return vars.values();
	}

	public void negate() {
		current = ctx.mkNot(current);
	}

	public boolean solve(boolean avoidInFuture) {
		Solver s = ctx.mkSolver();
		s.add(current);
		Status stat = s.check();
		if (stat == Status.SATISFIABLE) {
			// LOGGER.debug("SATISFIED: "+current);
			populateModel(s.getModel());
			if (avoidInFuture)
				addToCtx(s.getModel());
		} else {
			LOGGER.debug("NOT SATISFIED: " + current);
		}
		return stat == Status.SATISFIABLE;
	}

	private void addToCtx(Model model) {
		BoolExpr[] negs = new BoolExpr[variables.values().size()];
		int i = 0;
		for (Expr v : variables.values()) {
			Expr val = null;
			if (v instanceof BoolExpr) {
				BoolExpr be = (BoolExpr) v;
				negs[i] = ctx.mkNot(be);
			} else {
				val = model.getConstInterp(v);
				negs[i] = ctx.mkNot(ctx.mkEq(v, val));
			}
			i++;
		}
		if (negs.length > 1)
			current = ctx.mkAnd(new BoolExpr[] { current, ctx.mkOr(negs) }); // Replace with ctx.mkOr?
		else
			current = ctx.mkAnd(new BoolExpr[] { current, negs[0] });
	}

	private void populateModel(Model model) {

		String value = null;
		for (FuncDecl fd : model.getConstDecls()) {
			VariableAssignment<?> v = vars.get(fd.getName().toString());
			Expr interp = model.getConstInterp(fd);
			if (interp instanceof RatNum) {
				RatNum rn = (RatNum) interp;
				Double numerator = rn.getNumerator().getBigInteger().doubleValue();
				Double denominator = rn.getDenominator().getBigInteger().doubleValue();
				// System.out.println(fd.Name().toString()+": "+numerator / denominator);
				Double val = numerator / denominator;
				value = val.toString();
			} else if (interp instanceof IntNum) { // could be a string too...
				IntNum in = (IntNum) interp;
				Integer val = in.getInt();
				if (v instanceof StringVariableAssignment)
					value = unHash(val);
				else
					value = val.toString();
			} else if (interp instanceof BoolExpr) {
				BoolExpr be = (BoolExpr) interp;
				if (be.getBoolValue() == Z3_lbool.Z3_L_TRUE)
					value = "true";
				else
					value = "false";
			}
			v.setStringValue(value);
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExpressionToZ3 other = (ExpressionToZ3) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
