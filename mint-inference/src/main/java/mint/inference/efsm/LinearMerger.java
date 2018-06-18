package mint.inference.efsm;

import org.apache.log4j.Logger;
import mint.Configuration;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.inference.efsm.scoring.Scorer;
import mint.model.Machine;
import mint.model.dfa.TraceDFA;
import mint.model.statepair.OrderedStatePair;
import mint.model.statepair.OrderedStatePairWithScore;
import org.jgrapht.graph.DefaultEdge;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by neilwalkinshaw on 23/02/2017.
 */
public class LinearMerger <S extends Machine, T extends SimpleMergingState<S>> extends EDSMMerger<S,T> {

    public LinearMerger(Scorer<T> scorer, T state) {
        super(scorer, state);
    }

    final static Logger LOGGER = Logger.getLogger(LinearMerger.class.getName());


    public S infer() {
        LOGGER.debug("0% (" + state.getCurrent().getStates().size() + " total, " + state.getConfirmedSuccessfulPairs() + " successful merges, " + failedDeterminisations + " failed merges" + "\r");
        LinkedList<OrderedStatePairWithScore> possibleMerges = calculatePossibleMerges(state.getCurrent().getStates());
        assert(state.getCurrent().getAutomaton().consistentStates());
        assert(state.getCurrent().getAutomaton().consistentTransitions());
        //System.out.println(DotGraphWithLabels.summaryDotGraph(state.getCurrent()));
        Configuration configuration = Configuration.getInstance();
        failedDeterminisations = 0;
        int pairsProcessed =0;
        while(!possibleMerges.isEmpty()){
            if(Thread.currentThread().isInterrupted()){
                return null;
            }
            TraceDFA cloned = state.getCurrent().getAutomaton().clone();
            OrderedStatePair currentPair = possibleMerges.poll();

            //LOGGER.debug("Merging pair "+currentPair);
            if(configuration.STRATEGY != Configuration.Strategy.gktails)
                assert(state.getCurrent().isDeterministic());

            /**
             * If multiple merges might have occurred in a phase, must make sure that states
             * in current pair haven't been merged away.
             */

            OrderedStatePair equivPair = state.getMergedEquivalent(currentPair);
            boolean merged = false;
            pairsProcessed++;
            if(scorer.compatible(state,equivPair)) {

                assert (state.getCurrent().getStates().contains(equivPair.getFirstState()) &&
                        state.getCurrent().getStates().contains(equivPair.getSecondState()));


                merged = merge(equivPair);

                if (merged) {
                    if (configuration.STRATEGY != Configuration.Strategy.gktails)
                        assert (state.getCurrent().isDeterministic());
                    state.addConfirmedSuccessfulPair(currentPair);
                    state.setMerged(equivPair.getSecondState());
                    state.confirmTemps();
                } else {
                    failedDeterminisations++;
                    state.addConfirmedFailedPair(currentPair);
                    state.getCurrent().setAutomaton(cloned);
                    state.clearTemps();
                }
                //clearFirstStates();
                state.postProcess();
                calculateProgress();
                assert (state.getCurrent().getAutomaton().consistentStates());
                assert (state.getCurrent().getAutomaton().consistentTransitions());
            }
            else{
                state.addConfirmedFailedPair(currentPair);
                calculateProgress();
            }
            if(pairsProcessed >= maxPhase){
                if(merged || possibleMerges.isEmpty()) {
                    pairsProcessed = 0;
                    possibleMerges = calculatePossibleMerges(state.getCurrent().getAutomaton().getStates());
                }
            }
            else if(possibleMerges.isEmpty()) {
                pairsProcessed = 0;
                possibleMerges = calculatePossibleMerges(state.getCurrent().getAutomaton().getStates());
            }


        }
        S result = state.getCurrent();
        assert(state.getCurrent().isDeterministic());
        result.postProcess();
        LOGGER.debug("Finished inferring model");
        return result;
    }


    @Override
    protected boolean consistent(OrderedStatePair p) {
        if(!super.consistent(p))
            return false;
        Iterator<DefaultEdge> outgoing = state.getCurrent().getAutomaton().getOutgoingTransitions(p.getFirstState()).iterator();
        while(outgoing.hasNext()){
            DefaultEdge edge = outgoing.next();
            Integer target = state.getCurrent().getAutomaton().getTransitionTarget(edge);
            if(target.equals(p.getFirstState()))
                return false;
        }
        return true;
    }

    protected LinkedList<OrderedStatePairWithScore> calculatePossibleMerges(Collection<Integer> from){
        LOGGER.debug("Re-calculating possible merges.");
        return scorer.possibleMerges(state,from);
    }
}
