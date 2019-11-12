package mint.inference.gp.tree.terminals;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.Terminal;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 26/05/15.
 */
public class BooleanVariableAssignmentTerminal extends VariableTerminal<BooleanVariableAssignment> {

	public BooleanVariableAssignmentTerminal(VariableAssignment<Boolean> var, boolean constant) {
		super(constant);
		this.terminal = (BooleanVariableAssignment) var;
	}

	@Override
	public void mutate(Generator g, int depth) {
		if (depth == 0)
			swapWith(g.generateRandomBooleanExpression(1));
		else
			swapWith(g.generateRandomBooleanExpression(g.getRandom().nextInt(depth)));
	}

	@Override
	public Terminal<BooleanVariableAssignment> copy() {
		VariableAssignment<Boolean> copied = terminal.copy();
		return new BooleanVariableAssignmentTerminal(copied, constant);
	}

	@Override
	public String getType() {
		return "boolean";
	}

	@Override
	public boolean accept(NodeVisitor visitor) {
		visitor.visitEnter(this);
		return visitor.visitExit(this);
	}

	@Override
	public void reset() {
		super.reset();
		if (!isConstant()) {
			terminal.setValue(false);
		}
	}

	@Override
	public void setValue(Object val) {
		terminal.setValue((Boolean) val);
	}

	@Override
	protected Terminal<BooleanVariableAssignment> getTermFromVals() {
		BooleanVariableAssignment bvar = new BooleanVariableAssignment("res", (Boolean) vals.iterator().next());
		BooleanVariableAssignmentTerminal term = new BooleanVariableAssignmentTerminal(bvar, true);
		return term;
	}

	@Override
	public Expr toZ3(Context ctx) {
		if (this.isConstant()) {
			return ctx.mkBool(this.getTerminal().getValue());
		}
		return ctx.mkBoolConst(this.getName());
	}
}
