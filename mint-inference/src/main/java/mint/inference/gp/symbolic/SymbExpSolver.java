package mint.inference.gp.symbolic;

import com.microsoft.z3.*;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by neilwalkinshaw on 02/06/15.
 */
public class SymbExpSolver {

    protected Collection<BoolExpr> targets, constraints;
    protected Context ctx;
    private Map<String,ArithExpr> variables;


    private final static Logger LOGGER = Logger.getLogger(SymbExpSolver.class.getName());


    public SymbExpSolver(Collection<BoolExpr> targets, Context ctx, Map<String,ArithExpr> variables){
        this.targets = targets;
        this.ctx = ctx;
        this.variables = variables;
        this.constraints = new HashSet<BoolExpr>();
    }

    public Context getCtx(){
        return ctx;
    }

    public Collection<BoolExpr> getTargets() {
        return targets;
    }

    public void addConstraint(BoolExpr c){
        constraints.add(c);
    }

    public Collection<Model> solve(int numSols) {
        Collection<Model> models = new HashSet<Model>();
        if(targets.size() == 0)
            return models;
        int i = numSols / targets.size();
        Model model = null;
        BoolExpr[] targetArray = targets.toArray(new BoolExpr[targets.size()]);
        for (int j = 0; j < targetArray.length; j++) {
            BoolExpr be = targetArray[j];
            try {
                if(!constraints.isEmpty()){
                    List<BoolExpr> all = new ArrayList<BoolExpr>();
                    all.add(be);
                    all.addAll(constraints);
                    be = ctx.MkAnd(all.toArray(new BoolExpr[all.size()]));
                }
                Solver s = ctx.MkSolver();
                //LOGGER.debug(be);
                s.Assert(be);
                for(int k = 0; k<i; k++){
                    LOGGER.debug(s.Check().toString());
                    String status = s.Check().toString().trim();
                    if(status.equals("UNSATISFIABLE"))
                        continue;
                    if(status.equals("UNKNOWN"))
                        continue;
                    model = s.Model();
                    LOGGER.debug(model.toString());
                    models.add(model);
                    List<BoolExpr> ors = new ArrayList<BoolExpr>();
                    for (FuncDecl fd : model.ConstDecls()) {
                        Expr interp = model.ConstInterp(fd);

                        Expr var = variables.get(fd.Name().toString());
                        if(var!=null) {
                            BoolExpr e = ctx.MkNot(ctx.MkEq(variables.get(fd.Name().toString()), interp));
                            ors.add(e);
                        }
                    }
                    ors.add(be);
                    s.Assert(ctx.MkAnd(ors.toArray(new BoolExpr[ors.size()])));


                //LOGGER.debug(be +"\n-----------\n"+ model);
                }
            } catch (Z3Exception e) {
                //e.printStackTrace();
                 LOGGER.error("Could not solve: "+be.toString());
            }
        }
        return models;
    }

}
