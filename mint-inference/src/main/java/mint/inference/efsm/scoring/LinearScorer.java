package mint.inference.efsm.scoring;

import org.apache.log4j.Logger;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.inference.efsm.scoring.scoreComputation.LinearScoreComputer;
import mint.inference.efsm.scoring.scoreComputation.Score;
import mint.model.dfa.TraceDFA;
import mint.model.statepair.OrderedStatePair;
import mint.model.statepair.OrderedStatePairWithScore;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Only permit merges between states to happen if the resulting state machine
 * remains a DAG - i.e. there are no loops.
 *
 * Will only ensure non-looping inference if the merges are recomputed after every
 * merge (i.e. the mergePhase is 1).
 *
 * Created by neilwalkinshaw on 11/02/15.
 */

public class LinearScorer<T extends SimpleMergingState<?>> extends BasicScorer<T,LinearScoreComputer> {

    private final static Logger LOGGER = Logger.getLogger(LinearScorer.class.getName());


    public LinearScorer(int min_score, LinearScoreComputer scorer) {
        super(min_score, scorer);
    }

    @Override
    public boolean compatible(T rms, OrderedStatePair pair) {
        if(reachableOrCanReach(rms.getCurrent().getAutomaton(),pair.getFirstState(),pair.getSecondState())) {
            return false;
        }

        return true;
    }


    /**
     * Is firstState on the paths to or from secondState?
     * @param automaton
     * @param secondState
     * @param firstState
     * @return
     */
    public static boolean reachableOrCanReach(TraceDFA automaton, Integer secondState, Integer firstState) {
        Collection<Integer> states = automaton.nodesThatReach(secondState);
        states.addAll(automaton.nodesReachableFrom(secondState));

        return(states.contains(firstState));


    }

    @Override
    public LinkedList<OrderedStatePairWithScore> possibleMerges(T rms, Collection<Integer> from) {
        LinkedList<OrderedStatePairWithScore> possibleMerges = new LinkedList<OrderedStatePairWithScore>();
        Iterator<Integer> outerIt = from.iterator();
        List<OrderedStatePairWithScore> pairScores = new ArrayList<OrderedStatePairWithScore>();
        List<Callable<Score>> toRun = new ArrayList<Callable<Score>>();
        scorer.computeStateDepths(rms);
        while(outerIt.hasNext()){
            Integer a = outerIt.next();
            if(a.equals(rms.getCurrent().getInitialState()))
                continue;
            Collection<Integer> in = new HashSet<Integer>();
            in.addAll(from);
            in.removeAll(rms.getCurrent().getAutomaton().nodesReachableFrom(a));
            in.removeAll(rms.getCurrent().getAutomaton().nodesThatReach(a));
            Iterator<Integer> innerIt = in.iterator();
            while(innerIt.hasNext()){
                Integer b = innerIt.next();
                //StatePair current = new StatePair(a,b);

                if(b.equals(a))
                    continue;
                //if(scored.contains(current))
                  //  continue;
                OrderedStatePairWithScore pair = new OrderedStatePairWithScore(b, a);
                if (rms.alreadyAttempted(pair) || !compatible(rms,pair))
                    continue;
                Callable<Score> c = scorer.newInstance(rms, pair);
                pairScores.add(pair);
                //scored.add(current);
                toRun.add(c);

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
        Collections.sort(possibleMerges);
        return possibleMerges;
    }



}
