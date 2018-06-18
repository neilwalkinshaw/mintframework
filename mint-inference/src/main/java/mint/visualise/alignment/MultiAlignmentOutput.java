package mint.visualise.alignment;

import org.apache.log4j.Logger;
import mint.model.Machine;
import mint.model.dfa.TraceDFA;
import mint.model.walk.SimpleMachineAnalysis;
import mint.model.walk.WalkResult;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

/**
 * Given an inferred model (must be a linear DAG model, inferred with --noLoops option),
 * return a new set of sequences in FASTA format, with appropriate gaps inserted.
 *
 * This is achieved by projecting the alignments captured in the inferred machine back
 * to a set of sequences.
 *
 * Created by neilwalkinshaw on 09/02/15.
 */

public class MultiAlignmentOutput {

    final static Logger LOGGER = Logger.getLogger(MultiAlignmentOutput.class.getName());

    protected Machine machine;
    protected TraceSet traceData;
    protected Map<Integer,Set<Integer>> statesToTraces;
    protected Map<Integer,List<List<String>>> statesToAlignments;
    protected List<List<String>> aligned;

    public MultiAlignmentOutput(Machine m, TraceSet t){
        machine = m;
        traceData = t;
        statesToTraces = new HashMap<Integer,Set<Integer>>();
        aligned = new ArrayList<List<String>>();
        statesToAlignments = new HashMap<Integer,List<List<String>>>();
        computeAlignment();
        computeIndices();
        addTrailingBlanks();
        print();
    }

    private void addTrailingBlanks() {
        int longest = getLongest(aligned);
        for(List<String> a : aligned){
            while(a.size()<longest){
                a.add("-");
            }
        }
    }

    private void print() {
        for(List<String> aligned : this.aligned){
            Iterator<String> listIt = aligned.iterator();
            String out = ">\n";
            while(listIt.hasNext()){
                out = out.concat(listIt.next());
            }
            out = out.concat("\n");
            System.out.println(out);
        }
    }

    /**
     * Pre-processes machine, storing prefix for each sequence at a given state.
     */
    private void computeAlignment() {
        LOGGER.debug("Computing alignments - storing prefixes at a given state");
        SimpleMachineAnalysis<Machine<Set<TraceElement>>> analysis = new SimpleMachineAnalysis<Machine<Set<TraceElement>>>(machine);
        Iterator<List<TraceElement>> seqIt = traceData.getPos().iterator();
        TraceDFA automaton = machine.getAutomaton();
        List<List<DefaultEdge>> walks = new ArrayList<List<DefaultEdge>>();
        while (seqIt.hasNext()) {

            List<TraceElement> current = seqIt.next();
            WalkResult wr = analysis.walk(current, machine.getInitialState(), new ArrayList<DefaultEdge>(), automaton);
            walks.add(wr.getWalk());
        }
        removeEmpty(walks);
        int count = 0;
        for(List<DefaultEdge> walk: walks){
            int traceNumber = count++;
            for(DefaultEdge e: walk){
                Integer source = automaton.getTransitionSource(e);
                Integer target = automaton.getTransitionTarget(e);
                addTraceForState(source, traceNumber);
                addTraceForState(target, traceNumber);
            }
        }

    }

    private void addTraceForState(Integer target, Integer traceNumber) {
        if(!statesToTraces.containsKey(target)){
            Set<Integer> traces = new HashSet<Integer>();
            statesToTraces.put(target,traces);
        }
        Set<Integer> traces = statesToTraces.get(target);
        traces.add(traceNumber);
    }


    /**
     * Takes as input the set of (unadulterated) prefixes for each state.
     *
     */
    private void computeIndices() {
        LOGGER.debug("Computing indices - storing prefixes at a given state");

        SimpleMachineAnalysis<Machine<Set<TraceElement>>> analysis = new SimpleMachineAnalysis<Machine<Set<TraceElement>>>(machine);
        Iterator<List<TraceElement>> seqIt = traceData.getPos().iterator();
        TraceDFA automaton = machine.getAutomaton();
        List<List<DefaultEdge>> walks = new ArrayList<List<DefaultEdge>>();

        sequencesToMachineWalks(analysis, seqIt, automaton, walks);

        removeEmpty(walks);


        List<List<String>> prefixes = new ArrayList<List<String>>();
        for(int i = 0; i<walks.size(); i++){
            prefixes.add(new ArrayList<String>());

         }
        statesToAlignments.put(machine.getInitialState(), prefixes);

        int numberOfWalks = walks.size();
        Collection<Integer> finishedWalks = new HashSet<Integer>();
        while (aligned.size() < numberOfWalks) {
            int walkNr = 0;
            for (List<DefaultEdge> walk : walks) {

                if (finishedWalks.contains(walkNr)) {
                    walkNr++;
                    continue;
                }

                DefaultEdge first = walk.get(0);
                Integer source = machine.getAutomaton().getTransitionSource(first);
                if (!isReady(source)) {
                    walkNr++;
                    continue;
                }
                List<List<String>> pfs = statesToAlignments.get(source);

                int longest = getLongest(pfs);

                List<String> currentPrefix = pfs.get(walkNr);



                Integer target = machine.getAutomaton().getTransitionTarget(first);

                List<String> newPrefix = new ArrayList<String>();
                newPrefix.addAll(currentPrefix);
                while(newPrefix.size()<longest){
                    newPrefix.add("-");
                }
                newPrefix.add(automaton.getTransitionData(first).getLabel());

                addPrefix(target, newPrefix, walkNr);
                walk.remove(0);
                if (walk.isEmpty()) {
                    aligned.add(currentPrefix);
                    finishedWalks.add(walkNr);
                }
                walkNr++;
            }

        }
    }

    /**
     * For each sequence in seqIt, produce the sequence of states in the inferred machine.
     * @param analysis
     * @param seqIt
     * @param automaton
     * @param walks
     */
    private void sequencesToMachineWalks(SimpleMachineAnalysis<Machine<Set<TraceElement>>> analysis, Iterator<List<TraceElement>> seqIt, TraceDFA automaton, List<List<DefaultEdge>> walks) {
        LOGGER.debug("Mapping full sequences to states");
        while (seqIt.hasNext()) {

            List<TraceElement> current = seqIt.next();
            WalkResult wr = analysis.walk(current, machine.getInitialState(), new ArrayList<DefaultEdge>(), automaton);
            walks.add(wr.getWalk());

        }
    }

    private void removeEmpty(List<List<DefaultEdge>> walks) {
        Collection<List<DefaultEdge>> toRemove = new HashSet<List<DefaultEdge>>();
        for(List<DefaultEdge> walk : walks){
            if(walk.isEmpty())
                toRemove.add(walk);
        }
        walks.removeAll(toRemove);
    }



    private boolean isReady(Integer source) {
        List<List<String>> prefixes = statesToAlignments.get(source);
        int count = 0;
        for(List<String> lst : prefixes){
            if(lst !=null)
                count++;
        }
        int num = statesToTraces.get(source).size();
        return count == num;
    }

    private void addPrefix(Integer target, List<String> label, int walkNr) {
        List<List<String>> prefixes = statesToAlignments.get(target);
        if(prefixes==null) {
            prefixes = new ArrayList<List<String>>();
            for(int i = 0; i< traceData.getPos().size();i++){
                prefixes.add(null);
            }
            statesToAlignments.put(target,prefixes);
        }
        prefixes.set(walkNr, label);
    }

    private int getLongest(List<List<String>> prefixes) {
        int max = 0;
        for(List<String> l : prefixes){
          if(l == null)
             continue;
          max = Math.max(l.size(),max);
        }
        return max;
    }


}
