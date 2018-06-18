package mint.testgen.stateless.art;

import mint.Configuration;
import mint.tracedata.TestIO;
import mint.testgen.stateless.TestGenerator;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.VariableAssignment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Implementation of Adaptive Random Testing
 * Chen, Leung, Mak, Advances in Computer Science, 2005
 *
 * Created by neilwalkinshaw on 01/01/2016.
 */

public class ART extends TestGenerator {

    List<TestIO> prevInputs;

    protected final int poolSize;

    /**
     * name is the name of the function.
     * types is the signature.
     * testInputs is the list of previous test inputs.
     * @param name
     * @param types
     * @param testInputs
     */
    public ART(String name, Collection<VariableAssignment<?>> types, List<TestIO> testInputs){
        super(name,types);
        Configuration config = Configuration.getInstance();
        poolSize = config.RANDOM_POOL;
        prevInputs = testInputs;
    }


    @Override
    public List<TestIO> generateTestCases(int howMany) {
        List<TestIO> candidates = new ArrayList<TestIO>();

        for(int i = 0; i<howMany; i++){


            candidates.add(maximiseDischord());
        }
        return candidates;
    }

    @Override
    public List<TestIO> generateTestCases() {
        return generateTestCases(5);
    }

    protected TestIO maximiseDischord() {
        double biggestDistance = 0D;
        TestIO best = null;
        Collection<TestIO> candidateSet = new HashSet<TestIO>();
        for(int i = 0; i< poolSize; i++) {
            candidateSet.add(generateRandomTestIO());
        }
        for(TestIO io:candidateSet) {
            double dist = minDistance(io, prevInputs);
            if (dist >= biggestDistance) {
                best = io;
                biggestDistance = dist;
            }
        }

        return best;
    }

    /**
     * Calculate the minimum distance between io and the previous test cases.
     * @param io
     * @return
     */
    public static double minDistance(TestIO io, List<TestIO> prevInputs) {
        double[] ioParams = getVals(io);
        double minDistance = Double.MAX_VALUE;
        for(TestIO ti : prevInputs){
            assert(ti != null);
            double[] prevParams = getVals(ti);
            assert(prevParams.length >0 && ioParams.length > 0);
            double dist = calculateDistance(ioParams,prevParams);
            assert(dist>=0D);
            if(dist < minDistance)
                minDistance = dist;
        }
        return minDistance;
    }

    protected static double[] getVals(TestIO io) {
        assert(io!=null);
        assert(io.getVals()!=null);
        double[] vals = new double[io.getVals().size()];
        List<VariableAssignment<?>> vars = io.getVals();
        for(int i = 0; i< vars.size(); i++){
            VariableAssignment<?> var = vars.get(i);
            if(var instanceof DoubleVariableAssignment) {
                DoubleVariableAssignment dva = (DoubleVariableAssignment) var;
                vals[i] = dva.getValue();
            }
            else if(var instanceof IntegerVariableAssignment){
                IntegerVariableAssignment dva = (IntegerVariableAssignment) var;
                vals[i] = (double)dva.getValue();
            }
        }
        return vals;
    }

    protected static double calculateDistance(double[] array1, double[] array2)
    {
        double Sum = 0.0;
        for(int i=0;i<array1.length;i++) {
            Sum = Sum + Math.pow((array1[i]-array2[i]),2.0);
        }
        return Math.sqrt(Sum);
    }
}
