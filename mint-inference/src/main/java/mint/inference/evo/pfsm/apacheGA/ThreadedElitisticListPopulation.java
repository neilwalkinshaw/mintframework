package mint.inference.evo.pfsm.apacheGA;

import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by neilwalkinshaw on 27/05/2016.
 */
public class ThreadedElitisticListPopulation extends ElitisticListPopulation{


    double fittestVal = -Double.MAX_VALUE;

    private final static Logger LOGGER = Logger.getLogger(ThreadedElitisticListPopulation.class.getName());


    public ThreadedElitisticListPopulation(int populationSize, double v) {
        super(populationSize,v);
    }

    public Chromosome getFittestChromosome() {
        double bestScore = -Double.MAX_VALUE;
        Map<Future, AGPMergingTable> solMap = new HashMap<Future, AGPMergingTable>();
        Chromosome best = null;

        Set<Future<Double>> set = new HashSet<Future<Double>>();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            for (Chromosome node : this.getChromosomeList()) {
                AGPMergingTable chrom = (AGPMergingTable)node;
                Future<Double> future = pool.submit(chrom);
                solMap.put(future, chrom);
                set.add(future);
            }
            for (Future<Double> sol : set) {
                double score ;
                try {
                    score = sol.get(60000000, TimeUnit.MILLISECONDS);
                }
                catch(Exception ex){
                    ex.printStackTrace();
                    AGPMergingTable chrom = (AGPMergingTable)solMap.get(sol);
                    chrom.done = true;
                    sol.cancel(true);

                    score = -100000D;
                }
                if(score > bestScore){
                    bestScore = score;
                    best = solMap.get(sol);
                }
                if(score > fittestVal){
                    fittestVal = score;
                }
                else if (best == null)
                    best = solMap.get(sol);
                double done = 0;

                for(Future f : set){
                    if(f.isCancelled() || f.isDone())
                        done++;
                    //LOGGER.debug(done/set.size() +" complete for calculating fittest chromosome");
                }
                AGPMergingTable t = solMap.get(sol);
                assert(t.done);
                sol.cancel(true);
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            pool.shutdownNow();
        }
        return best;
    }

    public Population nextGeneration() {
        ThreadedElitisticListPopulation nextGeneration = new ThreadedElitisticListPopulation(this.getPopulationLimit(), this.getElitismRate());
        List<Chromosome> oldChromosomes = this.getChromosomeList();
        Set<Future<Double>> set = new HashSet<Future<Double>>();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        Map<Future,AGPMergingTable> tab = new HashMap<Future,AGPMergingTable>();
        System.out.println("s");

        try {
            for (Chromosome node : oldChromosomes) {
                AGPMergingTable chrom = (AGPMergingTable)node;
                Future<Double> future = pool.submit(chrom);
                set.add(future);
                tab.put(future,chrom);
            }
            for (Future<Double> sol : set) {
                double score = 0D;
                try {
                    score = sol.get(60000000, TimeUnit.MILLISECONDS);
                    System.out.print(".");

                }
                catch(Exception ex){
                    ex.printStackTrace();
                    AGPMergingTable t = tab.get(sol);
                    t.done = true;
                    System.out.print("x");
                    sol.cancel(true);

                    score = -100000D;

                }
                AGPMergingTable t = tab.get(sol);
                assert(t.done);
                if(score > nextGeneration.fittestVal){
                    nextGeneration.fittestVal = score;
                }

                sol.cancel(true);
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            pool.shutdownNow();
        }
        System.out.println("d");

        Collections.sort(oldChromosomes, new ChromosomeComparator());

        int boundIndex = (int) FastMath.ceil((1.0D - this.getElitismRate()) * (double) oldChromosomes.size());

        for(int i = 0; i < (oldChromosomes.size()-boundIndex); ++i) {
            nextGeneration.addChromosome((Chromosome)oldChromosomes.get(i));
        }

        return nextGeneration;
    }

    public double getFittest(){
        return fittestVal;
    }

}
