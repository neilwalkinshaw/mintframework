import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.microsoft.z3.Z3Exception;

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

	public static void main(String[] args) throws Z3Exception {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);

		Generator gpGenerator = new Generator(new Random(0));

		List<NonTerminal<?>> intNonTerms = new ArrayList<NonTerminal<?>>();
		intNonTerms.add(new AddIntegersOperator());
		intNonTerms.add(new SubtractIntegersOperator());
		gpGenerator.setIntegerFunctions(intNonTerms);

		List<VariableTerminal<?>> intTerms = new ArrayList<VariableTerminal<?>>();
		intTerms.add(new IntegerVariableAssignmentTerminal("i0"));
		intTerms.add(new IntegerVariableAssignmentTerminal("r1"));
		intTerms.add(new IntegerVariableAssignmentTerminal(50));
		intTerms.add(new IntegerVariableAssignmentTerminal(100));
//		intTerms.add(new IntegerVariableAssignmentTerminal(10));
		intTerms.add(new IntegerVariableAssignmentTerminal(0));
		gpGenerator.setIntegerTerminals(intTerms);

		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> trainingSet = new HashSetValuedHashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();

		IntegerVariableAssignment o150 = new IntegerVariableAssignment("o1", 50);
		IntegerVariableAssignment o1100 = new IntegerVariableAssignment("o1", 100);

		List<VariableAssignment<?>> s1 = new ArrayList<VariableAssignment<?>>();
		s1.add(new IntegerVariableAssignment("i0", 50));

		List<VariableAssignment<?>> s2 = new ArrayList<VariableAssignment<?>>();
		s2.add(new IntegerVariableAssignment("i0", 50));

		List<VariableAssignment<?>> s3 = new ArrayList<VariableAssignment<?>>();
		s3.add(new IntegerVariableAssignment("i0", 100));

		trainingSet.put(s1, o150);
		trainingSet.put(s2, o1100);
		trainingSet.put(s3, o1100);

//		IntegerVariableAssignment o160 = new IntegerVariableAssignment("o1", 60);

//		List<VariableAssignment<?>> s1 = new ArrayList<VariableAssignment<?>>();
//		s1.add(new IntegerVariableAssignment("i0", 40));
//
//		List<VariableAssignment<?>> s2 = new ArrayList<VariableAssignment<?>>();
//		s2.add(new IntegerVariableAssignment("i0", 50));
//
//		List<VariableAssignment<?>> s3 = new ArrayList<VariableAssignment<?>>();
//		s3.add(new IntegerVariableAssignment("i0", 90));
//
//		trainingSet.put(s1, o150);
//		trainingSet.put(s2, o160);
//		trainingSet.put(s3, o1100);

		System.out.println("Training set: " + trainingSet);
		System.out.println("IntTerms: " + intTerms);
		System.out.println("Int values: " + IntegerVariableAssignment.values());

		LatentVariableGP gp = new LatentVariableGP(gpGenerator, trainingSet,
				new GPConfiguration(20, 0.9f, 0.01f, 7, 7));

		Node<?> best = (Node<?>) gp.evolve(40);
		best.simplify();
		System.out.println(best);
	}

}
