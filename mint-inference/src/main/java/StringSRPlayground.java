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
import mint.inference.gp.tree.terminals.StringVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;

public class StringSRPlayground {

	public static void main(String[] args) {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);

		Generator gpGenerator = new Generator(new Random(0));

		List<VariableTerminal<?>> stringTerms = new ArrayList<VariableTerminal<?>>();
		stringTerms.add(new StringVariableAssignmentTerminal(new StringVariableAssignment("r1"), false));
		stringTerms.add(new StringVariableAssignmentTerminal("coke"));
		stringTerms.add(new StringVariableAssignmentTerminal("pepsi"));
		stringTerms.add(new StringVariableAssignmentTerminal("beer"));
		gpGenerator.setStringTerminals(stringTerms);

		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> trainingSet = new HashSetValuedHashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();
		trainingSet.put(new ArrayList<VariableAssignment<?>>(), new StringVariableAssignment("o1", "coke"));
		trainingSet.put(new ArrayList<VariableAssignment<?>>(), new StringVariableAssignment("o1", "pepsi"));
		trainingSet.put(new ArrayList<VariableAssignment<?>>(), new StringVariableAssignment("o1", "beer"));

		System.out.println(trainingSet);

		LatentVariableGP gp = new LatentVariableGP(gpGenerator, trainingSet,
				new GPConfiguration(10, 0.9f, 0.01f, 7, 7));

		Node<?> best = (Node<?>) gp.evolve(40);
		best.simplify();

		for (VariableAssignment<?> var : best.varsInTree()) {
			System.out.println(var.getName() + "->" + var.typeString());
		}

		System.out.println(best);
		System.out.println(best.simp());
		System.out.println(gp.isCorrect(best));

	}

}
