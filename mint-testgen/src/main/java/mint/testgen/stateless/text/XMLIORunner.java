package mint.testgen.stateless.text;

import mint.testgen.stateless.TestGenerator;
import mint.tracedata.TestIO;
import mint.tracedata.types.VariableAssignment;

import java.util.Collection;
import java.util.List;

/**
 * Represent XML as a graph, use graph2vec to derive vector
 *
 * Created by neilwalkinshaw on 04/06/2018.
 */
public class XMLIORunner extends TestGenerator {

    public XMLIORunner(String name, Collection<VariableAssignment<?>> types) {
        super(name, types);
    }

    @Override
    public List<TestIO> generateTestCases(int howMany) {
        return null;
    }

    @Override
    public List<TestIO> generateTestCases() {
        return null;
    }

    /**
     * Traverse each XML file pointed to by one of the strings in
     * xmlFilePaths, add nodes to single, large graph.
     * @param xmlFilePaths
     * @return
     */

}
