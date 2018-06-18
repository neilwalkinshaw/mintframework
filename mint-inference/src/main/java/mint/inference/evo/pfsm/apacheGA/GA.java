package mint.inference.evo.pfsm.apacheGA;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.genetics.*;
import org.apache.log4j.Logger;

/**
 * Created by neilwalkinshaw on 26/05/2016.
 */
public class GA extends GeneticAlgorithm {

    private final static Logger LOGGER = Logger.getLogger(GeneticAlgorithm.class.getName());


    public GA(CrossoverPolicy crossoverPolicy, double crossoverRate, MutationPolicy mutationPolicy, double mutationRate, SelectionPolicy selectionPolicy) throws OutOfRangeException {
        super(crossoverPolicy, crossoverRate, mutationPolicy, mutationRate, selectionPolicy);
    }

    @Override
    public Population nextGeneration(Population current){
        ThreadedElitisticListPopulation evolved = (ThreadedElitisticListPopulation)super.nextGeneration(current);
        LOGGER.debug("FITTEST: "+evolved.getFittest());
        return evolved;
    }
}
