package mint.testgen.stateless.gp.qbc;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.log4j.Logger;
import mint.inference.evo.Chromosome;
import mint.inference.gp.NodeExecutor;
import mint.inference.gp.tree.Node;
import mint.tracedata.TestIO;
import mint.tracedata.types.VariableAssignment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by neilwalkinshaw on 23/03/2017.
 */
public class DoubleOutputQuerySelector extends QuerySelector{

    private final static Logger LOGGER = Logger.getLogger(DoubleOutputQuerySelector.class.getName());

    public DoubleOutputQuerySelector(List<VariableAssignment<?>> typeSet, String name) {
        super(typeSet, name);
    }


    protected double simulate(TestIO seed, Collection<Chromosome> committee) {
        List<Double> outputs = new ArrayList<Double>();
        for(Chromosome member : committee){
            NodeExecutor<Node<VariableAssignment<?>>> nEx = new NodeExecutor((Node<?>)member);
            Object out = null;
            try {
                out = nEx.execute(seed);
                assert(out instanceof Double);
                if(out instanceof Integer){
                    Integer outInt = (Integer) out;
                    out = (double) outInt.intValue();
                }
                outputs.add((Double)out);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
            catch(Exception e){
                LOGGER.debug("Faulty member: "+member);
                LOGGER.debug("Inputs:" +seed);
                LOGGER.debug(e.toString());
                e.printStackTrace();
                System.exit(0);
            }

        }
        assert(!outputs.isEmpty());
        //LOGGER.debug(seed);
        return meanAbsoluteDeviation(outputs);
    }


    private double meanAbsoluteDeviation(List<Double> outputs){
        /*for(int i = 0; i<outputs.size();i++){
            System.out.print(outputs.get(i)+",");
        }*/
        Mean mean = new Mean();
        for(int i = 0  ;i<outputs.size(); i++){
            Double numb = outputs.get(i);
            if(numb.isInfinite() || numb.isNaN())
                continue;
            else
                mean.increment(numb);
        }
        double meanDoub = mean.getResult();
        Mean meanDist = new Mean();
        for(int i = 0  ;i<outputs.size(); i++){
            Double numb = Math.abs(outputs.get(i) - meanDoub);
            if(numb.isInfinite() || numb.isNaN())
                meanDist.increment(10000000);
            else
                meanDist.increment(numb);
        }
        double result = meanDist.getResult();
        //System.out.println(result);
        return result;
    }
}
