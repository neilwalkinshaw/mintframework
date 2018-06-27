package mint.inference.text;

import mint.tracedata.TestIO;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import weka.classifiers.Evaluation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by neilwalkinshaw on 25/06/2018.
 */
public class SingleInputNumericalOutputLearnerFASTATest {

    final static Logger LOGGER = Logger.getLogger(SingleInputNumericalOutputLearnerFASTATest.class.getName());



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




    @Test
    public void testWithFASTAData(){
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ALL);
        List<String> trainingSet = new ArrayList<>();

        //File targetDir = new File("src/tests/resources/testFASTAFilesDownloaded");
        File targetDir = new File("src/tests/resources/testFASTAFilesGT");
        Map<String,String> data = readTextFiles(targetDir);


        trainingSet.addAll(data.values());

        Map<TestIO,TestIO> training = runInputs(trainingSet);
        LOGGER.info("Learning");
        SingleInputNumericalOutputLearner sino = new SingleInputNumericalOutputLearner(false);
        sino.setTokenizerChoice(SingleInputNumericalOutputLearner.TokenizerChoice.NGram);
        sino.setClassifierChoice(SingleInputNumericalOutputLearner.ClassifierChoice.GaussianProcess);
        sino.train(training);

        try {
            /*ArffSaver saver = new ArffSaver();
            saver.setInstances(sino.trainInstances);
            saver.setFile(new File("test.arff"));
            saver.writeBatch();*/
            Evaluation eval = new Evaluation(sino.trainInstances);
            //eval.useNoPriors();
            //eval.crossValidateModel(sino.getWekaModel(),sino.testInstances,4,new Random(0));
            eval.crossValidateModel(sino.getWekaModel(),sino.trainInstances,10,new Random(0));
            //eval.crossValidateModel(sino.getWekaModel(),sino.testInstances,10,new Random(0));
            System.out.println(eval.correlationCoefficient());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<TestIO,TestIO> runInputs(List<String> inputs){
        Map<TestIO,TestIO> runs = new HashMap<TestIO,TestIO>();
        int counter = 0;
        for(String s : inputs){

            double numAtoms = BioJavaSUT.measureAlignment(s);
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


}