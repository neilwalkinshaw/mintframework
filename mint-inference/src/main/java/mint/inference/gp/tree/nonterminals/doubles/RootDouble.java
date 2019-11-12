package mint.inference.gp.tree.nonterminals.doubles;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.DoubleVariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public class RootDouble extends DoubleNonTerminal {

	public RootDouble() {
	}

	public RootDouble(Node<DoubleVariableAssignment> a) {
		super();
		addChild(a);
	}

	@Override
	public void simplify() {
		for (Node<?> child : getChildren()) {
			child.simplify();
		}
	}

	@Override
	public NonTerminal<DoubleVariableAssignment> createInstance(Generator g, int depth) {
		return new RootDouble(g.generateRandomDoubleExpression(depth));
	}

	@Override
	public DoubleVariableAssignment evaluate() throws InterruptedException {
		checkInterrupted();
		DoubleVariableAssignment res = new DoubleVariableAssignment("result",
				(Double) getChild(0).evaluate().getValue());
		vals.add(res.getValue());
		return res;
	}

	@Override
	public String nodeString() {
		return "R:" + childrenString();
	}

	@Override
	public boolean accept(NodeVisitor visitor) throws InterruptedException {
		if (visitor.visitEnter(this)) {
			for (Node<?> child : children) {
				child.accept(visitor);
			}
		}
		return visitor.visitExit(this);
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
	protected NonTerminal<DoubleVariableAssignment> newInstance() {
		return new RootDouble();
	}

}
