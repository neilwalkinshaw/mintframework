package mint.inference.efsm;

import org.apache.log4j.Logger;
import mint.Configuration;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.inference.efsm.scoring.Scorer;
import mint.model.Machine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.statepair.OrderedStatePair;
import mint.model.statepair.OrderedStatePairWithScore;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Callable;

/*
 * Descendents of the AbstractMerging class orchestrate the entire state-merging process.
 * They define what it means to merge a pair of states, the infer() function provides
 * the basic iterative state merging process, and they contain a link to the merging state.
 */

public abstract class  AbstractMerger<U extends Machine, T extends SimpleMergingState<U>> implements Callable<U> {
	
	final static Logger LOGGER = Logger.getLogger(AbstractMerger.class.getName());

	protected T state;
	protected int failedDeterminisations;
	protected Scorer<T> scorer;
	protected double progress;
	protected int initSize;
	protected int maxPhase = 1;

    /*firstStates is a set of "root" states belonging to ongoing merges which *must* be retained. I.e., if they are
    merged into another state, the whole merge sequence will be invalidated.
     */
    //protected Set<Integer> firstStates = new HashSet<Integer>();



    public U call(){
        return infer();
    }

	public AbstractMerger(Scorer<T> scorer, T state){
        failedDeterminisations = 0;
		progress = 0D;
		this.scorer = scorer;
		this.state = state;
        //firstStates.add(state.getCurrent().getInitialState());
	}

	public void setScorer(Scorer s){
		this.scorer = s;
	}
	
	public SimpleMergingState<U> getState() {
		return state;
	}

	public void setMaxPhase(int phase){
		this.maxPhase = phase;
	}

	protected void calculateProgress() {
		double processed =  (initSize - state.getCurrent().getStates().size());
		double newProgress = ((double)processed/initSize)*100;
		if(newProgress-progress>=0.0001D){
			DecimalFormat df = new DecimalFormat("#.##");
	        LOGGER.debug(df.format(newProgress)+"% ("+state.getCurrent().getStates().size()+" total, "+state.getConfirmedSuccessfulPairs()+" successful merges, "+failedDeterminisations+" failed merges"+"\r");
	        progress = newProgress;
		}
		
	}

	

	protected boolean containsAtLeastOne(Set<DefaultEdge> outgoing, String pred) {
		TraceDFA dfa = state.getCurrent().getAutomaton();
		for(DefaultEdge t : outgoing){
			TransitionData<Set<TraceElement>> tData = dfa.getTransitionData(t);
			if(tData.getLabel().equals(pred))
				return true;
		}
		return false;
	}


	public U infer() {
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
			//if(maxPhase > 1) {
			//	if (!state.getCurrent().getStates().contains(currentPair.getFirstState()) ||
			//			!state.getCurrent().getStates().contains(currentPair.getSecondState()))
			//		continue;
			//}

			//MAY NEED TO TRANSFORM THIS
			OrderedStatePair equivPair = state.getMergedEquivalent(currentPair);
			assert(state.getCurrent().getStates().contains(currentPair.getFirstState()) &&
					state.getCurrent().getStates().contains(currentPair.getSecondState()));
			pairsProcessed++;

            boolean merged = merge(equivPair);

			if(merged){
				if(configuration.STRATEGY != Configuration.Strategy.gktails)
					assert(state.getCurrent().isDeterministic());
				state.addConfirmedSuccessfulPair(currentPair);
				state.setMerged(equivPair.getSecondState());
				state.confirmTemps();
			}
			else{
                failedDeterminisations++;
				//ORIGINAL
				state.addConfirmedFailedPair(currentPair);
				state.getCurrent().setAutomaton(cloned);
				state.clearTemps();
			}
            //clearFirstStates();
			state.postProcess();
			calculateProgress();
			assert(state.getCurrent().getAutomaton().consistentStates());
			assert(state.getCurrent().getAutomaton().consistentTransitions());
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
		U result = state.getCurrent();
		assert(state.getCurrent().isDeterministic());
		result.postProcess();
		LOGGER.debug("Finished inferring model");
		return result;
	}
	
	protected LinkedList<OrderedStatePairWithScore> calculatePossibleMerges(Collection<Integer> from){
		//LOGGER.debug("Re-calculating possible merges.");
		return scorer.possibleMerges(state,from);
	}
	
	protected abstract boolean merge(OrderedStatePair p);



    protected abstract boolean consistent(OrderedStatePair p);


}