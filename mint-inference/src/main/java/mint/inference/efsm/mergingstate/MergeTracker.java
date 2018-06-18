package mint.inference.efsm.mergingstate;

import mint.model.statepair.OrderedStatePair;

import java.util.HashMap;

/**
 * Created by neilwalkinshaw on 23/02/2017.
 */
public class MergeTracker {

    // map from original states to destination states.
    protected HashMap<Integer, Integer> merges;

    public MergeTracker(){
        merges = new HashMap<Integer,Integer>();

    }

    public void registerMerge(OrderedStatePair sp){
        merges.put(sp.getSecondState(),sp.getFirstState());
        for(Integer f : merges.keySet()){
            if(merges.get(f).equals(sp.getSecondState()))
                merges.put(f,sp.getFirstState());
        }
    }

    public Integer getMergedTo(Integer state){
        Integer to = merges.get(state);
        if(to == null)
            to = state;
        return to;
    }

    public void clear(){
        merges.clear();
    }

    public OrderedStatePair getMergedEquivalent(OrderedStatePair sp){
        OrderedStatePair osp = new OrderedStatePair(getMergedTo(sp.getFirstState()),getMergedTo(sp.getSecondState()));
        return osp;
    }

}
