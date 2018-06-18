package mint.testgen.sequential.coq;

import mint.inference.InferenceBuilder;
import mint.model.Machine;
import mint.testgen.sequential.AbstractSMTester;
import mint.testgen.sequential.TestGenerator;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;

import java.util.*;

/**
 * Created by neilwalkinshaw on 20/09/2017.
 */
public class CoqSMTester extends AbstractSMTester {

    Map<String,Machine> clusterMachines;

    public CoqSMTester(InferenceBuilder ib, TraceSet tr) {
        super(ib, tr);

        createLemmaMachines();
    }

    private void createLemmaMachines() {
        clusterMachines = new HashMap<String,Machine>();
        //ACCESS files, infer machines for each cluster.
    }

    @Override
    protected TestGenerator getTestGenerator(){
        return null;
    }

    @Override
    protected List<List<TraceElement>> getTestSet(int perIteration) {
        //need to include model inference in here...



        Machine m = inferMachine();
        return getTestGenerator().generateTests(perIteration,m);

    }

    @Override
    protected TraceSet runTests(int iteration, List<List<TraceElement>> testSet) {
        TraceSet ts = new TraceSet();
        Set<String> clusteredLemmas = new HashSet<String>();
        return ts;
    }
}
