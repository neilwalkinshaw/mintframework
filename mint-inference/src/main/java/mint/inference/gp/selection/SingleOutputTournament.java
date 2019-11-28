package mint.inference.gp.selection;

import mint.inference.evo.Chromosome;
import mint.inference.gp.fitness.*;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeComparator;
import mint.tracedata.types.VariableAssignment;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by neilwalkinshaw on 25/06/15.
 */
public class SingleOutputTournament extends IOTournamentSelection<VariableAssignment<?>> {

    protected Map<Node<?>,List<Double>> distances = null;
    boolean mem_dist = false;

    public SingleOutputTournament(Map<List<VariableAssignment<?>>, VariableAssignment<?>> evals, List<Chromosome> totalPopulation, int maxDepth, boolean mem_dist) {
        super(evals, totalPopulation, maxDepth);
        distances = new HashMap<Node<?>,List<Double>>();
        this.mem_dist = mem_dist;
    }

    @Override
    public SingleOutputFitness getFitness(Chromosome toEvaluateC) {
        {
            Node<?> toEvaluate = (Node<?>)toEvaluateC;
            if(toEvaluate.getType().equals("string"))
                return new SingleOutputStringFitness(evals,(Node<VariableAssignment<String>>)toEvaluate, maxDepth);
            else if(toEvaluate.getType().equals("double"))
                return new SingleOutputDoubleFitness(evals,(Node<VariableAssignment<Double>>)toEvaluate, maxDepth);
            else if(toEvaluate.getType().equals("integer"))
                return new SingleOutputIntegerFitness(evals,(Node<VariableAssignment<Integer>>)toEvaluate, maxDepth);
            else if(toEvaluate.getType().equals("List"))
                return new SingleOutputListFitness(evals,(Node<VariableAssignment<List>>)toEvaluate, maxDepth);
            else {
                assert(toEvaluate.getType().equals("boolean"));
                return new SingleOutputBooleanFitness(evals, (Node<VariableAssignment<Boolean>>) toEvaluate, maxDepth);
            }
            //else return new IOIntegerFitness(evals);
        }
    }

    @Override
    protected Comparator<Chromosome> getComparator() {
        return new NodeComparator(this);
    }

    @Override
    protected void processResult(Map<Future, Chromosome> solMap, Future<Double> sol, double score, Fitness fitness) {
        super.processResult(solMap, sol, score, fitness);
        SingleOutputFitness sof = (SingleOutputFitness) fitness;
        if(mem_dist)
            distances.put(sof.getIndividual(),sof.getDistances());
    }

    public Map<Node<?>, List<Double>> getDistances() {
        return distances;
    }
}
