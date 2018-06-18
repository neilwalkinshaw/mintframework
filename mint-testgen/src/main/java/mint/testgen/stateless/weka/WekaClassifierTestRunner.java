package mint.testgen.stateless.weka;

import mint.Configuration;
import mint.testgen.stateless.ProcessExecution;
import mint.testgen.stateless.TestGenerator;
import mint.testgen.stateless.runners.execution.TestRunner;
import mint.tracedata.TestIO;
import mint.tracedata.types.FilePointerVariableAssignment;
import mint.tracedata.types.VariableAssignment;
import org.apache.log4j.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.util.*;


/**
 * Created by neilwalkinshaw on 02/06/15.
 */
public class WekaClassifierTestRunner extends TestRunner {

    protected Configuration.Data algo;
    private final static Logger LOGGER = Logger.getLogger(WekaClassifierTestRunner.class.getName());


    /**
     *
     * @param algo
     */
    public WekaClassifierTestRunner(String setupFile, String testPlan, Configuration.Data algo){

        super(setupFile,testPlan);
        this.algo = algo;
        this.minTests = 50;

    }

    @Override
    public String getLabel() {
        return "WEKA";
    }

    @Override
    public List<TestIO> generateTests(){
        List<TestIO> toInfer = null;
        try {
            InputOutputClassiferInference ioInf = new InputOutputClassiferInference(testInputs, testOutputs, algo);

            Classifier classifier = ioInf.getClassifier();
            LOGGER.debug(classifier);


            TestGenerator tester = getTester(classifier,algo,ioInf.getInstances());

            toInfer = tester.generateTestCases();


        }
        catch(Exception e){
            e.printStackTrace();
            LOGGER.error("Initial training set failed to elicit any varied output - please seed with initial training data.");

        }
        return toInfer;
    }

    /**
     * Get an instance of a TestGenerator that will produce new inputs
     * to supply to the SUT
     * @param c
     * @param algo
     * @param ins
     * @return
     */
    protected TestGenerator getTester(Classifier c,Configuration.Data algo, Instances ins) {
        //Configuration config = Configuration.getInstance();
        //if(config.TEST_SELECTION == Configuration.TestSelection.fromModel)
        return new DataModelTestGenerator(command,c,params,algo);
        //else if(config.TEST_SELECTION == Configuration.TestSelection.uncertaintySampling)
        //    return new UncertaintySamplingTestGenerator(command,params,c,100000,ins); //CHANGE ME
        //else
        //    return new SymbolicGPTestGenerator(command,c,params,algo);
    }

    /**
     * Test the results against the model to determine if they match.
     * @param newInputs
     * @param output
     * @param inf
     * @return
     */
    protected List<TestIO> test(List<TestIO> newInputs, List<VariableAssignment<?>> output, InputOutputClassiferInference inf) {
        List<Attribute> attributes = inf.getAttributes();
        List<TestIO> outlist = new ArrayList<TestIO>();
        Set<Instance> instances = new HashSet<Instance>();
        List<TestIO> failedTests = new ArrayList<TestIO>();
        File redirectFile = null;
        for(TestIO test : newInputs){
            List<String> commands = new ArrayList<String>();
            commands.add(test.getName());
            for(VariableAssignment<?> assignment : test.getVals()){
                if(!(assignment instanceof FilePointerVariableAssignment))
                    commands.add(assignment.getValue().toString());
                else{
                    FilePointerVariableAssignment fpv = (FilePointerVariableAssignment) assignment;
                    redirectFile = new File(fpv.getValue());
                    if(redirectFile.isDirectory()){
                        redirectFile = selectRandomFileFrom(redirectFile);
                        fpv.setValue(redirectFile.getAbsolutePath());
                    }
                }
            }


            TestIO res = timedCall(new ProcessExecution(commands, redirectFile, time, output));
            if(res == null)
                continue;
            outlist.add(res);
            Instance ins = new DenseInstance(params.size());

            InputOutputClassiferInference.convertToInstance(test.getVals(), ins, attributes);
            instances.add(ins);


        }
        Instances wekaIns = new Instances("tests",(ArrayList<Attribute>)attributes,instances.size());
        wekaIns.addAll(instances);
        wekaIns.setClassIndex(attributes.size()-1);

        failedTests.addAll(newInputs);
        Instances filtered = inf.getInstances();
        LOGGER.info("Eval: "+evaluate(filtered,inf.getClassifier())+", total tests: "+testInputs.size());
        return failedTests;
    }

    /**
     * Cross-validate the classifier c for the given instances i.
     * @return the kappa score for the model
     */
    protected double evaluate(Instances i, Classifier c){
        double retVal = 0D;
        try {
            Configuration configuration = Configuration.getInstance();
            Evaluation ev = new Evaluation(i);
            ev.crossValidateModel(c, i, 10, new Random(configuration.SEED));
            retVal = ev.kappa();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return retVal;
    }
}
