package mint.inference.gp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections4.MultiValuedMap;

import mint.Configuration;
import mint.inference.evo.AbstractIterator;
import mint.inference.evo.Chromosome;
import mint.inference.evo.GPConfiguration;
import mint.inference.evo.Selection;
import mint.inference.evo.TournamentSelection;
import mint.inference.gp.fitness.singleOutput.SingleOutputBooleanFitness;
import mint.inference.gp.fitness.singleOutput.SingleOutputDoubleFitness;
import mint.inference.gp.fitness.singleOutput.SingleOutputIntegerFitness;
import mint.inference.gp.fitness.singleOutput.SingleOutputListFitness;
import mint.inference.gp.fitness.singleOutput.SingleOutputStringFitness;
import mint.inference.gp.selection.SingleOutputTournament;
import mint.inference.gp.tree.Node;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 06/03/15.
 */
public class SingleOutputGP extends GP<VariableAssignment<?>> {

	protected TournamentSelection selection = null;

	@Deprecated
	public SingleOutputGP(Generator gen, MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> evals,
			GPConfiguration gpConf) {
		super(gpConf);
		this.gen = gen;
		this.evals = evals;
		distances = new HashMap<Node<?>, List<Double>>();
	}

	public SingleOutputGP(Generator gen, MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> evals,
			GPConfiguration gpConf, boolean memoriseDistances) {
		super(gpConf);
		this.evals = evals;
		this.gen = gen;
		this.mem_dist = memoriseDistances;
		distances = new HashMap<Node<?>, List<Double>>();
	}

	@Override
	public Selection getSelection(List<Chromosome> currentPop) {
		selection = new SingleOutputTournament(evals, currentPop, gpConf.getDepth(), mem_dist);
		return selection;
	}

	@Override
	protected String getType() {
		VariableAssignment<?> var = evals.values().iterator().next();
		if (var instanceof StringVariableAssignment)
			return "String";
		else if (var instanceof DoubleVariableAssignment)
			return "Double";
		else if (var instanceof IntegerVariableAssignment)
			return "Integer";
		else if (var instanceof BooleanVariableAssignment)
			return "Boolean";
		else
			return "List";
	}

	@Override
	protected AbstractIterator getIterator(List<Chromosome> population) {
		if (selection != null) {
			List<Chromosome> elites = selection.getElite();
			return new Iterate(elites, population, gpConf.getCrossOver(), gpConf.getMutation(), gen, gpConf.getDepth(),
					new Random(Configuration.getInstance().SEED));
		}
		return new Iterate(new ArrayList<Chromosome>(), population, gpConf.getCrossOver(), gpConf.getMutation(), gen,
				gpConf.getDepth(), new Random(Configuration.getInstance().SEED));
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public boolean isCorrect(Chromosome toEvaluateC) {
		Node<?> toEvaluate = (Node<?>) toEvaluateC;
		int maxDepth = 0;
		try {

			if (toEvaluate.getType().equals("string"))
				return new SingleOutputStringFitness(evals, (Node<VariableAssignment<String>>) toEvaluate, maxDepth)
						.correct();
			else if (toEvaluate.getType().equals("double"))
				return new SingleOutputDoubleFitness(evals, (Node<VariableAssignment<Double>>) toEvaluate, maxDepth)
						.correct();
			else if (toEvaluate.getType().equals("integer"))
				return new SingleOutputIntegerFitness(evals, (Node<VariableAssignment<Integer>>) toEvaluate, maxDepth)
						.correct();
			else if (toEvaluate.getType().equals("List"))
				return new SingleOutputListFitness(evals, (Node<VariableAssignment<List>>) toEvaluate, maxDepth)
						.correct();
			else {
				assert (toEvaluate.getType().equals("boolean"));
				return new SingleOutputBooleanFitness(evals, (Node<VariableAssignment<Boolean>>) toEvaluate, maxDepth)
						.correct();
			}
		} catch (InterruptedException e) {
			return false;
		}
	}
}
