package mint.inference.gp.selection;

import java.util.List;
import java.util.Random;

import org.apache.commons.collections4.MultiValuedMap;

import mint.inference.evo.Chromosome;
import mint.inference.evo.TournamentSelection;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 18/05/2016.
 */
public abstract class IOTournamentSelection<T> extends TournamentSelection {

	protected MultiValuedMap<List<VariableAssignment<?>>, T> evals;

	public IOTournamentSelection(MultiValuedMap<List<VariableAssignment<?>>, T> evals, List<Chromosome> totalPopulation,
			int maxDepth, Random rand) {
		super(totalPopulation, maxDepth, rand);
		this.evals = evals;
	}

}
