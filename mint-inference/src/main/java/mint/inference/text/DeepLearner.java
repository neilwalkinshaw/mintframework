package mint.inference.text;

import mint.tracedata.TestIO;

import java.util.Map;

/**
 * Created by neilwalkinshaw on 21/06/2018.
 */
public interface  DeepLearner {

    void train(Map<TestIO,TestIO> trainingData,int inputLayerSize, int epochs);



}
