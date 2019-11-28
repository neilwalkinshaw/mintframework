package mint.inference.gp.selection;

import mint.inference.evo.Chromosome;
import mint.inference.evo.TournamentSelection;
import mint.tracedata.types.VariableAssignment;

import java.util.List;
import java.util.Map;

/**
 * Created by neilwalkinshaw on 18/05/2016.
 */
public abstract class IOTournamentSelection<T> extends TournamentSelection {

    protected Map<List<VariableAssignment<?>>, T> evals;

    public IOTournamentSelection(Map<List<VariableAssignment<?>>, T> evals, List<Chromosome> totalPopulation, int maxDepth){
        super(totalPopulation,maxDepth);
        this.evals = evals;
    }

}
