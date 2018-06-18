package mint.inference.gp;

import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.nonterminals.booleans.*;
import mint.inference.gp.tree.nonterminals.doubles.*;
import mint.inference.gp.tree.nonterminals.integer.CastIntegersOperator;
import mint.inference.gp.tree.terminals.BooleanVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.DoubleVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.VariableAssignment;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by neilwalkinshaw on 18/05/2016.
 */
public class GPFSMInferenceTester {

    @Test
    public void inferenceTester(){
        Generator gpGenerator = new Generator(new Random(0));

        List<NonTerminal<?>> doubleNonTerms = new ArrayList<NonTerminal<?>>();
        doubleNonTerms.add(new AddDoublesOperator());
        doubleNonTerms.add(new SubtractDoublesOperator());
        doubleNonTerms.add(new MultiplyDoublesOperator());
        doubleNonTerms.add(new IfThenElseOperator());
        gpGenerator.setDoubleFunctions(doubleNonTerms);

        List<VariableTerminal<?>> doubleTerms = new ArrayList<VariableTerminal<?>>();
        doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("a"), false));
        doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("b"), false));
        doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("1",1.0), true));
        doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("0.5",0.5), true));
        gpGenerator.setDoubleTerminals(doubleTerms);

        List<NonTerminal<?>> intNonTerms = new ArrayList<NonTerminal<?>>();
        intNonTerms.add(new CastIntegersOperator());
        gpGenerator.setIntegerFunctions(intNonTerms);

        List<NonTerminal<?>> boolNonTerms = new ArrayList<NonTerminal<?>>();
        boolNonTerms.add(new AndBooleanOperator());
        boolNonTerms.add(new OrBooleanOperator());
        boolNonTerms.add(new LTBooleanDoublesOperator());
        boolNonTerms.add(new GTBooleanDoublesOperator());
        boolNonTerms.add(new EQBooleanOperator());
        boolNonTerms.add(new EQArithOperator());
        gpGenerator.setBooleanFunctions(boolNonTerms);

        List<VariableTerminal<?>> boolTerms = new ArrayList<VariableTerminal<?>>();
        VariableAssignment<Boolean> truevar = new BooleanVariableAssignment("true", true);
        BooleanVariableAssignmentTerminal trueterm = new BooleanVariableAssignmentTerminal(truevar, true);
        VariableAssignment<Boolean> falsevar = new BooleanVariableAssignment("false", false);
        BooleanVariableAssignmentTerminal falseterm = new BooleanVariableAssignmentTerminal(falsevar, true);
        boolTerms.add(trueterm);
        boolTerms.add(falseterm);
        gpGenerator.setBooleanTerminals(boolTerms);

        //SingleOutputGP gp = new SingleOutputGP(gpGenerator, generateBooleanTrainingSet(500),new GPConfiguration(600,0.95,0.05,7,10));



        //System.out.println(gp.evolve(20));
    }

}
