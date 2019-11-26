package mint.inference.gp.tree.terminals;

import java.util.HashSet;
import java.util.Set;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.Terminal;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 04/03/15.
 */
public class DoubleVariableAssignmentTerminal extends VariableTerminal<DoubleVariableAssignment> {

	double origVal;

	public DoubleVariableAssignmentTerminal(VariableAssignment<Double> var, boolean constant, boolean latent) {
		super(constant, latent);
		if (var.getValue() != null)
			origVal = var.getValue();
		this.terminal = (DoubleVariableAssignment) var;
	}

	@Override
	public void setValue(Object val) {
		if (val instanceof Double)
			terminal.setValue((Double) val);
		else if (val instanceof Integer) {
			Integer intval = (Integer) val;
			Double doubVal = (double) intval.intValue();
			terminal.setValue(doubVal);
		}
	}

	@Override
	protected Terminal<DoubleVariableAssignment> getTermFromVals() {
		DoubleVariableAssignment dvar = new DoubleVariableAssignment("res", (Double) vals.iterator().next());
		DoubleVariableAssignmentTerminal term = new DoubleVariableAssignmentTerminal(dvar, true, false);
		return term;
	}

	@Override
	public void mutate(Generator g, int depth) {

		if (this.isConstant()) {
			terminal.setToRandom();
		} else if (!this.isConstant()) {
			if (depth == 0)
				swapWith(g.generateRandomDoubleExpression(1));
			else
				swapWith(g.generateRandomDoubleExpression(g.getRandom().nextInt(depth)));

		}
	}

	@Override
	public DoubleVariableAssignmentTerminal copy() {
		VariableAssignment<Double> copied = terminal.copy();
		return new DoubleVariableAssignmentTerminal(copied, constant, LATENT);
	}

	@Override
	public String getType() {
		return "double";
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
			terminal.setValue(origVal);
		}
	}

	@Override
	public Expr toZ3(Context ctx) {
		if (this.isConstant()) {
			return ctx.mkReal(this.getTerminal().getValue().longValue());
		}
		if (this.isLatent())
			return ctx.mkRealConst("latent" + this.getName());

		return ctx.mkRealConst(this.getName());
	}

	@Override
	public Set<VariableTerminal<?>> varsInTree() {
		Set<VariableTerminal<?>> v = new HashSet<VariableTerminal<?>>();
		v.add(this.copy());
		return v;
	}
}
