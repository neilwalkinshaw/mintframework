package mint.inference.gp.tree.nonterminals.strings;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.StringVariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public class AssignmentOperator extends StringNonTerminal {

	protected String identifier;

	protected static int counter = 0;

	public AssignmentOperator() {
	}

	protected AssignmentOperator(String identifier) {
		super();
		this.identifier = identifier;

	}

	@Override
	public StringVariableAssignment evaluate() throws InterruptedException {
		checkInterrupted();
		StringVariableAssignment svar = new StringVariableAssignment("result", identifier);
		vals.add(svar);
		return svar;
	}

	@Override
	public NonTerminal<StringVariableAssignment> createInstance(Generator g, int depth) {
		counter++;
		return new AssignmentOperator("Assignment" + counter);
	}

	@Override
	public String nodeString() {
		return "identifier = " + identifier;
	}

	@Override
	public String opString() {
		return ":=";
	}

	@Override
	public Expr toZ3(Context ctx) {
		throw new IllegalArgumentException("Cannot do Assignment to z3");
	}

	@Override
	protected NonTerminal<StringVariableAssignment> newInstance() {
		return new AssignmentOperator(identifier);
	}

}
