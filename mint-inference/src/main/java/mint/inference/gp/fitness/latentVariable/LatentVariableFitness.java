package mint.inference.gp.fitness.latentVariable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.MultiValuedMap;

import mint.inference.gp.CallableNodeExecutor;
import mint.inference.gp.fitness.Fitness;
import mint.inference.gp.fitness.InvalidDistanceException;
import mint.inference.gp.tree.Node;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 05/03/15.
 */
public abstract class LatentVariableFitness<T> extends Fitness {

	final MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> evalSet;

	protected final int maxDepth;
	protected Node<VariableAssignment<T>> individual;
	protected boolean needHidden;

	public LatentVariableFitness(MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> evals,
			Node<VariableAssignment<T>> individual, int maxDepth) {
		this.evalSet = evals;
		this.individual = individual;
		this.maxDepth = maxDepth;
	}

	public LatentVariableFitness(MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> evals,
			Node<VariableAssignment<T>> individual) {
		this.evalSet = evals;
		this.individual = individual;
		this.maxDepth = 1;
	}

	public Node<?> getIndividual() {
		return individual;
	}

	private double calculateDistance(Entry<List<VariableAssignment<?>>, VariableAssignment<?>> current,
			Set<VariableAssignment<T>> undef) throws InterruptedException {
		individual.reset();
		List<VariableAssignment<?>> ctx = makeCtx(current);
		CallableNodeExecutor<T> executor = new CallableNodeExecutor<>(individual, ctx);
		double minDistance = Double.POSITIVE_INFINITY;
		T actual;

		try {
			if (undef.isEmpty()) {
				actual = executor.call();
				minDistance = distance(executor.call(), current.getValue().getValue());
				individual.reset();
			}

			for (VariableAssignment<T> var : undef) {
				T[] values = (T[]) var.getValues().toArray();
				for (T value : values) {
					var.setValue(value);
					ctx.add(var.copy());
					executor = new CallableNodeExecutor<>(individual, ctx);
					actual = executor.call();
					double offBy = distance(actual, current.getValue().getValue());
//					System.out.println("value: "+value+" expected: "+current.getValue().getValue()+" actual: "+actual+" distance: "+offBy);
					if (offBy < minDistance) {
						minDistance = offBy;
					}
				}
			}
		} catch (ClassCastException e) {
			System.out.println("ClassCastException");
			return Double.POSITIVE_INFINITY;
		} catch (InvalidDistanceException e) {
			System.out.println("InvalidDistanceException");
			return Double.POSITIVE_INFINITY;
		} catch (NullPointerException e) {
			System.out.println("NullPointerException");
			return Double.POSITIVE_INFINITY;
		}
		return minDistance;
	}

	@Override
	public Double call() throws InterruptedException {
		if (individual == null) {
			return Double.POSITIVE_INFINITY;
		}

		double mistakes = 0D;
		Set<String> totalUsedVars = totalUsedVars();
		List<Double> distances = new ArrayList<Double>();

		Set<VariableAssignment<T>> undef = undefVars(individual, totalUsedVars);

//		System.out.println("Evaluating: " + individual + " Undef: " + undef);

		Set<String> totalUnusedVars = totalUsedVars;
		for (VariableAssignment<T> vName : individual.varsInTree()) {
			totalUnusedVars.remove(vName.getName());
		}

		for (Entry<List<VariableAssignment<?>>, VariableAssignment<?>> current : evalSet.entries()) {
			double minDistance = calculateDistance(current, undef);
			distances.add(minDistance);
			if (minDistance > 0D) {
				mistakes++;
			}
		}

		double fitness = mistakes + rmsd(distances);

		if (individual.numVarsInTree() == 0) {
			return fitness;
		}

//		System.out.println("individual: " + individual);
		double proportionUnusedVars = totalUnusedVars.size() / (double) individual.numVarsInTree();
		return fitness + proportionUnusedVars;
	}

	public boolean correct() throws InterruptedException {
		Set<String> totalUsedVars = totalUsedVars();

		Set<VariableAssignment<T>> undef = undefVars(individual, totalUsedVars);

		Set<String> totalUnusedVars = totalUsedVars;
		for (VariableAssignment<T> vName : individual.varsInTree()) {
			totalUnusedVars.remove(vName.getName());
		}

		for (Entry<List<VariableAssignment<?>>, VariableAssignment<?>> current : evalSet.entries()) {
			double minDistance = calculateDistance(current, undef);
			if (minDistance > 0D) {
				return false;
			}
		}

		return true;
	}

	private Set<String> totalUsedVars() {
		Set<String> totalUsedVars = new HashSet<String>();
		for (Entry<List<VariableAssignment<?>>, VariableAssignment<?>> current : evalSet.entries()) {
			for (VariableAssignment<?> vName : current.getKey()) {
				totalUsedVars.add(vName.getName());
			}
		}
		return totalUsedVars;
	}

	public List<VariableAssignment<?>> makeCtx(Entry<List<VariableAssignment<?>>, VariableAssignment<?>> current) {
		List<VariableAssignment<?>> ctx = new ArrayList<VariableAssignment<?>>();
		for (VariableAssignment<?> v : current.getKey()) {
			ctx.add(v);
		}
		return ctx;
	}

	private Set<VariableAssignment<T>> undefVars(Node<VariableAssignment<T>> exp, Set<String> defVars) {
		Set<VariableAssignment<T>> varsInTree = exp.varsInTree();
		for (VariableAssignment<T> v1 : exp.varsInTree()) {
			if (defVars.contains(v1.getName())) {
				varsInTree.remove(v1);
			}
		}
		return varsInTree;
	}

	protected Double calculateFitness(List<Double> distances) {
		if (distances.isEmpty()) {
			return Double.POSITIVE_INFINITY;
		}
		return rmsd(distances);
	}

	protected abstract double distance(T actual, Object expected) throws InvalidDistanceException;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof LatentVariableFitness))
			return false;

		LatentVariableFitness singleOutputFitness = (LatentVariableFitness) o;

		if (!individual.equals(singleOutputFitness.individual))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return individual.hashCode();
	}
}