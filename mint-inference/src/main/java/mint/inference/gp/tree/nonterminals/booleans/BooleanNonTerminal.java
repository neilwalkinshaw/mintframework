package mint.inference.gp.tree.nonterminals.booleans;

import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.Terminal;
import mint.inference.gp.tree.terminals.BooleanVariableAssignmentTerminal;
import mint.tracedata.types.BooleanVariableAssignment;

/**
 * Created by neilwalkinshaw on 26/05/15.
 */
public abstract class BooleanNonTerminal extends NonTerminal<BooleanVariableAssignment> {

	Node<BooleanVariableAssignment> base;

	public BooleanNonTerminal() {
	}

	@SuppressWarnings("unchecked")
	public BooleanNonTerminal(Node<?> b) {
		this.base = (Node<BooleanVariableAssignment>) b;
	}

	@Override
	public Terminal<BooleanVariableAssignment> getTermFromVals() {
		BooleanVariableAssignment bvar = new BooleanVariableAssignment("res", (Boolean) vals.iterator().next());
		BooleanVariableAssignmentTerminal term = new BooleanVariableAssignmentTerminal(bvar, true, false);
		return term;
	}
}
