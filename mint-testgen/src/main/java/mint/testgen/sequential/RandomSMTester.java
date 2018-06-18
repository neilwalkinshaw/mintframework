package mint.testgen.sequential;

import org.apache.log4j.Logger;
import mint.Configuration;
import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.SimpleMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.prefixtree.FSMPrefixTreeFactory;
import mint.model.prefixtree.PrefixTreeFactory;
import mint.model.walk.SimpleMachineAnalysis;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.types.VariableAssignment;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by neilwalkinshaw on 11/07/2017.
 */
public class RandomSMTester implements TestGenerator {

    private final static Logger LOGGER = Logger.getLogger(RandomSMTester.class.getName());

    protected TraceSet traces;

    public RandomSMTester(TraceSet tr){
        this.traces = tr;
    }

    public List<List<TraceElement>> generateTests(Machine m){
        return generateTests(400,m);
    }

    public List<List<TraceElement>> generateTests(int t, Machine m) {
        Random r = new Random(Configuration.getInstance().SEED);
        //LOGGER.debug(DotGraphWithLabels.summaryDotGraph(m));
        List<List<TraceElement>> tests = new ArrayList<List<TraceElement>>();
        List<DefaultEdge> initial = new LinkedList<DefaultEdge>();
        initial.addAll(m.getAutomaton().getOutgoingTransitions(m.getInitialState()));
        //Map<DefaultEdge,DefaultEdge> parent = new HashMap<DefaultEdge,DefaultEdge>();
        //HashSet<DefaultEdge> done = new HashSet<DefaultEdge>();
        PrefixTreeFactory<SimpleMachine> ptF = new FSMPrefixTreeFactory(new PayloadMachine());
        Machine prefixTree = ptF.createPrefixTree(traces);
        TraceSet toAvoid = new TraceSet();
        for(List<TraceElement> done : traces.getPos()){
            toAvoid.addPos(done);
        }
        for(List<TraceElement> done : traces.getNeg()){
            toAvoid.addNeg(done);
        }
        SimpleMachineAnalysis analysis = new SimpleMachineAnalysis(prefixTree);
        while((tests.size()<t)){
            List<DefaultEdge> test = new ArrayList<DefaultEdge>();
            test.add(initial.get(r.nextInt(initial.size())));
            List<TraceElement> testSequence = getTraceElements(m, test);
            while(!novel(analysis,prefixTree,testSequence)){
                List<DefaultEdge> transitions = new ArrayList<DefaultEdge>();
                transitions.addAll(m.getAutomaton().getTransitions());
                test.add(transitions.get(r.nextInt(transitions.size())));
                testSequence = getTraceElements(m, test);

            }
            toAvoid.addPos(testSequence);
            tests.add(testSequence);
            prefixTree = ptF.createPrefixTree(toAvoid);
        }
        return tests;
    }

    private List<TraceElement> getTraceElements(Machine m, List<DefaultEdge> test) {
        List<TraceElement> testSequence = new ArrayList<TraceElement>();
        for(int i = 0; i<test.size(); i++){
            TransitionData td = m.getAutomaton().getTransitionData(test.get(i));
            if(td == null)
                return null;
            String label = td.getLabel();
            TraceElement el = new SimpleTraceElement(label, new ArrayList<VariableAssignment<?>>());
            testSequence.add(el);
            if(i>0){
                testSequence.get(i-1).setNext(el);
            }
        }
        return testSequence;
    }

    private boolean novel(SimpleMachineAnalysis analysis, Machine prefixTree, List<TraceElement> test) {
        TraceDFA.Accept inTree = analysis.walkAccept(test,true,prefixTree.getAutomaton());
        if(inTree.equals(TraceDFA.Accept.UNDEFINED))
            return true;
        else
            return false;
    }


}
