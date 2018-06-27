package mint.inference.text;

import mint.Configuration;
import mint.inference.text.doc2vec.Doc2Vec;
import mint.tracedata.TestIO;
import org.apache.commons.lang.StringUtils;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.SamplingDataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.*;

/**
 * Created by neilwalkinshaw on 21/06/2018.
 */
public class SingleInputNumericalOutputDeepLearner implements DeepLearner {

    MultiLayerNetwork net;

    @Override
    public void train(Map<TestIO, TestIO> trainingData, int inputLayerSize, int epochs) {


        ParagraphVectors inputModel = trainTextModel(trainingData.keySet(),inputLayerSize);

        DataSet data = buildDataSet(trainingData,inputModel);

        SamplingDataSetIterator sdi = new SamplingDataSetIterator(data,20,20);


        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(Configuration.getInstance().SEED)
                .updater(new Adam(2e-2))
                .l2(1e-5)
                .weightInit(WeightInit.XAVIER)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
                .trainingWorkspaceMode(WorkspaceMode.SEPARATE).inferenceWorkspaceMode(WorkspaceMode.SEPARATE)   //https://deeplearning4j.org/workspaces
                .list()
                .layer(0, new LSTM.Builder().nIn(inputLayerSize).nOut(100)
                        .activation(Activation.TANH).build())
                .layer(1, new RnnOutputLayer.Builder()//.activation(Activation.SOFTMAX)
                        .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(100).nOut(1).build())
                .pretrain(false).backprop(true).build();

        net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(1));

        for( int i=0; i<epochs; i++ ){
            net.fit(sdi);
        }

    }

    public MultiLayerNetwork getModel(){
        return net;
    }

    private DataSet buildDataSet(Map<TestIO, TestIO> trainingData, ParagraphVectors inputModel) {
        List<DataSet> records = new ArrayList<>();
        for(TestIO input : trainingData.keySet()){
            TestIO output = trainingData.get(input);
            String inVal = input.getVals().get(0).getValue().toString();
            Double outVal = (Double)output.getVals().get(0).getValue();
            INDArray inArray = inputModel.inferVector(preProcess(inVal));
            INDArray outArray =  Nd4j.scalar(outVal);
            DataSet toAdd = new DataSet(inArray,outArray);
            records.add(toAdd);
        }
        return DataSet.merge(records);
    }

    public String preProcess(String sentence) {
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
}
