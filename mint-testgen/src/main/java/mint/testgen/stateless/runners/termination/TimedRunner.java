package mint.testgen.stateless.runners.termination;

import mint.tracedata.TestIO;
import mint.testgen.stateless.runners.execution.TestRunner;

import java.util.List;

/**
 * Created by neilwalkinshaw on 25/08/2016.
 */
public class TimedRunner extends RepeatRunner {

    final protected long timeLimit;

    public TimedRunner(TestRunner host, long time){
        super(host);
        this.timeLimit= time;
    }

    @Override
    public void runTests(List< TestIO > toInfer) {
        long currentTime = System.currentTimeMillis();
        long finalTime = currentTime + timeLimit;
        do{

            runTest(toInfer);
            toInfer = host.generateTests();
        }while(System.currentTimeMillis() < finalTime);
        recordTestSet(host.getTestInputs(),indexList);
    }
}
