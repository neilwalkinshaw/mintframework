package mint.model.soa;

import citcom.subjectiveLogic.SubjectiveOpinion;
import mint.model.dfa.TraceDFA;

public class SOResult{
    SubjectiveOpinion so;
    double  probability;
    int states,transitions;
    TraceDFA.Accept predictedAccept, accept;

    public SOResult(SubjectiveOpinion so, TraceDFA.Accept predictedAccept, double probability, TraceDFA.Accept accept, int states, int transitions) {
        this.so = so;
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
        return so +","+probability+","+predictedAccept+","+accept+","+states+","+transitions;
    }
}