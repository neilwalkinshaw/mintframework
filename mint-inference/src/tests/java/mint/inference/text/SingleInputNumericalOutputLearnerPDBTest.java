package mint.inference.text;

import mint.tracedata.TestIO;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import weka.classifiers.Evaluation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by neilwalkinshaw on 25/06/2018.
 */
public class SingleInputNumericalOutputLearnerPDBTest {

    final static Logger LOGGER = Logger.getLogger(SingleInputNumericalOutputLearnerPDBTest.class.getName());



    @Test
    public void testWithPDBData(){
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ALL);
        List<String> compounds = getCompounds();
        Collections.shuffle(compounds);
        List<String> trainingSet = new ArrayList<>();
        List<String> testSet = new ArrayList<>();
        try {
            for (int i = 0; i < 40; i++) {

                String compound = compounds.get(i);
                LOGGER.info("reading "+compound);
                String protein = new Scanner(new URL("http://files.rcsb.org/view/"+compound+".cif").openStream(), "UTF-8").useDelimiter("\\A").next();
                trainingSet.add(protein);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }

        Map<TestIO,TestIO> training = runInputs(trainingSet);
        Map<TestIO,TestIO> testing = runInputs(testSet);
        LOGGER.info("Learning");
        SingleInputNumericalOutputLearner sino = new SingleInputNumericalOutputLearner(false);
        sino.train(training);

        try {
            Evaluation eval = new Evaluation(SingleInputNumericalOutputLearner.buildDataSet(testing));
            //eval.crossValidateModel(sino.getWekaModel(),SingleInputNumericalOutputLearner.buildDataSet(testing,sino.getTextModel()),4,new Random(2));
            eval.crossValidateModel(sino.getWekaModel(),sino.trainInstances,10,new Random(0));
            System.out.println(eval.correlationCoefficient());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<TestIO,TestIO> runInputs(List<String> inputs){
        Map<TestIO,TestIO> runs = new HashMap<TestIO,TestIO>();
        int counter = 0;
        for(String s : inputs){

            double numAtoms = BioJavaSUT.countAtoms(s);
            StringVariableAssignment inputString = new StringVariableAssignment(Integer.toString(counter),s);
            List<VariableAssignment<?>> vars = new ArrayList<>();
            vars.add(inputString);
            TestIO input = new TestIO(Integer.toString(counter),vars);
            DoubleVariableAssignment count = new DoubleVariableAssignment("count",numAtoms);
            List<VariableAssignment<?>> outs = new ArrayList<>();
            outs.add(count);
            TestIO output = new TestIO("output",outs);
            runs.put(input,output);
            counter++;
        }
        return runs;
    }

    private List<String> getCompounds() {
        List<String> compounds = new ArrayList<>();
        try (Scanner sc = new Scanner(new File("src/tests/resources/compound.idx"))){
            while (sc.hasNextLine()) {
                compounds.add(sc.next());
                sc.nextLine();// consume rest of text from that line
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compounds;
    }

}