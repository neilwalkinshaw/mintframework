package mint.inference.gp.fitness;

import mint.inference.gp.tree.Node;
import mint.tracedata.types.VariableAssignment;

import java.util.List;
import java.util.Map;

/**
 * Created by neilwalkinshaw on 05/03/15.
 */
public class SingleOutputDoubleFitness extends SingleOutputFitness<Double> {

    protected double ceiling = 100000D;//Cannot make this Double.MAX, because overall fitness will yield a NaN from multiple penalties.


    private final static org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(SingleOutputDoubleFitness.class.getName());

    public SingleOutputDoubleFitness(Map<List<VariableAssignment<?>>, VariableAssignment<?>> evals, Node<VariableAssignment<Double>> individual, int maxDepth) {
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
    protected double distance(Double actual, Object exp) throws InvalidDistanceException {
        if(exp instanceof Double) {
            Double expected = (Double) exp;
            if (actual.isNaN() || actual.isInfinite()) {
                if(expected.isInfinite() || expected.isNaN())
                    return 0;
                else
                    return ceiling;
            }
            if (expected.isNaN() || expected.isInfinite())
                 return ceiling;
                //return 0;
            //return Math.abs(actual-expected);
            return Math.min(Math.abs(actual - expected),ceiling); //prevent the fitness function from running away with massive errors.
        }
        else if(exp instanceof Integer){
            Integer intExp = (Integer) exp;
            return distance(actual,(double)intExp.intValue());
        }
        else
            throw new InvalidDistanceException();
    }
}
