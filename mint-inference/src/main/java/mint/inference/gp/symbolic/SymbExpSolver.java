package mint.inference.gp.symbolic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Z3Exception;

/**
 * Created by neilwalkinshaw on 02/06/15.
 */
public class SymbExpSolver {

	protected Collection<BoolExpr> targets, constraints;
	protected Context ctx;
	private Map<String, ArithExpr> variables;

	private final static Logger LOGGER = Logger.getLogger(SymbExpSolver.class.getName());

	public SymbExpSolver(Collection<BoolExpr> targets, Context ctx, Map<String, ArithExpr> variables) {
		this.targets = targets;
		this.ctx = ctx;
		this.variables = variables;
		this.constraints = new HashSet<BoolExpr>();
	}

	public Context getCtx() {
		return ctx;
	}

	public Collection<BoolExpr> getTargets() {
		return targets;
	}

	public void addConstraint(BoolExpr c) {
		constraints.add(c);
	}

	public Collection<Model> solve(int numSols) {
		Collection<Model> models = new HashSet<Model>();
		if (targets.size() == 0)
			return models;
		int i = numSols / targets.size();
		Model model = null;
		BoolExpr[] targetArray = targets.toArray(new BoolExpr[targets.size()]);
		for (int j = 0; j < targetArray.length; j++) {
			BoolExpr be = targetArray[j];
			try {
				if (!constraints.isEmpty()) {
					List<BoolExpr> all = new ArrayList<BoolExpr>();
					all.add(be);
					all.addAll(constraints);
					be = ctx.mkAnd(all.toArray(new BoolExpr[all.size()]));
				}
				Solver s = ctx.mkSolver();
				// LOGGER.debug(be);
				s.add(be);
				for (int k = 0; k < i; k++) {
					LOGGER.debug(s.check().toString());
					String status = s.check().toString().trim();
					if (status.equals("UNSATISFIABLE"))
						continue;
					if (status.equals("UNKNOWN"))
						continue;
					model = s.getModel();
					LOGGER.debug(model.toString());
					models.add(model);
					List<BoolExpr> ors = new ArrayList<BoolExpr>();
					for (FuncDecl fd : model.getConstDecls()) {
						Expr interp = model.getConstInterp(fd);

						Expr var = variables.get(fd.getName().toString());
						if (var != null) {
							BoolExpr e = ctx.mkNot(ctx.mkEq(variables.get(fd.getName().toString()), interp));
							ors.add(e);
						}
					}
					ors.add(be);
					s.add(ctx.mkAnd(ors.toArray(new BoolExpr[ors.size()])));

					// LOGGER.debug(be +"\n-----------\n"+ model);
				}
			} catch (Z3Exception e) {
				// e.printStackTrace();
				LOGGER.error("Could not solve: " + be.toString());
			}
		}
		return models;
	}

}
