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
 * Created by neilwalkinshaw on 05/03/2018.
 */
public class BooleanOutputQuerySelector extends QuerySelector {

    private final static Logger LOGGER = Logger.getLogger(BooleanOutputQuerySelector.class.getName());


    public BooleanOutputQuerySelector(List<VariableAssignment<?>> typeSet, String name) {
        super(typeSet, name);
    }

    @Override
    protected double simulate(TestIO input, Collection<Chromosome> committee) {
        List<Boolean> outputs = new ArrayList<Boolean>();
        for(Chromosome member : committee){
            NodeExecutor<Node<VariableAssignment<?>>> nEx = new NodeExecutor((Node<?>)member);
            Object out = null;
            try {
                out = nEx.execute(input);
                assert(out instanceof Boolean);
                outputs.add((Boolean)out);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
            catch(Exception e){
                LOGGER.debug("Faulty member: "+member);
                LOGGER.debug("Inputs:" +input);
                LOGGER.debug(e.toString());
                e.printStackTrace();
                System.exit(0);
            }

        }
        assert(!outputs.isEmpty());
        //LOGGER.debug(seed);
        return 1-consistency(outputs);
    }

    private double consistency(List<Boolean> outputs) {
        double numTrue = 0;
        for(boolean output: outputs){
            if(output)
                numTrue++;
        }
        double proportionTrue = numTrue / outputs.size();
        double proportionFalse = 1 - proportionTrue;
        return Math.max(proportionFalse,proportionTrue);
    }
}
