package mint.evaluation;

import mint.tracedata.TraceElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A class that will, given a set of pairs of events that are known to be impossible in sequence,
 * take a set of traces and return traces that are mutated to be guaranteed to be impossible / invalid.
 *
 * Created by neilwalkinshaw on 25/08/2014.
 */
public abstract class AbstractNegativePairMutator {

    protected Collection<List<TraceElement>> negs,pos;
    protected int max = 100;

    public AbstractNegativePairMutator(Collection<List<TraceElement>> traces){
        negs = new ArrayList<List<TraceElement>>();
        pos = traces;
    }

    public void setNumberOfNegs(int num){
        max = num;
    }

    public Collection<List<TraceElement>> getNegatives(){
        return negs;
    }

    /**
     * Synthesise a set of negative traces from the set of positive traces.
     */
    protected abstract void buildNegs();

    /**
     * Utility method to post-process a synthesised trace, to make sure that each
     * trace element is pointing to the next element in the trace.

     * @param pref
     * @param newTrace
     * @param last
     */
    protected void processTrace(List<TraceElement> pref,
                                List<TraceElement> newTrace, TraceElement last) {
        for(TraceElement te : pref){
            TraceElement newTe = te.copy();
            if(last!=null){
                last.setNext(newTe);
            }
            last = newTe;
            newTrace.add(newTe);
        }
    }

}
