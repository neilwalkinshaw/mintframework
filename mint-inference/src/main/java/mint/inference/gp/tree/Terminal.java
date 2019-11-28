package mint.inference.gp.tree;

import mint.tracedata.types.VariableAssignment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public abstract class Terminal<V extends VariableAssignment<?>> extends Node<V> {

    protected V terminal;

    protected boolean constant;

    public Terminal(boolean constant){
        this.constant = constant;
    }

    public V getTerminal(){
        return terminal;
    }

    public V evaluate(){
        vals.add(terminal.getValue());
        return terminal;
    }

    public abstract void setValue(Object val);

    @Override
    public List<Node<?>> getChildren(){

        return new ArrayList<Node<?>>();
    }

    public boolean isConstant(){
        return constant;
    }

    @Override
    public int numVarsInTree(){
        if(isConstant())
            return 0;
        else
            return 1;
    }
    
    @Override
    public void simplify() {
        if(isConstant())
            return;
        if (vals.size() == 1) {
            Terminal<V> term = getTermFromVals();
            swapWith(term);
        }
    }

    protected abstract Terminal<V> getTermFromVals();

    @Override
    public int size(){
        return 1;
    }
}
