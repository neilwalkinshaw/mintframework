package mint.evaluation.mutation;

import mint.Configuration;
import mint.model.Machine;
import mint.model.ProbabilisticMachine;
import mint.model.dfa.TraceDFA;
import mint.model.walk.SimpleMachineAnalysis;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.types.VariableAssignment;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class MutationOperator {

    protected Machine target, mutated;
    protected Random rand;
    protected Integer sourceState;

    public MutationOperator(Machine target, Random rand){
        this.target = target;
        this.rand = rand;
        this.sourceState = target.getInitialState();
        this.mutated = null;
    }

    public Machine applyMutation() throws NonDeterministicException, Exception {
        mutated = apply();
        return mutated;
    }

    protected abstract Machine apply() throws NonDeterministicException, Exception;

    /**
     * Code to identify traces that exercise the mutated code - traces that are
     * accepted by the mutated machine but aren't accepted by the original one.
     *
     * If limit is <0, it returns all negative traces. This can however be very
     * time consuming.
     */
    public TraceSet identifyNewTraces(int limit){
        int targetDepth = mutated.getAutomaton().getDepth()+5; //This is taken from the STAMINA random walk algorithm
        TraceSet posSet = new TraceSet();
        TraceDFA mutatedDFA = mutated.getAutomaton();
        GraphPath shortedPrefix = mutatedDFA.shortestPath(mutated.getInitialState(),sourceState);
        List<TraceElement> pathToSource = null;
        if(shortedPrefix==null)
            pathToSource = new ArrayList<>();
        else
            pathToSource = generateLabelledPath(shortedPrefix.getEdgeList(),mutatedDFA);
        List<GraphPath> outgoing = mutatedDFA.allPaths(sourceState,targetDepth);


        SimpleMachineAnalysis targetSMA = new SimpleMachineAnalysis(target);

        for(GraphPath path : outgoing){
            List<TraceElement> concatenated = new ArrayList<>();
            concatenated.addAll(pathToSource);
            concatenated.addAll(generateLabelledPath(path.getEdgeList(),mutatedDFA));
            boolean accepted = targetSMA.walk(concatenated,true,target.getAutomaton());
            if(!accepted)
                posSet.addNeg(concatenated);
            if(posSet.getNeg().size()>limit && limit >-1)
                break;
        }

        return posSet;

    }

    private List<TraceElement> generateLabelledPath(List<DefaultEdge> concatenated, TraceDFA machine) {
        List<TraceElement> toReturn = new ArrayList<>();
        for(DefaultEdge de : concatenated){
            String label = machine.getTransitionData(de).getLabel();
            toReturn.add(new SimpleTraceElement(label,new VariableAssignment[]{}));
        }
        return toReturn;
    }

    protected Integer pickRandomState(){
        ArrayList<Integer> states = new ArrayList<>();
        states.addAll(target.getStates());
        return (Integer)pickRandom(states);
    }

    protected String pickRandom(){
        ArrayList<String> alphabet = new ArrayList<>();
        alphabet.addAll(target.getAutomaton().getAlphabet());
        return (String)pickRandom(alphabet);
    }

    protected Object pickRandom(List pickFrom){
        return pickFrom.get(rand.nextInt(pickFrom.size()));
    }


    public class NonDeterministicException extends Throwable {
    }
}
