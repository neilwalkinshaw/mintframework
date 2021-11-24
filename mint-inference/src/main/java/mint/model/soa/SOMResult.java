package mint.model.soa;

import citcom.subjectiveLogic.SubjectiveOpinion;
import mint.model.dfa.TraceDFA;

public class SOMResult extends SOResult{

    double  probability, uncertainty, belief;
    int states,transitions;
    TraceDFA.Accept predictedAccept, accept;

    public SOMResult(double belief, double uncertainty, TraceDFA.Accept predictedAccept, double probability, TraceDFA.Accept accept, int states, int transitions) {
        super(null,predictedAccept,probability,accept,states,transitions);
        this.belief = belief;
        this.uncertainty = uncertainty;
        this.accept = accept;
        this.predictedAccept = predictedAccept;
        this.probability = probability;
        this.states = states;
        this.transitions=transitions;
    }

    public SubjectiveOpinion getSubjectiveOpinion() {
        return so;
    }


    public TraceDFA.Accept getAccept() {
        return accept;
    }

    public int getStates() {
        return states;
    }

    public int getTransitions() {
        return transitions;
    }

    public String toString(){
        return belief+","+uncertainty +","+probability+","+predictedAccept+","+accept+","+states+","+transitions;
    }
}