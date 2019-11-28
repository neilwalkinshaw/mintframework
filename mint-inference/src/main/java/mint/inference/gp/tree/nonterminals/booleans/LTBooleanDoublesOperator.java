package mint.inference.gp.tree.nonterminals.booleans;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.DoubleVariableAssignment;

/**
 * Created by neilwalkinshaw on 26/05/15.
 */
public class LTBooleanDoublesOperator extends BooleanNonTerminal {

    public LTBooleanDoublesOperator(Node<?> b){
        super(b);
    }

    public LTBooleanDoublesOperator(Node<DoubleVariableAssignment> a, Node<DoubleVariableAssignment> b){
        super(null);
        addChild(a);
        addChild(b);
    }

    public LTBooleanDoublesOperator() {

    }

    @Override
    public NonTerminal<BooleanVariableAssignment> createInstance(Generator g, int depth) {
        return new LTBooleanDoublesOperator(g.generateRandomDoubleExpression(depth),g.generateRandomDoubleExpression(depth));
    }

    @Override
    protected String nodeString() {
        return "LT("+childrenString()+")";
    }

    @Override
    public boolean accept(NodeVisitor visitor) throws InterruptedException {
        if(visitor.visitEnter(this)) {
            visitChildren(visitor);
        }
        return visitor.visitExit(this);
    }

    @Override
    public BooleanVariableAssignment evaluate() throws InterruptedException {
        checkInterrupted();
        BooleanVariableAssignment res =  new BooleanVariableAssignment("result",(Double)children.get(0).evaluate().getValue() < (Double)children.get(1).evaluate().getValue());
        vals.add(res.getValue());
        return res;
    }

    @Override
    public Node<BooleanVariableAssignment> copy() {
        return new LTBooleanDoublesOperator((Node<DoubleVariableAssignment>)children.get(0).copy(),(Node<DoubleVariableAssignment>)children.get(1).copy());

    }
}
