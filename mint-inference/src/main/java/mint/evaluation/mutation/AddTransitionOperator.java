package mint.evaluation.mutation;

import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.dfa.TransitionData;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class AddTransitionOperator extends MutationOperator {

    Integer to;
    String label;


    public AddTransitionOperator(Machine target, Random random) {
        super(target, random);
        sourceState = pickRandomState();
        to = pickRandomState();
        label = pickRandom();
    }

    @Override
    public Machine apply() throws NonDeterministicException {
        Machine newMachine = new PayloadMachine();
        newMachine.setAutomaton(target.getAutomaton().clone());
        TransitionData<String> td = new TransitionData<>(label,label);
        newMachine.getAutomaton().addTransition(sourceState,to,td);
        if(isNonDeterministic(newMachine))
            throw new NonDeterministicException();
        else
        return newMachine;
    }

    private boolean isNonDeterministic(Machine newMachine) {
        Set<DefaultEdge> outgoing = newMachine.getAutomaton().getOutgoingTransitions(sourceState);
        Set<String> labels = new HashSet<>();
        for(DefaultEdge e : outgoing){
            if(!labels.contains(newMachine.getLabel(e)))
                labels.add(newMachine.getLabel(e));
            else
                return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddTransitionOperator that = (AddTransitionOperator) o;
        return sourceState.equals(that.sourceState) &&
                to.equals(that.to) &&
                label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceState, to, label);
    }
}
