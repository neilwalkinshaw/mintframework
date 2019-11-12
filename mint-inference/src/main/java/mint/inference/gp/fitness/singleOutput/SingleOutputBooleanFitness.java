package mint.inference.gp.fitness.singleOutput;

import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections4.MultiValuedMap;

import mint.inference.gp.NodeExecutor;
import mint.inference.gp.tree.Node;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 07/07/15.
 */
public class SingleOutputBooleanFitness extends SingleOutputFitness<Boolean> {
	public SingleOutputBooleanFitness(MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> evals,
			Node<VariableAssignment<Boolean>> toEvaluate, int maxDepth) {
		super(evals, toEvaluate, maxDepth);
	}

	@Override
	public Double call() {
		NodeExecutor<Boolean> executor = new NodeExecutor<Boolean>(individual);
		double tp = 0.0000001D, fp = 0.0000001D, tn = 0.0000001D, fn = 0.0000001D;
//		double tp = 0.000000D, fp = 0.000000D, tn = 0.000000D, fn = 0.000000D;
		boolean penalize = false;
		boolean allCorrect = true;
		for (Entry<List<VariableAssignment<?>>, VariableAssignment<?>> current : evalSet.entries()) {
			VariableAssignment<?> expectedVar = current.getValue();
			if (!expectedVar.withinLimits()) {
				penalize = true;
				break;
			}

			boolean expected = (Boolean) expectedVar.getValue();

			try {
				Boolean actual = executor.execute(current.getKey());
				if (expected != actual)
					allCorrect = false;
				if (expected == true) {
					if (actual.booleanValue() == true) {
						tp++;
					} else {
						fn++;
					}
				} else {
					if (actual.booleanValue() == true) {
						fp++;
					} else {
						tn++;
					}
				}

			} catch (Exception e) { // GP candidate has crashed.
				e.printStackTrace();
				penalize = true;
				break;
			}
		}

		if (individual.subTreeMaxdepth() > maxDepth)
			penalize = true;

		if (penalize)
			return 100000D;
		else {
			if (allCorrect)
				return 0D;
			double errorRate = 1 - errorRate(tp, fp, tn, fn);
			return errorRate;
		}
	}

	/**
	 * Not necessary in this case - redesign required.
	 * 
	 * @param actual
	 * @param expected
	 * @return
	 */
	@Override
	protected double distance(Boolean actual, Object expected) {
		return 0;
	}

	private Double errorRate(double tp, double fp, double tn, double fn) {
		return ((tp + tn) / (tp + tn + fp + fn));
	}

}
