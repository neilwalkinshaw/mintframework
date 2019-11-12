package mint.inference.gp.tree.nonterminals.strings;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.StringVariableAssignment;

/**
 * Created by neilwalkinshaw on 16/03/16.
 */
public class RootString extends StringNonTerminal {

	public RootString() {
	}

	public RootString(Node<StringVariableAssignment> a) {
		super();
		addChild(a);
	}

	@Override
	public NonTerminal<StringVariableAssignment> createInstance(Generator g, int depth) {
		return new RootString(g.generateRandomStringExpression(depth + 1));
	}

	@Override
	public StringVariableAssignment evaluate() throws InterruptedException {
		checkInterrupted();
		Object obj = getChild(0).evaluate().getValue();
		StringVariableAssignment res = new StringVariableAssignment("result", (String) obj);
		vals.add(res.getValue());
		return res;
	}

	@Override
	public void simplify() {
		for (Node<?> child : getChildren()) {
			child.simplify();
		}
	}

	@Override
	public String nodeString() {
		return childrenString();
	}

	@Override
	public int depth() {
		return 0;
	}

	@Override
	public String opString() {
		return "";
	}

	@Override
	public Expr toZ3(Context ctx) {
		return getChild(0).toZ3(ctx);
	}

	@Override
	protected NonTerminal<StringVariableAssignment> newInstance() {
		return new RootString();
	}

}
