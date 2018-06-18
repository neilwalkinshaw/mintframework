package mint.model.matrix;

import mint.model.PayloadMachine;
import mint.model.SimpleMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.tracedata.TraceElement;
import org.junit.Before;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by neilwalkinshaw on 16/05/2016.
 */
public class ProbabilityMatrixTest {

    ProbabilityMatrix pbm;

    @Before
    public void buildTestMachine(){
        SimpleMachine sm = new PayloadMachine();
        TraceDFA dfa = new TraceDFA();
        Integer stateA = dfa.getInitialState();
        Integer stateB = dfa.addState();
        Integer stateC = dfa.addState();
        dfa.setAccept(stateA, TraceDFA.Accept.REJECT);
        dfa.setAccept(stateB, TraceDFA.Accept.REJECT);
        dfa.setAccept(stateC, TraceDFA.Accept.REJECT);
        dfa.setInitialState(0);
        dfa.addTransition(0,1,generateTransitionData("a",2));
        dfa.addTransition(0,2,generateTransitionData("b",4));
        dfa.addTransition(1,2,generateTransitionData("c",2));
        dfa.addTransition(2,1,generateTransitionData("a",2));
        dfa.addTransition(2,0,generateTransitionData("d",2));

        sm.setAutomaton(dfa);
        //pbm = new ProbabilityMatrix(sm);
    }



    private TransitionData generateTransitionData(String label, int num) {
        Set payload = new HashSet();
        for(int j = 0; j<num; j++) {
            payload.add(new Integer(j));
        }
        TransitionData<Set<TraceElement>> data = new TransitionData<Set<TraceElement>>(label,payload);
        return data;
    }

}
