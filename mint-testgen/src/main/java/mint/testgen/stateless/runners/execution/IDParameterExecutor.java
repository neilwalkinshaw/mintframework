package mint.testgen.stateless.runners.execution;

import java.util.List;

/**
 * Adds a parameter into the front of an execution, acting as an identifier for the set of tests being executed.
 *
 * Does not currently work for Java programs executed via Maven.
 *
 * Created by neilwalkinshaw on 03/07/2017.
 */
public class IDParameterExecutor extends CommandDecorator {

    protected String identifier = "";

    public IDParameterExecutor(Command toBeDecorated, String identifier) {
        super(toBeDecorated);
        this.identifier = identifier;
    }

    public List<String> getCommand(){
        List<String> comm = decorated.getCommand();
        comm.add(1,identifier);
        return comm;
    }
}
