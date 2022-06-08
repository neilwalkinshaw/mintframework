package fr.vergne.pareto;

import java.util.Collection;
import java.util.HashSet;

/**
 * This helper give some methods useful for Pareto efficiency check.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class ParetoHelper {

	/**
	 * This method looks for the individuals of the Pareto frontier, considering
	 * we are looking for maximal individuals.
	 * 
	 * @param population
	 *            the individuals to check
	 * @param comparator
	 *            the Pareto comparator to use
	 * @return the individuals at the Pareto frontier
	 */
	public static <Individual> Collection<Individual> getMaximalFrontierOf(
			final Collection<Individual> population,
			final ParetoComparator<Individual> comparator) {
		OrderChecker<Individual> checker = new OrderChecker<Individual>() {

			public boolean canOrderAs(Individual i1, Individual i2) {
				return comparator.compare(i1, i2) < 0;
			}
		};

		return getFrontierOf(population, checker);
	}

	/**
	 * This method looks for the individuals of the Pareto frontier, considering
	 * we are looking for minimal individuals.
	 * 
	 * @param population
	 *            the individuals to check
	 * @param comparator
	 *            the Pareto comparator to use
	 * @return the individuals at the Pareto frontier
	 */
	public static <Individual> Collection<Individual> getMinimalFrontierOf(
			final Collection<Individual> population,
			final ParetoComparator<Individual> comparator) {
		OrderChecker<Individual> checker = new OrderChecker<Individual>() {

			public boolean canOrderAs(Individual i1, Individual i2) {
				return comparator.compare(i1, i2) > 0;
			}
		};
		return getFrontierOf(population, checker);
	}

	/**
	 * This method is the common part of
	 * {@link #getMaximalFrontierOf(Collection, ParetoComparator)} and
	 * {@link #getMinimalFrontierOf(Collection, ParetoComparator)}.
	 * 
	 * @param population
	 *            the population to check
	 * @param checker
	 *            the checker to use
	 * @return the individuals of the frontier identified by the checker
	 */
	private static <Individual> Collection<Individual> getFrontierOf(
			final Collection<Individual> population,
			OrderChecker<Individual> checker) {
		Collection<Individual> frontier = new HashSet<Individual>();
		for (Individual i1 : population) {
			Boolean add = true;
			for (Individual i2 : population) {
				if (checker.canOrderAs(i1, i2)) {
					add = false;
					break;
				}
			}
			if (add) {
				frontier.add(i1);
			}
		}
		return frontier;
	}

	/**
	 * A basic interface to factor comparisons.
	 * 
	 * @author Matthieu Vergne <vergne@fbk.eu>
	 * 
	 * @param <Individual>
	 *            The type of individuals checked.
	 */
	private interface OrderChecker<Individual> {
		/**
		 * Tell if both individuals can be ordered as i1 worst than i2.
		 * 
		 * @return true if i1 is worst than i2, false otherwise
		 */
		public boolean canOrderAs(Individual i1, Individual i2);
	}
}
