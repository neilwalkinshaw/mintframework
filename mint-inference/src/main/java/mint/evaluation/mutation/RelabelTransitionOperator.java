package mint.evaluation.mutation;

import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RelabelTransitionOperator extends MutationOperator {

    Integer to;
    String label;


    public RelabelTransitionOperator(Machine target, Random random) {
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
        DefaultEdge originalEdge = (DefaultEdge)newMachine.getAutomaton().getOutgoingTransitions(sourceState,label).toArray()[0];
        Integer destination = newMachine.getAutomaton().getTransitionTarget(originalEdge);
        String lab = pickRandom();
        TransitionData newLabel = new TransitionData(lab,lab);

        DefaultEdge oldEdge = (DefaultEdge)target.getAutomaton().getOutgoingTransitions(sourceState,label).toArray()[0];

        if(!lab.equals(target.getLabel(oldEdge))){
            newMachine.getAutomaton().removeTransition(originalEdge);
            newMachine.getAutomaton().addTransition(sourceState,destination, newLabel);
        }
        else{
            throw new Exception("Inappropriate relabelling");
        }
        return newMachine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelabelTransitionOperator that = (RelabelTransitionOperator) o;
        return sourceState.equals(that.sourceState) &&
                to.equals(that.to) &&
                label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceState, to, label);
    }
}
