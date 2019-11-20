package mint.inference.evo;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Created by neilwalkinshaw on 06/03/15.
 */
public abstract class AbstractEvo {

	protected GPConfiguration gpConf;
	protected Collection<Chromosome> seeds;
	protected List<Chromosome> population;

	private final static Logger LOGGER = Logger.getLogger(AbstractEvo.class.getName());

	/**
	 * Takes as input a random program generator, a training set (a map from a list
	 * of input parameters to an output parameter) and a configuration.
	 * 
	 * @param gpConf
	 */
	public AbstractEvo(GPConfiguration gpConf) {
		this.gpConf = gpConf;
		seeds = new HashSet<Chromosome>();
	}

	/**
	 * Add seed nodes to be considered by GP.
	 *
	 * @param seeds
	 */
	public void setSeeds(Collection<Chromosome> seeds) {
		if (seeds != null)
			this.seeds = seeds;
	}

	public void addSeed(Chromosome seed) {
		if (seed != null)
			this.seeds.add(seed);
	}

	public abstract Selection getSelection(List<Chromosome> currentPop);

	public List<Chromosome> getPopulation() {
		return population;
	}

	public Chromosome evolve(int lim) {
		assert (lim > 0);
		population = generatePopulation(getGPConf().getPopulationSize() - seeds.size());

		population.addAll(seeds);

		System.out.println("Population: " + population);

		AbstractIterator it = getIterator(population);
		Chromosome fittest = null;
		for (int i = 0; i < lim; i++) {

			population = it.iterate(this);

			TournamentSelection latestSelection = (TournamentSelection) it.getLatestSelection();
			if (latestSelection != null) {
				Double bestFitness = latestSelection.getBestFitness();
				// LOGGER.debug(latestSelection.getBestFitnessSummary());
				// LOGGER.debug("Best fitness: "+it.getLatestSelection().getBestFitness());
				fittest = latestSelection.elite.get(0);
				LOGGER.debug("GP iteration: " + i + " - best fitness: " + bestFitness);
				System.out.println("New population: " + population);

				if (bestFitness <= 0D)
					break;
			}

			it = getIterator(population);
		}
		// TournamentSelection ts = (TournamentSelection) it.getLatestSelection();

		return fittest;
	}

	protected abstract AbstractIterator getIterator(List<Chromosome> population);

	/**
	 * Generate a population of size i
	 * 
	 * @param i
	 * @return
	 */
	public abstract List<Chromosome> generatePopulation(int i);

	public GPConfiguration getGPConf() {
		return gpConf;
	}

	public abstract List<Chromosome> removeDuplicates(List<Chromosome> pop);
}
