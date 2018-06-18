package mint.tracedata.types;

import org.apache.log4j.Logger;

import java.util.StringTokenizer;

/**
 * An integer that represents the number of tokens in a String. Intended for conversion of
 * String outputs into numerical formats that are more usable by Machine Learners.
 */
public class IntegerStringTokensVariableAssignment extends IntegerVariableAssignment {
    private final static Logger LOGGER = Logger.getLogger(IntegerStringTokensVariableAssignment.class.getName());


    String token = " ";

    public IntegerStringTokensVariableAssignment(String name, String s) {
        super(name);
        StringTokenizer tokenizer = new StringTokenizer(s);
        this.value = tokenizer.countTokens();
        setNull(false);

    }

    public IntegerStringTokensVariableAssignment(String name, String s, String token) {
        super(name);
        StringTokenizer tokenizer = new StringTokenizer(s,token);
        this.token = token;
        this.value = tokenizer.countTokens();
        setNull(false);
    }

    public IntegerStringTokensVariableAssignment(String name) {
        super(name);
        value = 0;
    }

    @Override
    public void setStringValue(String s) {
        StringTokenizer tokenizer = new StringTokenizer(s, token);
        this.value = tokenizer.countTokens();
        setNull(false);
    }

    @Override
    public VariableAssignment<Integer> copy() {
        IntegerStringTokensVariableAssignment copied = new IntegerStringTokensVariableAssignment(name, Integer.toString(value));
        copied.setParameter(isParameter());
        return copied;
    }

    @Override
    public VariableAssignment<?> createNew(String name, String value) {
        IntegerStringTokensVariableAssignment iva = new IntegerStringTokensVariableAssignment(name,value);
        iva.setParameter(isParameter());
        if(value == null)
            setNull(true);
        else if(value.trim().equals("*"))
            setNull(false);
        return iva;
    }
}
