package mint.inference.evo;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * Responsible for creating the offspring in an iteration by applying mutation and crossover.
 * Also retains a small number (3) elite offspring from the previous generation.
 *
 * Created by neilwalkinshaw on 06/03/15.
 */
public abstract class AbstractIterator {

    protected List<Chromosome> offSpring, population;
    protected List<Chromosome> elite;
    protected Random rand;
    protected double crossOver, mutation;


    private final static Logger LOGGER = Logger.getLogger(AbstractIterator.class.getName());


    public AbstractIterator(List<Chromosome> population, double crossOver, double mutation, Random r, Collection<Chromosome> elites) {
        offSpring = new ArrayList<Chromosome>();
        rand = r;
        this.population = population;
        this.mutation = mutation;
        this.crossOver = crossOver;

        this.elite = new ArrayList<Chromosome>();
        this.elite.addAll(elites);
    }

    public List<Chromosome> iterate(AbstractEvo gp){
        int numberCrossover = new Double(population.size() * crossOver).intValue();
        int elites = Math.min(3,elite.size());
        int numberMutation = new Double(population.size() * mutation).intValue();
        Collections.shuffle(population);

        crossOver(population, numberCrossover);
        mutate(numberCrossover, numberMutation);


        for(int i = 0; i<elites; i++){
            offSpring.add(elite.get(i));
        }
        int remainder = population.size() - offSpring.size();
        if(remainder > 0){
            offSpring.addAll(gp.generatePopulation(remainder));
        }
        Collections.shuffle(offSpring);
        return offSpring;
    }

    protected abstract void mutate(int numberCrossover,int mutation);

    protected abstract void crossOver(List<Chromosome> pop, int number) ;

}
