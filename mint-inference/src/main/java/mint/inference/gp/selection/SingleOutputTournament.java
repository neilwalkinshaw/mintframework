package mint.inference.gp.selection;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections4.MultiValuedMap;

import mint.inference.evo.Chromosome;
import mint.inference.gp.fitness.singleOutput.SingleOutputBooleanFitness;
import mint.inference.gp.fitness.singleOutput.SingleOutputDoubleFitness;
import mint.inference.gp.fitness.singleOutput.SingleOutputFitness;
import mint.inference.gp.fitness.singleOutput.SingleOutputIntegerFitness;
import mint.inference.gp.fitness.singleOutput.SingleOutputListFitness;
import mint.inference.gp.fitness.singleOutput.SingleOutputStringFitness;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeComparator;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 25/06/15.
 */
public class SingleOutputTournament extends IOTournamentSelection<VariableAssignment<?>> {

	protected Map<Node<?>, List<Double>> distances = null;
	boolean mem_dist = false;

	public SingleOutputTournament(MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> evals,
			List<Chromosome> totalPopulation, int maxDepth, boolean mem_dist, Random rand) {
		super(evals, totalPopulation, maxDepth, rand);
		distances = new HashMap<Node<?>, List<Double>>();
		this.mem_dist = mem_dist;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public SingleOutputFitness<?> getFitness(Chromosome toEvaluateC) {
		{
			Node<?> toEvaluate = (Node<?>) toEvaluateC;
			if (toEvaluate.getType().equals("string"))
				return new SingleOutputStringFitness(evals, (Node<VariableAssignment<String>>) toEvaluate, maxDepth);
			else if (toEvaluate.getType().equals("double"))
				return new SingleOutputDoubleFitness(evals, (Node<VariableAssignment<Double>>) toEvaluate, maxDepth);
			else if (toEvaluate.getType().equals("integer"))
				return new SingleOutputIntegerFitness(evals, (Node<VariableAssignment<Integer>>) toEvaluate, maxDepth);
			else if (toEvaluate.getType().equals("List"))
				return new SingleOutputListFitness(evals, (Node<VariableAssignment<List>>) toEvaluate, maxDepth);
			else {
				assert (toEvaluate.getType().equals("boolean"));
				return new SingleOutputBooleanFitness(evals, (Node<VariableAssignment<Boolean>>) toEvaluate, maxDepth);
			}
		}
	}

	@Override
	protected Comparator<Chromosome> getComparator() {
		return new NodeComparator(this);
	}

	public Map<Node<?>, List<Double>> getDistances() {
		return distances;
	}
}
