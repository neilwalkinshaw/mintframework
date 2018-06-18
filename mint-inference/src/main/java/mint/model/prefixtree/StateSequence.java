package mint.model.prefixtree;

import mint.tracedata.TraceElement;

import java.util.List;

/**
 * Simple data class to store state-sequence pairs.
 * Intended to communicate between PrefixTreeFactory and SuffixStringsMonitor.
 */
public class StateSequence {

    private Integer state;
    private List<TraceElement> sequence;

    public StateSequence(Integer state, List<TraceElement> sequence){
        this.state = state;
        this.sequence = sequence;
    }

    public Integer getState(){
        return state;
    }

    public List<TraceElement> getSequence(){
        return sequence;
    }
}
