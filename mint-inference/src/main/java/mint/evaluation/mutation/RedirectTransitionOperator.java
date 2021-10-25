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

public class RedirectTransitionOperator extends MutationOperator {

    Integer to;
    String label;


    public RedirectTransitionOperator(Machine target, Random random) {
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
        TraceDFA cloned = target.getAutomaton().clone();
        newMachine.setAutomaton(cloned);
        DefaultEdge originalEdge = (DefaultEdge)target.getAutomaton().getOutgoingTransitions(sourceState,label).toArray()[0];
        if(originalEdge!=null){
            Integer origTo = target.getAutomaton().getTransitionTarget(originalEdge);
            ArrayList<Integer> possibilities = new ArrayList<>();
            possibilities.addAll(target.getStates());
            possibilities.remove(origTo);
            to = (Integer)pickRandom(possibilities);

            DefaultEdge toRemove = (DefaultEdge)newMachine.getAutomaton().getOutgoingTransitions(sourceState,label).toArray()[0];


            TransitionData td = newMachine.getAutomaton().getTransitionData(toRemove);

            newMachine.getAutomaton().removeTransition(toRemove);
            newMachine.getAutomaton().addTransition(sourceState,to,td);
        }
        else{
            throw new Exception("Can't find transition to redirect");
        }
        return newMachine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedirectTransitionOperator that = (RedirectTransitionOperator) o;
        return sourceState.equals(that.sourceState) &&
                to.equals(that.to) &&
                label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceState, to, label);
    }
}
