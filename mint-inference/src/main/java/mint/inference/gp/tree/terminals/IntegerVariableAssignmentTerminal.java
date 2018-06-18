package mint.inference.gp.tree.terminals;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.Terminal;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 04/03/15.
 */
public class IntegerVariableAssignmentTerminal extends VariableTerminal<IntegerVariableAssignment> {

    protected int origVal;

    public IntegerVariableAssignmentTerminal(VariableAssignment<Integer> var, boolean constant) {
        super(constant);
        if(var.getValue() != null)
            origVal = var.getValue();
        this.terminal = (IntegerVariableAssignment)var;
    }

    @Override
    public void setValue(Object val) {

        if(val instanceof Integer){
            Integer intval = (Integer) val;
            terminal.setValue(intval);
        }
    }

    @Override
    protected Terminal<IntegerVariableAssignment> getTermFromVals() {
        IntegerVariableAssignment ivar = new IntegerVariableAssignment("res",(Integer)vals.iterator().next());
        IntegerVariableAssignmentTerminal term = new IntegerVariableAssignmentTerminal(ivar,true);
        return term;
    }

    @Override
    public void mutate(Generator g, int depth){
        if(!this.isConstant()) {
            int limit = depth;
            if(limit == 0)
                limit++;
            swapWith(g.generateRandomIntegerExpression(g.getRandom().nextInt(limit)));
        }
        else if(this.isConstant()){
            terminal.setToRandom();
        }
    }

    @Override
    public Terminal<IntegerVariableAssignment> copy() {
        VariableAssignment<Integer> copied = terminal.copy();
        return new IntegerVariableAssignmentTerminal(copied,constant);
    }

    @Override
    public String getType() {
        return "integer";
    }

    @Override
    public boolean accept(NodeVisitor visitor) {
        visitor.visitEnter(this);
        return visitor.visitExit(this);
    }

    public void reset(){
        super.reset();
        if(! isConstant()) {
            terminal.setValue(0);
        }
    }
}
