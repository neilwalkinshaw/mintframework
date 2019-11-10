package mint.tracedata.types;

import java.util.Collection;

public abstract class NumberVariableAssignment<T extends Number> extends VariableAssignment<T> {

	protected T min, max;
	protected boolean enforcing;

	/**
	 * Augments the VariableAssignment with a numerical range. Although minimum and
	 * maximum values can be specified, they are not enforced at any point.
	 * 
	 * @param name
	 * @param value
	 */
	public NumberVariableAssignment(String name, T value, T min, T max) {
		super(name, value);
		this.min = min;
		this.max = max;
		this.enforcing = false;
	}

	public NumberVariableAssignment(String name, T min, T max) {
		super(name);
		this.min = min;
		this.max = max;
		this.enforcing = false;
	}

	public NumberVariableAssignment(String name, T min, T max, Collection<T> from) {
		super(name, from);
		this.min = min;
		this.max = max;
		this.enforcing = true;
	}

	public void setEnforcing(boolean e) {
		this.enforcing = e;
	}

	public T getMin() {
		return min;
	}

	public void setMin(T min) {
		this.min = min;
	}

	public T getMax() {
		return max;
	}

	public void setMax(T max) {
		this.max = max;
	}

}
