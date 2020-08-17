package mint.model.soa;

import mint.model.dfa.TraceDFA;

public class SOResult{
    SubjectiveOpinion so;
    double predictedProbability;
    int states,transitions;
    TraceDFA.Accept accept;

    public SOResult(SubjectiveOpinion so, double predictedProbability, TraceDFA.Accept accept, int states, int transitions) {
        this.so = so;
        this.predictedProbability = predictedProbability;
        this.accept = accept;
        this.states = states;
        this.transitions=transitions;
    }

    public SubjectiveOpinion getSubjectiveOpinion() {
        return so;
    }

    public double getPredictedProbability() {
        return predictedProbability;
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
}