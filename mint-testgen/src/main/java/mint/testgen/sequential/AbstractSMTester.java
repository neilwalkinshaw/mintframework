package mint.testgen.sequential;

import org.apache.log4j.Logger;
import mint.inference.InferenceBuilder;
import mint.model.Machine;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;

import java.util.List;

/**
 * Created by neilwalkinshaw on 11/07/2017.
 */
public abstract class AbstractSMTester {

    protected TraceSet traces;
    protected InferenceBuilder ib;
    protected TestGenerator generator;
    protected long[] times;

    private final static Logger LOGGER = Logger.getLogger(AbstractSMTester.class.getName());


    public AbstractSMTester(InferenceBuilder ib, TraceSet tr){
        this.ib = ib;
        this.traces = tr;
    }

    protected Machine inferMachine(){
        return ib.getInference(traces).infer();
    }

    public void setTestGenerator(TestGenerator tg){
        this.generator = tg;
    }

    /**
     * Generate a fixed number of tests perIteration, and run for
     * a fixed number of iterations.
     * @param perIteration
     * @param iterations
     */
    public void run(int perIteration, int iterations){
        times = new long[iterations];
        long currentTime = System.nanoTime();
        times[0] = currentTime;
        for(int i = 0; i<iterations; i++) {

            //System.out.println(DotGraphWithLabels.summaryDotGraph(m));
            List<List<TraceElement>> testSet = getTestSet(perIteration);
            if(!testSet.isEmpty()) {
                TraceSet executions = runTests(i, testSet);
                if (!(executions.getPos().isEmpty() && executions.getNeg().isEmpty())){
                    traces.getPos().addAll(executions.getPos());
                    traces.getNeg().addAll(executions.getNeg());
                }
            }
            long newTime = System.nanoTime();
            times[i] = newTime - currentTime;
            currentTime = newTime;

        }
    }

    public long[] getTimes(){
        return times;
    }

    /**
     * Called each iteration to obtain tests. If model inference is included in the loop,
     * this should be carried out within this method.
     * @param perIteration
     * @return
     */
    protected abstract List<List<TraceElement>> getTestSet(int perIteration);

    protected TestGenerator getTestGenerator(){
        return generator;
    }


    /**
     * Run test set
     * @return
     */
    protected abstract TraceSet runTests(int iteration, List<List<TraceElement>> testSet);



}
