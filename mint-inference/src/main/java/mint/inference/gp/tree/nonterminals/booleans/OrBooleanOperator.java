package mint.inference.gp.tree.nonterminals.booleans;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.BooleanVariableAssignment;

/**
 * Created by neilwalkinshaw on 26/05/15.
 */
public class OrBooleanOperator extends BooleanNonTerminal {

    public OrBooleanOperator(Node<?> b){
        super(b);
    }

    public OrBooleanOperator(Node<?> a, Node<?> b){
        super(null);
        addChild(a);
        addChild(b);
    }

    public OrBooleanOperator() {

    }

    @Override
    public NonTerminal<BooleanVariableAssignment> createInstance(Generator g, int depth) {
        return new OrBooleanOperator(g.generateRandomBooleanExpression(depth),g.generateRandomBooleanExpression(depth));
    }

    @Override
    protected String nodeString() {
        return "OR("+childrenString()+")";
    }

    @Override
    public boolean accept(NodeVisitor visitor)throws InterruptedException {
        if(visitor.visitEnter(this)) {
            for (Node<?> child : children) {
                child.accept(visitor);
            }
        }
        return visitor.visitExit(this);
    }

    @Override
    public BooleanVariableAssignment evaluate() throws InterruptedException {
        checkInterrupted();
        Boolean from = (Boolean)children.get(0).evaluate().getValue();
        Boolean to = (Boolean) children.get(1).evaluate().getValue();
        BooleanVariableAssignment res =  new BooleanVariableAssignment("result",to || from);
        vals.add(res.getValue());
        return res;
    }

    @Override
    public Node<BooleanVariableAssignment> copy() {
        return new OrBooleanOperator(children.get(0).copy(),children.get(1).copy());

    }
}
