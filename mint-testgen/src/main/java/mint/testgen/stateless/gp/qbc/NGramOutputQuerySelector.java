package mint.testgen.stateless.gp.qbc;

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
public class NGramOutputQuerySelector extends QuerySelector{

    private final static Logger LOGGER = Logger.getLogger(NGramOutputQuerySelector.class.getName());



    public NGramOutputQuerySelector(List<VariableAssignment<?>> typeSet, String name) {
        super(typeSet, name);
    }



    protected double simulate(TestIO seed, Collection<Chromosome> committee) {
        List<List<Double>> outputs = new ArrayList<List<Double>>();
        for(Chromosome member : committee){
            NodeExecutor<Node<VariableAssignment<?>>> nEx = new NodeExecutor((Node<?>)member);
            List<Double> out = null;
            try {
                out = (List<Double>)nEx.execute(seed);
                outputs.add(out);
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
        return variance(outputs);
    }

    private double variance(List<List<Double>> outputs) {
        List<Double> rmsdErrors = new ArrayList<Double>();
        for(int i = 0; i< outputs.size(); i++){
            for(int j = i+1; j<outputs.size(); j++){
                List<Double> from = outputs.get(i);
                List<Double> to = outputs.get(j);
                int minLength = Math.min(from.size(),to.size());
                List<Double> errors = new ArrayList<Double>();
                for(int k = 0; k<minLength; k++){
                    double error = Math.abs(from.get(k) - to.get(k));
                    if(Double.isNaN(error) || Double.isInfinite(error))
                        errors.add(10000000D);
                    else
                        errors.add(error);
                }
                rmsdErrors.add(rmsd(errors));
            }
        }
        return mean(rmsdErrors);
    }

    private double mean(List<Double> rmsdErrors) {
        double sum = 0;
        for(int i = 0; i<rmsdErrors.size(); i++){
            sum+=rmsdErrors.get(i);
        }
        return sum/rmsdErrors.size();
    }

    protected double rmsd(Collection<Double> errors){
        double sum = 0D;
        for(double d : errors){
            sum += (d*d);
        }
        double mean = sum / errors.size();
        return Math.sqrt(mean);
    }
}
