package mint.inference.efsm.mergingstate;

import mint.model.statepair.OrderedStatePair;
import mint.model.statepair.StatePair;

public interface MergingState {

	public void addConfirmedFailedPair(StatePair p);
	
	public void addTempSuccessfulPair(StatePair p);
	
	public void addConfirmedSuccessfulPair(StatePair p);
	
	public void confirmTemps();
	
	public void clearTemps();
	
	public void postProcess();

	public void registerMerge(OrderedStatePair sp);

	public OrderedStatePair getMergedEquivalent(OrderedStatePair sp);

}
