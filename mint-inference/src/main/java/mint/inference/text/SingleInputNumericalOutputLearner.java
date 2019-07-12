package mint.inference.text;

import mint.inference.UncertaintyBagging;
import mint.tracedata.TestIO;
import org.apache.log4j.Logger;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMOreg;
import weka.classifiers.meta.FilteredClassifier;
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
import weka.filters.MultiFilter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.*;

/**
 * Created by neilwalkinshaw on 21/06/2018.
 */
public class SingleInputNumericalOutputLearner implements Learner {

    final static Logger LOGGER = Logger.getLogger(SingleInputNumericalOutputLearner.class.getName());


    ArrayList<Attribute> attributes;

    protected boolean bagging;

    protected List<List<Double>> fitnessHistory = new ArrayList<>();

    protected boolean recordFitness = false;

    protected Classifier wekaClassifier;

    protected Instances allInstances;

    protected Instances trainingInstances, testingInstances;

    protected Map<TestIO,Instance> instanceMap;

    public enum ClassifierChoice {
        GaussianProcess,LinearRegression,M5Rules,M5P,SMOreg,MultiLayerPerceptron
    }

    public enum TokenizerChoice {
        Word,NGram
    }

    protected TokenizerChoice tokenizerChoice = TokenizerChoice.Word;

    protected ClassifierChoice classifierChoice = ClassifierChoice.GaussianProcess;

    public List<List<Double>> getFitnessHistory() {
        return fitnessHistory;
    }

    public SingleInputNumericalOutputLearner(boolean bagging){
        this.bagging = bagging;
    }

    public void setRecordFitness(boolean recordFitness){
        this.recordFitness = recordFitness;
    }

    public boolean isRecordFitness() {
        return recordFitness;
    }

    public Instances getTestingInstances() {
        return testingInstances;
    }

    public void setTokenizerChoice(TokenizerChoice tokenizerChoice) {
        this.tokenizerChoice = tokenizerChoice;
    }

    public void setClassifierChoice(ClassifierChoice classifierChoice) {
        this.classifierChoice = classifierChoice;
    };


    @Override
    public void train(Map<TestIO, TestIO> trainingSet, Map<TestIO, TestIO> testSet) {

        LOGGER.info("Training text model");

        instanceMap  = buildDataSet(trainingSet, testSet);

        trainingInstances = new Instances("samples", attributes,trainingSet.size());
        for(TestIO key : trainingSet.keySet()){
            trainingInstances.add(instanceMap.get(key));
        }
        trainingInstances.setClassIndex(trainingInstances.numAttributes()-1);
        if(testSet!=null) {
            testingInstances = new Instances("tests", attributes, testSet.size());
            for (TestIO key : testSet.keySet()) {
                testingInstances.add(instanceMap.get(key));
            }
        }
        wekaClassifier = getClassifier();

        LOGGER.info("Training classifier");


        try {
            wekaClassifier.buildClassifier(trainingInstances);
            if(recordFitness){
                Evaluation eval = new Evaluation(trainingInstances);
                eval.crossValidateModel(wekaClassifier,trainingInstances,10,new Random(0));
                List<Double> results = new ArrayList<>();
                results.add(eval.correlationCoefficient());
                results.add(eval.meanAbsoluteError());
                results.add(eval.relativeAbsoluteError());
                results.add(eval.rootMeanSquaredError());
                results.add(eval.rootRelativeSquaredError());
                fitnessHistory.add(results);
                LOGGER.info("5-CV for model is: "+eval.toSummaryString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



    }


    protected Filter getFilter(Instances trainInstances) {
        MultiFilter filter = new MultiFilter();
        StringToWordVector swv = new StringToWordVector();
        swv.setTokenizer(getTokenizer());
        swv.setWordsToKeep(100000);
        swv.setMinTermFreq(20);
        swv.setDoNotOperateOnPerClassBasis(true);
        swv.setLowerCaseTokens(true);
        //filter.setTFTransform(true);
        //filter.setIDFTransform(true);

        AttributeSelection attSec = new AttributeSelection();  // package weka.filters.supervised.attribute!
        CfsSubsetEval eval = new CfsSubsetEval();
        GreedyStepwise search = new GreedyStepwise();
        search.setSearchBackwards(true);
        attSec.setEvaluator(eval);
        attSec.setSearch(search);
        filter.setFilters(new Filter[]{swv,attSec});
        try {
            filter.setInputFormat(trainInstances);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filter;
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
        if(bagging){
            UncertaintyBagging toRet = new UncertaintyBagging();
            toRet.setClassifier(toReturn);
            toReturn = toRet;
        }
        Filter filt = getFilter(allInstances);
        if (filt != null) {
            FilteredClassifier fc = new FilteredClassifier();
            fc.setClassifier(toReturn);
            fc.setFilter(filt);
            toReturn = fc;
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


    public Map<TestIO,Instance> buildDataSet(Map<TestIO, TestIO> trainingData, Map<TestIO, TestIO> testSet) {

        Map<TestIO,Instance> testIOToInstances = new HashMap<>();


         attributes = new ArrayList<>();


        Attribute input = new Attribute("stringinput",  (List<String>) null);
        attributes.add(input);
        Attribute output = new Attribute("output");
        attributes.add(output);
        Instances ret = new Instances("samples",attributes,trainingData.size());
        ret.setClass(output);



        for(TestIO in : trainingData.keySet()){
            Instance newInstance = buildInstance(trainingData, attributes, input, output, ret, in);
            testIOToInstances.put(in,newInstance);
        }
        if(testSet !=null) {
            for (TestIO in : testSet.keySet()) {
                Instance newInstance = buildInstance(testSet, attributes, input, output, ret, in);
                testIOToInstances.put(in,newInstance);
            }
        }
        allInstances = ret;
        return testIOToInstances;
    }

    public Instance  buildInstance(Map<TestIO, TestIO> trainingData, ArrayList<Attribute> attributes, Attribute input, Attribute output, Instances ret, TestIO in) {
        TestIO out = trainingData.get(in);
        String inVal = in.getVals().get(0).getValue().toString();
        Double outVal = null;
        if(out!=null)
            outVal = (Double)out.getVals().get(0).getValue();
        Instance ins = new DenseInstance(attributes.size());
        ins.setDataset(ret);
        ins.setValue(input,inVal);
        if(outVal!=null)
            ins.setValue(output,outVal);
        ret.add(ins);
        return ins;
    }


    public Classifier getWekaModel(){
        return wekaClassifier;
    }
}
