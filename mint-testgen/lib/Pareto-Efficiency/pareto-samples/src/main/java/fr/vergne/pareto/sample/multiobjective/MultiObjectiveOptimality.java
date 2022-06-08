package fr.vergne.pareto.sample.multiobjective;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;

import fr.vergne.pareto.ParetoComparator;
import fr.vergne.pareto.ParetoHelper;

/**
 * This sample aims at showing a use case where an optimization algorithm tries
 * to find the best solution for a cost minimization problem.<br/>
 * <br/>
 * The context is that each candidate solution is generated without having
 * knowledge of its local optimality: the topology of the neighboring is
 * unknown, so we don't know if there is a neighbor which is better or even how
 * many neighbors are available. However, the topology is fixed and decided by
 * modification operators which are applied to the candidate solution. Thus, the
 * optimality of the candidate is approximated for each operator and, starting
 * from 0, goes closer to 1 each time a proposed modification does not improve
 * the solution value. By having all the candidates appearing as more and more
 * optimal, new candidate are added to explore other parts of the solution
 * space.<br/>
 * <br/>
 * However, due to performance issues, only a limited amount of candidate
 * solutions can be stored. Thus, when the limit is reached, we need to remove
 * the least interesting candidate to add a new one. The least interesting is
 * considered to be a candidate which shows a great local optimality on each
 * operator (a few chance to be improved) with a high cost. However, no explicit
 * combination of these values is provided, so no unique evaluation of the
 * candidate can be provided to choose which one to remove.<br/>
 * <br/>
 * By retrieving the Pareto fronts, the aim is to identify which are the
 * candidates which have clearly spend too much time optimizing them without
 * having good results.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class MultiObjectiveOptimality implements Runnable {

	public static void main(String[] args) throws URISyntaxException {
		new MultiObjectiveOptimality().run();
	}

	@Override
	public void run() {
		Collection<Candidate> candidates = new LinkedList<Candidate>();
		candidates.add(new Candidate(18, 0, 0));// new candidate
		candidates.add(new Candidate(9, 0.9, 0.8));// good candidate
		candidates.add(new Candidate(15, 0.8, 0.9));// bad candidate
		candidates.add(new Candidate(13, 0.99, 0.98));// old bad candidate
		candidates.add(new Candidate(5, 0.98, 0.99));// old good candidate

		ParetoComparator<Candidate> comparator = new ParetoComparator<Candidate>();
		comparator.add(new Comparator<Candidate>() {

			@Override
			public int compare(Candidate c1, Candidate c2) {
				// lower cost = better individual
				return c1.getCost().compareTo(c2.getCost());
			}
		});
		comparator.add(new Comparator<Candidate>() {

			@Override
			public int compare(Candidate c1, Candidate c2) {
				// lower optimality = better individual
				return c1.getOptimality1().compareTo(c2.getOptimality1());
			}
		});
		comparator.add(new Comparator<Candidate>() {

			@Override
			public int compare(Candidate c1, Candidate c2) {
				// lower optimality = better individual
				return c1.getOptimality2().compareTo(c2.getOptimality2());
			}
		});

		while (!candidates.isEmpty()) {
			Collection<Candidate> frontier = ParetoHelper.getMinimalFrontierOf(
					candidates, comparator);
			System.out.println(frontier);
			candidates.removeAll(frontier);
		}
	}

	private static class Candidate {
		private final int cost;
		private final double optimality1;
		private final double optimality2;

		public Candidate(int cost, double optimality1, double optimality2) {
			this.cost = cost;
			this.optimality1 = optimality1;
			this.optimality2 = optimality2;
		}

		public Integer getCost() {
			return cost;
		}

		public Double getOptimality1() {
			return optimality1;
		}

		public Double getOptimality2() {
			return optimality2;
		}

		@Override
		public String toString() {
			return cost + "[" + optimality1 + "," + optimality2 + "]";
		}
	}
}
