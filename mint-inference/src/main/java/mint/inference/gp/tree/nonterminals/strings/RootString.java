package mint.inference.gp.tree.nonterminals.strings;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.StringVariableAssignment;

/**
 * Created by neilwalkinshaw on 16/03/16.
 */
public class RootString extends StringNonTerminal {


    public RootString(){
    }

    public RootString(Node<StringVariableAssignment> a){
        super();
        addChild(a);
    }

    @Override
    public NonTerminal<StringVariableAssignment> createInstance(Generator g, int depth){
      return  new RootString(g.generateRandomStringExpression(depth + 1));
    }

    @Override
    public StringVariableAssignment evaluate() throws InterruptedException {
        checkInterrupted();
        Object obj = getChild(0).evaluate().getValue();
        StringVariableAssignment res =  new StringVariableAssignment("result",(String)obj);
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
    public Node<StringVariableAssignment> copy() {
        return new RootString((Node<StringVariableAssignment>)getChild(0).copy());
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
