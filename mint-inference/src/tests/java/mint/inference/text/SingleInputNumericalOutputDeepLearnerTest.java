package mint.inference.text;

import mint.tracedata.TestIO;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by neilwalkinshaw on 22/06/2018.
 */
public class SingleInputNumericalOutputDeepLearnerTest {

    final static Logger LOGGER = Logger.getLogger(SingleInputNumericalOutputDeepLearnerTest.class.getName());




    @Test
    public void testWithXMLData(){
        PropertyConfigurator.configure("log4j.properties");

        Map<TestIO,TestIO> testData = createXMLTestData();
        SingleInputNumericalOutputDeepLearner sino = new SingleInputNumericalOutputDeepLearner();
        sino.train(testData,30,10);

        //RegressionEvaluation eval = new RegressionEvaluation(1);
        //while(mnistTest.hasNext()){
        //    DataSet next = mnistTest.next();
        //    INDArray output = model.output(next.getFeatureMatrix()); //get the networks prediction
        //    eval.eval(next.getLabels(), output); //check the prediction against the true class
        //}
    }


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
            for (int i = 0; i < 200; i++) {

                String compound = compounds.get(i);
                LOGGER.info("reading "+compound);
                String protein = new Scanner(new URL("http://files.rcsb.org/view/"+compound+".cif").openStream(), "UTF-8").useDelimiter("\\A").next();
                trainingSet.add(protein);
            }

            for (int i = 200; i < 400; i++) {
                String compound = compounds.get(i);
                LOGGER.info("reading "+compound);
                String protein = new Scanner(new URL("http://files.rcsb.org/view/"+compound+".cif").openStream(), "UTF-8").useDelimiter("\\A").next();
                testSet.add(protein);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }

        Map<TestIO,TestIO> training = runInputs(trainingSet);
        Map<TestIO,TestIO> testing = runInputs(testSet);
        LOGGER.info("Learning");
        SingleInputNumericalOutputDeepLearner sino = new SingleInputNumericalOutputDeepLearner();
        sino.train(training,100,10);

        /*RegressionEvaluation eval = new RegressionEvaluation(1);
        while(mnistTest.hasNext()){
            DataSet next = mnistTest.next();
            INDArray output = model.output(next.getFeatureMatrix()); //get the networks prediction
            eval.eval(next.getLabels(), output); //check the prediction against the true class
        }*/
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

    private Map<TestIO,TestIO> createXMLTestData() {
        File targetDir = new File("src/tests/resources/testXMLFiles");
        Map<String,String> data = readTextFiles(targetDir);

        List<String> labels = new ArrayList<>();
        List<String> paras = new ArrayList<>();

        for(String key : data.keySet()){
            labels.add(key);
            paras.add(data.get(key));
        }

        Map<TestIO,TestIO> testData = new HashMap<>();

        for(String text : paras){
            double count = numStrings(text,"maction");
            List<VariableAssignment<?>> inputs = new ArrayList<>();
            inputs.add(new StringVariableAssignment("xml",text));
            TestIO input = new TestIO("xml",inputs);
            List<VariableAssignment<?>> out = new ArrayList<>();
            out.add(new DoubleVariableAssignment("count",count));
            TestIO output = new TestIO("count",out);
            testData.put(input,output);
        }

        return testData;
    }

    private int numStrings(String str, String findStr){
        int lastIndex = 0;
        int count = 0;

        while(lastIndex != -1){

            lastIndex = str.indexOf(findStr,lastIndex);

            if(lastIndex != -1){
                count ++;
                lastIndex += findStr.length();
            }
        }
        return count;
    }

    Map<String,String> readTextFiles(File directory){

        Map<String,String> data = new HashMap<>();

        File[] listOfFiles = directory.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            File file = listOfFiles[i];
            if (file.isFile()) {
                try {
                    String content = FileUtils.readFileToString(file, Charset.defaultCharset());
                    String label = file.getParent()+file.getName();
                    data.put(label,content);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return data;
    }

}