package mint.inference.gp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.log4j.Logger;

import edu.emory.mathcs.backport.java.util.Collections;
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
	private Chromosome fittest = null;

	public LatentVariableGP(Generator gen, MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> evals,
			GPConfiguration gpConf) {
		super(gpConf);
		this.evals = evals;
		this.gen = gen;
		distances = new HashMap<Node<?>, List<Double>>();
	}

	@Override
	public Selection getSelection(List<Chromosome> currentPop) {
		selection = new LatentVariableTournament(evals, currentPop, getGPConf().getDepth(), gen.rand);
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
			return new SteadyStateIterator(elites, population, getGPConf().getCrossOver(), getGPConf().getMutation(),
					gen, getGPConf().getDepth(), new Random(Configuration.getInstance().SEED));
		}
		return new SteadyStateIterator(new ArrayList<Chromosome>(), population, getGPConf().getCrossOver(),
				getGPConf().getMutation(), gen, getGPConf().getDepth(), new Random(Configuration.getInstance().SEED));
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean isCorrect(Chromosome c) {
		try {
			if (((Node<?>) c).getType() == "string")
				return new StringFitness(evals, (Node<VariableAssignment<String>>) c).correct();
			else if (((Node<?>) c).getType() == "integer")
				return new IntegerFitness(evals, (Node<VariableAssignment<Integer>>) c).correct();
			else if (((Node<?>) c).getType() == "boolean") {
				return new BooleanFitness(evals, (Node<VariableAssignment<Boolean>>) c).correct();
			}
			System.out.println(c.getClass());
			throw new IllegalArgumentException(
					"Could not calculate correctness for node of type " + ((Node<?>) c).getType());
		} catch (InterruptedException e) {
			return false;
		}
	}

	public String popInfo() {
		List<Chromosome> orderedByFitness = new ArrayList<Chromosome>(population);
		Collections.sort(orderedByFitness);

		List<String> popString = new ArrayList<String>();

		for (Chromosome c : orderedByFitness) {
			popString.add(c + ": " + c.getFitness());
		}

		return popString.toString();
	}

	@Override
	public void evaluatePopulation(List<Chromosome> pop) {
		for (Chromosome c : pop) {
			LatentVariableFitness<?> fit;
			Node<?> node = (Node<?>) c;
			if (node.getFitness() == null) {
				if (node.getType() == "string")
					fit = new StringFitness(evals, (Node<VariableAssignment<String>>) c);
				else if (node.getType() == "integer")
					fit = new IntegerFitness(evals, (Node<VariableAssignment<Integer>>) c);
				else {
					assert (node.getType() == "boolean");
					fit = new BooleanFitness(evals, (Node<VariableAssignment<Boolean>>) c);
				}
				try {
					double fitness = fit.call();
					node.setFitness(fitness);

					// We need to have a secondary fitness function in here to break ties
					if (fittest == null || fitness < fittest.getFitness()) {
						fittest = c;
					}
				} catch (InterruptedException e) {
				}
			}
		}
	}

	@Override
	public Chromosome evolve(int lim) {
		System.out.println("Michael's");
		System.out.println("Evals: " + this.evals);
		assert (lim > 0);
		population = generatePopulation(getGPConf().getPopulationSize() - seeds.size());

		population.addAll(seeds);

		LOGGER.debug("Population: " + population);

		AbstractIterator it = getIterator(population);
		for (int i = 0; i < lim; i++) {

			population = it.iterate(this);

			evaluatePopulation(population);

			LOGGER.debug("GP iteration: " + i + " - best individual: " + fittest + " fitness: " + fittest.getFitness()
					+ " New population: " + popInfo());

			if (fittest.getFitness() <= 0D)
				break;

			it = getIterator(population);
		}

		return fittest;
	}

}
