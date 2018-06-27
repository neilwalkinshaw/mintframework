package mint.testgen.stateless.text;

import mint.testgen.stateless.TestGenerator;
import mint.tracedata.TestIO;
import mint.tracedata.types.VariableAssignment;

import java.util.Collection;
import java.util.List;

/**
 * Vectorise input files with doc2vec.
 *
 * Created by neilwalkinshaw on 04/06/2018.
 */
public class TextIORunner extends TestGenerator {

    protected final int increment;

    public TextIORunner(String name, Collection<VariableAssignment<?>> types, int increment) {
        super(name, types);
        this.increment=increment;
    }

    @Override
    public List<TestIO> generateTestCases(int howMany) {
        return null;
    }

    @Override
    public List<TestIO> generateTestCases() {
        return generateTestCases(increment);
    }



}
