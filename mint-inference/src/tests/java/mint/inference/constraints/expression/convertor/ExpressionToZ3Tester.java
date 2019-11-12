package mint.inference.constraints.expression.convertor;

import com.microsoft.z3.Z3Exception;
import mint.inference.constraints.expression.Atom;
import mint.inference.constraints.expression.Compound;
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

public class ExpressionToZ3Tester {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCompoundDistancesSat() {
		List<Expression> expressions = new ArrayList<Expression>();
		VariableAssignment<?> va1 = new DoubleVariableAssignment("x",50D);
		Atom a = new Atom(va1,Atom.Rel.LT);
		VariableAssignment<?> va2 = new DoubleVariableAssignment("y", 20D);
		Atom b = new Atom(va2,Atom.Rel.GT);
		VariableAssignment<?> va3 = new DoubleVariableAssignment("x",12D);
		Atom c = new Atom(va3,Atom.Rel.GT);
		//x < 50 and y > 20 and x > 12
		expressions.add(a);
		expressions.add(b);
		expressions.add(c);
		Compound comp = new Compound(expressions,Compound.Rel.AND);
		ExpressionToZ3 conv = new ExpressionToZ3(comp,true);
		conv.solve(false);
		
	}
}
