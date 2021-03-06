package mint.inference.gp.tree.nonterminals.doubles;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.DoubleVariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public class CosDoublesOperator extends DoubleNonTerminal {


    public CosDoublesOperator(){}

    protected CosDoublesOperator(Node<DoubleVariableAssignment> a){
        super();
        addChild(a);
    }

    @Override
    public DoubleVariableAssignment evaluate() throws InterruptedException {
        checkInterrupted();
        DoubleVariableAssignment res = copyResVar();
        res.setValue(Math.cos((Double)children.get(0).evaluate().getValue()));
        vals.add(res.getValue());
        return res;
    }

    @Override
    public NonTerminal<DoubleVariableAssignment> createInstance(Generator g, int depth){
        CosDoublesOperator cdo =  new CosDoublesOperator(g.generateRandomDoubleExpression(depth));
        cdo.setResVar(copyResVar());
        return cdo;
    }

    @Override
    public Node<DoubleVariableAssignment> copy() {
        CosDoublesOperator cdo =  new CosDoublesOperator((Node<DoubleVariableAssignment>)children.get(0).copy());
        cdo.setResVar(copyResVar());
        return cdo;
    }

    @Override
    public String nodeString(){
        return "Cos("+childrenString()+")";
    }

    @Override
    public boolean accept(NodeVisitor visitor) throws InterruptedException {
        if(visitor.visitEnter(this)) {
            visitChildren(visitor);
        }
        return visitor.visitExit(this);
    }
}
