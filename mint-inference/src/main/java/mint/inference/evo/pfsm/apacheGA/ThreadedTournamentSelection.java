package mint.inference.evo.pfsm.apacheGA;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ChromosomePair;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.ListPopulation;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.TournamentSelection;

/**
 * Created by neilwalkinshaw on 27/05/2016.
 */
public class ThreadedTournamentSelection extends TournamentSelection {
	public ThreadedTournamentSelection(int arity) {
		super(arity);
	}

	@Override
	public ChromosomePair select(Population population) throws MathIllegalArgumentException {
		return new ChromosomePair(this.threadedTournament((ListPopulation) population),
				this.threadedTournament((ListPopulation) population));
	}

	protected Chromosome threadedTournament(ListPopulation population) throws MathIllegalArgumentException {
		if (population.getPopulationSize() < getArity()) {
			throw new MathIllegalArgumentException(LocalizedFormats.TOO_LARGE_TOURNAMENT_ARITY,
					new Object[] { Integer.valueOf(getArity()), Integer.valueOf(population.getPopulationSize()) });
		} else {
			ListPopulation tournamentPopulation = new ThreadedElitisticListPopulation(getArity(), 0) {
				@Override
				public Population nextGeneration() {
					return null;
				}
			};
			List<Chromosome> chromosomes = new ArrayList<Chromosome>(population.getChromosomes());

			for (int i = 0; i < getArity(); ++i) {
				int rind = GeneticAlgorithm.getRandomGenerator().nextInt(chromosomes.size());
				tournamentPopulation.addChromosome(chromosomes.get(rind));
				chromosomes.remove(rind);
			}

			return tournamentPopulation.getFittestChromosome();
		}
	}
}
