package mint.inference.gp.tree.terminals;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.Terminal;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 04/03/15.
 */
public class StringVariableAssignmentTerminal extends VariableTerminal<StringVariableAssignment> {


    public StringVariableAssignmentTerminal(VariableAssignment<String> var, boolean constant) {
        super(constant);
        this.terminal = (StringVariableAssignment)var;
    }

    @Override
    public void mutate(Generator g, int depth){
         swapWith(g.generateRandomStringExpression(g.getRandom().nextInt(depth)));
    }

    @Override
    public Terminal<StringVariableAssignment> copy() {
        VariableAssignment<String> copied = terminal.copy();
        return new StringVariableAssignmentTerminal(copied,constant);
    }

    @Override
    public String getType() {
        return "string";
    }

    @Override
    public boolean accept(NodeVisitor visitor) {
        visitor.visitEnter(this);
        return visitor.visitExit(this);
    }

    @Override
    public void setValue(Object val) {
        terminal.setValue(val.toString());
    }

    @Override
    protected Terminal<StringVariableAssignment> getTermFromVals() {
        StringVariableAssignment svar = new StringVariableAssignment("res",vals.iterator().next().toString());
        StringVariableAssignmentTerminal term = new StringVariableAssignmentTerminal(svar,true);
        return term;
    }
}