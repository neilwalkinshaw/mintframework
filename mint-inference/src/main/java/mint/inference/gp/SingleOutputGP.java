package mint.inference.gp;

import mint.inference.evo.Chromosome;
import mint.inference.evo.GPConfiguration;
import mint.inference.evo.Selection;
import mint.inference.gp.selection.SingleOutputTournament;
import mint.inference.gp.tree.Node;
import mint.tracedata.types.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by neilwalkinshaw on 06/03/15.
 */
public class SingleOutputGP extends GP<VariableAssignment<?>> {


    @Deprecated
    public SingleOutputGP(Generator gen, Map<List<VariableAssignment<?>>, VariableAssignment<?>> evals, GPConfiguration gpConf){
        super(gpConf);
        this.gen = gen;
        this.evals = evals;
        distances = new HashMap<Node<?>,List<Double>>();
    }

    public SingleOutputGP(Generator gen, Map<List<VariableAssignment<?>>, VariableAssignment<?>> evals, GPConfiguration gpConf, boolean memoriseDistances){
        super(gpConf);
        this.evals = evals;
        this.gen = gen;
        this.mem_dist = memoriseDistances;
        distances = new HashMap<Node<?>,List<Double>>();
    }


    @Override
    protected Selection buildSelection(List<Chromosome> population) {
        return new SingleOutputTournament(evals,population, gpConf.getDepth(),mem_dist);
    }


    protected String getType(){
        VariableAssignment<?> var = evals.values().iterator().next();
        if(var instanceof StringVariableAssignment)
            return "String";
        else if(var instanceof DoubleVariableAssignment)
            return "Double";
        else if(var instanceof IntegerVariableAssignment)
            return "Integer";
        else if(var instanceof BooleanVariableAssignment)
            return "Boolean";
        else
            return "List";
    }





}
