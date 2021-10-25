package mint.testgen.sequential;

import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.dfa.reader.DotReader;
import mint.tracedata.TraceElement;
import mint.visualise.dot.DotGraphWithLabels;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

import java.nio.file.FileSystems;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class WMethodSMTesterTest {


    @Test
    public void wSet() {
        BasicConfigurator.configure();

        DotReader reader = new DotReader(FileSystems.getDefault().getPath("/Users/neil/Google Drive/Research/Software/SubjectSystems/DynamicLstarM/mealyInference/Benchmark/SSH/OpenSSH.dot.fixed"),"2");
        PayloadMachine model = (PayloadMachine)reader.getImported();
        //System.out.println(DotGraphWithLabels.summaryDotGraph(model));
        WMethodSMTester tester = new WMethodSMTester();
        Collection<List<TraceElement>> tests = tester.generateTests(model);
        assert(tests.size()>0);
    }

    @Test
    public void characterisationSet(){
        Machine m = new PayloadMachine();
        TraceDFA dfa = new TraceDFA();
        Integer a = dfa.getInitialState();
        Integer b = dfa.addState();
        TransitionData aLab = new TransitionData("a", null);
        TransitionData bLab = new TransitionData("b",null);
        dfa.addTransition(a,b,aLab);
        dfa.addTransition(b,a,bLab);
        dfa.setInitialState(a);
        dfa.setAccept(a, TraceDFA.Accept.ACCEPT);
        dfa.setAccept(b, TraceDFA.Accept.ACCEPT);
        m.setAutomaton(dfa);
        WMethodSMTester tester = new WMethodSMTester();
        tester.m = m;
        Collection<List<String>> cset = tester.characterisationSet();
        assert(cset.size()>0);
    }

}