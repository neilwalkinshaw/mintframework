package mint.testgen.sequential.gui.efg;

import mint.model.Machine;
import mint.testgen.sequential.TestGenerator;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.types.VariableAssignment;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;

import java.util.*;

/**
 * Created by neilwalkinshaw on 01/09/2017.
 */
public class EFGDFSTester implements TestGenerator{

    List<List<String>> tests = new ArrayList<List<String>>();
    List<List<String>> negTests = new ArrayList<List<String>>();


    public EFGDFSTester(DirectedPseudograph<String,DefaultEdge> efg, Set<String> initial, boolean addNeg){
        FloydWarshallShortestPaths adp = new FloydWarshallShortestPaths(efg);
        for(String init : initial) {
            ShortestPathAlgorithm.SingleSourcePaths<String,DefaultEdge> paths = adp.getPaths(init);
            for(String v : efg.vertexSet()){
                if(v.equals(init))
                    continue;
                GraphPath<String,DefaultEdge> path = paths.getPath(v);
                if(path!=null) {
                    tests.add(path.getVertexList());
                    if (addNeg) {
                        Collection<String> alphabet = new HashSet<String>();
                        alphabet.addAll(efg.vertexSet());
                        Collection<String> following = new HashSet<String>();
                        for (DefaultEdge outgoing : efg.outgoingEdgesOf(v)) {
                            following.add(efg.getEdgeTarget(outgoing));
                        }
                        alphabet.removeAll(following);
                        for (String neg : alphabet) {
                            List<String> negPath = new ArrayList<String>();
                            negPath.addAll(path.getVertexList());
                            negPath.add(neg);
                            negTests.add(negPath);
                        }
                    }
                }
            }
        }
    }

    public TraceSet getTests(boolean minimise){
        TraceSet traces = new TraceSet();
        Collection<List<String>> fullTests = new HashSet<List<String>>();
        fullTests.addAll(tests);
        if(minimise){
            minimise(fullTests);
        }
        for(List<String> test : fullTests){
            List<TraceElement> trace = listToTrace(test);
            traces.addPos(trace);
        }
        if(!negTests.isEmpty()){
            for(List<String> neg : negTests){
                List<TraceElement> trace = listToTrace(neg);
                traces.addNeg(trace);
            }
        }
        return traces;
    }

    private List<TraceElement> listToTrace(List<String> test) {
        List<TraceElement> trace = new ArrayList<TraceElement>();
        for(int i = 0; i<test.size(); i++){
            Collection<VariableAssignment<?>> vars = new HashSet<VariableAssignment<?>>();
            TraceElement te = new SimpleTraceElement(test.get(i),vars);
            trace.add(te);
            if(i>0){
                trace.get(i-1).setNext(te);
            }
        }
        return trace;
    }


    private void minimise(Collection<List<String>> testSet) {
        Set<String> done = new HashSet<String>();
        Set<List<String>> toRemove = new HashSet<List<String>>();
        for(List<String> test : testSet){
            if(done.containsAll(test))
                toRemove.add(test);
            else
                done.addAll(test);
        }
        testSet.removeAll(toRemove);

    }

    @Override
    public List<List<TraceElement>> generateTests(Machine m) {
        TraceSet testSet = getTests(false);
        List<List<TraceElement>> selected = new ArrayList<List<TraceElement>>();
        selected.addAll(testSet.getPos());
        Collections.shuffle(selected);
        return selected;
    }

    @Override
    public List<List<TraceElement>> generateTests(int t, Machine m) {
        return generateTests(m).subList(0,t);
    }




}
