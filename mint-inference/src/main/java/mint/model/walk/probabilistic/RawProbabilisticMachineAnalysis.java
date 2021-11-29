package mint.model.walk.probabilistic;

import mint.model.dfa.TraceDFA;
import mint.model.Machine;
import mint.model.RawProbabilisticMachine;
import mint.model.matrix.ProbabilityMatrix;
import mint.model.walk.SimpleMachineAnalysis;
import mint.model.walk.WalkResult;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.types.VariableAssignment;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by neilwalkinshaw on 27/04/2016.
 */
public class RawProbabilisticMachineAnalysis extends SimpleMachineAnalysis<Machine<?>> {

    protected ProbabilityMatrix matrix;


    public RawProbabilisticMachineAnalysis(RawProbabilisticMachine m) {
        super(m);
        matrix = new ProbabilityMatrix(m);
    }



    public List<Double> getNGramDistribution(List<List<String>> ngrams){
        ArrayList<Double> dist = new ArrayList<Double>();
        dist.ensureCapacity(ngrams.size());
        for(int i = 0; i<ngrams.size(); i++){
            List<String> ngram = ngrams.get(i);
            dist.add(i,count(ngram));
        }
        return dist;
    }

    public double getProbabilityOfWalk(List<TraceElement> walk){
        WalkResult wr = walk(walk);
        if(wr.getWalk() == null)
            return 0D;
        if(wr.isAccept(machine.getAutomaton())== TraceDFA.Accept.REJECT)
            return 0D;
        if(wr.getWalk().size()<walk.size())
            return 0D;
        Double probability = 1D;
        for(DefaultEdge step : wr.getWalk()){
            probability = probability * machine.getProbability(step);
        }
        return probability;
    }

    private Double count(List<String> ngram) {
        Collection<WalkResult> results = getStateSequences(ngram);
        double count = 0;
        for(WalkResult result : results){
            if(result.getWalk() != null){
                count+=matrix.getCount(result);
            }
        }
        return count;
    }

    private Collection<WalkResult> getStateSequences(List<String> ngram) {
        List<TraceElement> teNgram = new ArrayList<TraceElement>();
        for(String s : ngram){
            TraceElement te = new SimpleTraceElement(s,new VariableAssignment[]{});
            teNgram.add(te);
        }
        Collection<WalkResult> results = new HashSet<WalkResult>();
        for(Integer state : machine.getStates()){
            results.add(walk(teNgram,state,new ArrayList<DefaultEdge>(),machine.getAutomaton()));
        }
        return results;
    }



}
