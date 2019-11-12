package mint.inference.gp.tree.nonterminals.integers;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.IntegerVariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public class AddIntegersOperator extends IntegerNonTerminal {
	public AddIntegersOperator() {
	}

	public AddIntegersOperator(Node<IntegerVariableAssignment> a, Node<IntegerVariableAssignment> b) {
		super();
		addChild(a);
		addChild(b);
	}

	@Override
	public IntegerVariableAssignment evaluate() throws InterruptedException {
		checkInterrupted();
		IntegerVariableAssignment childRes1 = null;
		IntegerVariableAssignment childRes2 = null;
		Integer c1 = null;
		Integer c2 = null;
		try {
			childRes1 = (IntegerVariableAssignment) getChild(0).evaluate();
			childRes2 = (IntegerVariableAssignment) getChild(1).evaluate();
			c1 = childRes1.getValue();
			c2 = childRes2.getValue();
			IntegerVariableAssignment res = copyResVar();
			res.setValue(c1 + c2);
			vals.add(res.getValue());
			return res;
		} catch (Exception e) {
		}

		return null;
	}

	@Override
	public NonTerminal<IntegerVariableAssignment> createInstance(Generator g, int depth) {
		IntegerNonTerminal created = new AddIntegersOperator(g.generateRandomIntegerExpression(depth),
				g.generateRandomIntegerExpression(depth));
		created.setResVar(copyResVar());
		return created;
	}

	@Override
	public String nodeString() {
		return "Add(" + childrenString() + ")";
	}

	@Override
	public boolean accept(NodeVisitor visitor) throws InterruptedException {
		if (visitor.visitEnter(this)) {
			visitChildren(visitor);
		}
		return visitor.visitExit(this);
	}

	@Override
	public String opString() {
		return "+";
	}

	@Override
	public Expr toZ3(Context ctx) {
		return ctx.mkAdd((ArithExpr) getChild(0).toZ3(ctx), (ArithExpr) getChild(1).toZ3(ctx));
	}

	@Override
	protected NonTerminal<IntegerVariableAssignment> newInstance() {
		return new AddIntegersOperator();
	}
}
