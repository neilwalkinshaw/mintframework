package mint.evaluation.mutation;

import mint.Configuration;
import mint.model.Machine;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

public class StateMachineMutator {

    protected Machine toMutate;
    protected Random rand;

    public StateMachineMutator(Machine toMutate){
        rand = new Random(Configuration.getInstance().SEED);
        this.toMutate = toMutate;
    }

    public Collection<MutationOperator> generateMutated(int number){
        Collection<MutationOperator> mutated = new HashSet<>();
        for(int successfulMutations = 0; successfulMutations<number; ) {
            int operator = rand.nextInt(5);
            MutationOperator op = null;
            try {
                switch (operator) {
                    case 0:
                        op = new AddTransitionOperator(toMutate, rand);
                        break;
                    case 1:
                        op = new ChangeInitialStateOperator(toMutate, rand);
                        break;
                    case 2:
                        op = new RedirectTransitionOperator(toMutate, rand);
                        break;
                    case 3:
                        op = new RelabelTransitionOperator(toMutate, rand);
                        break;
                    case 4:
                        op = new RemoveTransitionOperator(toMutate, rand);
                        break;
                }
                op.applyMutation();
                mutated.add(op);
                successfulMutations++;
            } catch (Exception | MutationOperator.NonDeterministicException e) {
            }
        }
        return mutated;
    }
}
