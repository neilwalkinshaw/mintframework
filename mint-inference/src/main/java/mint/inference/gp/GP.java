package mint.inference.gp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.log4j.Logger;

import mint.inference.evo.AbstractEvo;
import mint.inference.evo.Chromosome;
import mint.inference.evo.GPConfiguration;
import mint.inference.gp.tree.Node;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.ListVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 06/03/2018.
 */
public abstract class GP<T> extends AbstractEvo {

	protected Generator gen;
	private final static Logger LOGGER = Logger.getLogger(GP.class.getName());
	protected boolean mem_dist = false;
	protected MultiValuedMap<List<VariableAssignment<?>>, T> evals;
	protected Map<Node<?>, List<Double>> distances;

	/**
	 * Takes as input a random program generator, a training set (a map from a list
	 * of input parameters to an output parameter) and a configuration.
	 *
	 * @param gpConf
	 */
	public GP(GPConfiguration gpConf) {
		super(gpConf);
	}

	public MultiValuedMap<List<VariableAssignment<?>>, T> getEvals() {
		return evals;
	}

	@Override
	public List<Chromosome> generatePopulation(int i) {
		String type = getType();
		List<Chromosome> population = null;
		if (type.equals("Double")) {
			population = gen.generateDoublePopulation(i, getGPConf().getDepth());
		} else if (type.equals("Integer")) {
			population = gen.generateIntegerPopulation(i, getGPConf().getDepth());
		} else if (type.equals("String")) {
			population = gen.generateStringPopulation(i, getGPConf().getDepth());
		} else if (type.equals("Boolean")) {
			population = gen.generateBooleanPopulation(i, getGPConf().getDepth());
		} else if (type.equals("List")) {
			population = gen.generateListPopulation(i, getGPConf().getDepth(), getTypeString());
		} else {
			LOGGER.error("Failed to generate population for undefined type.");
		}
		population.addAll(seeds);
//		System.out.println("Population: " + population);
		return population;
	}

	protected abstract String getType();

	private String getTypeString() {
		String typeString = "";
		ListVariableAssignment var = (ListVariableAssignment) evals.values().iterator().next();
		List<?> val = var.getValue();
		for (int i = 0; i < val.size(); i++) {
			Object element = val.get(i);
			if (element instanceof DoubleVariableAssignment) {
				typeString += "d";
			} else if (element instanceof BooleanVariableAssignment) {
				typeString += "b";

			} else
				typeString += "i";
		}
		return typeString;
	}

	public Map<Node<?>, List<Double>> getDistances() {
		return distances;
	}

	public abstract boolean isCorrect(Chromosome c);

	@Override
	public List<Chromosome> removeDuplicates(List<Chromosome> pop) {
		List<Chromosome> newPop = new ArrayList<Chromosome>();
		for (Chromosome c : pop) {
			if (!gen.populationContains(newPop, c))
				newPop.add(c);
		}
		return newPop;
	}
}
