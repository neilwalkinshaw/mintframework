package mint.testgen.stateless.gp;

import mint.inference.gp.tree.Node;
import mint.testgen.stateless.TestGenerator;
import mint.tracedata.types.VariableAssignment;

import java.util.Collection;

/**
 * Test generator to generate tests from a GP tree.
 *
 * Created by neilwalkinshaw on 26/05/15.
 */
public abstract class GPModelTestGenerator extends TestGenerator {

    protected Node<?> model;

    public GPModelTestGenerator(String name, Collection<VariableAssignment<?>> types, Node<?> tree) {
        super(name, types);
        this.model = tree;
    }


}
