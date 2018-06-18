package mint.model.walk;

import mint.model.Machine;
import mint.model.dfa.TraceDFA;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by neilwalkinshaw on 19/03/15.
 */
public abstract class MachineAnalysis<T extends Machine> {

    protected T machine;

    /**
     * Which of the possibleTransitions are compatible with element?
     * possibleTransitions is a set of edges that share the same label as element.
     * In this case, there is no data to distinguish which edges are incompatible,
     * so they are all treated as compatible.
     */
    public abstract Collection<DefaultEdge> getCompatible(Set<DefaultEdge> possibleTransitions, TraceElement element);

    /**
     * Obtain the state corresponding to a particular walk s in automaton a.
     * @param s
     * @param automaton
     * @return
     */
    public abstract WalkResult getState(List<TraceElement> s, TraceDFA automaton);

    /**
     * Is the list s accepted in the automaton? resetCoverage determines whether the transitions
     * covered by the processed transitions are incorporated.
     */
    public abstract boolean walk(List<TraceElement> s, boolean resetCoverage, TraceDFA automaton);


    /**
     * Get the proportion of transitions covered so far in the automaton.
     * @param automaton
     * @return
     */
    public abstract double getProportionTransitionsCovered(TraceDFA automaton);

    /**
     * Get the number of transitions covered.
     * @return
     */
    public abstract  int getNumberTransitionsCovered();


    /**
     * run the entire testset through the automaton. Does not return anything, but the object will record
     * coverage of the model.
     * @param testSet
     * @param automaton
     */
    public abstract void walk(Set<List<TraceElement>> testSet, TraceDFA automaton);

    /**
     * Walk according to the traceelements in, starting from initialState, recording the transitions walked
     * (sofar) on automaton.
     * @param in
     * @param initialState
     * @param soFar
     * @param automaton
     * @return
     */
    public abstract WalkResult walk(List<TraceElement> in,
                                    Integer initialState,
                                    List<DefaultEdge> soFar, TraceDFA automaton);

    /**
     * Reset coverage.
     */
    public abstract void resetCoverage();

    /**
     * Which transition to choose during a walk of the machine, given a set of possible transitions, and the current
     * TraceElement containing the label and data.
     *
     * This simply returns the first element in the set (it makes the tacit assumption that the set of transitions
     * only contains one element).
     * @param transitions
     * @param current
     * @return
     */
    protected abstract DefaultEdge chooseTransition(Set<DefaultEdge> transitions,
                                           TraceElement current, boolean isLast);
}
