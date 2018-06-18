package mint.evaluation;

import org.apache.log4j.Logger;
import mint.tracedata.TraceElement;
import mint.tracedata.types.VariableAssignment;

import java.util.*;

/**
 * Takes in the constructor a set of positive traces, as well as a set of negative traces, where the final
 * element represents the proscribed event.
 *
 * Created by neilwalkinshaw on 26/08/2014.
 */
public class NegativePairDataMutator extends AbstractNegativePairMutator{

    private Collection<List<TraceElement>> negProps;
    private final static Logger LOGGER = Logger.getLogger(NegativePairDataMutator.class.getName());


    public NegativePairDataMutator(Collection<List<TraceElement>> negs, Collection<List<TraceElement>> from){
        super(from);
        this.negProps = negs;
    }

    @Override
    protected void buildNegs() {
        Iterator<List<TraceElement>> negIt = negProps.iterator();
        while(negIt.hasNext()){
            List<TraceElement> next = negIt.next();
            Collection<List<TraceElement>> prefixes = findPrefixes(next);
            if(prefixes.isEmpty()){
                LOGGER.warn("Found no prefixes.");
            }
            Collection<TraceElement> suffixes = findSuffixes(next.get(next.size()-1));
            if(suffixes.isEmpty()){
                LOGGER.warn("Found no suffixes.");
            }
            negs.addAll(synthesiseNegs(prefixes, suffixes,max / negProps.size()));
        }
    }

    /**
     * Iterate through positive traces and identify all traceelements that correspond to the element `next'.
     * @param next
     * @return
     */
    private Collection<TraceElement> findSuffixes(TraceElement next) {
        Collection<TraceElement> suffixes = new HashSet<TraceElement>();
        Iterator<List<TraceElement>> posIt = pos.iterator();
        while(posIt.hasNext()){
            List<TraceElement> pos = posIt.next();
            for(TraceElement te : pos){
                if(compatible(te,next))
                    suffixes.add(te);
            }
        }
        return suffixes;
    }

    private Collection<? extends List<TraceElement>> synthesiseNegs(Collection<List<TraceElement>> prefixes, Collection<TraceElement> suffixes, int limit) {
        Collection<List<TraceElement>> negs = new HashSet<List<TraceElement>>();
        for(List<TraceElement> prefix : prefixes){
            for(TraceElement te : suffixes) {
                List<TraceElement> suff = new ArrayList<TraceElement>();
                suff.add(te);
                List<TraceElement> newTrace = new ArrayList<TraceElement>();
                TraceElement last = null;
                processTrace(prefix, newTrace, last);
                processTrace(suff, newTrace, last);
                negs.add(newTrace);
                if(negs.size()>limit-1);
                    return negs;
            }
        }
        return negs;
    }

    private Collection<List<TraceElement>> findPrefixes(List<TraceElement> next) {
        Collection<List<TraceElement>> prefixes = new HashSet<List<TraceElement>>();
        Iterator<List<TraceElement>> posIt = pos.iterator();
        while(posIt.hasNext()){
            List<TraceElement> posTrace = posIt.next();
            Collection<Integer> matches = findMatches(posTrace,next);
            for(Integer index : matches){
                ArrayList<TraceElement> trace = new ArrayList<TraceElement>();
                for(int i = 0; i< index+1; i++){
                    trace.add(posTrace.get(i));
                }
                prefixes.add(trace);
            }
        }
        return prefixes;
    }

    /**
     * Return a list of indices in posTrace that contain matches for the first-but-one
     * elements in target.
     * @param posTrace
     * @param target
     * @return
     */
    protected Collection<Integer> findMatches(List<TraceElement> posTrace, List<TraceElement> target) {
        assert(target.size()>1); //There should at least be a criterion element and a match element.
        Map<Integer,Integer> candidates = new HashMap<Integer,Integer>();
        Collection results = new HashSet<Integer>();
        for(int i = 0; i<posTrace.size(); i++){
            TraceElement p = posTrace.get(i);
            //check existing candidates
            for(Integer candidate: candidates.keySet()){
                Integer targetIndex = candidates.get(candidate);
                TraceElement nextTargetPoint = target.get(targetIndex+1);
                if(compatible(p,nextTargetPoint)){
                    candidates.put(candidate,targetIndex+1);
                }
                else{
                    candidates.remove(candidate);
                }
            }

            //add start-points
            if(compatible(p,target.get(0))) {
                candidates.put(i, 0);
            }

            //move any complete candidates into results
            for(Integer candidate: candidates.keySet()){
                if(i-(target.size()-2)==candidate){
                    candidates.remove(candidate);
                    results.add(candidate);
                }
            }


        }
        return results;
    }



    /**
     * Returns true if p is compatible with q, where q can have wild-card variables.
     * @param p
     * @param q
     * @return
     */
    private boolean compatible(TraceElement p, TraceElement q) {
        if(!p.getName().equals(q.getName()))
            return false;
        else{
            Set<VariableAssignment<?>> pVars = p.getData();
            Iterator<VariableAssignment<?>> qVarIt = q.getData().iterator();
            while(qVarIt.hasNext()){
                VariableAssignment<?> current = qVarIt.next();
                if(current.isNull())
                    continue;
                VariableAssignment<?> pEquivalent = find(pVars,current);
                if(pEquivalent == null)
                    return false;
            }
        }
        return true;
    }

    /**
     * Find VariableAssignment in pvars that is equivalent to target.
     * @param pVars
     * @param target
     * @return
     */
    private VariableAssignment<?> find(Set<VariableAssignment<?>> pVars, VariableAssignment<?> target) {
        Iterator<VariableAssignment<?>> varIt = pVars.iterator();
        while(varIt.hasNext()){
            VariableAssignment<?> current = varIt.next();
            if(!current.getName().equals(target.getName()))
                continue;
            if(current.getValue().equals(target.getValue()))
                return current;
        }
        return null;
    }


}
