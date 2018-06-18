package mint.tracedata;

import mint.tracedata.types.VariableAssignment;

import java.util.List;

public class IOTraceElement extends SimpleTraceElement {

	protected boolean isInput;
	
	public IOTraceElement(String name, List<VariableAssignment<?>> data, boolean isInput) {
		super(name, data);
		this.isInput = isInput;
	}
	
	public boolean isInput(){
		return isInput;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IOTraceElement)) return false;
        if (!super.equals(o)) return false;

        IOTraceElement that = (IOTraceElement) o;

        if (isInput != that.isInput) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isInput ? 1 : 0);
        return result;
    }
}
