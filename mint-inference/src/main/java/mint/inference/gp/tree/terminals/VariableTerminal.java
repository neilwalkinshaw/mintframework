package mint.inference.gp.tree.terminals;

import mint.inference.gp.tree.Terminal;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 07/03/15.
 */
public abstract class VariableTerminal<T extends VariableAssignment<?>> extends Terminal<T> {

    public VariableTerminal(boolean constant){
        super(constant);
    }

    public String getName(){
        return terminal.getName();
    }

    @Override
    public String toString() {
        if(!terminal.isParameter() && isConstant())
            return "" + terminal.getValue();
        else
            return terminal.getName();
    }

    public void reset(){
        super.reset();
        if(! isConstant()) {
            terminal.setValue(null);
            terminal.setNull(true);
        }
    }



}
