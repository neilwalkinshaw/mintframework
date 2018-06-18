package mint.tracedata.types;

/**
 *
 * A boolean variable that is, for Machine Learning reasons, stored as a 2-valued integer.
 *
 * Created by neilwalkinshaw on 01/09/2014.
 */
public class IntegerBooleanVariableAssignment extends IntegerVariableAssignment {


    public IntegerBooleanVariableAssignment(String name, Boolean value) {
        super(name);
        if(value)
            setToValue(1);
        else
            setToValue(0);
        setMax(1);
        setMin(0);
    }

    public IntegerBooleanVariableAssignment(String name, int value) {
        super(name);
        assert(value >=0 && value <=1);
        this.value = value;
        setMax(1);
        setMin(0);
    }

    public IntegerBooleanVariableAssignment(String name) {
        super(name);
        value = 0;
        setMax(1);
        setMin(0);
    }

    @Override
    public void setStringValue(String s) {
        if(s.equals("true"))
            setToValue(1);
        else
            setToValue(0);
    }

    @Override
    public VariableAssignment<Integer> copy() {

        IntegerBooleanVariableAssignment copied = new IntegerBooleanVariableAssignment(name,value);
        copied.setParameter(isParameter());
        return copied;
    }

    @Override
    public VariableAssignment<?> createNew(String name, String value) {
        IntegerBooleanVariableAssignment iva = new IntegerBooleanVariableAssignment(name);
        iva.setParameter(isParameter());
        if(value == null)
            setNull(true);
        else if(value.trim().equals("*"))
            setNull(true);
        else
            iva.setStringValue(value);
        iva.setMax(1);
        iva.setMin(0);
        return iva;
    }

}
