package mint.inference.gp.tree.nonterminals.doubles;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.DoubleVariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public class ExpDoublesOperator extends DoubleNonTerminal {


    public ExpDoublesOperator(){
    }

    public ExpDoublesOperator(Node<DoubleVariableAssignment> a){
        super();
        addChild(a);
    }

    @Override
    public NonTerminal<DoubleVariableAssignment> createInstance(Generator g, int depth){
        ExpDoublesOperator edo =   new ExpDoublesOperator(g.generateRandomDoubleExpression(depth));
        edo.setResVar(copyResVar());
        return edo;
    }

    @Override
    public DoubleVariableAssignment evaluate() throws InterruptedException {
        checkInterrupted();
        DoubleVariableAssignment res = copyResVar();
        res.setValue(Math.exp((Double)getChild(0).evaluate().getValue()));
        vals.add(res.getValue());
        return res;
    }

    @Override
    public Node<DoubleVariableAssignment> copy() {
        ExpDoublesOperator edo =  new ExpDoublesOperator((Node<DoubleVariableAssignment>)getChild(0).copy());
        edo.setResVar(copyResVar());
        return edo;
    }

    @Override
    public String nodeString(){
        return "Exp("+childrenString()+")";
    }

    @Override
    public boolean accept(NodeVisitor visitor) throws InterruptedException {
        if(visitor.visitEnter(this)) {
            visitChildren(visitor);
        }
        return visitor.visitExit(this);
    }

}
