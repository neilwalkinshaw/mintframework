package mint.testgen.stateless.gp.qbc;

import org.apache.log4j.Logger;
import mint.Configuration;
import mint.inference.evo.AbstractEvo;
import mint.inference.evo.Chromosome;
import mint.tracedata.TestIO;
import mint.testgen.stateless.TestGenerator;
import mint.tracedata.types.VariableAssignment;

import java.util.*;

/**
 * Query by Committee test selection for Node with a numerical output.
 *
 * Created by neilwalkinshaw on 26/08/15.
 */
public class QBC extends TestGenerator {

    private final static Logger LOGGER = Logger.getLogger(QBC.class.getName());
    protected final int increment; // how many tests should be added at each increment?
    protected Collection<Chromosome> committee;
    protected List<VariableAssignment<?>> typeSet;
    protected final int committeeSize;
    protected List<TestIO> testInputs;
    protected String label = "QBC";
    protected QuerySelector querySelector;

    public QBC(String name, Collection<VariableAssignment<?>> types, AbstractEvo gp, List<TestIO> testInputs, QuerySelector selector) {
        super(name, types);
        Configuration config = Configuration.getInstance();
        //randomPool = config.RANDOM_POOL;
        committeeSize = config.QBC_COMMITTEE;
        increment = config.QBC_ITERATIONS;
        computeCommittee(gp);
        typeSet = new ArrayList<VariableAssignment<?>>();
        this.testInputs = testInputs;
        setUpPastVals(types, testInputs);
        this.querySelector = selector;
    }

    private void setUpPastVals(Collection<VariableAssignment<?>> types, List<TestIO> testInputs) {
        Map<String,VariableAssignment> namesToVars = new HashMap<String,VariableAssignment>();
        for(VariableAssignment<?> var: types){
            VariableAssignment copy = var.copy();
            typeSet.add(copy);
            namesToVars.put(var.getName(),copy);
        }
        for(TestIO ti : testInputs){
            for(VariableAssignment<?> input : ti.getVals()){
                namesToVars.get(input.getName()).recordValue(input.getValue());
            }
        }
    }

    protected void computeCommittee(AbstractEvo gp) {
        List<Chromosome> population = gp.getPopulation();
        committee = new ArrayList<Chromosome>();
        Set<String> progs = new HashSet<String>();
        for(int i = 0; i< population.size(); i++){
            String nodeString = population.get(i).toString();
            if(!progs.contains(nodeString)) { //make sure committee are different.
                committee.add(population.get(i).copy());
                progs.add(nodeString);
                //LOGGER.debug("COMMITTEE MEMBER: "+nodeString);
            }
            if(committee.size() >= committeeSize)
                break;
        }

    }


    @Override
    public List<TestIO> generateTestCases(int howMany) {
        LOGGER.debug("Generating "+howMany+" test cases ("+testInputs.size()+" priors to take into account)");
        List<TestIO> candidates = new ArrayList<TestIO>();
        List<TestIO> done = new ArrayList<TestIO>();
        done.addAll(testInputs);
        for(int i = 0; i<howMany; i++){
            TestIO add = querySelector.leastCertainQuery(done,committee);
            assert(add !=null);
            candidates.add(add);
            done.add(add);
        }
        assert(candidates !=null);
        return candidates;
    }


    @Override
    public List<TestIO> generateTestCases() {
        return generateTestCases(increment);
    }
}
