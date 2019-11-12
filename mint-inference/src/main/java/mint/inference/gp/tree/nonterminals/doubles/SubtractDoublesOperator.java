package mint.inference.gp.tree.nonterminals.doubles;

import com.microsoft.z3.ArithExpr;
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
public class SubtractDoublesOperator extends DoubleNonTerminal {

	public SubtractDoublesOperator() {
	}

	public SubtractDoublesOperator(Node<DoubleVariableAssignment> a, Node<DoubleVariableAssignment> b) {
		super();
		addChild(a);
		addChild(b);
	}

	@Override
	public DoubleVariableAssignment evaluate() throws InterruptedException {
		checkInterrupted();
		DoubleVariableAssignment res = copyResVar();
		res.setValue((Double) getChild(0).evaluate().getValue() - (Double) getChild(1).evaluate().getValue());
		vals.add(res.getValue());
		return res;
	}

	@Override
	public NonTerminal<DoubleVariableAssignment> createInstance(Generator g, int depth) {
		SubtractDoublesOperator sdo = new SubtractDoublesOperator(g.generateRandomDoubleExpression(depth),
				g.generateRandomDoubleExpression(depth));
		sdo.setResVar(copyResVar());
		return sdo;
	}

	@Override
	public String nodeString() {
		return "Subtract(" + childrenString() + ")";
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
		return "-";
	}

	@Override
	public Expr toZ3(Context ctx) {
		return ctx.mkSub((ArithExpr) getChild(0).toZ3(ctx), (ArithExpr) getChild(1).toZ3(ctx));
	}

	@Override
	protected NonTerminal<DoubleVariableAssignment> newInstance() {
		return new SubtractDoublesOperator();
	}
}
