package mint.inference.gp.tree.terminals;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.Terminal;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 04/03/15.
 */
public class DoubleVariableAssignmentTerminal extends VariableTerminal<DoubleVariableAssignment> {

    double origVal;

    public DoubleVariableAssignmentTerminal(VariableAssignment<Double> var, boolean constant) {
        super(constant);
        if(var.getValue() != null)
            origVal = var.getValue();
        this.terminal = (DoubleVariableAssignment)var;
    }

    @Override
    public void setValue(Object val) {
        if(constant)
            return;
        if(val instanceof Double)
            terminal.setValue((Double)val);
        else if(val instanceof Integer){
            Integer intval = (Integer) val;
            Double doubVal = (double) intval.intValue();
            terminal.setValue(doubVal);
        }
    }

    @Override
    protected Terminal<DoubleVariableAssignment> getTermFromVals() {
        DoubleVariableAssignment dvar = new DoubleVariableAssignment("res",(Double)vals.iterator().next());
        DoubleVariableAssignmentTerminal term = new DoubleVariableAssignmentTerminal(dvar,true);
        return term;
    }

    @Override
    public void mutate(Generator g, int depth){

        if(this.isConstant()) {
            terminal.setToRandom();
        }
        else if(!this.isConstant()){
            if(depth == 0)
                swapWith(g.generateRandomDoubleExpression(1));
            else
                swapWith(g.generateRandomDoubleExpression(g.getRandom().nextInt(depth)));

        }
    }

    @Override
    public Terminal<DoubleVariableAssignment> copy() {
        VariableAssignment<Double> copied = terminal.copy();
        return new DoubleVariableAssignmentTerminal(copied,constant);
    }

    @Override
    public String getType() {
        return "double";
    }

    @Override
    public boolean accept(NodeVisitor visitor) {
        visitor.visitEnter(this);
        return visitor.visitExit(this);
    }

    public void reset(){
        super.reset();
        if(! isConstant()) {
            terminal.setValue(origVal);
        }
    }
}
