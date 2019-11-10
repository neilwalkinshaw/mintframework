package mint.inference.gp.selection;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.commons.collections4.MultiValuedMap;

import mint.inference.evo.Chromosome;
import mint.inference.gp.fitness.Fitness;
import mint.inference.gp.fitness.latentVariable.IntegerFitness;
import mint.inference.gp.fitness.latentVariable.LatentVariableFitness;
import mint.inference.gp.fitness.latentVariable.StringFitness;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeComparator;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 25/06/15.
 */
public class LatentVariableTournament extends IOTournamentSelection<VariableAssignment<?>> {

	protected Map<Node<?>, List<Double>> distances = null;

	public LatentVariableTournament(MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> evals,
			List<Chromosome> totalPopulation, int maxDepth) {
		super(evals, totalPopulation, maxDepth);
		distances = new HashMap<Node<?>, List<Double>>();
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public LatentVariableFitness<?> getFitness(Chromosome toEvaluateC) {
		Node<?> toEvaluate = (Node<?>) toEvaluateC;
		if (toEvaluate.getType().equals("string"))
			return new StringFitness(evals, (Node<VariableAssignment<String>>) toEvaluate, maxDepth);
		else {
			assert (toEvaluate.getType().equals("integer"));
			return new IntegerFitness(evals, (Node<VariableAssignment<Integer>>) toEvaluate, maxDepth);
		}
	}

	@Override
	protected Comparator<Chromosome> getComparator() {
		return new NodeComparator(this);
	}

	@Override
	protected void processResult(Map<Future<Double>, Chromosome> solMap, Future<Double> sol, double score,
			Fitness fitness) {
		super.processResult(solMap, sol, score, fitness);
	}

	public Map<Node<?>, List<Double>> getDistances() {
		return distances;
	}
}
