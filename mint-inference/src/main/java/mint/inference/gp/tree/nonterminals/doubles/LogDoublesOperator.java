package mint.inference.gp.tree.nonterminals.doubles;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.DoubleVariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public class LogDoublesOperator extends DoubleNonTerminal {


    public LogDoublesOperator(){}

    protected LogDoublesOperator(Node<DoubleVariableAssignment> a){
        super();
        addChild(a);
    }

    @Override
    public DoubleVariableAssignment evaluate() throws InterruptedException {
        checkInterrupted();
        DoubleVariableAssignment res = copyResVar();
        res.setValue(Math.log((Double)children.get(0).evaluate().getValue()));
        vals.add(res.getValue());
        return res;
    }

    @Override
    public NonTerminal<DoubleVariableAssignment> createInstance(Generator g, int depth){
        LogDoublesOperator ldo =  new LogDoublesOperator(g.generateRandomDoubleExpression(depth));
        ldo.setResVar(copyResVar());
        return ldo;
    }

    @Override
    public Node<DoubleVariableAssignment> copy() {
        LogDoublesOperator ldo =  new LogDoublesOperator((Node<DoubleVariableAssignment>)children.get(0).copy());
        ldo.setResVar(copyResVar());
        return ldo;
    }

    @Override
    public String nodeString(){
        return "Log("+childrenString()+")";
    }

    @Override
    public boolean accept(NodeVisitor visitor)throws InterruptedException {
        if(visitor.visitEnter(this)) {
            visitChildren(visitor);
        }
        return visitor.visitExit(this);
    }
}
