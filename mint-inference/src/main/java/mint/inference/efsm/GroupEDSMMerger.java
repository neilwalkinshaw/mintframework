package mint.inference.efsm;

import mint.Configuration;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.inference.efsm.scoring.LinearScorer;
import mint.inference.efsm.scoring.Scorer;
import mint.model.Machine;
import mint.model.dfa.TraceDFA;
import mint.model.statepair.OrderedStatePairWithScore;
import mint.model.statepair.StatePair;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by neilwalkinshaw on 13/02/15.
 */
public class GroupEDSMMerger<S extends Machine, T extends SimpleMergingState<S>> extends EDSMMerger<S,T> {

    public GroupEDSMMerger(Scorer<T> scorer, T state) {
        super(scorer, state);
    }

    public S infer() {
        LOGGER.debug("0% (" + state.getCurrent().getStates().size() + " total, " + state.getConfirmedSuccessfulPairs() + " successful merges, " + failedDeterminisations + " failed merges" + "\r");
        LinkedList<OrderedStatePairWithScore> possibleMerges = calculatePossibleMerges(state.getCurrent().getStates());
        assert(state.getCurrent().getAutomaton().consistentStates());
        assert(state.getCurrent().getAutomaton().consistentTransitions());
        //System.out.println(DotGraphWithLabels.summaryDotGraph(state.getCurrent()));
        Configuration configuration = Configuration.getInstance();
        failedDeterminisations = 0;
        if(Thread.currentThread().isInterrupted()){
            return null;
        }

        Iterator<OrderedStatePairWithScore> possibleMergesIt = possibleMerges.descendingIterator();
        Collection<StatePair> completed = new HashSet<StatePair>();
        int failedMerges = 0;
        while(possibleMergesIt.hasNext()) {

            OrderedStatePairWithScore currentPair = possibleMergesIt.next();

            if(!state.getCurrent().getAutomaton().containsState(currentPair.getFirstState()))
                continue;
            if(!state.getCurrent().getAutomaton().containsState(currentPair.getSecondState()))
                continue;
            if(!scorer.compatible(state, currentPair))
                continue;
            boolean merged = false;

            //only merge all mutually independent pairs at each iteration.

            if(independentFrom(currentPair,completed))
                merged = doMerge(configuration, currentPair);
            if(merged){
                completed.add(currentPair);
            }
            else{
                failedMerges++;
                if(failedMerges > 1000) {
                    failedMerges = 0;
                    completed.clear();
                    possibleMerges = calculatePossibleMerges(state.getCurrent().getStates());
                    possibleMergesIt = possibleMerges.descendingIterator();
                    continue;
                }

            }
            if(!possibleMergesIt.hasNext()){
                completed.clear();
                possibleMerges = calculatePossibleMerges(state.getCurrent().getStates());
                possibleMergesIt = possibleMerges.descendingIterator();
            }
        }

        S result = state.getCurrent();
        assert(state.getCurrent().isDeterministic());
        result.postProcess();
        LOGGER.debug("Finished inferring model");
        return result;
    }

    private boolean independentFrom(OrderedStatePairWithScore currentPair, Collection<StatePair> completed) {
        if(completed.isEmpty())
            return true;
        boolean retValue = true;
        Iterator<StatePair> pairIt = completed.iterator();
        while(pairIt.hasNext()){
            StatePair current = pairIt.next();
            Integer mergedState = current.getFirstState();
            if(!state.getCurrent().getStates().contains(mergedState))
                mergedState = current.getSecondState();
            if(LinearScorer.reachableOrCanReach(state.getCurrent().getAutomaton(),mergedState,currentPair.getFirstState()))
                return false;
            if(LinearScorer.reachableOrCanReach(state.getCurrent().getAutomaton(),mergedState,currentPair.getSecondState()))
                return false;
        }
        return retValue;
    }

    private boolean doMerge(Configuration configuration, OrderedStatePairWithScore currentPair) {
        assert(state.getCurrent().getStates().contains(currentPair.getFirstState()));
        LOGGER.debug("Merging pair "+currentPair);
        TraceDFA cloned = state.getCurrent().getAutomaton().clone();
        if(configuration.STRATEGY != Configuration.Strategy.gktails)
            assert(state.getCurrent().isDeterministic());
        boolean merged = merge(currentPair);

        if(merged){
            if(configuration.STRATEGY != Configuration.Strategy.gktails)
                assert(state.getCurrent().isDeterministic());
            state.addConfirmedSuccessfulPair(currentPair);
            state.setMerged(currentPair.getSecondState());
            state.confirmTemps();
        }
        else{
            failedDeterminisations++;
            state.addConfirmedFailedPair(currentPair);
            state.getCurrent().setAutomaton(cloned);
            state.clearTemps();
        }
        state.postProcess();
        calculateProgress();
        assert(state.getCurrent().getAutomaton().consistentStates());
        assert(state.getCurrent().getAutomaton().consistentTransitions());
        return merged;
    }


}
