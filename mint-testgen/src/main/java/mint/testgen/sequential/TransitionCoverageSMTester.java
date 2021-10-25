package mint.testgen.sequential;

import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.SimpleMachine;
import mint.model.dfa.TraceDFA;
import mint.model.prefixtree.FSMPrefixTreeFactory;
import mint.model.prefixtree.PrefixTreeFactory;
import mint.model.walk.SimpleMachineAnalysis;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.types.VariableAssignment;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by neilwalkinshaw on 11/07/2017.
 */
public class TransitionCoverageSMTester implements TestGenerator {


    protected TraceSet traces;


    public void setTraces(TraceSet traces){
        this.traces = traces;
    }


    public List<List<TraceElement>> generateTests(Machine m){
        List<List<TraceElement>> tests = new ArrayList<List<TraceElement>>();
        transitionCover(m, tests);
        return tests;
    }

    public List<List<TraceElement>> generateTests(int t, Machine m) {
        List<List<TraceElement>> tests = generateTests(m);
        return tests.subList(0,t);
    }

    protected void transitionCover(Machine m, List<List<TraceElement>> tests) {
        Collection<GraphPath<Integer, DefaultEdge>> paths = m.getAutomaton().allPaths();
        PrefixTreeFactory<SimpleMachine> ptF = new FSMPrefixTreeFactory(new PayloadMachine());
        Machine prefixTree = ptF.createPrefixTree(traces);
        SimpleMachineAnalysis analysis = new SimpleMachineAnalysis(prefixTree);
        for(GraphPath<Integer,DefaultEdge> path : paths){
            List<TraceElement> test = new ArrayList<TraceElement>();
            for(DefaultEdge de : path.getEdgeList()){
                String label = m.getAutomaton().getTransitionData(de).getLabel();
                TraceElement el = new SimpleTraceElement(label, new ArrayList<VariableAssignment<?>>());
                test.add(el);
                if(test.size()>0){
                    test.get(test.size()-1).setNext(el);
                }
            }
            TraceDFA.Accept inTree = analysis.walkAccept(test, true, prefixTree.getAutomaton());
            if (inTree.equals(TraceDFA.Accept.UNDEFINED))
                tests.add(test);
        }

        order(tests, analysis, prefixTree.getAutomaton());
    }

    protected void order(List<List<TraceElement>> tests, SimpleMachineAnalysis analysis, TraceDFA automaton) {
        Collections.shuffle(tests);
    }





}


