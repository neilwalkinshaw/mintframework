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

import com.microsoft.z3.*;
import com.microsoft.z3.enumerations.Z3_lbool;
import org.apache.log4j.Logger;
import mint.inference.constraints.expression.Atom;
import mint.inference.constraints.expression.Compound;
import mint.inference.constraints.expression.Expression;
import mint.tracedata.types.*;

import java.util.*;

public class ExpressionToZ3 {
	
	private final static Logger LOGGER = Logger.getLogger(ExpressionToZ3.class.getName());
	private static int counter = 0;
	private int id = 0;
	protected Expression target;
	protected final boolean respectLimits;
	protected Map<String,VariableAssignment<?>> vars;
	private Map<String,Integer> miniHash;
	private Map<Integer,String> unHash;
	private Map<String,Expr> variables;
	private Context ctx;
	private BoolExpr current;

	
	public ExpressionToZ3(Expression t, boolean respectLimits) throws Z3Exception{
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
        variables = new HashMap<String,Expr>();
		miniHash = new HashMap<String,Integer>();
		unHash = new HashMap<Integer,String>();
		vars = new HashMap<String,VariableAssignment<?>>();
		current = add(t);
		
	}
	
	private BoolExpr add(Expression e) throws Z3Exception{
		BoolExpr toReturn = null;
		if(e instanceof Compound){
			Compound c = (Compound) e;
			toReturn =  add(c);
		}
		else{
			Atom a = (Atom) e;
			toReturn = add(a);
		}
		if(e.isNegated()){
			toReturn = ctx.MkNot(toReturn);
		}
		return toReturn;
	}
	
	private BoolExpr add(Compound e) throws Z3Exception{
		List<BoolExpr> exprs = new ArrayList<BoolExpr>();
		
		for(Expression expression : e.getExps()){
			exprs.add(add(expression));
		}
		
		if(e.getRel() == Compound.Rel.AND)
			return ctx.MkAnd(exprs.toArray(new BoolExpr[exprs.size()]));
		else
			return ctx.MkOr(exprs.toArray(new BoolExpr[exprs.size()]));
	}
	
	private BoolExpr add(Atom e) throws Z3Exception{
		VariableAssignment<?> var = e.getVariableAssignment();
		vars.put(var.getName(), var);
		String varName = var.getName();
		BoolExpr ret = ctx.MkTrue();
		if(var instanceof BooleanVariableAssignment){
			BooleanVariableAssignment converted = (BooleanVariableAssignment) var;
			Expr z3Var = (BoolExpr)getBoolean(varName);
			Expr boolVal = ctx.MkBool(converted.getValue());
			switch (e.getR()) {
			case NEQ:
				ret = ctx.MkNot(ctx.MkEq(z3Var, boolVal));
				break;
			case EQ:
				ret = ctx.MkEq(z3Var, boolVal);
				break;
			default:
				LOGGER.error("GEQ / LEQ / GT / LT for Booleans not supported");
				ret = ctx.MkEq(z3Var, boolVal);
				break;
			}
		}
		else if(var instanceof DoubleVariableAssignment || var instanceof IntegerVariableAssignment ||
				var instanceof StringVariableAssignment){
			
			ArithExpr z3Var = null;
			ArithExpr doubVal = null;
			if(var instanceof DoubleVariableAssignment){
				DoubleVariableAssignment converted = (DoubleVariableAssignment) var;
				z3Var = (ArithExpr)getReal(varName);
				doubVal = ctx.MkReal(converted.getValue().toString());
				if(this.respectLimits){
					ret = ctx.MkAnd(new BoolExpr[]{ret,
							ctx.MkLe(z3Var, ctx.MkReal(converted.getMax().toString())),
							ctx.MkGe(z3Var, ctx.MkReal(converted.getMin().toString()))});
				}
			}
			else if(var instanceof IntegerVariableAssignment){
				IntegerVariableAssignment converted = (IntegerVariableAssignment) var;
				z3Var = (ArithExpr)getScalar(varName);
				doubVal = ctx.MkInt(converted.getValue().toString());
				if(this.respectLimits){
					ret = ctx.MkAnd(new BoolExpr[]{ret,
							ctx.MkLe(z3Var, ctx.MkInt(converted.getMax().toString())),
							ctx.MkGe(z3Var, ctx.MkInt(converted.getMin().toString()))});
				}
			}
			else if(var instanceof StringVariableAssignment){
				StringVariableAssignment converted = (StringVariableAssignment) var;
				z3Var = (ArithExpr)getScalar(varName);
				Integer h = hash(converted.getValue());
				doubVal = ctx.MkInt(h);
			}
			switch (e.getR()) {
			case NEQ:
				ret = ctx.MkAnd(new BoolExpr[]{ret,ctx.MkNot(ctx.MkEq(z3Var, doubVal))});
				break;
			case EQ:
				ret = ctx.MkAnd(new BoolExpr[]{ret,ctx.MkEq(z3Var, doubVal)});
				break;
			case GT:
				ret = ctx.MkAnd(new BoolExpr[]{ret,ctx.MkGt(z3Var, doubVal)});
				break;
			case LT:
				ret = ctx.MkAnd(new BoolExpr[]{ret,ctx.MkLt(z3Var, doubVal)});
				break;
			case GEQ:
				ret = ctx.MkAnd(new BoolExpr[]{ret,ctx.MkGe(z3Var, doubVal)});
				break;
			case LEQ:
				ret = ctx.MkAnd(new BoolExpr[]{ret,ctx.MkLe(z3Var, doubVal)});
				break;
			default:
				LOGGER.error("Unexpected relation for Double Z3 constraint");
				ret = ctx.MkEq(z3Var, doubVal);
				break;
			}
		}
		return ret;
	}
	
	
	private int hash(String s){
		if(miniHash.get(s) == null){
			int hash = miniHash.size()+1;
			miniHash.put(s, hash);
			unHash.put(hash, s);
		}
		return miniHash.get(s);
	}
	
	private String unHash(Integer i){
		return unHash.get(i);
	}
	
	private Expr getScalar(String var1) throws Z3Exception {
		ArithExpr var = null;
		if(variables.containsKey(var1))
			var =  (ArithExpr)variables.get(var1);
		else{
			var = (ArithExpr) ctx.MkConst(ctx.MkSymbol(var1),ctx.MkIntSort());
			variables.put(var1, var);
		}
		return var;
	}
	
	private Expr getReal(String var1) throws Z3Exception {
		ArithExpr var = null;
		if(variables.containsKey(var1))
			var =  (ArithExpr)variables.get(var1);
		else{
			var = (ArithExpr) ctx.MkConst(ctx.MkSymbol(var1),ctx.MkRealSort());
			variables.put(var1, var);
		}
		return var;
	}
	
	private Expr getBoolean(String var1) throws Z3Exception {
		BoolExpr var = null;
		if(variables.containsKey(var1))
			var =  (BoolExpr)variables.get(var1);
		else{
			var = (BoolExpr) ctx.MkConst(ctx.MkSymbol(var1),ctx.MkBoolSort());
			variables.put(var1, var);
		}
		return var;
	}
	
	
	public Collection<VariableAssignment<?>> getVars(){
		return vars.values();
	}
	
	public void negate() throws Z3Exception{
		current = ctx.MkNot(current);
	}
	
	public boolean solve(boolean avoidInFuture) throws Z3Exception{
		Solver s = ctx.MkSolver();
		s.Assert(current);
		Status stat = s.Check();
		if(stat == Status.SATISFIABLE){
			//LOGGER.debug("SATISFIED: "+current);
			populateModel(s.Model());
			if(avoidInFuture)
				addToCtx(s.Model());
		}
		else{
			LOGGER.debug("NOT SATISFIED: "+current);
		}
		return stat == Status.SATISFIABLE;
	}


	private void addToCtx(Model model) throws Z3Exception {
		BoolExpr[] negs = new BoolExpr[variables.values().size()];
		int i = 0;
		for(Expr v : variables.values()){
			Expr val = null;
			if(v instanceof BoolExpr){
				BoolExpr be = (BoolExpr) v;
				negs[i] = ctx.MkNot(be);
			}
			else {
				val = model.ConstInterp(v);
				negs[i]=ctx.MkNot(ctx.MkEq(v, val));
			}
			i++;
		}
		if(negs.length>1)
			current = ctx.MkAnd(new BoolExpr[]{current,ctx.MkOr(negs)}); //Replace with ctx.MkOr?
		else
			current = ctx.MkAnd(new BoolExpr[]{current,negs[0]});
	}
	

	private void populateModel(Model model) throws Z3Exception {
		
		String value = null;
		for(FuncDecl fd:model.ConstDecls()){
			VariableAssignment<?> v = vars.get(fd.Name().toString());
			Expr interp = model.ConstInterp(fd);
			if(interp instanceof RatNum){
				RatNum rn = (RatNum) interp;
				Double numerator = rn.Numerator().BigInteger().doubleValue();
				Double denominator = rn.Denominator().BigInteger().doubleValue();
				//System.out.println(fd.Name().toString()+": "+numerator / denominator);
				Double val = numerator / denominator;
				value = val.toString();
			}
			else if(interp instanceof IntNum){ //could be a string too...
				IntNum in = (IntNum)interp;
				Integer val = in.Int();
				if(v instanceof StringVariableAssignment)
					value = unHash(val);
				else
					value = val.toString();
			}
			else if(interp instanceof BoolExpr){
				BoolExpr be = (BoolExpr) interp;
				if(be.BoolValue() == Z3_lbool.Z3_L_TRUE)
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
