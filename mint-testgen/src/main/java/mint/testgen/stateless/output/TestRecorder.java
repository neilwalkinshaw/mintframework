package mint.testgen.stateless.output;

import mint.tracedata.TestIO;

import java.util.List;

public interface TestRecorder {

    public void record(List<TestIO> inputs, List<Integer> indices);

}
