package mint.inference.gp.tree.nonterminals.integer;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.IntegerVariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public class CastIntegersOperator extends IntegerNonTerminal {


    public CastIntegersOperator(){}

    public CastIntegersOperator(Node<DoubleVariableAssignment> a){
        super();
        addChild(a);
    }

    @Override
    public NonTerminal<IntegerVariableAssignment> createInstance(Generator g, int depth){
      return  new CastIntegersOperator(g.generateRandomDoubleExpression(depth));
    }

    @Override
    public IntegerVariableAssignment evaluate() throws InterruptedException {
        checkInterrupted();
        Double child = (Double)getChild(0).evaluate().getValue();
        IntegerVariableAssignment iva = new IntegerVariableAssignment("result");
        if(child.isInfinite() || child.isNaN()) {
            iva.setValue(0);
        }
        else{
            iva.setValue(child.intValue());
        }
        vals.add(iva);
        return iva;
    }

    @Override
    public boolean accept(NodeVisitor visitor) throws InterruptedException {
        visitor.visitEnter(this);
        for(Node<?> child:children){
            child.accept(visitor);
        }
        return visitor.visitExit(this);
    }

    @Override
    public Node<IntegerVariableAssignment> copy() {
        return new CastIntegersOperator((Node<DoubleVariableAssignment>)getChild(0).copy());
    }

    @Override
    public String nodeString(){
        return "IntCast("+childrenString()+")";
    }

}
