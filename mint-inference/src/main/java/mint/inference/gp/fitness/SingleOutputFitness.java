package mint.inference.gp.fitness;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mint.inference.gp.CallableNodeExecutor;
import mint.inference.gp.tree.Node;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 05/03/15.
 */
public abstract class SingleOutputFitness<T> extends Fitness {

	final Map<List<VariableAssignment<?>>, VariableAssignment<?>> evalSet;

	protected final int maxDepth;
	protected Node<VariableAssignment<T>> individual;
	List<Double> distances;

	public SingleOutputFitness(Map<List<VariableAssignment<?>>, VariableAssignment<?>> evals,
			Node<VariableAssignment<T>> individual, int maxDepth) {
		this.evalSet = evals;
		this.individual = individual;
		this.maxDepth = maxDepth;
		this.distances = new ArrayList<Double>();
	}

	public Node<?> getIndividual() {
		return individual;
	}

	public List<Double> getDistances() {
		return distances;
	}

	@Override
	public Double call() throws InterruptedException {
		distances.clear();
		double penaltyFactor = 0;
		for (Entry<List<VariableAssignment<?>>, VariableAssignment<?>> current : evalSet.entrySet()) {
			boolean penalize = false;
			if (Thread.interrupted())
				throw new InterruptedException();
			double distance = 0D;
			T actual = null;
			try {
				CallableNodeExecutor<T> executor = new CallableNodeExecutor<T>(individual, current.getKey());

				try {
					actual = executor.call();
				} catch (Exception e) { // GP candidate has crashed.
					e.printStackTrace();
					penaltyFactor = 100;
				}
				if (actual == null)
					penaltyFactor = 100;
				if (!penalize) {
					distance = (distance(actual, current.getValue().getValue()));
					distances.add(distance);
				}
			} catch (InvalidDistanceException e) {
				penaltyFactor = 100;
			}
			if (individual.subTreeMaxdepth() > maxDepth)
				penaltyFactor = Math.abs((individual.subTreeMaxdepth() - maxDepth) * 2);

			distances.add(distance);
		}
		double distance = calculateFitness(distances);
		return distance + penaltyFactor;
	}

	protected Double calculateFitness(List<Double> distances) {
		return rmsd(distances);
	}

	protected abstract double distance(T actual, Object expected) throws InvalidDistanceException;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SingleOutputFitness))
			return false;

		SingleOutputFitness singleOutputFitness = (SingleOutputFitness) o;

		if (!individual.equals(singleOutputFitness.individual))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return individual.hashCode();
	}
}
