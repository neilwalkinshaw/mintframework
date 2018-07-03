package mint.testgen.stateless.text;

import mint.inference.text.SingleInputNumericalOutputLearner;
import mint.testgen.stateless.TestGenerator;
import mint.tracedata.TestIO;
import mint.tracedata.types.VariableAssignment;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.log4j.Logger;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

/**
 * Created by neilwalkinshaw on 27/06/2018.
 */
public class TextIOGenerator extends TestGenerator {

    private final static Logger LOGGER = Logger.getLogger(TextIOGenerator.class.getName());


    protected int increment = 5;

    protected SingleInputNumericalOutputLearner sino;

    protected List<TestIO> pool;


    protected final Classifier latestModel;




    public TextIOGenerator(String name, Collection<VariableAssignment<?>> types, int increment, Classifier model, List<TestIO> pool, SingleInputNumericalOutputLearner sino) {
        super(name, types);
        this.increment = increment;
        this.latestModel = model;
        this.sino = sino;
        this.pool = pool;
    }

    @Override
    public List<TestIO> generateTestCases(int howMany) {
        return pickLeastCertain(howMany,pool);
    }


    /**
     * Pick least certain candidates from a set. This involves computing an uncertainty
     * score for every element in candidates.
     *
     * Can also output list of uncertainties if this is helpful.
     * @param howMany
     * @param candidates
     * @return
     */
    private List<TestIO> pickLeastCertain(int howMany, List<TestIO> candidates) {
        LinkedHashMap<TestIO,Double> scores = new LinkedHashMap<>();
        Instances trainingIns = sino.getTestingInstances();
        List<Double> uncertainties = new ArrayList<>();
        for(int i = 0; i<trainingIns.size(); i++){
            Instance current = trainingIns.get(i);
            double[] classifications = new double[0];
            try {
                classifications = latestModel.distributionForInstance(current);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Variance var = new Variance();
            double variance = var.evaluate(classifications);
            uncertainties.add(variance);

        }
        for(int i = 0; i<candidates.size(); i++){
            scores.put(candidates.get(i),uncertainties.get(i));
        }
        scores = sortByComparator(scores,true);
        List<TestIO> ordered = new ArrayList<>();
        ordered.addAll(scores.keySet());
        ordered = ordered.subList(0,howMany);
        return ordered;
    }



    private static LinkedHashMap<TestIO, Double> sortByComparator(Map<TestIO, Double> unsortMap, final boolean order)
    {

        List<Map.Entry<TestIO, Double>> list = new LinkedList<Map.Entry<TestIO, Double>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<TestIO, Double>>()
        {
            public int compare(Map.Entry<TestIO, Double> o1,
                               Map.Entry<TestIO, Double> o2)
            {
                if (order)
                {
                    return o2.getValue().compareTo(o1.getValue());
                }
                else
                {
                    return o1.getValue().compareTo(o2.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        LinkedHashMap<TestIO, Double> sortedMap = new LinkedHashMap<TestIO, Double>();
        for (Map.Entry<TestIO, Double> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }



    @Override
    public List<TestIO> generateTestCases() {
        return generateTestCases(increment);
    }
}
