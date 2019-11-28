package mint.inference.gp.tree.nonterminals.doubles;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.DoubleVariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public class MultiplyDoublesOperator extends DoubleNonTerminal {


    public MultiplyDoublesOperator(){}

    public MultiplyDoublesOperator(Node<DoubleVariableAssignment> a, Node<DoubleVariableAssignment> b){
        super();
        addChild(a);
        addChild(b);
    }

    @Override
    public DoubleVariableAssignment evaluate() throws InterruptedException {
        checkInterrupted();
        DoubleVariableAssignment res = copyResVar();
        res.setValue((Double)getChild(0).evaluate().getValue() * (Double)getChild(1).evaluate().getValue());
        vals.add(res.getValue());
        return res;
    }

    @Override
    public NonTerminal<DoubleVariableAssignment> createInstance(Generator g, int depth){
        MultiplyDoublesOperator mdo =  new MultiplyDoublesOperator(g.generateRandomDoubleExpression(depth), g.generateRandomDoubleExpression(depth));
        mdo.setResVar(copyResVar());
        return mdo;
    }

    @Override
    public Node<DoubleVariableAssignment> copy() {
        MultiplyDoublesOperator mdo =  new MultiplyDoublesOperator((Node<DoubleVariableAssignment>)getChild(0).copy(),(Node<DoubleVariableAssignment>)getChild(1).copy());
        mdo.setResVar(copyResVar());
        return mdo;
    }

    @Override
    public String nodeString(){
        return "Mult("+childrenString()+")";
    }

    @Override
    public boolean accept(NodeVisitor visitor)throws InterruptedException {
        if(visitor.visitEnter(this)) {
            visitChildren(visitor);
        }
        return visitor.visitExit(this);
    }
}
