package mint.inference.gp.tree.nonterminals.integers;

import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.Terminal;
import mint.inference.gp.tree.terminals.IntegerVariableAssignmentTerminal;
import mint.tracedata.types.IntegerVariableAssignment;

/**
 * Created by neilwalkinshaw on 06/03/15.
 */
public abstract class IntegerNonTerminal extends NonTerminal<IntegerVariableAssignment> {

	protected IntegerVariableAssignment result = null;

	public IntegerNonTerminal() {
		this.result = new IntegerVariableAssignment("res", Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Override
	public String getType() {
		return "integer";
	}

	public void setResVar(IntegerVariableAssignment res) {
		this.result = res;
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
	public Terminal<IntegerVariableAssignment> getTermFromVals() {
		IntegerVariableAssignment ivar = new IntegerVariableAssignment("res", (Integer) vals.iterator().next());
		IntegerVariableAssignmentTerminal term = new IntegerVariableAssignmentTerminal(ivar, true);
		return term;
	}

	protected IntegerVariableAssignment copyResVar() {
		IntegerVariableAssignment ivar = new IntegerVariableAssignment("result", result.getMin(), result.getMax());
		ivar.setEnforcing(true);
		return ivar;
	}
}
