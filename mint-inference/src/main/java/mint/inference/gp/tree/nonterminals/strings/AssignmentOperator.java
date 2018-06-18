package mint.inference.gp.tree.nonterminals.strings;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.StringVariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public class AssignmentOperator extends StringNonTerminal {

    protected String identifier;

    protected static int counter = 0;

    public AssignmentOperator(){}

    protected AssignmentOperator(String identifier){
        super();
        this.identifier = identifier;

    }

    @Override
    public StringVariableAssignment evaluate() throws InterruptedException {
        checkInterrupted();
        StringVariableAssignment svar = new StringVariableAssignment("result",identifier);
        vals.add(svar);
        return svar;
    }

    @Override
    public NonTerminal<StringVariableAssignment> createInstance(Generator g, int depth){
        counter++;
        return  new AssignmentOperator("Assignment"+counter);
    }

    @Override
    public Node<StringVariableAssignment> copy() {
        return new AssignmentOperator(identifier);
    }

    @Override
    public String nodeString(){
        return "identifier = "+identifier;
    }
}
