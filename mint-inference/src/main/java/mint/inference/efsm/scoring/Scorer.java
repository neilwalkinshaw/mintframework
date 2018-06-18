package mint.inference.efsm.scoring;

import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.model.statepair.OrderedStatePair;
import mint.model.statepair.OrderedStatePairWithScore;

import java.util.Collection;
import java.util.LinkedList;

public interface Scorer <T extends SimpleMergingState<?>> {
	

    LinkedList<OrderedStatePairWithScore> possibleMerges(T rms, Collection<Integer> from);


    boolean compatible(T rms, OrderedStatePair pair);


	
}
