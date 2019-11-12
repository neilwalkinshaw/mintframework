package mint.inference.gp.tree.nonterminals.booleans;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.DoubleVariableAssignment;

/**
 * Created by neilwalkinshaw on 26/05/15.
 */
public class GTBooleanDoublesOperator extends BooleanNonTerminal {

	public GTBooleanDoublesOperator(Node<?> b) {
		super(b);
	}

	public GTBooleanDoublesOperator(Node<DoubleVariableAssignment> a, Node<DoubleVariableAssignment> b) {
		super(null);
		addChild(a);
		addChild(b);
	}

	public GTBooleanDoublesOperator() {
	}

	@Override
	public NonTerminal<BooleanVariableAssignment> createInstance(Generator g, int depth) {
		return new GTBooleanDoublesOperator(g.generateRandomDoubleExpression(depth),
				g.generateRandomDoubleExpression(depth));
	}

	@Override
	protected String nodeString() {
		return "GT(" + childrenString() + ")";
	}

	@Override
	public boolean accept(NodeVisitor visitor) throws InterruptedException {
		if (visitor.visitEnter(this)) {
			visitChildren(visitor);
		}
		return visitor.visitExit(this);
	}

	@Override
	public BooleanVariableAssignment evaluate() throws InterruptedException {
		checkInterrupted();
		BooleanVariableAssignment res = new BooleanVariableAssignment("result",
				(Double) children.get(0).evaluate().getValue() > (Double) children.get(1).evaluate().getValue());
		vals.add(res.getValue());
		return res;
	}

	@Override
	public String opString() {
		return ">";
	}

	@Override
	public Expr toZ3(Context ctx) {
		return ctx.mkGt((ArithExpr) getChild(0).toZ3(ctx), (ArithExpr) getChild(1).toZ3(ctx));
	}

	@Override
	protected NonTerminal<BooleanVariableAssignment> newInstance() {
		return new GTBooleanDoublesOperator();
	}
}
