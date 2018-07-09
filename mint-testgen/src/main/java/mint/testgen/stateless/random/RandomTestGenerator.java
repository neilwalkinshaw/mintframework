package mint.testgen.stateless.random;

import mint.tracedata.TestIO;
import mint.testgen.stateless.TestGenerator;
import mint.tracedata.types.VariableAssignment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Simple random test generator
 *
 * Created by neilwalkinshaw on 01/01/2016.
 */

public class RandomTestGenerator extends TestGenerator {

    /**
     * name is the name of the function.
     * types is the signature.
     * testInputs is the list of previous test inputs.
     * @param name
     * @param types
     */
    public RandomTestGenerator(String name, Collection<VariableAssignment<?>> types){
        super(name,types);
    }


    @Override
    public List<TestIO> generateTestCases(int howMany) {
        List<TestIO> candidates = new ArrayList<TestIO>();

        for(int i = 0; i<howMany; i++){
            candidates.add(generateRandomTestIO());
        }
        return candidates;
    }

    @Override
    public List<TestIO> generateTestCases() {
        return generateTestCases(5);
    }


}
