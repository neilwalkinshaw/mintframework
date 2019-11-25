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

public class UpdateSRPlayground {

	public static void main(String[] args) {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);

		Generator gpGenerator = new Generator(new Random(3));

		List<NonTerminal<?>> intNonTerms = new ArrayList<NonTerminal<?>>();
		intNonTerms.add(new AddIntegersOperator());
		intNonTerms.add(new SubtractIntegersOperator());
		gpGenerator.setIntegerFunctions(intNonTerms);

		List<VariableTerminal<?>> intTerms = new ArrayList<VariableTerminal<?>>();
		intTerms.add(new IntegerVariableAssignmentTerminal("i0", false));
		intTerms.add(new IntegerVariableAssignmentTerminal("r2", false));
		intTerms.add(new IntegerVariableAssignmentTerminal(0));
		intTerms.add(new IntegerVariableAssignmentTerminal(50));
		intTerms.add(new IntegerVariableAssignmentTerminal(100));
		gpGenerator.setIntegerTerminals(intTerms);

		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> trainingSet = new HashSetValuedHashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();

		List<VariableAssignment<?>> s1 = new ArrayList<VariableAssignment<?>>();
		s1.add(new IntegerVariableAssignment("i0", 50));
		s1.add(new IntegerVariableAssignment("r2", 0));

		trainingSet.put(s1, new IntegerVariableAssignment("r2", 50));

		System.out.println("Training set: " + trainingSet);
		System.out.println("IntTerms: " + intTerms);
		System.out.println("Int values: " + IntegerVariableAssignment.values());

		LatentVariableGP gp = new LatentVariableGP(gpGenerator, trainingSet, new GPConfiguration(9, 0.9f, 0.01f, 5, 2));

//		IntegerVariableAssignmentTerminal seed = new IntegerVariableAssignmentTerminal(50);
//		gp.addSeed(seed);

		Node<?> best = (Node<?>) gp.evolve(1);
		System.out.println(best + ": " + best.getFitness());
		System.out.println("correct? " + gp.isCorrect(best));
	}
}
