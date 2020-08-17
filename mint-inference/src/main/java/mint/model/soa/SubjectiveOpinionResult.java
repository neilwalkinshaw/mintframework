package mint.model.soa;

import mint.Configuration;
import mint.evaluation.kfolds.SimpleResult;
import mint.model.dfa.TraceDFA;

import java.util.ArrayList;
import java.util.List;


public class SubjectiveOpinionResult extends SimpleResult {

    List<SOResult> results;


    public SubjectiveOpinionResult(String name, String algo, int seed, int tail, boolean data,  Configuration.Strategy strategy) {
        super(name, algo, seed, tail, data, 0, 0, strategy, 0);
        results = new ArrayList<>();
    }

    public void addResult(SOResult result){
        results.add(result);
    }

    public String toString(){
        String ret = "";
        for(SOResult r : results){
            ret+= name+","+algo+","+seed+","+tail+","+data+","+strategy+","+r.getStates()+","+r.getTransitions()+","+r.getSubjectiveOpinion().getBelief()+","+
            r.getSubjectiveOpinion().getDisbelief()+","+r.getSubjectiveOpinion().getUncertainty()+","+
            r.accept+","+r.predictedProbability+"\n";
        }
        return ret;
    }

}
