package mint.inference.gp.fitness.singleOutput;

import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;

import mint.inference.gp.tree.Node;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 05/03/15.
 */
public class SingleOutputStringFitness extends SingleOutputFitness<String> {

	public SingleOutputStringFitness(MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> evals,
			Node<VariableAssignment<String>> individual, int maxDepth) {
		super(evals, individual, maxDepth);
	}

	/**
	 * Computes the Levenshtein distance - algorithm taken from Rosetta Code.
	 * 
	 * @param actual
	 * @param exp
	 * @return
	 */

	@Override
	protected double distance(String actual, Object exp) {
		String expected = exp.toString();
		String a = actual.toLowerCase();
		String b = expected.toLowerCase();
		// i == 0
		int[] costs = new int[b.length() + 1];
		for (int j = 0; j < costs.length; j++)
			costs[j] = j;
		for (int i = 1; i <= a.length(); i++) {
			// j == 0; nw = lev(i - 1, j)
			costs[0] = i;
			int nw = i - 1;
			for (int j = 1; j <= b.length(); j++) {
				int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
						a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
				nw = costs[j];
				costs[j] = cj;
			}
		}
		return costs[b.length()];

	}
}
