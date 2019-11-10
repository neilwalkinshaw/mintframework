package mint.inference.gp.fitness.latentVariable;

import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;

import mint.inference.gp.fitness.InvalidDistanceException;
import mint.inference.gp.tree.Node;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 05/03/15.
 */
public class IntegerFitness extends LatentVariableFitness<Integer> {

	public IntegerFitness(MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> evals,
			Node<VariableAssignment<Integer>> individual, int maxDepth) {
		super(evals, individual, maxDepth);
	}

	@Override
	protected double distance(Integer actual, Object expected) throws InvalidDistanceException {
		if (expected instanceof Integer) {
			Integer exp = (Integer) expected;

			return Math.abs(actual.intValue() - exp.intValue());
		} else if (expected instanceof Double) {
			Double exp = (Double) expected;
			return Math.abs(actual.intValue() - exp.intValue());
		} else
			throw new InvalidDistanceException();
	}
}
