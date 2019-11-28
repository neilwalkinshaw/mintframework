package mint.tracedata.types;

import java.util.ArrayList;
import java.util.List;

public class BooleanVariableAssignment extends VariableAssignment<Boolean> {

	private static List<Boolean> values = new ArrayList<Boolean>();

	public BooleanVariableAssignment(String name, Boolean value) {
		super(name, value);
	}

	public BooleanVariableAssignment(String name) {
		super(name);
	}

	@Override
	public void setStringValue(String s) {
		setToValue(Boolean.valueOf(s));
	}

	@Override
	public String printableStringOfValue() {
		return Boolean.toString(value);
	}

	@Override
	public String typeString() {
		return ":B";
	}

	@Override
	public VariableAssignment<?> createNew(String name, String value) {
		BooleanVariableAssignment bva = new BooleanVariableAssignment(name);
		bva.setParameter(isParameter());
		if (value == null)
			setNull(true);
		else if (value.trim().equals("*"))
			setNull(true);
		else
			bva.setStringValue(value);
		return bva;
	}

	@Override
	public VariableAssignment<Boolean> copy() {
		VariableAssignment<Boolean> copied = new BooleanVariableAssignment(name, value);
		copied.setParameter(isParameter());
		return copied;

	}

	@Override
	protected Boolean generateRandom() {
		int bool = rand.nextInt(2);
		if (bool == 1)
			return true;
		else
			return false;

	}

	@Override
	public List<Boolean> getValues() {
		return values;
	}

	@Override
	public void addValue(Boolean v) {
		if (!values.contains(v))
			values.add(v);
	}

}
