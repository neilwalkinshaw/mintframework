package mint.inference.evo.pfsm.apacheGA;


import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.genetics.*;

import java.util.ArrayList;

/**
 * Created by neilwalkinshaw on 27/05/2016.
 */
public class ThreadedTournamentSelection extends TournamentSelection {
    public ThreadedTournamentSelection(int arity) {
        super(arity);
    }

    public ChromosomePair select(Population population) throws MathIllegalArgumentException {
        return new ChromosomePair(this.threadedTournament((ListPopulation) population), this.threadedTournament((ListPopulation)population));
    }

    protected Chromosome threadedTournament(ListPopulation population) throws MathIllegalArgumentException {
        if(population.getPopulationSize() < getArity()) {
            throw new MathIllegalArgumentException(LocalizedFormats.TOO_LARGE_TOURNAMENT_ARITY, new Object[]{Integer.valueOf(getArity()), Integer.valueOf(population.getPopulationSize())});
        } else {
            ListPopulation tournamentPopulation = new ThreadedElitisticListPopulation(getArity(),0) {
                public Population nextGeneration() {
                    return null;
                }
            };
            ArrayList chromosomes = new ArrayList(population.getChromosomes());

            for(int i = 0; i < getArity(); ++i) {
                int rind = GeneticAlgorithm.getRandomGenerator().nextInt(chromosomes.size());
                tournamentPopulation.addChromosome((Chromosome)chromosomes.get(rind));
                chromosomes.remove(rind);
            }

            return tournamentPopulation.getFittestChromosome();
        }
    }
}
