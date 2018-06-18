package mint.inference.gp;

import mint.inference.gp.tree.Node;
import mint.tracedata.types.VariableAssignment;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by neilwalkinshaw on 08/01/2016.
 */
public class CallableNodeExecutor<T> extends NodeExecutor<T> implements Callable<T> {

    protected List<VariableAssignment<?>> input;

    public CallableNodeExecutor(Node<VariableAssignment<T>> individual, List<VariableAssignment<?>> input) {
        super(individual);
        this.input = input;
    }

    @Override
    public T call() throws Exception {
        T result = execute(input);
        return result;
    }
}
