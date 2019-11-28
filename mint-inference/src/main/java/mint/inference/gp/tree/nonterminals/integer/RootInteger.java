package mint.inference.gp.tree.nonterminals.integer;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.IntegerVariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public class RootInteger extends IntegerNonTerminal {


    public RootInteger(){
    }

    public RootInteger(Node<IntegerVariableAssignment> a){
        super();
        addChild(a);
    }

    @Override
    public NonTerminal<IntegerVariableAssignment> createInstance(Generator g, int depth){
      return  new RootInteger(g.generateRandomIntegerExpression(depth + 1));
    }

    @Override
    public IntegerVariableAssignment evaluate() throws InterruptedException {
        checkInterrupted();
        Object obj = getChild(0).evaluate().getValue();
        IntegerVariableAssignment res = new IntegerVariableAssignment("result",(Integer)obj);
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
    public Node<IntegerVariableAssignment> copy() {
        return new RootInteger((Node<IntegerVariableAssignment>)getChild(0).copy());
    }

    @Override
    public String nodeString(){
        return childrenString();
    }

    @Override
    public int depth(){
        return 0;
    }

}
