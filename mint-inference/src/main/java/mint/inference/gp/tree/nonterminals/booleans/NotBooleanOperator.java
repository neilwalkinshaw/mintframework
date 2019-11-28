package mint.inference.gp.tree.nonterminals.booleans;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.BooleanVariableAssignment;

/**
 * Created by neilwalkinshaw on 26/05/15.
 */
public class NotBooleanOperator extends BooleanNonTerminal {

	public NotBooleanOperator(Node<?> a) {
		super(null);
		addChild(a);
	}

	public NotBooleanOperator() {
		super();
	}

	@Override
	public NonTerminal<BooleanVariableAssignment> createInstance(Generator g, int depth) {
		return new NotBooleanOperator(g.generateRandomBooleanExpression(depth));
	}

	@Override
	protected String nodeString() {
		return "(! " + childrenString() + ")";
	}

	@Override
	public boolean accept(NodeVisitor visitor) throws InterruptedException {
		visitor.visitEnter(this);
		for (Node<?> child : children) {
			child.accept(visitor);
		}
		return visitor.visitExit(this);
	}

	@Override
	public BooleanVariableAssignment evaluate() throws InterruptedException {
		checkInterrupted();
		Boolean from = (Boolean) children.get(0).evaluate().getValue();
		BooleanVariableAssignment res = new BooleanVariableAssignment("result", !from);
		vals.add(res.getValue());
		return res;
	}

	@Override
	public String opString() {
		return "not";
	}

	@Override
	public Expr toZ3(Context ctx) {
		return ctx.mkNot((BoolExpr) getChild(0).toZ3(ctx));
	}

	@Override
	protected NonTerminal<BooleanVariableAssignment> newInstance() {
		return new NotBooleanOperator();
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.BOOLEAN, Datatype.BOOLEAN };
	}

}
