package mint.inference.gp;

import org.apache.log4j.Logger;
import mint.inference.evo.*;
import mint.inference.gp.selection.SingleOutputTournament;
import mint.inference.gp.tree.Node;
import mint.tracedata.types.*;

import java.util.List;
import java.util.Map;

/**
 * Created by neilwalkinshaw on 06/03/2018.
 */
public abstract class GP<T> extends AbstractEvo {

    protected Generator gen;
    private final static Logger LOGGER = Logger.getLogger(GP.class.getName());
    protected boolean mem_dist = false;
    protected Map<List<VariableAssignment<?>>, T> evals;
    protected Map<Node<?>,List<Double>> distances;

    /**
     * Takes as input a random program generator, a training set (a map from a list of input parameters to an output parameter)
     * and a configuration.
     *
     * @param gpConf
     */
    public GP(GPConfiguration gpConf) {
        super(gpConf);
    }

    public Map<List<VariableAssignment<?>>, T> getEvals() {
        return evals;
    }

    @Override
    protected AbstractIterator getIterator(Selection selection) {
        return new ListIterate(population, gpConf.getCrossOver(), gpConf.getMutation(), gen, gpConf.getDepth(), gen.getRandom(), selection.getElites());
    }

    @Override
    protected List<Chromosome> select(List<Chromosome> population, Selection selection) {
        assert(selection instanceof SingleOutputTournament);
        List<Chromosome> sel = selection.select(gpConf);
        SingleOutputTournament sot = (SingleOutputTournament) selection;
        if(mem_dist)
            distances = sot.getDistances();
        return sel;
    }

    @Override
    protected List<Chromosome> generatePopulation(int i) {
        String type = getType();
        List<Chromosome> population = null;
        if(type.equals("Double")) {
            population = gen.generateDoublePopulation(i, gpConf.getDepth());
        }
        else if(type.equals("Integer")){
            population = gen.generateIntegerPopulation(i, gpConf.getDepth());

        }
        else if(type.equals("String")) {
            population = gen.generateStringPopulation(i, gpConf.getDepth());
        }
        else if(type.equals("Boolean")){
            population = gen.generateBooleanPopulation(i, gpConf.getDepth());
        }
        else if(type.equals("List")){
            population = gen.generateListPopulation(i, gpConf.getDepth(), getTypeString());
        }
        else{
            LOGGER.error("Failed to generate population for undefined type.");
        }
        population.addAll(seeds);
        return population;
    }

    protected abstract String getType();

    private String getTypeString() {
        String typeString = "";
        ListVariableAssignment var = (ListVariableAssignment)evals.values().iterator().next();
        List val = var.getValue();
        for(int i = 0; i<val.size(); i++){
            Object element = val.get(i);
            if(element instanceof DoubleVariableAssignment){
                typeString+="d";
            }
            else if(element instanceof BooleanVariableAssignment){
                typeString +="b";

            }
            else typeString +="i";
        }
        return typeString;
    }


    public Map<Node<?>,List<Double>> getDistances(){
        return distances;
    }
}
