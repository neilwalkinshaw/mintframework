import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import mint.inference.evo.GPConfiguration;
import mint.inference.gp.Generator;
import mint.inference.gp.LatentVariableGP;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.nonterminals.integers.AddIntegersOperator;
import mint.inference.gp.tree.nonterminals.integers.SubtractIntegersOperator;
import mint.inference.gp.tree.terminals.IntegerVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.VariableAssignment;

public class UpdateSRPlayground {

	static MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> trainingSet = new HashSetValuedHashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();

	private static void scenario(int i0, int r2, int r2_prime) {
		trainingSet.put(Arrays.asList(new IntegerVariableAssignment("i0", i0), new IntegerVariableAssignment("r2", r2)),
				new IntegerVariableAssignment("r2", r2_prime));
	}

	public static void main(String[] args) {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);

		Generator gpGenerator = new Generator(new Random(7));

		List<NonTerminal<?>> intNonTerms = new ArrayList<NonTerminal<?>>();
		intNonTerms.add(new AddIntegersOperator());
		intNonTerms.add(new SubtractIntegersOperator());
		gpGenerator.setIntegerFunctions(intNonTerms);

		List<VariableTerminal<?>> intTerms = new ArrayList<VariableTerminal<?>>();
		intTerms.add(new IntegerVariableAssignmentTerminal("i0", false));
		intTerms.add(new IntegerVariableAssignmentTerminal("r2", false));
		intTerms.add(new IntegerVariableAssignmentTerminal(0));
		intTerms.add(new IntegerVariableAssignmentTerminal(30));
		intTerms.add(new IntegerVariableAssignmentTerminal(70));
		intTerms.add(new IntegerVariableAssignmentTerminal(90));
		intTerms.add(new IntegerVariableAssignmentTerminal(10));
		intTerms.add(new IntegerVariableAssignmentTerminal(20));
		intTerms.add(new IntegerVariableAssignmentTerminal(40));
		intTerms.add(new IntegerVariableAssignmentTerminal(60));
		intTerms.add(new IntegerVariableAssignmentTerminal(80));
		intTerms.add(new IntegerVariableAssignmentTerminal(50));
		intTerms.add(new IntegerVariableAssignmentTerminal(100));
		gpGenerator.setIntegerTerminals(intTerms);

		scenario(50, 0, 50);
		scenario(20, 0, 20);
		scenario(20, 20, 40);
		scenario(20, 40, 60);
		scenario(20, 60, 80);
		scenario(20, 50, 70);
		scenario(20, 70, 90);
		scenario(10, 0, 10);
		scenario(20, 10, 30);
		scenario(20, 30, 50);
		scenario(10, 50, 60);
		scenario(10, 60, 70);
		scenario(50, 10, 60);
		scenario(50, 20, 70);

		System.out.println("Training set: " + trainingSet);
		System.out.println("IntTerms: " + intTerms);
		System.out.println("Int values: " + IntegerVariableAssignment.values());

		LatentVariableGP gp = new LatentVariableGP(gpGenerator, trainingSet, new GPConfiguration(20, 0.9f, 1f, 5, 2));

//		IntegerVariableAssignmentTerminal seed = new IntegerVariableAssignmentTerminal(50);
//		gp.addSeed(seed);

		Node<?> best = (Node<?>) gp.evolve(50);
		System.out.println(best + ": " + best.getFitness());
		System.out.println("correct? " + gp.isCorrect(best));
	}
}
