package mint.inference.gp.tree.nonterminals.integers;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.IntegerVariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public class SubtractIntegersOperator extends IntegerNonTerminal {

	public SubtractIntegersOperator() {
	}

	public SubtractIntegersOperator(Node<IntegerVariableAssignment> a, Node<IntegerVariableAssignment> b) {
		super();
		addChild(a);
		addChild(b);
	}

	@Override
	public IntegerVariableAssignment evaluate() throws InterruptedException {
		checkInterrupted();
		IntegerVariableAssignment res = copyResVar();
		res.setValue((Integer) getChild(0).evaluate().getValue() - (Integer) getChild(1).evaluate().getValue());
		vals.add(res.getValue());
		return res;
	}

	@Override
	public Node<IntegerVariableAssignment> copy() {
		IntegerNonTerminal created = new AddIntegersOperator();
		for (Node<?> child : getChildren()) {
			created.addChild(child.copy());
		}
		created.setResVar(copyResVar());
		return created;
	}

	@Override
	public NonTerminal<IntegerVariableAssignment> createInstance(Generator g, int depth) {
		SubtractIntegersOperator sdo = new SubtractIntegersOperator(g.generateRandomIntegerExpression(depth),
				g.generateRandomIntegerExpression(depth));
		sdo.setResVar(copyResVar());
		return sdo;
	}

	@Override
	public String nodeString() {
		return "(- " + childrenString() + ")";
	}

	@Override
	public boolean accept(NodeVisitor visitor) throws InterruptedException {
		if (visitor.visitEnter(this)) {
			visitChildren(visitor);
		}
		return visitor.visitExit(this);
	}
}
