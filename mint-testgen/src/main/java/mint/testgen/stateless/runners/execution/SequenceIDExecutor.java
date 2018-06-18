package mint.testgen.stateless.runners.execution;

import java.util.List;

/**
 * Adds a parameter into the front of an execution, acting as an identifier for the number
 * in a sequence of tests being executed.
 *
 * Number corresponds to the size of the current set of test inputs.
 *
 * Does not currently work for Java programs executed via Maven.
 *
 * Created by neilwalkinshaw on 03/07/2017.
 */
public class SequenceIDExecutor extends CommandDecorator {

    TestRunner host;

    public SequenceIDExecutor(TestRunner host, Command commandBuilder) {
        super(commandBuilder);
        this.host = host;
    }

    public List<String> getCommand(){
        List<String> comm = decorated.getCommand();
        comm.add(1,Integer.toString(host.getTestInputs().size()));
        return comm;
    }
}
