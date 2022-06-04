package mint.model.walk.probabilistic;

import mint.model.LatentProbabilitiesMachine;
import mint.model.dfa.TraceDFA;
import mint.model.walk.SimpleMachineAnalysis;
import mint.model.walk.WalkResult;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.List;

public class LatentDependenciesProbabilisticMachineAnalysis extends SimpleMachineAnalysis<LatentProbabilitiesMachine> {

    public LatentDependenciesProbabilisticMachineAnalysis(LatentProbabilitiesMachine m) {
        super(m);
    }

    public double walkProbability(List<TraceElement> in){
        double result = 1D;
        WalkResult walk = walk(in);
        if(walk.getWalk().size()<in.size() || walk.isAccept().equals(TraceDFA.Accept.REJECT))
            return 0D;
        for(DefaultEdge de : walk.getWalk()){
            result = result * machine.getProbability(de);
        }
        return result;
    }


    public double walkConditionalProbability(List<TraceElement> in){
        double result = 1D;
        WalkResult walk = walk(in);
        if(walk.getWalk().size()<in.size() || walk.isAccept().equals(TraceDFA.Accept.REJECT))
            return 0D;
        DefaultEdge de;
        for(int i = 0; i<walk.getWalk().size(); i++){
            de = walk.getWalk().get(i);

            double multiplier = machine.getProbability(de);

            for(int j = 0; j<i;j++){
                Double latentEffect = machine.aGivenB(de,walk.getWalk().get(j));
                if(latentEffect>0) {
                        multiplier = latentEffect;
                }
            }
            multiplier = Math.min(1D,multiplier);
            multiplier = Math.max(0D,multiplier);
            result = result * multiplier;

        }
        return result;
    }


}
