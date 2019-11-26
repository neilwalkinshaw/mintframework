import java.util.ArrayList;
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

public class SRPlayground {

	public static void main(String[] args) {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);

		Generator gpGenerator = new Generator(new Random(0));

		List<NonTerminal<?>> intNonTerms = new ArrayList<NonTerminal<?>>();
		intNonTerms.add(new AddIntegersOperator());
		intNonTerms.add(new SubtractIntegersOperator());
		gpGenerator.setIntegerFunctions(intNonTerms);

		List<VariableTerminal<?>> intTerms = new ArrayList<VariableTerminal<?>>();
		intTerms.add(new IntegerVariableAssignmentTerminal("i0", false));
		intTerms.add(new IntegerVariableAssignmentTerminal("r2", true));
		intTerms.add(new IntegerVariableAssignmentTerminal(0));
		intTerms.add(new IntegerVariableAssignmentTerminal(50));
		intTerms.add(new IntegerVariableAssignmentTerminal(100));
		gpGenerator.setIntegerTerminals(intTerms);

		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> trainingSet = new HashSetValuedHashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();

		List<VariableAssignment<?>> s1 = new ArrayList<VariableAssignment<?>>();
		s1.add(new IntegerVariableAssignment("i0", 50));

		List<VariableAssignment<?>> s2 = new ArrayList<VariableAssignment<?>>();
		s2.add(new IntegerVariableAssignment("i0", 50));

		List<VariableAssignment<?>> s3 = new ArrayList<VariableAssignment<?>>();
		s3.add(new IntegerVariableAssignment("i0", 100));

		trainingSet.put(s1, new IntegerVariableAssignment("o1", 50));
		trainingSet.put(s2, new IntegerVariableAssignment("o1", 100));
		trainingSet.put(s3, new IntegerVariableAssignment("o1", 100));

		System.out.println("Training set: " + trainingSet);
		System.out.println("IntTerms: " + intTerms);
		System.out.println("Int values: " + IntegerVariableAssignment.values());

		LatentVariableGP gp = new LatentVariableGP(gpGenerator, trainingSet, new GPConfiguration(20, 0.9f, 1, 5, 2));

//		AddIntegersOperator seed = new AddIntegersOperator(new IntegerVariableAssignmentTerminal("i0", false),
//				new IntegerVariableAssignmentTerminal("r1", true));
//		gp.addSeed(seed);

		Node<?> best = (Node<?>) gp.evolve(100);
		System.out.println(best + ": " + best.getFitness());
		System.out.println("correct? " + gp.isCorrect(best));

//		System.out.println();
//		for (Chromosome c1 : gp.getPopulation()) {
//			for (Chromosome c2 : gp.getPopulation()) {
//				System.out.print(c1 + " == " + c2 + "? ");
//				System.out.print(c1.sameSyntax(c2) + " ");
//			}
//			System.out.println();
//		}

//		int counter = 0;
//		for (Chromosome c : gp.getPopulation()) {
//			counter++;
//			Node<?> node = (Node<?>) c;
//			LatentVariableFitness<?> fit = new IntegerFitness(trainingSet, (Node<VariableAssignment<Integer>>) node, 0);
//			try {
//				System.out.println(counter + ". " + node + ": " + fit.call());
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}
}
