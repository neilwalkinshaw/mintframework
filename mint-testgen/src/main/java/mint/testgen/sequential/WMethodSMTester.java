package mint.testgen.sequential;

import mint.Configuration;
import mint.model.Machine;
import mint.model.dfa.TraceDFA;
import mint.model.walk.SimpleMachineAnalysis;
import mint.model.walk.WalkResult;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.types.VariableAssignment;
import org.apache.log4j.Logger;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

/**
 * Created by Neil Walkinshaw, September 2020.
 */

public class WMethodSMTester implements TestGenerator {

    private final static Logger LOGGER = Logger.getLogger(WMethodSMTester.class.getName());


    Machine m;
    int addedStates = 2;

    public void setK(int k){
        this.addedStates = k;
    }


    public List<List<TraceElement>> generateTests(Machine m){
        this.m = m;
        Configuration.getInstance().PREFIX_CLOSED = true;

        List<List<String>> tests = new ArrayList<>();
        //tests.addAll(transitionCover());
        //tests.addAll(stateCover());
        //tests.addAll(product(tests,characterisationSet()));
        //tests.addAll(product(tests,sigmaStar()));
        tests.addAll(randomWalks(1000,10));

        List<List<TraceElement>> finalTests = new ArrayList<>();
        for(List<String> seq : tests){
            finalTests.add(getTraceElements(seq));
        }
        return finalTests;
    }

    public List<List<TraceElement>> generateTests(int t, Machine m) {
        List<List<TraceElement>> tests = generateTests(m);
        return tests.subList(0,Math.min(t,tests.size()));
    }

    protected Collection<List<String>> transitionCover(){
        Collection<DefaultEdge> transitions = m.getAutomaton().getTransitions();
        Collection<List<String>> cover = new ArrayList<>();
        for(DefaultEdge de : transitions){
            List<String> sequence = new ArrayList<>();
            GraphPath<Integer,DefaultEdge> sp = m.getAutomaton().shortestPath(m.getInitialState(),m.getAutomaton().getTransitionSource(de));
            if(sp == null)
                continue;
            for(DefaultEdge e : sp.getEdgeList()){
                sequence.add(m.getLabel(e));
            }
            sequence.add(m.getLabel(de));
            cover.add(sequence);
        }
        return cover;
    }


    protected Collection<List<String>> randomWalks(int n, int length){
        Random rand = new Random(Configuration.getInstance().SEED);
        Collection<List<String>> cover = new ArrayList<>();
        for(int i = 0; i<n; i++){
            Integer initialState = m.getInitialState();
            List<DefaultEdge> sequence = new ArrayList<>();
            List<String> inputs = new ArrayList<>();
            for(int j = 0; j<length; j++){
                List<DefaultEdge> pool = new ArrayList<>();
                pool.addAll(m.getAutomaton().getOutgoingTransitions(initialState));
                if(!pool.isEmpty()) {
                    DefaultEdge selected = pool.get(rand.nextInt(pool.size()));
                    sequence.add(selected);
                    initialState = m.getAutomaton().getTransitionTarget(selected);
                }
                else{
                    break;
                }
            }
            for(DefaultEdge e : sequence){
                inputs.add(m.getLabel(e));
            }
            cover.add(inputs);
        }
        return cover;
    }

    protected Collection<List<String>> stateCover(){
        Collection<Integer> states = m.getStates();
        Collection<List<String>> stateCover = new ArrayList<>();
        for(Integer state : states){
            List<String> sequence = new ArrayList<>();
            if(state == m.getInitialState())
                continue;
            GraphPath<Integer,DefaultEdge> sp = m.getAutomaton().shortestPath(m.getInitialState(),state);
            if(sp == null)
                continue;
            for(DefaultEdge de : sp.getEdgeList()){
                sequence.add(m.getLabel(de));
            }
            stateCover.add(sequence);
        }
        return stateCover;
    }

    protected Collection<List<String>> sigmaStar(){
        Set<List<String>> sigStar = new HashSet<>();
        List<String> alphabet = new ArrayList<>();
        alphabet.addAll(m.getAutomaton().getAlphabet());
        for(int i = 0; i<addedStates; i++){
            Set<List<String>> newList = new HashSet<>();
            newList.addAll(sigStar);
            sigStar.addAll(product(newList,convertToList(alphabet)));
        }
        return sigStar;
    }

    private Set<List<String>> convertToList(List<String> alphabet) {
        Set<List<String>> converted = new HashSet<>();
        for(String a : alphabet){
            List<String> element = new ArrayList<>();
            element.add(a);
            converted.add(element);
        }
        return converted;
    }

    protected Collection<List<String>> characterisationSet() {
       List<String> alphabet = new ArrayList<>();
       Set<List<String>> added = new HashSet<>();
       alphabet.addAll(m.getAutomaton().getAlphabet());
       boolean successful = false;
       while(!successful) {
           Set<List<String>> candidates = product(added,convertToList(alphabet));
           for(List<String> candidate : candidates){
               added.add(candidate);
               if(checkSuccessful(added)) {
                   successful = true;
                   break;
               }
           }
       }
       added = removeRedundant(added);
       return added;
    }

    private Set<List<String>> removeRedundant(Collection<List<String>> lists) {

        List<List<String>> added = new ArrayList<>();
        added.addAll(lists);

        Set<List<String>> toReturn = new HashSet<>();

        TraceDFA.Accept[][] results = new TraceDFA.Accept[added.size()][m.getStates().size()];

        List<Integer> states = new ArrayList<>();
        states.addAll(m.getStates());
        SimpleMachineAnalysis sma = new SimpleMachineAnalysis(m);

        for(int j = 0; j<added.size(); j++){
            List<TraceElement> walkable = getTraceElements(added.get(j));
            for(int i = 0; i<states.size(); i++){
                Integer currentState = states.get(i);
                WalkResult wr = sma.walk(walkable,currentState,new ArrayList<>(),m.getAutomaton());
                TraceDFA.Accept result = wr.isAccept();
                if(result == TraceDFA.Accept.UNDEFINED)
                    result = TraceDFA.Accept.REJECT;
                results[j][i]=result;

            }

        }

        List<Integer> toRemove = new ArrayList<>();

        for(int j = 0; j<added.size(); j++) {
            TraceDFA.Accept[] list = results[j];
            boolean duplicate = false;
            for(int k = j+1; k<added.size() && !duplicate; k++){
                if(toRemove.contains(k))
                    continue;
                TraceDFA.Accept[] other = results[k];
                boolean same = true;
                for(int l = 0; l<other.length; l++){
                    if(list[l]!=other[l]){
                        same=false;
                        break;
                    }
                }
                if(same){
                    duplicate = true;
                    toRemove.add(k);
                }
            }

        }

        for(int i = 0; i<added.size(); i++){
            if(!toRemove.contains(i))
                toReturn.add(added.get(i));
        }
        return toReturn;
    }

    protected Set<List<String>> product(Collection<List<String>> a, Collection<List<String>>b){
        Set<List<String>> product = new HashSet<>();
        if(a.isEmpty()){
            for(List<String> alpha : b){
                product.add(alpha);
            }
        }
        else{
            for(List<String> element : a){
                for(List<String> add : b) {
                    List<String> toAdd = new ArrayList<>();
                    toAdd.addAll(element);
                    toAdd.addAll(add);
                    product.add(toAdd);
                }
            }
        }
        return product;
    }


    private boolean checkSuccessful(Set<List<String>> added) {
        Set<List<TraceDFA.Accept>> unique = new HashSet<>();
        Collection<Integer> states = m.getStates();
        SimpleMachineAnalysis sma = new SimpleMachineAnalysis(m);
        for(Integer state : states){
            List<TraceDFA.Accept> accept = new ArrayList<>();
            for(List<String> sequence : added){
                List<TraceElement> walkable = getTraceElements(sequence);
                WalkResult wr = sma.walk(walkable,state,new ArrayList<>(),m.getAutomaton());
                TraceDFA.Accept result = wr.isAccept();
                if(result == TraceDFA.Accept.UNDEFINED)
                    result = TraceDFA.Accept.REJECT;
                accept.add(result);
            }
            if(unique.contains(accept)) {
                return false;
            }
            else{
                unique.add(accept);
            }
        }
        return true;
    }

    private List<TraceElement> getTraceElements(List<String> sequence) {
        List<TraceElement> generated = new ArrayList<>();
        for(String element : sequence){
            generated.add(new SimpleTraceElement(element, new VariableAssignment[]{}));
        }
        return generated;
    }


}


