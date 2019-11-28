package mint.inference.gp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.microsoft.z3.Context;

import mint.inference.evo.Chromosome;
import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.nonterminals.lists.RootListNonTerminal;
import mint.inference.gp.tree.nonterminals.strings.AssignmentOperator;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 *
 * A random expression generator - generating random tree-shaped expressions for
 * evaluation in a GP context.
 *
 * Created by neilwalkinshaw on 04/03/15.
 */
public class Generator {

	protected Random rand;
	protected List<NonTerminal<?>> dFunctions;
	protected List<VariableTerminal<?>> dTerminals;
	protected List<NonTerminal<?>> iFunctions;
	protected List<VariableTerminal<?>> iTerminals;
	protected List<NonTerminal<?>> sFunctions;
	protected List<VariableTerminal<?>> sTerminals;
	protected List<NonTerminal<?>> bFunctions;
	protected List<VariableTerminal<?>> bTerminals;
	protected AssignmentOperator aop;
	protected int listLength = 0;

	public void setListLength(int length) {
		listLength = length;
	}

	public Generator(Random r) {
		rand = r;
		dFunctions = new ArrayList<NonTerminal<?>>();
		iFunctions = new ArrayList<NonTerminal<?>>();
		dTerminals = new ArrayList<VariableTerminal<?>>();
		iTerminals = new ArrayList<VariableTerminal<?>>();
		sTerminals = new ArrayList<VariableTerminal<?>>();
		sFunctions = new ArrayList<NonTerminal<?>>();
		bTerminals = new ArrayList<VariableTerminal<?>>();
		bFunctions = new ArrayList<NonTerminal<?>>();
		aop = new AssignmentOperator();
	}

	public Random getRandom() {
		return rand;
	}

	public void setDoubleFunctions(List<NonTerminal<?>> doubleFunctions) {
		dFunctions = doubleFunctions;
	}

	public void setIntegerFunctions(List<NonTerminal<?>> intFunctions) {
		iFunctions = intFunctions;
	}

	public void setDoubleTerminals(List<VariableTerminal<?>> doubleTerms) {
		dTerminals = doubleTerms;
	}

	public void setIntegerTerminals(List<VariableTerminal<?>> intFunctions) {
		iTerminals = intFunctions;
	}

	public void setStringTerminals(List<VariableTerminal<?>> sTerms) {
		sTerminals = sTerms;
	}

	public void setStringFunctions(List<NonTerminal<?>> sFunctions) {
		this.sFunctions = sFunctions;
	}

	public void setBooleanTerminals(List<VariableTerminal<?>> bTerms) {
		bTerminals = bTerms;
	}

	public void setBooleanFunctions(List<NonTerminal<?>> bFunctions) {
		this.bFunctions = bFunctions;
	}

	/*
	 * public Chromosome generateRandomExpression(int maxD, List<NonTerminal<?>>
	 * nonTerms, List<VariableTerminal<?>> terms){ if(nonTerms.isEmpty()){ return
	 * selectRandomTerminal(terms); } if((maxD < 2 || rand.nextDouble() <
	 * threshold())&&!terms.isEmpty()){ return selectRandomTerminal(terms); } else
	 * return selectRandomNonTerminal(nonTerms, maxD); }
	 */

	public Chromosome generateRandomExpression(int maxD, List<NonTerminal<?>> nonTerms,
			List<VariableTerminal<?>> terms) {
		if (nonTerms.isEmpty() || maxD < 2) {
			return selectRandomTerminal(terms);
		} else {
			if (rand.nextDouble() > 0.7)
				return selectRandomTerminal(terms);

			NonTerminal<?> selected = nonTerms.get(rand.nextInt(nonTerms.size()));
			return selected.createInstance(this, maxD - 1);
		}
	}

	public boolean populationContains(List<Chromosome> population, Chromosome c1) {
		for (Chromosome c2 : population) {
			if (c1.sameSyntax(c2))
				return true;
		}
		return false;
	}

	public List<Chromosome> generateBooleanPopulation(int size, int maxD) {
		List<Chromosome> population = new ArrayList<Chromosome>();
		for (int i = 0; i < size; i++) {
			population.add(generateRandomBooleanExpression(maxD + 1));
		}
		return population;
	}

	public List<Chromosome> generateDoublePopulation(int size, int maxD) {
		List<Chromosome> population = new ArrayList<Chromosome>();
		for (int i = 0; i < size; i++) {
			population.add(generateRandomDoubleExpression(maxD + 1));
		}
		return population;
	}

	public List<Chromosome> generateIntegerPopulation(int size, int maxD) {
		List<Chromosome> population = new ArrayList<Chromosome>();
		for (int i = 0; i < size; i++) {
			Chromosome instance;
			// We want to make sure the initial population is filled with unique individuals
			// if we can. If there are no nonterminals then we can't do this.
			if (!iFunctions.isEmpty()) {
				do {
					instance = generateRandomIntegerExpression(maxD + 1);

				} while (populationContains(population, instance));
			} else {
				instance = generateRandomIntegerExpression(maxD + 1);
			}
			population.add(instance);
		}
		return population;
	}

	public List<Chromosome> generateStringPopulation(int size, int maxD) {
		List<Chromosome> population = new ArrayList<Chromosome>();
		for (int i = 0; i < size; i++) {
			population.add(generateRandomStringExpression(maxD + 1));
		}
		return population;
	}

	/*
	 * public Chromosome generate(String type, int maxD){ if(type.equals("String"))
	 * return generateRandomStringExpression(maxD); else if(type.equals("Double"))
	 * return generateRandomDoubleExpression(maxD); else if(type.equals("Integer"))
	 * return generateRandomIntegerExpression(maxD); else if(type.equals("Boolean"))
	 * return generateRandomBooleanExpression(maxD); else return null; }
	 */

	@SuppressWarnings("unchecked")
	public Node<DoubleVariableAssignment> generateRandomDoubleExpression(int maxD) {
		return (Node<DoubleVariableAssignment>) generateRandomExpression(maxD, dFunctions, dTerminals).simp();
	}

	@SuppressWarnings("unchecked")
	public Node<StringVariableAssignment> generateRandomStringExpression(int maxD) {
		return (Node<StringVariableAssignment>) generateRandomExpression(maxD, sFunctions, sTerminals).simp();
	}

	@SuppressWarnings("unchecked")
	public Node<IntegerVariableAssignment> generateRandomIntegerExpression(int maxD) {
		return (Node<IntegerVariableAssignment>) generateRandomExpression(maxD, iFunctions, iTerminals).simp();
	}

	@SuppressWarnings("unchecked")
	public Node<BooleanVariableAssignment> generateRandomBooleanExpression(int maxD) {
		Node<BooleanVariableAssignment> individual = (Node<BooleanVariableAssignment>) generateRandomExpression(maxD,
				bFunctions, bTerminals);
		Context ctx = new Context();
		ctx.close();
		return individual.simp();
	}

	public NonTerminal<StringVariableAssignment> generateAssignment() {
		return aop.createInstance(this, 0);
	}

	/*
	 * private Chromosome selectRandomNonTerminal(List<NonTerminal<?>> nodes, int
	 * depth) { int index = rand.nextInt(nodes.size()); NonTerminal<?> selected =
	 * nodes.get(index); return selected.createInstance(this,depth-1); }
	 */

	public Node<? extends VariableAssignment<?>> selectRandomTerminal(List<VariableTerminal<?>> nodes) {
		int index = rand.nextInt(nodes.size());
		VariableTerminal<?> selected = nodes.get(index);

		return selected.copy();
	}

	public List<Chromosome> generateListPopulation(int size, int maxD, String typeString) {
		List<Chromosome> population = new ArrayList<Chromosome>();
		for (int i = 0; i < size; i++) {
			RootListNonTerminal rs = new RootListNonTerminal(typeString);
			population.add(rs.createInstance(this, maxD));
		}
		return population;
	}

	public List<NonTerminal<?>> nonTerms(Datatype s) {
		switch (s) {
		case INTEGER:
			return this.iFunctions;
		case DOUBLE:
			return this.dFunctions;
		case BOOLEAN:
			return this.bFunctions;
		case STRING:
			return this.sFunctions;
		default:
			break;
		}
		throw new IllegalArgumentException("Invaild type " + s);
	}

	public Node<?> generateRandomTerminal(Datatype type) {
		switch (type) {
		case BOOLEAN:
			return selectRandomTerminal(bTerminals);
		case STRING:
			return selectRandomTerminal(sTerminals);
		case INTEGER:
			return selectRandomTerminal(iTerminals);
		case DOUBLE:
			return selectRandomTerminal(dTerminals);
		default:
			break;
		}

		throw new IllegalArgumentException("Datatype must be one of BOOLEAN, STRING, INTEGER, or DOUBLE");
	}

	public Node<?> generateRandomNonTerminal(Datatype[] typeSignature) {
		Datatype returnType = typeSignature[typeSignature.length - 1];
		List<NonTerminal<?>> suitable;

		switch (returnType) {
		case BOOLEAN:
			suitable = bFunctions.stream().filter(f -> Datatype.typeChecks(f.typeSignature(), typeSignature))
					.collect(Collectors.toList());
			return suitable.get(rand.nextInt(suitable.size()));
		case STRING:
			suitable = sFunctions.stream().filter(f -> Datatype.typeChecks(f.typeSignature(), typeSignature))
					.collect(Collectors.toList());
			return suitable.get(rand.nextInt(suitable.size()));
		case INTEGER:
			suitable = iFunctions.stream().filter(f -> Datatype.typeChecks(f.typeSignature(), typeSignature))
					.collect(Collectors.toList());
			return suitable.get(rand.nextInt(suitable.size()));
		case DOUBLE:
			suitable = dFunctions.stream().filter(f -> Datatype.typeChecks(f.typeSignature(), typeSignature))
					.collect(Collectors.toList());
			return suitable.get(rand.nextInt(suitable.size()));
		default:
			break;
		}
		throw new IllegalArgumentException("Datatype must be one of BOOLEAN, STRING, INTEGER, or DOUBLE");
	}
}
