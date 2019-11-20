package mint.inference.gp.tree.nonterminals.doubles;

import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.Terminal;
import mint.inference.gp.tree.terminals.DoubleVariableAssignmentTerminal;
import mint.tracedata.types.DoubleVariableAssignment;

/**
 * Created by neilwalkinshaw on 06/03/15.
 */
public abstract class DoubleNonTerminal extends NonTerminal<DoubleVariableAssignment> {

	protected DoubleVariableAssignment result = null;

	public DoubleNonTerminal() {
		this.result = new DoubleVariableAssignment("res");
	}

	public void setResVar(DoubleVariableAssignment res) {
		this.result = res;
	}

	protected DoubleVariableAssignment copyResVar() {
		DoubleVariableAssignment dvar = new DoubleVariableAssignment("result", result.getMin(), result.getMax());
		dvar.setEnforcing(true);
		return dvar;
	}

	@Override
	public String getType() {
		return "double";
	}

	@Override
	public Terminal<DoubleVariableAssignment> getTermFromVals() {
		DoubleVariableAssignment dvar = new DoubleVariableAssignment("res", (Double) vals.iterator().next());
		DoubleVariableAssignmentTerminal term = new DoubleVariableAssignmentTerminal(dvar, true, false);
		return term;
	}

}
