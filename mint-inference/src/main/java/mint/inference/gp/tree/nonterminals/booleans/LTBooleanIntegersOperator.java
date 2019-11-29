package mint.inference.gp.tree.nonterminals.booleans;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.IntegerVariableAssignment;

/**
 * Created by neilwalkinshaw on 26/05/15.
 */
public class LTBooleanIntegersOperator extends BooleanNonTerminal {

	public LTBooleanIntegersOperator(Node<?> b) {
		super(b);
	}

	public LTBooleanIntegersOperator(Node<IntegerVariableAssignment> a, Node<IntegerVariableAssignment> b) {
		super(null);
		addChild(a);
		addChild(b);
	}

	public LTBooleanIntegersOperator() {

	}

	@Override
	public NonTerminal<BooleanVariableAssignment> createInstance(Generator g, int depth) {
		return new LTBooleanIntegersOperator(g.generateRandomIntegerExpression(depth),
				g.generateRandomIntegerExpression(depth));
	}

	@Override
	protected String nodeString() {
		return "LT(" + childrenString() + ")";
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
				(Integer) children.get(0).evaluate().getValue() < (Integer) children.get(1).evaluate().getValue());
		vals.add(res.getValue());
		return res;
	}

	@Override
	public String opString() {
		return "<";
	}

	@Override
	public Expr toZ3(Context ctx) {
		return ctx.mkLt((ArithExpr) getChild(0).toZ3(ctx), (ArithExpr) getChild(1).toZ3(ctx));
	}

	@Override
	protected NonTerminal<BooleanVariableAssignment> newInstance() {
		return new LTBooleanIntegersOperator();
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.INTEGER, Datatype.INTEGER, Datatype.BOOLEAN };
	}
}
