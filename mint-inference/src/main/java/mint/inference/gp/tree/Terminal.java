package mint.inference.gp.tree;

import java.util.ArrayList;
import java.util.List;

import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public abstract class Terminal<V extends VariableAssignment<?>> extends Node<V> {

	protected V terminal;

	protected boolean constant;

	public Terminal(boolean constant) {
		this.constant = constant;
	}

	public V getTerminal() {
		return terminal;
	}

	@Override
	public V evaluate() {
		vals.add(terminal.getValue());
		return terminal;
	}

	public abstract void setValue(Object val);

	@Override
	public List<Node<?>> getChildren() {

		return new ArrayList<Node<?>>();
	}

	public boolean isConstant() {
		return constant;
	}

	@Override
	public int numVarsInTree() {
		if (isConstant())
			return 0;
		else
			return 1;
	}

	protected abstract Terminal<V> getTermFromVals();

	@Override
	public int size() {
		return 1;
	}
}
