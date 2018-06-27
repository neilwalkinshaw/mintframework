package mint.inference.text;

import mint.inference.text.doc2vec.Doc2Vec;
import mint.tracedata.TestIO;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.nd4j.linalg.api.ndarray.INDArray;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Classifier;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMOreg;
import weka.classifiers.rules.M5Rules;
import weka.classifiers.trees.M5P;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.tokenizers.CharacterNGramTokenizer;
import weka.core.tokenizers.Tokenizer;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.*;

/**
 * Created by neilwalkinshaw on 21/06/2018.
 */
public class SingleInputNumericalOutputLearner implements Learner {

    protected boolean deeplearn;

    public SingleInputNumericalOutputLearner(boolean deepLearn){
        this.deeplearn = deepLearn;
    }

    final static Logger LOGGER = Logger.getLogger(SingleInputNumericalOutputLearner.class.getName());

    //ParagraphVectors inputModel;

    Classifier wekaClassifier;

    Instances trainInstances;

    enum ClassifierChoice {
        GaussianProcess,LinearRegression,M5Rules,M5P,SMOreg,MultiLayerPerceptron
    }

    enum TokenizerChoice {
        Word,NGram
    }

    protected TokenizerChoice tokenizerChoice = TokenizerChoice.Word;

    protected ClassifierChoice classifierChoice = ClassifierChoice.GaussianProcess;

    public void setTokenizerChoice(TokenizerChoice tokenizerChoice) {
        this.tokenizerChoice = tokenizerChoice;
    }

    public void setClassifierChoice(ClassifierChoice classifierChoice) {
        this.classifierChoice = classifierChoice;
    };

    ParagraphVectors inputModel;

    @Override
    public void train(Map<TestIO, TestIO> trainingData) {

        LOGGER.info("Training text model");
        trainInstances = null;
        wekaClassifier = getClassifier();
        if(deeplearn) {
            inputModel = trainTextModel(trainingData.keySet(), 100);
            trainInstances = buildDataSet(trainingData,inputModel);

        }
        else{
            trainInstances = buildDataSet(trainingData);

            StringToWordVector filter = new StringToWordVector();
            filter.setTokenizer(getTokenizer());
            filter.setWordsToKeep(100000);
            filter.setMinTermFreq(20);
            filter.setDoNotOperateOnPerClassBasis(true);
            filter.setLowerCaseTokens(true);
            //filter.setTFTransform(true);
            //filter.setIDFTransform(true);

            try {
                filter.setInputFormat(trainInstances);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try{
                trainInstances = Filter.useFilter(trainInstances,filter);

            }
            catch(Exception e){
                e.printStackTrace();
            }

            AttributeSelection attSec = new AttributeSelection();  // package weka.filters.supervised.attribute!
            CfsSubsetEval eval = new CfsSubsetEval();
            GreedyStepwise search = new GreedyStepwise();
            search.setSearchBackwards(true);
            attSec.setEvaluator(eval);
            attSec.setSearch(search);
            try {
                attSec.setInputFormat(trainInstances);
                // generate new data
                trainInstances = Filter.useFilter(trainInstances, attSec);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        LOGGER.info("Training classifier");


        try {
            wekaClassifier.buildClassifier(trainInstances);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private Classifier getClassifier() {
        Classifier toReturn = null;
        switch(classifierChoice){
            case GaussianProcess:
                toReturn =  new GaussianProcesses();
                break;
            case LinearRegression:
                toReturn = new LinearRegression();
                break;
            case SMOreg:
                toReturn =  new SMOreg();
                break;
            case M5P:
                toReturn = new M5P();
                break;
            case M5Rules:
                toReturn = new M5Rules();
                break;
            case MultiLayerPerceptron:
                toReturn = new MultilayerPerceptron();
                break;
        }
        return toReturn;
    }

    private Tokenizer getTokenizer() {
        Tokenizer toReturn = null;
        switch(tokenizerChoice){
            case Word:
                toReturn = new WordTokenizer();
                break;
            case NGram:{
                CharacterNGramTokenizer tok = new CharacterNGramTokenizer();
                tok.setNGramMaxSize(2);
                tok.setNGramMinSize(2);
                toReturn =  tok;
                break;
            }
        }
        return toReturn;
    }

    public void train(Map<TestIO, TestIO> trainingData, Map<TestIO, TestIO> testingData) {

        Set<TestIO> allForTraining = new HashSet<TestIO>();
        allForTraining.addAll(trainingData.keySet());
        allForTraining.addAll(testingData.keySet());

        LOGGER.info("Training text model");
        ParagraphVectors inputModel = trainTextModel(allForTraining,100);

        LOGGER.info("Training Logistic Regression classifier");
        GaussianProcesses classifier = new GaussianProcesses();

        Instances toLearn = buildDataSet(trainingData,inputModel);
        try {
            classifier.buildClassifier(toLearn);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }



    public static Instances buildDataSet(Map<TestIO, TestIO> trainingData, ParagraphVectors inputModel) {
        ArrayList<Attribute> attributes = new ArrayList<>();

        for(int i = 0; i<inputModel.getLayerSize(); i++){

            Attribute at = new Attribute(Integer.toString(i));
            attributes.add(at);
        }
        attributes.add(new Attribute("output"));
        Instances ret = new Instances("samples",attributes,trainingData.size());
        ret.setClassIndex(ret.numAttributes()-1);

        for(TestIO input : trainingData.keySet()){
            TestIO output = trainingData.get(input);
            String inVal = input.getVals().get(0).getValue().toString();
            Double outVal = (Double)output.getVals().get(0).getValue();
            INDArray inArray = inputModel.inferVector(preProcess(inVal));
            float[] fVec = inArray.toFloatVector();
            Instance ins = new DenseInstance(attributes.size());
            for(int i = 0; i<fVec.length; i++){
                ins.setValue(i,(double)fVec[i]);
            }
            ins.setValue(fVec.length,outVal);
            ret.add(ins);
        }
        return ret;
    }

    public static Instances buildDataSet(Map<TestIO, TestIO> trainingData) {



        ArrayList<Attribute> attributes = new ArrayList<>();


        Attribute input = new Attribute("stringinput",  (List<String>) null);
        attributes.add(input);
        Attribute output = new Attribute("output");
        attributes.add(output);
        Instances ret = new Instances("samples",attributes,trainingData.size());
        ret.setClass(output);

        for(TestIO in : trainingData.keySet()){
            TestIO out = trainingData.get(in);
            String inVal = in.getVals().get(0).getValue().toString();
            Double outVal = (Double)out.getVals().get(0).getValue();
            Instance ins = new DenseInstance(attributes.size());
            ins.setDataset(ret);
            ins.setValue(input,inVal);
            ins.setValue(output,outVal);
            ret.add(ins);
        }



        return ret;
    }


    protected static String preProcess(String sentence) {
        sentence = sentence.replaceAll("[^\\w]", " ");

        String[] splitUp = StringUtils.splitByCharacterTypeCamelCase(sentence);
        sentence = "";
        for(String s : splitUp){
            sentence += s+" ";
        }
        System.out.println(sentence.toLowerCase());
        return sentence.toLowerCase();
    }


    private ParagraphVectors trainTextModel(Set<TestIO> testIOs, int inputLayerSize) {

        List<String> docs = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for(TestIO io : testIOs){
            labels.add(io.getName());
            String data = (String)io.getVals().get(0).getValue();
            docs.add(data);
        }
        Doc2Vec d2v = new Doc2Vec(docs,labels,inputLayerSize);
        return d2v.getModel();

    }



    public Classifier getWekaModel(){
        return wekaClassifier;
    }
}
