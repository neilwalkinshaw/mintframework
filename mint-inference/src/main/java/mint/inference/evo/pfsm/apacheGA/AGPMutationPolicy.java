package mint.inference.evo.pfsm.apacheGA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.MutationPolicy;
import org.apache.commons.math3.random.RandomGenerator;

import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.model.statepair.StatePair;
import mint.model.statepair.StatePairComparator;

/**
 * Created by neilwalkinshaw on 26/05/2016.
 */
public class AGPMutationPolicy implements MutationPolicy {

	final protected SimpleMergingState tree;

	public AGPMutationPolicy(SimpleMergingState tree) {

		this.tree = tree;
	}

	@Override
	public Chromosome mutate(Chromosome chromosome) throws MathIllegalArgumentException {
		AGPMergingTable toMutate = (AGPMergingTable) chromosome;
		List<StatePair> newMerges = new ArrayList<StatePair>();
		newMerges.addAll(toMutate.getList());
		RandomGenerator generator = GA.getRandomGenerator();
		double proportion = generator.nextDouble();
		int number = (int) (proportion * newMerges.size());

		Collections.sort(newMerges, new StatePairComparator(tree));
		List<Integer> states = new ArrayList<Integer>();
		states.addAll(tree.getCurrent().getStates());
		for (int i = 0; i < number; i++) {
			StatePair sp = newMerges.get(i);
			if (generator.nextBoolean()) {
				sp.setFirstState(states.get(generator.nextInt(states.size())));
			} else {
				sp.setSecondState(states.get(generator.nextInt(states.size())));
			}

		}

		return toMutate.newFixedLengthChromosome(newMerges);
	}
}
