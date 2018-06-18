package mint.tracedata.readers.ngram;

import weka.attributeSelection.PrincipalComponents;
import weka.core.Instances;

import java.util.List;

/**
 * Created by neilwalkinshaw on 27/04/2017.
 */
public class PCAReducer extends Reducer {

    public PCAReducer(List<List<Double>> documentMatrix, List<String> headers){
        Instances instances = createInstances(documentMatrix,headers);
        PrincipalComponents pc = new PrincipalComponents();
        try {
            pc.buildEvaluator(instances);
            transformed = pc.transformedData(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
