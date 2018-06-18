package mint.testgen.stateless.art;

import org.apache.log4j.Logger;
import mint.tracedata.TestIO;
import mint.testgen.stateless.TestGenerator;
import mint.testgen.stateless.runners.execution.TestRunner;

import java.util.List;


/**
 * Created by neilwalkinshaw on 02/06/15.
 */
public class ARTTestRunner extends TestRunner {

    private final static Logger LOGGER = Logger.getLogger(ARTTestRunner.class.getName());

    protected final String label = "ART";


    /**
     *
     */
    public ARTTestRunner(String setupFile, String testPlan){

        super(setupFile,testPlan);

    }

    @Override
    public String getLabel() {
        return label;
    }


    @Override
    public List<TestIO>  generateTests(){
        List<TestIO> toInfer = null;
        try {
            TestGenerator tester = new ART(command,params, testInputs);
            toInfer = tester.generateTestCases();


        }
        catch(Exception e){
            e.printStackTrace();
            LOGGER.error("FAILURE: "+e);

        }
        return toInfer;
    }

}
