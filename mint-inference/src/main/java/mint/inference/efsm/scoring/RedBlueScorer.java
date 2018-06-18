package mint.inference.efsm.scoring;

import org.apache.log4j.Logger;
import mint.inference.efsm.mergingstate.RedBlueMergingState;
import mint.inference.efsm.scoring.scoreComputation.ComputeScore;
import mint.inference.efsm.scoring.scoreComputation.Score;
import mint.model.statepair.OrderedStatePairWithScore;

import java.util.*;
import java.util.concurrent.*;

/**
 * This implements the "Blue Fringe" process of selecting pairs of potential
 * merge candidates. It was Price's winning strategy for the ABBADINGO competition,
 * published by Lange et al. in 1998.
 * @param <T>
 */

public class RedBlueScorer<T extends RedBlueMergingState<?>> extends BasicScorer<T,ComputeScore> {

    final static Logger LOGGER = Logger.getLogger(RedBlueScorer.class.getName());


    public RedBlueScorer(int min_score, ComputeScore scorer) {
		super(min_score, scorer);
	}

    @Override
	public LinkedList<OrderedStatePairWithScore> possibleMerges(T rms, Collection<Integer> fromColl) {
        LinkedList<OrderedStatePairWithScore> possibleMerges = new LinkedList<OrderedStatePairWithScore>();

        Set<Integer> interimBlues = rms.getBlues();
        if(interimBlues.isEmpty()) {
            assert(rms.getReds().size() == rms.getCurrent().getStates().size());
            return possibleMerges;
        }
        while(possibleMerges.isEmpty()){
            Iterator<Integer> blueIt = interimBlues.iterator();

            List<OrderedStatePairWithScore> pairScores = new ArrayList<OrderedStatePairWithScore>();
            List<Callable<Score>> toRun = new ArrayList<Callable<Score>>();
            //For each blue state...
            while(blueIt.hasNext()){

                Integer blue = blueIt.next();
                HashSet<Integer> red = new HashSet<Integer>();
                red.addAll(rms.getReds());
                Iterator<Integer> redIt =  red.iterator();
                int addedMerges = 0;
                // ... pair it up with each red state...
                while(redIt.hasNext()){
                    Integer r = redIt.next();
                    OrderedStatePairWithScore pair = new OrderedStatePairWithScore(r,blue);
                    if(rms.alreadyAttempted(pair) || !compatible(rms,pair))
                        continue;
                    // if not already attempted, prepare it for score computation.
                    Callable<Score> c = scorer.newInstance(rms, pair);
                    pairScores.add(pair);
                    toRun.add(c);

                }
                ExecutorService pool = Executors.newFixedThreadPool(threads);
                //Run all of the score computations for the candidate pairs...
                try {
                    List<Future<Score>> results = pool.invokeAll(toRun);
                    for(int i = 0; i<results.size();i++){
                        Future<Score> result = results.get(i);
                        Score score = result.get();
                        OrderedStatePairWithScore pair = pairScores.get(i);
                        // and if the score is greater than the threshold, add it as a possible merge.
                        if (score.getPrimaryScore() >= getMinScore()) {
                            pair.setScore(score);
                            possibleMerges.add(pair);
                            addedMerges++;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                finally{
                    pool.shutdownNow();
                }

                if(addedMerges == 0){
                    //If a blue state has no possible red states to merge to, mark it to be consolidated to a red state.
                    rms.addToConsolidate(blue);
                }
            }
            //If no merge candidates were found for any of the blue states
            if(possibleMerges.isEmpty()){
                //consolidate all of the marked states into red states.
                rms.postProcess();
                //... and calculate the new blue states.
                interimBlues = rms.getBlues();
                //If there are no blue states, we are done.
                if(interimBlues.isEmpty()){
                    assert(rms.getReds().size() == rms.getCurrent().getStates().size());
                    return possibleMerges;
                }
            }
        }
        Collections.sort(possibleMerges);
		return possibleMerges;
	
	}


}
