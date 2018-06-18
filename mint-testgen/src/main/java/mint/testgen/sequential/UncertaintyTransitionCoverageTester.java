package mint.testgen.sequential;

import org.apache.log4j.Logger;
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
import mint.tracedata.types.VariableAssignment;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

/**
 * For a set of tests, each test will be mapped to a value of "uncertainty". This is computed
 * by, first of all, summing out the payloads for all outgoing transitions from a state. For a single
 * transition, the uncertainty is calculated by dividing its payload by the total payload. For a test (a sequence
 * of transitions), the final uncertainty is calculated in the Markovian way, by multiplying them together.
 *
 * Created by neilwalkinshaw on 15/09/2017.
 */
public class UncertaintyTransitionCoverageTester extends TransitionCoverageSMTester {

    protected Map<List<TraceElement>,Double> uncertainty;
    private final static Logger LOGGER = Logger.getLogger(UncertaintyTransitionCoverageTester.class.getName());

    @Override
    public List<List<TraceElement>> generateTests(int t, Machine m) {
        uncertainty = new HashMap<List<TraceElement>, Double>();
        List<List<TraceElement>> tests = new ArrayList<List<TraceElement>>();
        Collection<GraphPath<Integer,DefaultEdge>> paths = m.getAutomaton().allPaths();
        PrefixTreeFactory<SimpleMachine> ptF = new FSMPrefixTreeFactory(new PayloadMachine());
        Machine prefixTree = ptF.createPrefixTree(traces);
        SimpleMachineAnalysis analysis = new SimpleMachineAnalysis(prefixTree);

        Map<Integer,Double> stateTotals = new HashMap<Integer, Double>();
        Collection<Integer> states = m.getStates();
        for(Integer s : states){
            Collection<DefaultEdge> out = m.getAutomaton().getOutgoingTransitions(s);
            double counter = 0;
            for(DefaultEdge o : out){
                TransitionData td = m.getAutomaton().getTransitionData(o);
                Collection<TraceElement> payload = (Collection<TraceElement>) td.getPayLoad();
                counter+= payload.size();
            }
            stateTotals.put(s,counter);
        }

        for(GraphPath<Integer,DefaultEdge> path : paths){
            List<TraceElement> test = new ArrayList<TraceElement>();
            double unct = 1D;
            for(DefaultEdge de : path.getEdgeList()){

                //Compute uncertainty so far
                int edgeSource = m.getAutomaton().getTransitionSource(de);
                double stateTotal = stateTotals.get(edgeSource);
                TransitionData td = m.getAutomaton().getTransitionData(de);
                Collection<TraceElement> payload = (Collection<TraceElement>) td.getPayLoad();
                double edgeTotal = (double) payload.size();
                double edgeUncertainty = edgeTotal/stateTotal;
                unct = edgeUncertainty * unct;

                String label = m.getAutomaton().getTransitionData(de).getLabel();
                TraceElement el = new SimpleTraceElement(label, new ArrayList<VariableAssignment<?>>());
                test.add(el);
                if(test.size()>0){
                    test.get(test.size()-1).setNext(el);
                }
            }
            uncertainty.put(test,unct);
            TraceDFA.Accept inTree = analysis.walkAccept(test, true, prefixTree.getAutomaton());
            if (inTree.equals(TraceDFA.Accept.UNDEFINED))
                tests.add(test);
        }

        order(tests, analysis, prefixTree.getAutomaton());

        return tests.subList(0,t);
    }


    protected void order(List<List<TraceElement>> tests, SimpleMachineAnalysis analysis, TraceDFA automaton) {
        Collections.sort(tests,new UncertaintyComparator());
    }

    public class UncertaintyComparator implements java.util.Comparator<List> {


        public UncertaintyComparator() {
            super();
        }

        public int compare(List s1, List s2) {
            double s1Uncertainty = uncertainty.get(s1);
            double s2Uncertainty = uncertainty.get(s2);
            if(s2Uncertainty > s1Uncertainty)
                return -1;
            else if(s1Uncertainty> s2Uncertainty)
                return 1;
            else
                return 0;
        }
    }


}
