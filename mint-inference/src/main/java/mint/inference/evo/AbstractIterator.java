package mint.inference.evo;

import mint.inference.gp.selection.SingleOutputTournament;
import mint.inference.gp.tree.Node;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Responsible for creating the offspring in an iteration by applying mutation and crossover.
 * Also retains a small number (3) elite offspring from the previous generation.
 *
 * Created by neilwalkinshaw on 06/03/15.
 */
public abstract class AbstractIterator {

    protected List<Chromosome>  population;
    protected List<Chromosome> elite;
    protected Random rand;
    protected double crossOver, mutation;
    protected Selection sel;


    private final static Logger LOGGER = Logger.getLogger(AbstractIterator.class.getName());


    public AbstractIterator(List<Chromosome> elites, List<Chromosome> population, double crossOver, double mutation, Random r) {
        rand = r;
        this.population = population;
        this.mutation = mutation;
        this.crossOver = crossOver;
        this.elite = new ArrayList<Chromosome>();
        this.elite.addAll(elites);
    }

    public List<Chromosome> iterate(AbstractEvo gp){

        Collections.shuffle(population);
        List<Chromosome> newPopulation = new ArrayList<>();
        for(Chromosome el : elite){
            newPopulation.add(el.copy());
        }
        int numberCrossover = new Double((population.size()-elite.size()) * crossOver).intValue();
        //int elites = Math.min(3,elite.size());
        int numberMutation = new Double((population.size()-elite.size()) * mutation).intValue();
        for(int crossOvers = 0; crossOvers<numberCrossover; crossOvers++){
            sel = gp.getSelection(population);
            List<Chromosome> parents = sel.select(gp.gpConf,2);
            newPopulation.add(crossOver(parents.get(0),parents.get(1)));
        }
        for(int mutations = 0; mutations<numberMutation; mutations++) {
            newPopulation.add(mutate(population.get(rand.nextInt(population.size()))));
        }


        //for(int i = 0; i<elites; i++){
        //    offSpring.add(elite.get(i));
        //}
        int remainder = gp.gpConf.getPopulationSize() - newPopulation.size();
        if(remainder > 0){
            newPopulation.addAll(gp.generatePopulation(remainder));
        }

        Collections.shuffle(newPopulation);
        return newPopulation;
    }

    protected abstract Chromosome mutate(Chromosome toMutate);

    protected abstract Chromosome crossOver(Chromosome parentA, Chromosome parentB) ;

    public Selection getLatestSelection(){
        return sel;
    }

}
