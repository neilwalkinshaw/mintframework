package mint.inference.gp;

import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.nonterminals.doubles.*;
import mint.inference.gp.tree.nonterminals.integers.CastIntegersOperator;
import mint.inference.gp.tree.terminals.DoubleVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.tracedata.types.DoubleVariableAssignment;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneratorTester {


	@Before
	public void setUp() throws Exception {

	}



    @Test
    public void testZeroDepthDoubles() throws InterruptedException {
        Generator gpGenerator = new Generator(new Random(0));

        List<NonTerminal<?>> doubleNonTerms = new ArrayList<NonTerminal<?>>();
        doubleNonTerms.add(new AddDoublesOperator());
        doubleNonTerms.add(new SubtractDoublesOperator());
        doubleNonTerms.add(new MultiplyDoublesOperator());
        doubleNonTerms.add(new CosDoublesOperator());

        gpGenerator.setDoubleFunctions(doubleNonTerms);

        List<VariableTerminal<?>> doubleTerms = new ArrayList<VariableTerminal<?>>();
        doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("x",2D), false));
        doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("y",4D), false));
        gpGenerator.setDoubleTerminals(doubleTerms);

        Node<DoubleVariableAssignment> exp = gpGenerator.generateRandomDoubleExpression(0);

        System.out.println(exp);
        System.out.println(exp.evaluate());

    }

    @Test
    public void testMedDepthDoubles() throws InterruptedException {
        Generator gpGenerator = new Generator(new Random(0));

        List<NonTerminal<?>> doubleNonTerms = new ArrayList<NonTerminal<?>>();
        doubleNonTerms.add(new AddDoublesOperator());
        doubleNonTerms.add(new SubtractDoublesOperator());
        doubleNonTerms.add(new MultiplyDoublesOperator());
        doubleNonTerms.add(new CosDoublesOperator());

        gpGenerator.setDoubleFunctions(doubleNonTerms);

        List<VariableTerminal<?>> doubleTerms = new ArrayList<VariableTerminal<?>>();
        doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("x", 2D), false));
        doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("y", 4D), false));
        gpGenerator.setDoubleTerminals(doubleTerms);

        Node<DoubleVariableAssignment> exp = gpGenerator.generateRandomDoubleExpression(11);

        System.out.println(exp);
        System.out.println(exp.evaluate());

    }

    @Test
    public void testMedDepthAll() throws InterruptedException {
        Generator gpGenerator = new Generator(new Random(0));

        List<NonTerminal<?>> doubleNonTerms = new ArrayList<NonTerminal<?>>();
        doubleNonTerms.add(new AddDoublesOperator());
        doubleNonTerms.add(new SubtractDoublesOperator());
        doubleNonTerms.add(new MultiplyDoublesOperator());
        doubleNonTerms.add(new CosDoublesOperator());
        doubleNonTerms.add(new CastDoublesOperator());
        gpGenerator.setDoubleFunctions(doubleNonTerms);

        List<VariableTerminal<?>> doubleTerms = new ArrayList<VariableTerminal<?>>();
        doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("a", 2D), false));
        doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("b", 4D), false));
        gpGenerator.setDoubleTerminals(doubleTerms);

        List<NonTerminal<?>> integerNonTerms = new ArrayList<NonTerminal<?>>();

        integerNonTerms.add(new CastIntegersOperator());
        gpGenerator.setIntegerFunctions(integerNonTerms);


        Node<DoubleVariableAssignment> exp = gpGenerator.generateRandomDoubleExpression(11);

        System.out.println(exp);
        System.out.println(exp.evaluate());

    }


	

}
