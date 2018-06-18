package mint.testgen;

import com.microsoft.z3.Z3Exception;
import org.apache.log4j.BasicConfigurator;
import mint.inference.constraints.expression.Atom;
import mint.inference.constraints.expression.Compound;
import mint.inference.constraints.expression.Compound.Rel;
import mint.inference.constraints.expression.Expression;
import mint.inference.constraints.expression.convertors.ExpressionToZ3;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.VariableAssignment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ConstraintSatisfactionTester {

	@Before
	public void setUp() throws Exception {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * (x<50) && (y>20) && (x >12) && ((y==170)||(y<50))
	 * @throws Z3Exception 
	 */
	@Test
	public void testConstraintSatisfactionSatisfiable() throws Z3Exception {
		List<Expression> expressions = new ArrayList<Expression>();
		VariableAssignment<?> va1 = new DoubleVariableAssignment("x", 50D);
		Atom a = new Atom(va1,Atom.Rel.LT);
		VariableAssignment<?> va2 = new DoubleVariableAssignment("y", 20D);
		Atom b = new Atom(va2,Atom.Rel.GT);
		VariableAssignment<?> va3 = new DoubleVariableAssignment("x", 12D);
		Atom c = new Atom(va3,Atom.Rel.GT);
		VariableAssignment<?> va4 = new DoubleVariableAssignment("y", 50D);
		Atom d = new Atom(va4,Atom.Rel.LT);
		VariableAssignment<?> va5 = new DoubleVariableAssignment("y", 170D);
		Atom e = new Atom(va5,Atom.Rel.EQ);
		List<Expression> subList = new ArrayList<Expression>();
		subList.add(d);
		subList.add(e);
		Compound comp = new Compound(subList,Rel.OR);
		expressions.add(a);
		expressions.add(b);
		expressions.add(c);
		expressions.add(comp);
		Compound comp2 = new Compound(expressions,Rel.AND);
		ExpressionToZ3 cs = new ExpressionToZ3(comp2,false);
		assertTrue(cs.solve(false));
	}
	
	@Test
	public void testConstraintSatisfactionUnSatisfiable() throws Z3Exception {
		List<Expression> expressions = new ArrayList<Expression>();
		VariableAssignment<?> va1 = new DoubleVariableAssignment("x", 50D);
		Atom a = new Atom(va1,Atom.Rel.LT);
		VariableAssignment<?> va2 = new DoubleVariableAssignment("y", 20D);
		Atom b = new Atom(va2,Atom.Rel.GT);
		VariableAssignment<?> va3 = new DoubleVariableAssignment("x", 12D);
		Atom c = new Atom(va3,Atom.Rel.GT);
		VariableAssignment<?> va4 = new DoubleVariableAssignment("y", 50D);
		Atom d = new Atom(va4,Atom.Rel.LT);
		VariableAssignment<?> va5 = new DoubleVariableAssignment("y", 170D);
		Atom e = new Atom(va5,Atom.Rel.EQ);
		List<Expression> subList = new ArrayList<Expression>();
		subList.add(d);
		subList.add(e);
		Compound comp = new Compound(subList,Rel.AND);
		expressions.add(a);
		expressions.add(b);
		expressions.add(c);
		expressions.add(comp);
		Compound comp2 = new Compound(expressions,Rel.AND);
		ExpressionToZ3 cs = new ExpressionToZ3(comp2,false);
		assertFalse(cs.solve(false));
	}

}
