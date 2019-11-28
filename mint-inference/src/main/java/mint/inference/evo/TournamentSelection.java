package mint.inference.evo;

import org.apache.log4j.Logger;
import mint.inference.gp.fitness.Fitness;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 *
 * Implements the Tournament Selection strategy for GP. Partition the population of individuals
 * into groups (of a given size). For each group, select the best ones.
 *
 * Created by neilwalkinshaw on 05/03/15.
 */

public abstract class TournamentSelection implements Selection{


    protected Map<Chromosome,Double> fitnessCache;
    protected Map<Chromosome,String> summaryCache;
    protected List<Chromosome> totalPopulation;
    protected List<Chromosome> elite;
    protected int eliteSize;
    protected double bestFitness;
    protected int maxDepth;


    private final static Logger LOGGER = Logger.getLogger(TournamentSelection.class.getName());


    public List<Chromosome> getElite(){
        return elite;
    }

    public TournamentSelection(List<Chromosome> totalPopulation, int maxDepth){
        this.summaryCache = new HashMap<Chromosome, String>();
        eliteSize = 10;
        //this.elite = new ArrayList<Chromosome>();
        this.totalPopulation = totalPopulation;
        this.bestFitness = Double.MAX_VALUE;
        this.maxDepth = maxDepth;
        this.fitnessCache = new HashMap<Chromosome,Double>();
    }

    public double getBestFitness(){
        return bestFitness;
    }



    public List<Chromosome> select(GPConfiguration config, int number){
        //fitnessCache.clear();
        List<List<Chromosome>> partitions = partition(config.getTournamentSize(), number);
        List<Chromosome> best =  bestIndividuals(partitions);
        bestScoresAndElites(best);
        return best;
    }

    public double computeFitness(Chromosome toEvaluate) throws InterruptedException {
        if(fitnessCache.containsKey(toEvaluate))
            return fitnessCache.get(toEvaluate);
        else
        {
            Fitness f = getFitness(toEvaluate);
            double fitness = f.call();
            fitnessCache.put(toEvaluate,fitness);
            summaryCache.put(toEvaluate,f.getFitnessSummary());
            return fitness;
        }
    }

    public abstract Fitness getFitness(Chromosome toEvaluate);

    protected List<List<Chromosome>> partition(int tournamentSize, int number){
        List<List<Chromosome>> best = new ArrayList<List<Chromosome>>();
        int counter = 0;
        while(best.size()<number) {

            Collections.shuffle(totalPopulation);
            List<Chromosome> pop = new ArrayList<Chromosome>();
            //if(counter < elite.size()){
              //  pop.add(elite.get(counter));
            //}
            for(int i = pop.size(); i<tournamentSize; i++) {
                pop.add(totalPopulation.get(i).copy());
            }
            best.add(pop);
            counter++;
        }
        return best;
    }

    protected List<Chromosome> bestIndividuals(List<List<Chromosome>> partitions){
        List<Chromosome> bestIndividuals = new ArrayList<Chromosome>();
        for(List<Chromosome> p : partitions){
            bestIndividuals.add(evaluatePopulation(p));
        }
        return bestIndividuals;
    }

    protected abstract Comparator<Chromosome> getComparator();

    protected void bestScoresAndElites(List<Chromosome> population){
        Collections.sort(population,getComparator());
        if(population.isEmpty())
            return;
        bestFitness = fitnessCache.get(population.get(0));
        elite = new ArrayList<>();
        for(int i = 0; (i<eliteSize && i<population.size()); i++){
            elite.add(population.get(i));
        }
        /*elite.clear();
        elite.add(population.get(0));
        for(int i =1; i< population.size() && elite.size()<maxElite; i++){
            if(!elite.contains(population.get(i))) {
                elite.add(population.get(i));
            }
        }*/
    }


    protected Chromosome evaluatePopulation(Collection<Chromosome> population) {

        assert (!population.isEmpty());
        double bestScore = Double.MAX_VALUE;
        Chromosome best = null;
        Map<Future, Chromosome> solMap = new HashMap<Future, Chromosome>();
        Set<Future<Double>> set = new HashSet<Future<Double>>();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        Fitness fitness = null;
        double totalScore = 0D;
        try {
            for (Chromosome node : population) {
                fitness = getFitness(node);
                Future<Double> future = pool.submit(fitness);
                solMap.put(future, node);
                set.add(future);
            }
            for (Future<Double> sol : set) {
                double score = 0D;
                try {
                    score = sol.get(5000000, TimeUnit.MILLISECONDS);
                    processResult(solMap, sol, score, fitness);
                }
                catch(Exception ex){
                    ex.printStackTrace();
                    score = 1000D;
                    processResult(solMap, sol, score, fitness);
                }
                if(score < bestScore){
                    bestScore = score;
                    best = solMap.get(sol);
                }
                else if (best == null)
                    best = solMap.get(sol);
                totalScore += score;
                sol.cancel(true);
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            pool.shutdownNow();
        }
        return best.copy();
    }

    protected void processResult(Map<Future, Chromosome> solMap, Future<Double> sol, double score, Fitness fitness) {
        fitnessCache.put(solMap.get(sol),score);
    }

    //public Collection<Chromosome> getElites(){
    //    return elite;
    //}



}
