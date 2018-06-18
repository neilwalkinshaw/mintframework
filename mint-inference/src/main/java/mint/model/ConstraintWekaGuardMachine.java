package mint.model;

import com.microsoft.z3.Z3Exception;
import mint.Configuration;
import mint.inference.constraints.expression.Atom;
import mint.inference.constraints.expression.Compound;
import mint.inference.constraints.expression.Expression;
import mint.inference.constraints.expression.convertors.ExpressionToZ3;
import mint.model.dfa.TransitionData;
import mint.tracedata.TraceElement;
import mint.tracedata.types.VariableAssignment;
import org.jgrapht.graph.DefaultEdge;

import java.util.Set;

/**
 * Created by neilwalkinshaw on 19/03/15.
 */
public class ConstraintWekaGuardMachine extends WekaGuardMachineDecorator {

    public ConstraintWekaGuardMachine(Machine decorated, boolean data) {
        super(decorated, data);
    }

    /**
     * Is the constraint on transition de compatible with the TraceElement current?
     * @param current
     * @param de
     * @return
     */
    public boolean compatible(TraceElement current, DefaultEdge de) {
        TransitionData<Set<TraceElement>> td = getAutomaton().getTransitionData(de);
        if(!current.getName().equals(td.getLabel()))
            return false;
        Expression constraints = getConstraint(de);
        if(constraints == null)
            return true;
        Set<VariableAssignment<?>> vars = current.getData();
        Compound fullEx = new Compound(Compound.Rel.AND);
        for(VariableAssignment<?> v : vars){
            Expression ex = new Atom(v,Atom.Rel.EQ);
            fullEx.add(ex);
        }
        fullEx.add(constraints);

        boolean solved = false;
        try {
            Configuration configuration = Configuration.getInstance();

            ExpressionToZ3 solver = new ExpressionToZ3(fullEx,configuration.RESPECT_LIMITS);
            solved = solver.solve(false);
        } catch (Z3Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return solved;
    }
}
