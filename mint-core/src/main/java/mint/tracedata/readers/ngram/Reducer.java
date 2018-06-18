package mint.tracedata.readers.ngram;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by neilwalkinshaw on 09/05/2017.
 */
public class Reducer {
    Instances transformed;

    public List<List<Double>> getDistribution(){
        List<List<Double>> trans = new ArrayList<List<Double>>();
        Iterator<Instance> insIt = transformed.iterator();
        while(insIt.hasNext()){
            List<Double> dist = new ArrayList<Double>();
            Instance in = insIt.next();
            double[] array = in.toDoubleArray();
            for(int i = 0; i<array.length; i++){
                dist.add(array[i]);
            }
            trans.add(dist);
        }
        return trans;
    }

    protected Instances createInstances(List<List<Double>> documentMatrix, List<String> headers) {
        ArrayList<Attribute> attributes = createAttributes(headers);
        Instances instances = new Instances("dataset",attributes,documentMatrix.size());
        for(List<Double> line : documentMatrix){
            double[] elements = new double[line.size()];
            for(int i = 0; i<line.size(); i++){
                elements[i] = line.get(i);
            }
            Instance ins = new SparseInstance(1,elements);
            instances.add(ins);
        }
        return instances;
    }

    private ArrayList<Attribute> createAttributes(List<String> headers) {
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        for(String header : headers){
            Attribute att = new Attribute(header);
            attributes.add(att);
        }
        return attributes;
    }
}
