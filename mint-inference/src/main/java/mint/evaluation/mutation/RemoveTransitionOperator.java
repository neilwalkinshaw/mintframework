package mint.evaluation.mutation;

import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.dfa.TraceDFA;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

public class RemoveTransitionOperator extends MutationOperator {

    String label;


    public RemoveTransitionOperator(Machine target, Random random) {
        super(target, random);
        sourceState = pickRandomState();
        label = (String)pickRandom(outgoingLabels(sourceState));

    }

    protected List<String> outgoingLabels(Integer state){
        ArrayList<String> labels = new ArrayList<String>();
        TraceDFA<String> dfa = target.getAutomaton();
        for(DefaultEdge outgoing : dfa.getOutgoingTransitions(sourceState)){
            labels.add(target.getLabel(outgoing));
        }
        return labels;
    }

    @Override
    public Machine apply() throws Exception {
        Machine newMachine = new PayloadMachine();
        newMachine.setAutomaton(target.getAutomaton().clone());
        DefaultEdge toRemove = (DefaultEdge)newMachine.getAutomaton().getOutgoingTransitions(sourceState,label).toArray()[0];
        if(toRemove == null)
            throw new Exception("Can't find transition to remove");
        newMachine.getAutomaton().removeTransition(toRemove);
        return newMachine;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemoveTransitionOperator that = (RemoveTransitionOperator) o;
        return sourceState.equals(that.sourceState) &&
                label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceState, label);
    }
}
