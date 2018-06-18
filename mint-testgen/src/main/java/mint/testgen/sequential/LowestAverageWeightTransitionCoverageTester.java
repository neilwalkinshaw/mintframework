package mint.testgen.sequential;

import org.apache.log4j.Logger;
import mint.model.Machine;
import mint.model.dfa.TraceDFA;
import mint.model.walk.SimpleMachineAnalysis;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;

import java.util.*;

/**
 * For a set of tests, each test will be mapped to a value of "uncertainty". This is computed
 * by simply calculating the average payload over a test case.
 *
 * Created by neilwalkinshaw on 15/09/2017.
 */
public class LowestAverageWeightTransitionCoverageTester extends TransitionCoverageSMTester {

    private final static Logger LOGGER = Logger.getLogger(LowestAverageWeightTransitionCoverageTester.class.getName());

    protected TraceSet globalTests;
    boolean random = false;

    public LowestAverageWeightTransitionCoverageTester(TraceSet globalTests) {
        this.globalTests = globalTests;
    }

    public void setRandom(boolean rand){
        this.random = rand;
    }

    @Override
    public List<List<TraceElement>> generateTests(int t, Machine m) {
        List<List<TraceElement>> tests = new ArrayList<List<TraceElement>>();
        if(m != null && !random){
            SimpleMachineAnalysis analysis = new SimpleMachineAnalysis(m);

            //PrefixTreeFactory<SimpleMachine> ptF = new FSMPrefixTreeFactory(new PayloadMachine());
            //Machine prefixTree = ptF.createPrefixTree(traces);
            //SimpleMachineAnalysis prefAnalysis = new SimpleMachineAnalysis(prefixTree);

            for(List<TraceElement> glob : globalTests.getPos()){
                if(!analysis.walkAccept(glob,false,m.getAutomaton()).equals(TraceDFA.Accept.REJECT))
                    tests.add(glob);
            }

            if(tests.isEmpty()){
                tests.addAll(globalTests.getPos());
            }
            //order(tests, prefAnalysis,m.getAutomaton());
        }
        else{
            tests.addAll(globalTests.getPos());

        }
        Collections.shuffle(tests);
        if(tests.size()>=t)
            return tests.subList(0,t);
        else
            return tests;
    }


    //protected void order(List<List<TraceElement>> tests, SimpleMachineAnalysis analysis, TraceDFA automaton) {
    //    Collections.sort(tests,new UncertaintyComparator(analysis, automaton));
    //}

    public class UncertaintyComparator implements Comparator<List> {

        protected SimpleMachineAnalysis prefixTree;
        protected Set<String> alphabet;
        protected TraceDFA automaton;

        public UncertaintyComparator(SimpleMachineAnalysis prefixTree, TraceDFA automaton) {
            super();
            this.prefixTree = prefixTree;
            this.alphabet = automaton.getAlphabet();
            this.automaton = automaton;
        }

        public int compare(List s1, List s2) {
            Set alphas1 = new HashSet();
            Set alphas2 = new HashSet();
            alphas1.addAll(s1);
            alphas2.addAll(s2);
            alphas1.removeAll(alphabet);
            alphas2.removeAll(alphabet);
            if(alphas1.size() > alphas2.size())
                return -1;
            else if (alphas1.size() < alphas2.size())
                return 1;
            else {
                int lengthInPrefixS1 = newStuff(s1);
                int lengthInPrefixS2 = newStuff(s2);
                if (lengthInPrefixS1 > lengthInPrefixS2)
                    return -1;
                else if (lengthInPrefixS1 < lengthInPrefixS2)
                    return 1;
                else
                    return 0;
            }

        }

        private int newStuff(List s1) {
            List<TraceElement> elements = (List<TraceElement>) s1;
            prefixTree.walk(elements,true,automaton);
            return s1.size() - prefixTree.getNumberTransitionsCovered();
        }
    }


}
