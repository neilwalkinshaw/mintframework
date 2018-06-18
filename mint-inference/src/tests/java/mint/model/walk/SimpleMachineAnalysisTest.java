package mint.model.walk;

import mint.Configuration;
import mint.inference.InferenceBuilder;
import mint.model.Machine;
import mint.model.dfa.TraceDFA;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.types.VariableAssignment;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neilwalkinshaw on 23/09/2017.
 */
public class SimpleMachineAnalysisTest {

    SimpleMachineAnalysis analysis;
    Machine inferred;

    @Before
    public void setUp() throws Exception {
        List<TraceElement> negSequence = new ArrayList<TraceElement>();
        TraceElement a = new SimpleTraceElement("a",new VariableAssignment[]{});
        negSequence.add(a);
        TraceSet ts = new TraceSet();
        ts.addNeg(negSequence);
        Configuration conf = Configuration.getInstance();
        conf.PREFIX_CLOSED = true;
        conf.K = 0;
        conf.DATA = false;
        conf.STRATEGY = Configuration.Strategy.redblue;
        InferenceBuilder ib = new InferenceBuilder(conf);
        inferred = ib.getInference(ts).infer();
        analysis = new SimpleMachineAnalysis(inferred);
    }

    @Test
    public void walkAcceptUndefinedInputs() throws Exception {
        List<TraceElement> testSequence = new ArrayList<TraceElement>();
        TraceElement b = new SimpleTraceElement("b",new VariableAssignment[]{});
        testSequence.add(b);
        assert(analysis.walkAccept(testSequence,true,inferred.getAutomaton()) == TraceDFA.Accept.UNDEFINED);
    }

    @Test
    public void walkRejectWithNegativePrefix() throws Exception {
        List<TraceElement> testSequence = new ArrayList<TraceElement>();
        TraceElement a = new SimpleTraceElement("a",new VariableAssignment[]{});
        TraceElement b = new SimpleTraceElement("b",new VariableAssignment[]{});
        testSequence.add(a); //should lead to reject state, after which all sequencs should be rejected.
        testSequence.add(b);
        assert(analysis.walkAccept(testSequence,true,inferred.getAutomaton()) == TraceDFA.Accept.REJECT);
    }

}