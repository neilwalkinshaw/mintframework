package mint.inference.efsm.scoring;

import mint.Configuration;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.inference.efsm.scoring.scoreComputation.ComputeScore;
import mint.inference.efsm.scoring.scoreComputation.Score;
import mint.model.statepair.OrderedStatePair;
import mint.model.statepair.OrderedStatePairWithScore;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.concurrent.*;

/*
 * Class will compute the score indicating the similarity between a pair of states
 * by the usual EDSM scoring principle of counting the number of overlapping 
 * transitions in their suffixes.
 */

public class BasicScorer< T extends SimpleMergingState<?>, U extends ComputeScore>  implements Scorer<T> {


    protected int min_score;
    protected int threads = Runtime.getRuntime().availableProcessors();
    protected U scorer;
    protected int maxBacklog = 1000;
    protected boolean bestFirst = false; // more time consuming

	public BasicScorer( int min_score, U scorer){
		this.min_score = min_score;
        this.scorer = scorer;

	}

    public void setBestFirst(boolean bestFirst){
        this.bestFirst = bestFirst;
    }

    public void setThreads(int threads){
        this.threads = threads;
    }




	public int getScore(T rms,OrderedStatePair sp) {

        ComputeScore<T> scoreComputer = scorer.newInstance(rms,sp);
        Score score = new Score(0);
        try {
            score = scoreComputer.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
		return score.getPrimaryScore();
	}


    @Override
    public boolean compatible(T rms, OrderedStatePair pair) {
        if(!Configuration.getInstance().CAREFUL_DETERMINIZATION)
            return getScore(rms,pair)>=0;
        else
            return getScore(rms,pair)>=Configuration.getInstance().K;

    }

    @Override
    public LinkedList<OrderedStatePairWithScore> possibleMerges(T rms, Collection<Integer> fromColl) {
        LinkedList<OrderedStatePairWithScore> possibleMerges = new LinkedList<OrderedStatePairWithScore>();
        List<Integer> from = getBFSStates(rms);
        List<OrderedStatePairWithScore> pairScores = new ArrayList<OrderedStatePairWithScore>();
        List<Callable<Score>> toRun = new ArrayList<Callable<Score>>();
        for(Integer f : from) {
            for(Integer t : from){
                if(f == t)
                    continue;
                OrderedStatePairWithScore pair = new OrderedStatePairWithScore(f,t);
                if(!rms.allowed(pair) || !compatible(rms,pair))
                    continue;
                else {
                    Callable<Score> c = scorer.newInstance(rms, pair);
                    pairScores.add(pair);
                    toRun.add(c);
                }

            }
            if(toRun.size()>= this.maxBacklog) {
                dispense(possibleMerges, pairScores, toRun,rms);
                toRun.clear();
                pairScores.clear();
            }
        }
        if(toRun.size()>0) {
            dispense(possibleMerges, pairScores, toRun,rms);
            toRun.clear();
            pairScores.clear();
        }
        if(bestFirst)
            Collections.sort(possibleMerges);
        return possibleMerges;
    }

    protected List<Integer> getBFSStates(T rms){
        List<Integer> from = new ArrayList<Integer>();
        bfsStates(rms, from);
        return from;
    }

    /**
     * Populate from with a bfs traversal of states, starting from the initial state.
     * @param rms
     * @param from
     */
    private void bfsStates(T rms, List<Integer> from) {
        Queue<Integer> todo = new LinkedList<Integer>();
        Integer initialState= rms.getCurrent().getInitialState();
        todo.add(initialState);
        from.add(initialState);
        while(!todo.isEmpty()){
            Integer current = todo.poll();
            for(DefaultEdge outgoing : (Set<DefaultEdge>)rms.getCurrent().getAutomaton().getOutgoingTransitions(current)) {
                Integer target = rms.getCurrent().getAutomaton().getTransitionTarget(outgoing);
                if(!from.contains(target)) {
                    from.add(target);
                    todo.add(target);
                }
            }

        }
    }

    /**
     * Calculate the scores for the backlog in toRun.
     * @param possibleMerges
     * @param pairScores
     * @param toRun
     * @param rms
     */
    protected void dispense(Queue<OrderedStatePairWithScore> possibleMerges, List<OrderedStatePairWithScore> pairScores, List<Callable<Score>> toRun, T rms) {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Future<Score>> results = pool.invokeAll(toRun);
            for(int i = 0; i<results.size();i++){
                Future<Score> result = results.get(i);
                Score score = result.get();
                OrderedStatePairWithScore pair = pairScores.get(i);
                if (score.getPrimaryScore() >= getMinScore()) {
                    pair.setScore(score);
                    possibleMerges.add(pair);
                }

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        pool.shutdown();
    }

    public int getMinScore() {
		return min_score;
	}


}
