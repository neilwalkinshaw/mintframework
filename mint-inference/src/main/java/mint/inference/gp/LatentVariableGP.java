package mint.inference.gp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.log4j.Logger;

import mint.Configuration;
import mint.inference.evo.AbstractIterator;
import mint.inference.evo.Chromosome;
import mint.inference.evo.GPConfiguration;
import mint.inference.evo.Selection;
import mint.inference.evo.TournamentSelection;
import mint.inference.gp.fitness.latentVariable.BooleanFitness;
import mint.inference.gp.fitness.latentVariable.IntegerFitness;
import mint.inference.gp.fitness.latentVariable.LatentVariableFitness;
import mint.inference.gp.fitness.latentVariable.StringFitness;
import mint.inference.gp.selection.LatentVariableTournament;
import mint.inference.gp.tree.Node;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 06/03/15.
 */
public class LatentVariableGP extends GP<VariableAssignment<?>> {

	private final static Logger LOGGER = Logger.getLogger(GP.class.getName());

	protected TournamentSelection selection = null;

	public LatentVariableGP(Generator gen, MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> evals,
			GPConfiguration gpConf) {
		super(gpConf);
		this.evals = evals;
		this.gen = gen;
		distances = new HashMap<Node<?>, List<Double>>();
	}

	@Override
	public Selection getSelection(List<Chromosome> currentPop) {
		selection = new LatentVariableTournament(evals, currentPop, getGPConf().getDepth());
		return selection;
	}

	@Override
	protected String getType() {
		VariableAssignment<?> var = evals.values().iterator().next();
		if (var instanceof StringVariableAssignment)
			return "String";
		else if (var instanceof DoubleVariableAssignment)
			return "Double";
		else if (var instanceof IntegerVariableAssignment)
			return "Integer";
		else if (var instanceof BooleanVariableAssignment)
			return "Boolean";
		else
			return "List";
	}

	@Override
	protected AbstractIterator getIterator(List<Chromosome> population) {
		if (selection != null) {
			List<Chromosome> elites = selection.getElite();
			return new Iterate(elites, population, getGPConf().getCrossOver(), getGPConf().getMutation(), gen,
					getGPConf().getDepth(), new Random(Configuration.getInstance().SEED));
		}
		return new Iterate(new ArrayList<Chromosome>(), population, getGPConf().getCrossOver(),
				getGPConf().getMutation(), gen, getGPConf().getDepth(), new Random(Configuration.getInstance().SEED));
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean isCorrect(Chromosome c) {
		int maxDepth = 0;
		try {
			if (((Node<?>) c).getType() == "string")
				return new StringFitness(evals, (Node<VariableAssignment<String>>) c, maxDepth).correct();
			else if (((Node<?>) c).getType() == "integer")
				return new IntegerFitness(evals, (Node<VariableAssignment<Integer>>) c, maxDepth).correct();
			else if (((Node<?>) c).getType() == "boolean") {
				return new BooleanFitness(evals, (Node<VariableAssignment<Boolean>>) c, maxDepth).correct();
			}
			System.out.println(c.getClass());
			throw new IllegalArgumentException(
					"Could not calculate correctness for node of type " + ((Node<?>) c).getType());
		} catch (InterruptedException e) {
			return false;
		}
	}

	@Override
	public Chromosome evolve(int lim) {
		System.out.println("Michael's");
		assert (lim > 0);
		population = generatePopulation(getGPConf().getPopulationSize() - seeds.size());

		population.addAll(seeds);

		System.out.println("Population: " + population);

		AbstractIterator it = getIterator(population);
		Chromosome fittest = null;
		double bestFitness = 0;
		for (int i = 0; i < lim; i++) {

			population = it.iterate(this);

			for (Chromosome c : getPopulation()) {
				LatentVariableFitness<?> fit;
				if (((Node<?>) c).getType() == "string")
					fit = new StringFitness(evals, (Node<VariableAssignment<String>>) c, this.getGPConf().getDepth());
				else if (((Node<?>) c).getType() == "integer")
					fit = new IntegerFitness(evals, (Node<VariableAssignment<Integer>>) c, this.getGPConf().getDepth());
				else {
					assert (((Node<?>) c).getType() == "boolean");
					fit = new BooleanFitness(evals, (Node<VariableAssignment<Boolean>>) c, this.getGPConf().getDepth());
				}
				try {
					double fitness = fit.call();
					if (fittest == null || fitness < bestFitness) {
						fittest = c;
						bestFitness = fitness;
					}
				} catch (InterruptedException e) {
				}
			}

			LOGGER.debug("GP iteration: " + i + " - best fitness: " + bestFitness + " New population: " + population);

			if (bestFitness <= 0D)
				break;

			it = getIterator(population);
		}

		return fittest;
	}

}
