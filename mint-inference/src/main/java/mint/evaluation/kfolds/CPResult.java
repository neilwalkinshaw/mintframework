package mint.evaluation.kfolds;

import mint.Configuration;
import mint.model.dfa.TraceDFA;


public class CPResult extends Result{

    protected int fold, prefixLimit, numTraces;
    protected double pred_p, pred_cp, act_p, act_cp, act_cp_ref;
    protected TraceDFA.Accept predAccept, actAccept;


    public CPResult(String name, int numTraces, int seed, int fold, int prefixLimit, double pred_p, double pred_cp, double act_p, double act_cpref, double act_Cp, TraceDFA.Accept predAccept, TraceDFA.Accept actAccept) {
        super(name, "", numTraces, 0D, 0D, 0D, 0L, seed, 0, false, 0, 0, Configuration.Strategy.exhaustive);
        this.numTraces = numTraces;
        this.fold = fold;
        this.pred_p = pred_p;
        this.pred_cp = pred_cp;
        this.act_p = act_p;
        this.act_cp = act_Cp;
        this.act_cp_ref = act_cpref;
        this.predAccept = predAccept;
        this.actAccept = actAccept;
        this.prefixLimit = prefixLimit;
    }

    public String toString(){
        String ret = name + ","+numTraces+","+seed+","+fold+","+prefixLimit+","+pred_p+","+pred_cp+","+act_p+","+act_cp_ref+","+act_cp+","+predAccept+","+actAccept+"\n";
        return ret;
    }

}
