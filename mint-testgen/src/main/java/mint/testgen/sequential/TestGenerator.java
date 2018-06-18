package mint.testgen.sequential;

import mint.model.Machine;
import mint.tracedata.TraceElement;

import java.util.List;

/**
 * Created by neilwalkinshaw on 13/07/2017.
 */
public interface TestGenerator {

    List<List<TraceElement>> generateTests(Machine m);

    List<List<TraceElement>> generateTests(int t, Machine m);

}
