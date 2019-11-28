package mint.testgen.stateless.gp.qbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import mint.inference.evo.Chromosome;
import mint.inference.gp.NodeExecutor;
import mint.inference.gp.fitness.singleOutput.SingleOutputListFitness;
import mint.inference.gp.tree.Node;
import mint.tracedata.TestIO;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 6/03/2018.
 */
public class ListOutputQuerySelector extends QuerySelector {

	private final static Logger LOGGER = Logger.getLogger(ListOutputQuerySelector.class.getName());

	public ListOutputQuerySelector(List<VariableAssignment<?>> typeSet, String name) {
		super(typeSet, name);
	}

	@Override
	protected double simulate(TestIO seed, Collection<Chromosome> committee) {
		List<List> outputs = new ArrayList<List>();
		for (Chromosome member : committee) {
			NodeExecutor<Node<VariableAssignment<?>>> nEx = new NodeExecutor((Node<?>) member);
			List out = null;
			try {
				out = (List) nEx.execute(seed);
				outputs.add(out);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				LOGGER.debug("Faulty member: " + member);
				LOGGER.debug("Inputs:" + seed);
				LOGGER.debug(e.toString());
				e.printStackTrace();
				System.exit(0);
			}

		}
		assert (!outputs.isEmpty());
		// LOGGER.debug(seed);

		return variance(convertToDoubles(outputs));
	}

	private List<List<Double>> convertToDoubles(List<List> outputs) {
		List<List<Double>> result = new ArrayList<List<Double>>();
		for (List output : outputs) {
			ArrayList<Double> doubles = new ArrayList<Double>();
			for (Object ob : output) {
				doubles.add(SingleOutputListFitness.getDouble(ob));
			}
			result.add(doubles);
		}
		return result;
	}

	private double variance(List<List<Double>> results) {
		List<Double> distances = new ArrayList<Double>();
		for (int i = 0; i < results.size(); i++) {
			List<Double> fromList = results.get(i);
			double[] fromArray = buildArray(fromList);
			for (int j = i + 1; j < results.size(); j++) {
				double[] toArray = buildArray(results.get(j));
				distances.add(1 - Math.abs(SingleOutputListFitness.cosineSimilarity(fromArray, toArray)));
			}
		}
		double sum = 0D;
		for (int i = 0; i < distances.size(); i++) {
			sum += distances.get(i);
		}
		return sum / distances.size();
	}

	private double[] buildArray(List<Double> fromList) {
		double[] from = new double[fromList.size()];
		for (int fl = 0; fl < fromList.size(); fl++) {
			from[fl] = fromList.get(fl);
		}
		return from;
	}

}
