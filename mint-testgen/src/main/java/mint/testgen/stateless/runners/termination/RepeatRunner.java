package mint.testgen.stateless.runners.termination;

import mint.testgen.stateless.output.BasicTextRecorder;
import mint.testgen.stateless.output.TestRecorder;
import mint.testgen.stateless.runners.execution.TestRunner;
import mint.tracedata.TestIO;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by neilwalkinshaw on 25/08/2016.
 */
public abstract class RepeatRunner {

    protected TestRunner host;

    protected List<Integer> indexList;
    protected List<Long> timeList;
    protected Long time;

    protected TestRecorder recorder;

    public RepeatRunner(TestRunner host){

        indexList = new ArrayList<Integer>();
        timeList = new ArrayList<Long>();
        this.host = host;
        time = System.currentTimeMillis();
        recorder = host.getTestRecorder();
    }

    protected void runTest(List< TestIO > toInfer){
        host.run(toInfer);

        indexList.add(host.getAllTestInputs().size()-1);

        timeList.add(System.currentTimeMillis()-time);
        assert(toInfer !=null);
    }

    public abstract void runTests(List< TestIO > toInfer);



}
