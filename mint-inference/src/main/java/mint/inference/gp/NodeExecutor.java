package mint.inference.gp;

import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.Terminal;
import mint.tracedata.TestIO;
import mint.tracedata.types.VariableAssignment;

import java.util.*;

/**
 *
 * Will execute a GP Node given a TestIO
 *
 * Created by neilwalkinshaw on 26/08/15.
 */

public class NodeExecutor<T> {

    protected Map<String,Collection<Node<VariableAssignment<?>>>> varMap;
    Node<VariableAssignment<T>> individual;

    public NodeExecutor(Node<VariableAssignment<T>> individual){

        this.individual = individual;
        varMap = new HashMap<String, Collection<Node<VariableAssignment<?>>>>();
        findVariables(individual);

    }


    protected void findVariables(Node<? extends VariableAssignment<?>> individual){
        if(individual instanceof Terminal){
            Terminal term = (Terminal) individual;
            if(term.getTerminal() != null) {

                if (!varMap.containsKey(term.getTerminal().getName())) {
                    Collection<Node<VariableAssignment<?>>> termSet = new HashSet<Node<VariableAssignment<?>>>();
                    termSet.add(term);
                    varMap.put(term.getTerminal().getName(), termSet);
                } else {
                    Collection<Node<VariableAssignment<?>>> termSet = varMap.get(term.getTerminal().getName());
                    termSet.add(term);
                }
            }

        }
        for(Node child : individual.getChildren()){
            if(child instanceof Terminal){
                Terminal term = (Terminal) child;
                if(term.getTerminal()!=null) {
                    if (!varMap.containsKey(term.getTerminal().getName())) {
                        Collection<Node<VariableAssignment<?>>> termSet = new HashSet<Node<VariableAssignment<?>>>();
                        termSet.add(term);
                        varMap.put(term.getTerminal().getName(), termSet);
                    } else {
                        Collection<Node<VariableAssignment<?>>> termSet = varMap.get(term.getTerminal().getName());
                        termSet.add(term);
                    }
                }
            }
            else{
                findVariables(child);
            }
        }

    }


    public T execute(TestIO inputs)throws InterruptedException {
        return execute(inputs.getVals());
    }


    public T execute(List<VariableAssignment<?>> inputs)throws InterruptedException {
        individual.reset();
        for(VariableAssignment<?> var:inputs){
            assign(var, varMap);
        }
        return individual.evaluate().getValue();
    }

    /**
     * Sets a variable in varMap to the value carried by var.
     *
     *  @param var
     * @param varMap
     */
    protected static void assign(VariableAssignment<?> var, Map<String, Collection<Node<VariableAssignment<?>>>> varMap) {

        Collection<Node<VariableAssignment<?>>> vars = varMap.get(var.getName());
        if(vars == null)
            return;
        for(Node<VariableAssignment<?>> v : vars){
            Terminal term = (Terminal) v;
            term.setValue(var.getValue());
        }

    }
}
