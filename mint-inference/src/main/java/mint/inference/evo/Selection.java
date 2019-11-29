package mint.inference.evo;

import java.util.List;

/**
 * Created by neilwalkinshaw on 18/06/15.
 */
public interface Selection {

	public double getBestFitness();

	public List<Chromosome> select(GPConfiguration config, int number);

}
