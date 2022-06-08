package fr.vergne.pareto;

import java.util.Comparator;
import java.util.LinkedList;

/**
 * A Pareto comparator allows to compare multidimensional individuals in a
 * Pareto way. Look at the Javadoc of {@link #compare(Object, Object)} for the
 * formalization.<br/>
 * <br/>
 * An individual is considered better than another regarding the comparators
 * given to the Pareto comparator (one for each dimension). There is no
 * constraint about which direction (positive or negative comparison) tells
 * which one is the best (so you can decide for the most natural way), but
 * <b>all the comparators have to be consistent</b>: if one comparator uses a
 * positive value to say A is better than B, the others must use the same
 * convention.<br/>
 * <br/>
 * <b>ATTENTION</b> Two individuals said equivalent through this comparator can
 * be different (a.equals(b) == <code>false</code>)!
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 * @param <Individual>
 *            The individuals to compare.
 */
@SuppressWarnings("serial")
public class ParetoComparator<Individual> extends
		LinkedList<Comparator<Individual>> implements Comparator<Individual> {

	/**
	 * Compare multidimensional individuals in a Pareto way :
	 * <ul>
	 * <li>if A is better than B on all the dimensions (some can be equivalent),
	 * A is considered as the best one</li>
	 * <li>if A is equivalent to B on all the dimensions, A and B are considered
	 * as equivalent</li>
	 * <li>if A is better than B on at least one dimension and worst on at least
	 * one another, A and B are considered as equivalent, as we cannot decide
	 * which one is better</li>
	 * </ul>
	 */
	public int compare(Individual a, Individual b) {
		int reference = 0;
		for (Comparator<Individual> comparator : this) {
			if (reference == 0) {
				reference = (int) Math.signum(comparator.compare(a, b));
			} else {
				int comparison = (int) Math.signum(comparator.compare(a, b));
				if (comparison * reference < 0) {
					// one better, another worst : cannot decide
					return 0;
				}
			}
		}
		return reference;
	}

}
