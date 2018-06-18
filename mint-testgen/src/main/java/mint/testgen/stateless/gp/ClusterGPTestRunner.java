package mint.testgen.stateless.gp;

import org.apache.log4j.Logger;
import mint.inference.gp.tree.Node;
import mint.tracedata.TestIO;
import mint.testgen.stateless.TestGenerator;
import mint.testgen.stateless.gp.qbc.ClusteringQBC;
import mint.testgen.stateless.gp.qbc.QuerySelector;

import java.util.List;

/**
 * Created by neilwalkinshaw on 26/08/2016.
 */
public class ClusterGPTestRunner extends GPTestRunner {

    private final static Logger LOGGER = Logger.getLogger(ClusterGPTestRunner.class.getName());


    /**
     * @param setupFile
     * @param testPlan
     */
    public ClusterGPTestRunner(String setupFile, String testPlan) {
        super(setupFile, testPlan);
    }

    /**
     * Infers a GP tree, and generates a test set from it by symbolic execution.
     */
    @Override
    public List<TestIO> generateTests(){
        List<TestIO> toInfer = null;
        try {
            Node<?> gp = infer(testInputs,testOutputs);
            QuerySelector selector = createQuerySelector();
            TestGenerator tester = new ClusteringQBC(command,params,latestGP, testInputs,selector);

            toInfer = tester.generateTestCases();


        }
        catch(Exception e){
            e.printStackTrace();
            LOGGER.error("FAILURE: "+e);

        }
        return toInfer;
    }
}
