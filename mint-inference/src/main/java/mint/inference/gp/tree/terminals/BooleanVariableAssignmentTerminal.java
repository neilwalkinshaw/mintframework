package mint.inference.gp.tree.terminals;

import java.util.HashSet;
import java.util.Set;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.Terminal;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 26/05/15.
 */
public class BooleanVariableAssignmentTerminal extends VariableTerminal<BooleanVariableAssignment> {

	public BooleanVariableAssignmentTerminal(VariableAssignment<Boolean> var, boolean constant, boolean latent) {
		super(constant, latent);
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
	public BooleanVariableAssignmentTerminal copy() {
		VariableAssignment<Boolean> copied = terminal.copy();
		return new BooleanVariableAssignmentTerminal(copied, constant, LATENT);
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
		BooleanVariableAssignmentTerminal term = new BooleanVariableAssignmentTerminal(bvar, true, false);
		return term;
	}

	@Override
	public Expr toZ3(Context ctx) {
		if (this.isConstant()) {
			return ctx.mkBool(this.getTerminal().getValue());
		}
		if (this.isLatent())
			return ctx.mkBoolConst("latent" + this.getName());

		return ctx.mkBoolConst(this.getName());
	}

	@Override
	public Set<VariableTerminal<?>> varsInTree() {
		Set<VariableTerminal<?>> v = new HashSet<VariableTerminal<?>>();
		v.add(this.copy());
		return v;
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.BOOLEAN };
	}

}
