package mint.inference.gp.fitness;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * Created by neilwalkinshaw on 25/06/15.
 */
public abstract class Fitness implements Callable<Double> {

    /**
     * This string is a general purpose string to store data pertaining to the
     * fitness of the Chromosome that was evaluated. E.g. a CSV string that can be
     * piped to a file and visualised for debugging purposes.
     */
    protected String fitnessSummary = "";

    /**
     * Calculate the root-mean square deviation from a collection of errors.
     * @param errors
     * @return
     */
    protected double rmsd(Collection<Double> errors){
        double sum = 0D;
        for(double d : errors){
            sum += (d*d);
        }
        double mean = sum / errors.size();
        return Math.sqrt(mean);
    }

    /**
     * Calculate the root-mean square deviation from a collection of errors.
     * @param errors
     * @return
     */
    protected double amd(Collection<Double> errors){
        double sum = 0D;
        for(double d : errors){
            sum += d;
        }
        double mean = sum / errors.size();
        return mean;
    }

    public abstract Double call() throws InterruptedException;

    public String getFitnessSummary(){
        return fitnessSummary;
    }
}
