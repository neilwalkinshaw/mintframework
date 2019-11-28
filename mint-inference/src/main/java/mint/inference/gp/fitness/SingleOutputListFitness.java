package mint.inference.gp.fitness;

import mint.inference.gp.tree.Node;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.VariableAssignment;

import java.util.List;
import java.util.Map;

/**
 * Created by neilwalkinshaw on 05/03/15.
 */
public class SingleOutputListFitness extends SingleOutputFitness<List> {

    protected double ceiling = 100000D;//Cannot make this Double.MAX, because overall fitness will yield a NaN from multiple penalties.

    protected int distanceCall = 0;

    private final static org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(SingleOutputListFitness.class.getName());

    public SingleOutputListFitness(Map<List<VariableAssignment<?>>, VariableAssignment<?>> evals, Node<VariableAssignment<List>> individual, int maxDepth) {
        super(evals,individual, maxDepth);
        //System.out.println(penalty);
    }

    /**
     *
     * @param actual - returned by SUT
     * @param exp - produced by model
     * @return
     * @throws InvalidDistanceException
     */
    @Override
    protected double distance(List actual, Object exp) throws InvalidDistanceException {
        distanceCall++;
        List expected = (List) exp;
        double[] from = new double[actual.size()];
        double[] to = new double[expected.size()];

        if(expected.size() != actual.size())
            LOGGER.error(expected.size() +","+actual.size());

        assert(expected.size()==actual.size());

        for(int i = 0; i<expected.size();i++){
            double act = getDouble(actual.get(i));
            from[i] = act;
            double ex = getDouble(expected.get(i));
            to[i] = ex;

            fitnessSummary+=distanceCall+","+actual.get(i)+","+expected.get(i)+"\n";
        }

        return 1 - Math.abs(cosineSimilarity(from,to));

    }

    public static double getDouble(Object o) {
        if(o instanceof Boolean){
            Boolean boo = (Boolean)o;
            if(boo.booleanValue())
                return 1D;
            else
                return 0D;
        }
        else if(o instanceof Double){
            Double val = (Double) o;
            return val.doubleValue();
        }
        else if (o instanceof Integer) {
            Integer val = (Integer) o;
            return (double)val.intValue(); //must be integer or double.
        }
        else if(o instanceof BooleanVariableAssignment){
            BooleanVariableAssignment boo = (BooleanVariableAssignment) o;
            if(boo.getValue().booleanValue()){
                return 1D;
            }
            else
                return 0D;
        }
        else{
            VariableAssignment var = (VariableAssignment)o;
            Number val = (Number) var.getValue();
            return val.doubleValue();
        }
    }

    public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }


}
