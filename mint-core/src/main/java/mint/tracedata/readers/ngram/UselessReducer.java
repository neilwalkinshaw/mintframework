package mint.tracedata.readers.ngram;

import org.apache.commons.math3.stat.descriptive.moment.Variance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.RemoveUseless;

import java.util.*;

/**
 *
 * Uses the WEKA UselessReducer filter to remove features that are "useless",
 * i.e. are constants, etc.
 *
 * Created by neilwalkinshaw on 09/05/2017.
 */
public class UselessReducer extends Reducer {

    public UselessReducer(List<List<Double>> documentMatrix, List<String> headers) {
        Instances instances = createInstances(documentMatrix, headers);
        RemoveUseless pc = new RemoveUseless();

        try {
            pc.setInputFormat(instances);
            transformed = Filter.useFilter(instances,pc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get n-gram distribution for a given number num of the
     * most frequently occurring, non-"useless" n-grams.
     * @param num
     * @return
     */
    public List<List<Double>> getDistribution(int num){
        List<List<Double>> trans = new ArrayList<List<Double>>();
        List<List<Double>> cols = new ArrayList<List<Double>>();
        Iterator<Instance> insIt = transformed.iterator();
        int row = 0;
        while(insIt.hasNext()){
            List<Double> dist = new ArrayList<Double>();
            Instance in = insIt.next();
            double[] array = in.toDoubleArray();
            for(int i = 0; i<array.length; i++){
                dist.add(array[i]);
                List<Double> colList;
                if(cols.size()<=i){
                    colList = new ArrayList<Double>();
                    cols.add(i,colList);
                }
                else
                    colList = cols.get(i);
                colList.add(row,array[i]);
            }
            trans.add(dist);
            row++;
        }
        Map<List<Double>,Double> variances = new HashMap<List<Double>, Double>();
        //Not as many entries as cols, because some list<double>'s are duplicates.

        Variance variance = new Variance();
        for(List<Double> list : cols){
            double[] toCompute = new double[list.size()];
            for(int i = 0; i<list.size(); i++){
                toCompute[i] = list.get(i);
            }
            double v = variance.evaluate(toCompute);
            variances.put(list,v);
        }
        List sortedVariances = new ArrayList<List<Double>>();
        sortedVariances.addAll(cols);
        Collections.sort(sortedVariances,new MappingComparator(variances));
        Iterator<List<Double>> variancesIt = sortedVariances.iterator();
        int counter = 0;
        while(variancesIt.hasNext()){
            List<Double> current = variancesIt.next();
            if(counter>=num) {
                while(cols.contains(current)) { //can contain multiple instances of the same list
                    cols.remove(current);
                }
            }
            counter++;
        }

        trans = new ArrayList<List<Double>>();
        for(int col = 0; col<cols.size(); col++){
            List<Double> c = cols.get(col);
            for(int r = 0; r<c.size(); r++){
                List<Double> instance;
                if(col==0){
                    instance = new ArrayList<Double>();
                    trans.add(r,instance);
                }
                else{
                    instance = trans.get(r);
                }
                instance.add(col,c.get(r));
            }
        }
        return trans;
    }

    class MappingComparator implements Comparator<List<Double>> {
        Map<List<Double>, Double> base;

        public MappingComparator(Map<List<Double>, Double> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with
        // equals.
        public int compare(List<Double> a, List<Double> b) {
            if (base.get(a) > base.get(b)) {
                return -1;
            } else if(base.get(a).equals(base.get(b))) {
                return 0;
            }
            else{
                return 1;
            } // returning 0 would merge keys
        }
    }
}
