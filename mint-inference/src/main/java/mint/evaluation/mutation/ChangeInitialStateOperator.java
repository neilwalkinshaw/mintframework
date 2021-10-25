package mint.evaluation.mutation;

import mint.model.Machine;
import mint.model.PayloadMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ChangeInitialStateOperator extends MutationOperator {

    Integer to;

    public ChangeInitialStateOperator(Machine target, Random random) {
        super(target, random);
        List<Integer> allStates = new ArrayList<>();
        allStates.addAll(target.getStates());
        allStates.remove(target.getInitialState());

        sourceState = target.getInitialState();
        to = pickRandomState();

    }

    @Override
    public Machine apply() throws Exception {
        Machine newMachine = new PayloadMachine();
        newMachine.setAutomaton(target.getAutomaton().clone());
        newMachine.getAutomaton().setInitialState(to);
        return newMachine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeInitialStateOperator that = (ChangeInitialStateOperator) o;
        return to.equals(that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(to);
    }
}
