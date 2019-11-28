package mint.inference.gp.tree.nonterminals.doubles;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.DoubleVariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public class DivideDoublesOperator extends DoubleNonTerminal {


    public DivideDoublesOperator(){}

    protected DivideDoublesOperator(Node<DoubleVariableAssignment> a, Node<DoubleVariableAssignment> b){
        super();

        addChild(a);
        addChild(b);
    }

    @Override
    public DoubleVariableAssignment evaluate() throws InterruptedException {
        checkInterrupted();
        double top = (Double)getChild(0).evaluate().getValue();
        double bottom = (Double)getChild(1).evaluate().getValue();
        double result = top/bottom;
        //should throw an exception for divide-by-zero. Would lead to a penalisation in fitness function.
        DoubleVariableAssignment res = copyResVar();
        res.setValue(result);
        vals.add(res.getValue());
        return res;
    }

    @Override
    public NonTerminal<DoubleVariableAssignment> createInstance(Generator g, int depth){
        DivideDoublesOperator ddo =  new DivideDoublesOperator(g.generateRandomDoubleExpression(depth), g.generateRandomDoubleExpression(depth));
        ddo.setResVar(copyResVar());
        return ddo;
    }

    @Override
    public Node<DoubleVariableAssignment> copy() {
        DivideDoublesOperator ddo =  new DivideDoublesOperator((Node<DoubleVariableAssignment>)getChild(0).copy(),(Node<DoubleVariableAssignment>)getChild(1).copy());
        ddo.setResVar(copyResVar());
        return ddo;
    }

    @Override
    public String nodeString(){
        return "Div("+childrenString()+")";
    }

    @Override
    public boolean accept(NodeVisitor visitor)throws InterruptedException {
        if(visitor.visitEnter(this)) {
            visitChildren(visitor);
        }
        return visitor.visitExit(this);
    }
}
