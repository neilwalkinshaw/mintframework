package mint.model.walk.probabilistic;

import mint.model.Machine;
import mint.model.walk.WalkResult;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.List;

public class StateInsensitiveProbabilisticMachineAnalysis extends ParameterisableProbabilisticMachineAnalysis<StateEventPair,List<String>>{



    public StateInsensitiveProbabilisticMachineAnalysis(Machine m, int prefixLimit, TraceSet traces) {
        super(m, prefixLimit, traces);
    }

    protected double totalEvents() {
        double total = 0;
        for(List l : traces.getPos()){
            total= total +l.size();
        }
        return total;
    }


    @Override
    protected StateEventPair createA(Integer state, String name) {
        return new StateEventPair(state,name);
    }

    @Override
    protected List<String> createB(List prefix, Integer state) {
        List<String> p = new ArrayList<>();
        p.addAll(prefix);
        return p;
    }
}
