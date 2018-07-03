package mint.inference.text;

import mint.tracedata.TestIO;

import java.util.Map;

/**
 * Created by neilwalkinshaw on 25/06/2018.
 */
public interface Learner {

    void train(Map<TestIO, TestIO> trainingSet, Map<TestIO, TestIO> trainingData);
}
