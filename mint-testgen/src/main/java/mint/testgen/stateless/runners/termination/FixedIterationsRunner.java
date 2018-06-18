package mint.testgen.stateless.runners.termination;

import org.apache.log4j.Logger;
import mint.tracedata.TestIO;
import mint.testgen.stateless.runners.execution.TestRunner;

import java.util.List;

/**
 * Created by neilwalkinshaw on 25/08/2016.
 */
public class FixedIterationsRunner extends RepeatRunner {

    protected int iterations;

    private final static Logger LOGGER = Logger.getLogger(TestRunner.class.getName());


    public FixedIterationsRunner(TestRunner host, int iterations){
        super(host);
        this.iterations= iterations;
    }

    @Override
    public void runTests(List< TestIO > toInfer) {
        for (int i=0; i<iterations; i++){
            LOGGER.debug("Running "+toInfer.size()+" tests.");
            runTest(toInfer);
            toInfer = host.generateTests();
        }
        recordTestSet(host.getTestInputs(),indexList);
    }

}
