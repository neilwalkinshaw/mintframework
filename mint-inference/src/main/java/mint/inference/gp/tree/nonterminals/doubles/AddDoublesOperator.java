package mint.inference.gp.tree.nonterminals.doubles;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.DoubleVariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public class AddDoublesOperator extends DoubleNonTerminal {

	public AddDoublesOperator() {
	}

	public AddDoublesOperator(Node<DoubleVariableAssignment> a, Node<DoubleVariableAssignment> b) {
		super();
		addChild(a);
		addChild(b);
	}

	@Override
	public DoubleVariableAssignment evaluate() throws InterruptedException {
		checkInterrupted();
		DoubleVariableAssignment childRes1 = null;
		DoubleVariableAssignment childRes2 = null;
		Double c1 = null;
		Double c2 = null;
		try {
			childRes1 = (DoubleVariableAssignment) getChild(0).evaluate();
			childRes2 = (DoubleVariableAssignment) getChild(1).evaluate();
			c1 = childRes1.getValue();
			c2 = childRes2.getValue();
			DoubleVariableAssignment res = copyResVar();
			res.setValue(c1 + c2);
			vals.add(res.getValue());
			return res;
		} catch (Exception e) {
			System.out.println(getChild(0).evaluate());
			System.out.println(getChild(1).evaluate());
		}

		return null;
	}

	@Override
	public NonTerminal<DoubleVariableAssignment> createInstance(Generator g, int depth) {
		DoubleNonTerminal created = new AddDoublesOperator(g.generateRandomDoubleExpression(depth),
				g.generateRandomDoubleExpression(depth));
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
	protected NonTerminal<DoubleVariableAssignment> newInstance() {
		return new AddDoublesOperator();
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.DOUBLE, Datatype.DOUBLE, Datatype.DOUBLE };
	}
}
