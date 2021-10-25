package mint.evaluation;

import mint.evaluation.mutation.MutationOperator;
import mint.evaluation.mutation.StateMachineMutator;
import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.dfa.reader.DotReader;
import mint.testgen.sequential.WMethodSMTester;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import org.junit.Test;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class SLAPFDEvaluatorTest {

    @Test
    public void mutationTestScore(){
        String referenceMachine = "src/tests/resources/OpenSSH.dot.fixed2";

        DotReader dr = new DotReader(FileSystems.getDefault().getPath(referenceMachine),"0");
        PayloadMachine dfa = (PayloadMachine)dr.getImported();

        StateMachineMutator smm = new StateMachineMutator(dfa);
        List<Machine> mutated = new ArrayList<>();
        for(MutationOperator mo : smm.generateMutated(1000)){
            Machine toAdd = null;
            try {
                toAdd = mo.applyMutation();
            } catch (MutationOperator.NonDeterministicException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(toAdd!=null)
                mutated.add(toAdd);
        }

        SLAPFDEvaluator.mutated = mutated;
        SLAPFDEvaluator.dfa = dfa;

        //dfa.getAutomaton().completeWithRejects();

        WMethodSMTester tester = new WMethodSMTester();
        tester.setK(1);
        List<List<TraceElement>> baseTests = tester.generateTests(dfa);
        Collections.shuffle(baseTests);


        List<Double> apfd1 = SLAPFDEvaluator.mutationScore(baseTests);
        Collections.shuffle(baseTests);
        List<Double> apfd2 = SLAPFDEvaluator.mutationScore(baseTests);

        System.out.println(apfd1.get(apfd1.size()-1) - apfd2.get(apfd2.size()-1));


        assertTrue(apfd1.get(apfd1.size()-1) - apfd2.get(apfd2.size()-1) == 0D);

    }

}