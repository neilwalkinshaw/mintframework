package mint.inference.evo.pfsm.matrix;

import org.apache.log4j.Logger;
import mint.inference.evo.AbstractIterator;
import mint.inference.evo.Chromosome;
import mint.model.statepair.StatePair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Responsible for creating the offspring in an iteration by applying mutation and crossover.
 * Also retains a small number (3) elite offspring from the previous generation.
 *
 * Created by neilwalkinshaw on 06/03/15.
 */
public class Iterate extends AbstractIterator {


    private final static Logger LOGGER = Logger.getLogger(Iterate.class.getName());


    public Iterate(List<Chromosome> population, double crossOver, double mutation, Random r, Collection<Chromosome> elites) {
        super(population, crossOver, mutation, r, elites);
    }

    protected void mutate(int numberCrossover,int mutation) {
        for(int i = numberCrossover; i<numberCrossover+mutation; i++){
            MergingTable aNode = (MergingTable)population.get(i);
            aNode.mutate(this.rand);
            offSpring.add(aNode);
        }
    }

    private int select(boolean[] done, List<Integer> avoid, int limit){
        for(int i = 0; i< avoid.size(); i++){
            int index = avoid.get(i);
            done[index] = true;
        }
        int parentB = -1;
        for(int i = 0; i<limit; i++){
            if(!done[i]) {
                parentB = i;
                break;
            }
        }
        return parentB;
    }

    protected void crossOver(List<Chromosome> pop, int number) {

        int count = 0;

        boolean[] done = new boolean[number];
        for(int i = 0; i<number; i++){
            done[i] = false;
        }
        while(count<number){

            int parentA = select(done,new ArrayList(), number);
            if(parentA<0)
                break; //no more crossovers possible.
            ArrayList<Integer> avoid = new ArrayList<Integer>();
            avoid.add(parentA);

            int parentB = select(done, avoid, number);
            if (parentB < 0) {
                done[parentA] = true;
                break;
            }

            MergingTable parentBTable = (MergingTable)pop.get(parentB);
            MergingTable parentATable = (MergingTable)pop.get(parentA).copy();
            parentATable.sortMerges();
            parentBTable.sortMerges();
            double proportion = rand.nextDouble();
            int n = (int)((double)parentATable.merges.size() * proportion);
            List<StatePair> bMerges = parentBTable.merges;
            n = Math.min(n,bMerges.size());
            List<StatePair> aMerges = parentATable.merges;
            //LOGGER.debug("Proportion: "+proportion+", n: "+n+" (Total: "+ aMerges.size()+")");
            int origSize = parentATable.merges.size();
            for(int i = origSize-1; i> origSize-n; i--){

                aMerges.remove(i);
            }
            for(int i = 0; i< n; i++){

                aMerges.add(new StatePair(bMerges.get(i).getFirstState(),bMerges.get(i).getSecondState()));
            }
            count = count + 1;
            offSpring.add(parentATable);

        }
    }





}
