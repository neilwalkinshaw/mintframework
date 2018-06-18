package mint.inference.gp.tree.nonterminals.booleans;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.BooleanVariableAssignment;

/**
 * Created by neilwalkinshaw on 26/05/15.
 */
public class EQArithOperator extends BooleanNonTerminal {


    public EQArithOperator(Node<?> a, Node<?> b){
        super(null);
        addChild(a);
        addChild(b);
    }

    public EQArithOperator() {

    }

    @Override
    public NonTerminal<BooleanVariableAssignment> createInstance(Generator g, int depth) {
        return new EQArithOperator(g.generateRandomDoubleExpression(depth),g.generateRandomDoubleExpression(depth));
    }

    @Override
    protected String nodeString() {
        return "EQ("+childrenString()+")";
    }

    @Override
    public boolean accept(NodeVisitor visitor) throws InterruptedException {
        if (visitor.visitEnter(this)) {
            visitChildren(visitor);
        }
        return visitor.visitExit(this);
    }

    @Override
    public BooleanVariableAssignment evaluate() throws InterruptedException {
        checkInterrupted();
        Object from = children.get(0).evaluate().getValue();
        Object to = children.get(1).evaluate().getValue();
        BooleanVariableAssignment res =  new BooleanVariableAssignment("result",to.equals(from));
        vals.add(res.getValue());
        return res;
    }

    @Override
    public Node<BooleanVariableAssignment> copy() {
        return new EQArithOperator(children.get(0).copy(),children.get(1).copy());

    }
}
