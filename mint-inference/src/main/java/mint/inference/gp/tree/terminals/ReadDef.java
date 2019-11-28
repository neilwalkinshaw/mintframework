package mint.inference.gp.tree.terminals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.nonterminals.strings.AssignmentOperator;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 14/11/2017.
 */
public class ReadDef extends StringVariableAssignmentTerminal {

	Random rand;
	VariableAssignment<Double> dvar = null;

	public ReadDef(VariableAssignment<Double> var, Random r) {
		super(new StringVariableAssignment("result"), true, false);
		this.rand = r;
		this.dvar = var;
	}

	@Override
	public StringVariableAssignment evaluate() {
		List<AssignmentOperator> defs = getDefsInScope(this, new ArrayList<AssignmentOperator>());
		StringVariableAssignment sva = null;
		if (defs.isEmpty()) {
			dvar.setToRandom();
			this.setValue(Double.toString(dvar.getValue()));
			sva = getTerminal();
		} else {
			AssignmentOperator aop = defs.get(rand.nextInt(defs.size()));
			try {
				sva = (aop.evaluate());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return sva;
	}

	@Override
	public StringVariableAssignmentTerminal copy() {
		return new ReadDef(dvar.copy(), rand);
	}

	public static List<AssignmentOperator> getDefsInScope(Node current, List<AssignmentOperator> defs) {
		if (current.getDef() != null)
			defs.add(current.getDef());
		if (current.getParent() != null) {
			getDefsInScope(current.getParent(), defs);
		}
		return defs;
	}

}
