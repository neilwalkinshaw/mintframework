package mint.inference;

import weka.classifiers.Classifier;
import weka.classifiers.meta.Bagging;
import weka.core.Instance;

/**
 * Created by neilwalkinshaw on 27/06/2018.
 */
public class UncertaintyBagging extends Bagging {

    @Override
    public double[] distributionForInstance(Instance instance) throws Exception {
        double[] predictions = new double[this.m_Classifiers.length];
        for(int i = 0; i<m_Classifiers.length; i++) {
            Classifier current = m_Classifiers[i];
            predictions[i] = current.classifyInstance(instance);
        }
        return predictions;
    }
}
