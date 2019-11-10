package mint.inference.gp;

import java.util.List;
import java.util.concurrent.Callable;

import mint.inference.gp.tree.Node;
import mint.tracedata.types.VariableAssignment;

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
	public T call() throws InterruptedException {
		T result = execute(input);
		return result;
	}
}
