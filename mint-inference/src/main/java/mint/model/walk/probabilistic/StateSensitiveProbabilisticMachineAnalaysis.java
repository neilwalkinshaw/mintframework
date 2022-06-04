package mint.model.walk.probabilistic;

import mint.model.Machine;
import mint.model.dfa.TransitionData;
import mint.model.walk.WalkResult;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.List;

public class StateSensitiveProbabilisticMachineAnalaysis extends ParameterisableProbabilisticMachineAnalysis<StateEventPair,ListAndElement<Integer>>{

    /**
     * Here, P(A) denotes the probability of some event occurring *at a given state*, and P(B) represents the sequence of events leading to that state.
     * @param m
     * @param prefixLimit
     * @param traces
     */


    public StateSensitiveProbabilisticMachineAnalaysis(Machine m, int prefixLimit, TraceSet traces) {
        super(m, prefixLimit, traces);
    }

    /**
     * What do we count as the denominator for computing the probability
     * of some occurrence A?
     *
     * @return
     */
    @Override
    protected double totalA(StateEventPair sep) {
        double total = 0D;
        for(List<TraceElement> sequence : traces.getPos()){
            /*WalkResult wr = walk(sequence);
            for(DefaultEdge de : wr.getWalk()){
                Integer sourceState = machine.getAutomaton().getTransitionSource(de);
                if(sourceState == sep.getState()){
                   String label = machine.getLabel(de);
                   if(label.equals(sep.getEvent()))
                       total++;
                }
            }*/
            total+=sequence.size();
        }
        return total;
    }

    /**
     * What do we count as the denominator for computing the probability
     * of some occurrence B?
     *
     * @return
     */
    @Override
    protected double totalB(ListAndElement<Integer> le) {
        double total = 0D;
        for(List<TraceElement> sequence : traces.getPos()){
            /*WalkResult wr = walk(sequence);
            DefaultEdge latestEdge = null;
            List<String> prefix = new ArrayList<>();
            for(DefaultEdge de : wr.getWalk()){
                latestEdge = de;
                Integer sourceState = machine.getAutomaton().getTransitionSource(de);
                if(sourceState == le.getElement()){
                    if(prefix.size()>=le.getList().size()){
                        List context = prefix.subList(prefix.size()-le.getList().size(),prefix.size());
                        if(context.containsAll(le.getList()))
                            total++;
                    }
                }
                prefix.add(machine.getLabel(de));
            }
            //because the above loop only looks at source-stakes, it will leave a "dangling" state that we haven't
            //checked ... which we do with the below condition.
            if(machine.getAutomaton().getTransitionTarget(latestEdge) == le.getElement())
                if(prefix.size()>=le.getList().size()){
                    List context = prefix.subList(prefix.size()-le.getList().size(),prefix.size());
                    if(context.containsAll(le.getList()))
                        total++;
                }*/
            total+=sequence.size();
        }
        return total;
    }

    @Override
    protected StateEventPair createA(Integer state, String name) {
        return new StateEventPair(state,name);
    }

    @Override
    protected ListAndElement<Integer> createB(List prefix, Integer state) {
        return new ListAndElement(prefix,state);
    }


}
