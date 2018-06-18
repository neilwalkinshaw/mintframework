package mint.inference.evo.pfsm.apacheGA;

import org.apache.commons.math3.genetics.Chromosome;
import org.apache.log4j.Logger;

import java.util.Comparator;

/**
 * Created by neilwalkinshaw on 09/03/15.
 */
public class ChromosomeComparator implements Comparator<Chromosome> {

    private final static Logger LOGGER = Logger.getLogger(ChromosomeComparator.class.getName());


    /**
     * Returns 1 if fitness of o1 is better than o2, returns -1
     * if fitness of o1 is worse, and 0 otherwise.
     *
     *
     * @param o1
     * @param o2
     * @return
     */
    @Override
    public int compare(Chromosome o1, Chromosome o2) {

        double o1Fit = o1.fitness();
        double o2Fit = o2.fitness();
        //LOGGER.debug("o1: "+o1Fit+", o2: "+o2Fit+ " -> "+(o1Fit - o2Fit));
        double fitVal =  o1Fit-o2Fit;

        if(fitVal < 0D)
            return 1;
        else if(fitVal>0D)
            return -1;
        else return 0;
    }
}
