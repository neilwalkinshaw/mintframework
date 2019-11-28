package mint.inference.gp.tree.nonterminals.booleans;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.BooleanVariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public class RootBoolean extends BooleanNonTerminal {


    public RootBoolean(){
    }

    public RootBoolean(Node<BooleanVariableAssignment> a){
        super();
        addChild(a);
    }

    @Override
    public NonTerminal<BooleanVariableAssignment>  createInstance(Generator g, int depth){
      return  new RootBoolean(g.generateRandomBooleanExpression(depth));
    }

    @Override
    public BooleanVariableAssignment evaluate() throws InterruptedException {
        checkInterrupted();
        BooleanVariableAssignment bvar = (BooleanVariableAssignment)getChild(0).evaluate();
        BooleanVariableAssignment res = new BooleanVariableAssignment("result",bvar.getValue());
        vals.add(res.getValue());
        return res;
    }

    @Override
    public void simplify(){
        for(Node<?> child : getChildren()){
            child.simplify();
        }
    }

    @Override
    public Node<BooleanVariableAssignment> copy() {
        return new RootBoolean((Node<BooleanVariableAssignment>)getChild(0).copy());

    }

    @Override
    public String nodeString(){
        return "R:"+childrenString();
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
    public int depth(){
        return 0;
    }

}
