package mint.inference.gp.tree.nonterminals.lists;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.Terminal;
import mint.tracedata.types.ListVariableAssignment;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * For the string, set out the types of the elements in this list:
 * d - double
 * b - boolean
 * i - integer
 *
 * Created by neilwalkinshaw on 5/03/18.
 */
public class RootListNonTerminal extends NonTerminal<ListVariableAssignment> {

    protected String types;

    public RootListNonTerminal(String types){
        this.types = types;
    }

    public RootListNonTerminal(List<Node> a){
        super();

        for(Node<?> element : a){

            addChild(element);
        }

    }

    @Override
    public void simplify(){
        for(Node<?> child : getChildren()){
            child.simplify();
        }
    }

    @Override
    public NonTerminal<ListVariableAssignment> createInstance(Generator g, int depth){
        List<Node> elements = new ArrayList<Node>();
        for(int i = 0; i<types.length();i++){
            char c = types.charAt(i);
            if(c == 'd')
                elements.add(g.generateRandomDoubleExpression(depth));
            else if(c == 'i')
                elements.add(g.generateRandomIntegerExpression(depth));
            else elements.add(g.generateRandomBooleanExpression(depth));
        }
        return new RootListNonTerminal(elements);
    }

    @Override
    public ListVariableAssignment evaluate() throws InterruptedException {
        checkInterrupted();
        List result = new ArrayList();
        for(int i = 0; i<getChildren().size(); i++){
            result.add(getChild(i).evaluate().getValue());
        }
        ListVariableAssignment res =  new ListVariableAssignment("result",result);
        vals.add(res);
        return res;
    }

    @Override
    public Node<ListVariableAssignment> copy() {
        List<Node> result = new ArrayList<Node>();
        for(int i = 0; i<getChildren().size(); i++){
            result.add(getChild(i).copy());
        }
        return new RootListNonTerminal(result);
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

    @Override
    public String getType() {
        return "List";
    }

    /**
     * Not meaningful for a list.
     * @return
     */
    @Override
    public Terminal<ListVariableAssignment> getTermFromVals(){
        return null;
    }



}
