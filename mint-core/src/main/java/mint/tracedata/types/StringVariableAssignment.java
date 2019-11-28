package mint.tracedata.types;

import java.util.Collection;

public class StringVariableAssignment extends VariableAssignment<String> {


	public StringVariableAssignment(String name, String value) {
		super(name, value);
	}

	
	public StringVariableAssignment(String name) {
		super(name);
	}

    public StringVariableAssignment(String name, Collection<String> from) {
        super(name,from);
    }

	@Override
	public void setStringValue(String s) {
		setNull(false);
		setToValue(String.valueOf(s));
	}

	@Override
	public String printableStringOfValue() {
		return value;
	}

	@Override
	public String typeString() {
		return ":S";
	}

	@Override
	public VariableAssignment<?> createNew(String name, String value) {
		StringVariableAssignment sva = new StringVariableAssignment(name);
        sva.setParameter(isParameter());
        if(value == null)
            setNull(true);
        else if(value.trim().equals("*"))
            setNull(true);
        else
		    sva.setStringValue(value);
		return sva;
	}
	
	@Override
	public VariableAssignment<String> copy() {
        StringVariableAssignment copied = new StringVariableAssignment(name,value);
        copied.setParameter(isParameter());
		copied.setRange(from);
		assert(copied.isNull() == isNull());
        return copied;
	}

	@Override
	protected String generateRandom() {
		// TODO Auto-generated method stub
		return null;
	}

}
