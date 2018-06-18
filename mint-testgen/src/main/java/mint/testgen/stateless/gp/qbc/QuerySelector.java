package mint.testgen.stateless.gp.qbc;

import org.apache.log4j.Logger;
import mint.Configuration;
import mint.inference.evo.Chromosome;
import mint.tracedata.TestIO;
import mint.tracedata.types.VariableAssignment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by neilwalkinshaw on 23/03/2017.
 */
public abstract class QuerySelector {

    protected List<VariableAssignment<?>> typeSet;
    protected int randomPool;
    protected String name;
    private final static Logger LOGGER = Logger.getLogger(QuerySelector.class.getName());


    public QuerySelector(List<VariableAssignment<?>> typeSet, String name) {
        Configuration config = Configuration.getInstance();
        this.typeSet = typeSet;
        randomPool = config.RANDOM_POOL;
        this.name = name;
    }

    /**
     * Given a set of previously executed test cases, produce a test set that
     * evokes the most uncertainty from an inferred model.
     * @param done
     * @return
     */
    public TestIO leastCertainQuery(List<TestIO> done, Collection<Chromosome> committee) {
        double biggestVariance = 0D;
        TestIO best = null;

        for(int i = 0; i< randomPool; i++) {
            TestIO seed = generateRandomTestIO(done);
            double variance = simulate(seed, committee);
            if(!(variance>=0D)){ // NaN variance, div by zero?
                LOGGER.warn("NaN variance");
                variance=0.001D;
            }
            assert(variance>=0D);
            if(variance >= biggestVariance){
                best = seed;
                biggestVariance = variance;
            }

        }
        assert(best!=null);
        return best;
    }

    /**
     * Run input on each element in the committee, and return a value indicating level
     * of uncertainty.
     *
     * @param input
     * @param committee
     * @return
     */
    protected abstract double simulate(TestIO input, Collection<Chromosome> committee);

    protected TestIO generateRandomTestIO(Collection<TestIO> done) {

        boolean original = false;
        TestIO ret = null;
        while(!original) {
            List<VariableAssignment<?>> params = new ArrayList<VariableAssignment<?>>();
            for (VariableAssignment<?> type : getParamTypes()) {
                type.setToRandom();
                VariableAssignment newRandom = type.createNew(type.getName(), type.getValue().toString());

                newRandom.setValue(type.getValue());
                params.add(newRandom);
            }
            ret = new TestIO(name,params);
            if(!done.contains(ret)){
                original = true;
            }


        }
        assert(ret!=null);
        return ret;
    }

    protected Collection<VariableAssignment<?>> getParamTypes() {
        return typeSet;
    }

}
