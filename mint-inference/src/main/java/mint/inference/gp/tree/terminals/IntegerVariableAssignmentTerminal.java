package mint.inference.gp.tree.terminals;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.Terminal;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 04/03/15.
 */
public class IntegerVariableAssignmentTerminal extends VariableTerminal<IntegerVariableAssignment> {

	protected int origVal;

	public IntegerVariableAssignmentTerminal(VariableAssignment<Integer> var, boolean constant) {
		super(constant);
		if (var.getValue() != null)
			origVal = var.getValue();
		this.terminal = (IntegerVariableAssignment) var;
	}

	// For initialising constants
	public IntegerVariableAssignmentTerminal(int value) {
		super(true);
		IntegerVariableAssignment var = new IntegerVariableAssignment(String.valueOf(value), value, true);
		this.terminal = var;
	}

	// For initialising variables
	public IntegerVariableAssignmentTerminal(String name) {
		super(false);
		IntegerVariableAssignment var = new IntegerVariableAssignment(name);
		this.terminal = var;
	}

	@Override
	public void setValue(Object val) {

		if (val instanceof Integer) {
			Integer intval = (Integer) val;
			terminal.setValue(intval);
		}
	}

	@Override
	protected Terminal<IntegerVariableAssignment> getTermFromVals() {
		IntegerVariableAssignment ivar = new IntegerVariableAssignment("res", (Integer) vals.iterator().next());
		IntegerVariableAssignmentTerminal term = new IntegerVariableAssignmentTerminal(ivar, true);
		return term;
	}

	@Override
	public void mutate(Generator g, int depth) {
		if (!this.isConstant()) {
			int limit = depth;
			if (limit == 0)
				limit++;
			swapWith(g.generateRandomIntegerExpression(g.getRandom().nextInt(limit)));
		} else if (this.isConstant()) {
			terminal.setToRandom();
		}
	}

	@Override
	public Terminal<IntegerVariableAssignment> copy() {
		VariableAssignment<Integer> copied = terminal.copy();
		return new IntegerVariableAssignmentTerminal(copied, constant);
	}

	@Override
	public String getType() {
		return "integer";
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
			terminal.setValue(0);
		}
	}

	@Override
	public Expr toZ3(Context ctx) {
		if (this.isConstant()) {
			return ctx.mkInt(this.getTerminal().getValue());
		}
		return ctx.mkIntConst(this.getName());
	}
}
