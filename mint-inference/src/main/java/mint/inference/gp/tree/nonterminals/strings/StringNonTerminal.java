package mint.inference.gp.tree.nonterminals.strings;

import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.Terminal;
import mint.inference.gp.tree.terminals.StringVariableAssignmentTerminal;
import mint.tracedata.types.StringVariableAssignment;

/**
 * Created by neilwalkinshaw on 06/03/15.
 */
public abstract class StringNonTerminal extends NonTerminal<StringVariableAssignment> {

	@Override
	public String getType() {
		return "string";
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
	public Terminal<StringVariableAssignment> getTermFromVals() {
		StringVariableAssignment svar = new StringVariableAssignment("res", vals.iterator().next().toString());
		StringVariableAssignmentTerminal term = new StringVariableAssignmentTerminal(svar, true, false);
		return term;
	}
}
