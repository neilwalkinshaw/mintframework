package mint.model;

import com.google.common.collect.HashBasedTable;

import mint.Configuration;
import mint.model.dfa.TraceDFA;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

public class LatentProbabilitiesMachine extends RawProbabilisticMachine{

    HashBasedTable<DefaultEdge,DefaultEdge,Double> transitionsToCoefficient = HashBasedTable.create();

    Random rand = new Random(Configuration.getInstance().SEED);


    /**
     * For experimental purposes
     */
    public void addRandomLatentDependencies(){
        for(DefaultEdge de : automaton.getTransitions()){
            for(DefaultEdge to : getTransitiveSuccessors(de)) {
                if(to.equals(de))
                    continue;
                double addLatentDependency = rand.nextDouble();
                if(addLatentDependency<0.25){
                    double sampled = rand.nextDouble();
                    transitionsToCoefficient.put(to,de,sampled);
                }
            }
        }
    }

    public void addLatentDependency(Integer source, String label, Integer source2, String label2, Double coefficient){
        DefaultEdge sourceEdge = getEdge(source,label);
        DefaultEdge targetEdge = getEdge(source2,label2);
        if(sourceEdge!=null && targetEdge!=null){
            transitionsToCoefficient.put(targetEdge,sourceEdge,coefficient);
        }
    }

    private DefaultEdge getEdge(Integer source, String label) {
        Object[] candidates = automaton.getOutgoingTransitions(source,label).toArray();
        if(candidates.length>0)
            return (DefaultEdge)candidates[0];
        else
            return null;


    }


    public double aGivenB(DefaultEdge a, DefaultEdge b){
        if(transitionsToCoefficient.contains(a,b))
            return transitionsToCoefficient.get(a,b);
        else return 0D;
    }

    /**
     * Return all transitive successors of m - i.e. any instructions
     * that could eventually be reached from m.
     * @param m
     * @return
     */
    public Collection<DefaultEdge> getTransitiveSuccessors(DefaultEdge m){
        return transitiveSuccessors(m, new HashSet<DefaultEdge>());
    }

    private Collection<DefaultEdge> transitiveSuccessors(DefaultEdge m, Set<DefaultEdge> done){
        Collection<DefaultEdge> successors = new HashSet<DefaultEdge>();
        for(DefaultEdge n : getSuccessors(m)){
            if(!done.contains(n)) {
                successors.add(n);
                done.add(n);
                successors.addAll(transitiveSuccessors(n, done));
            }
        }
        return successors;
    }

    private Iterable<? extends DefaultEdge> getSuccessors(DefaultEdge m) {
        Integer target = automaton.getTransitionTarget(m);
        return automaton.getOutgoingTransitions(target);
    }


    public List<DefaultEdge> randomWalk(int targetDepth, Random rand, boolean accepting) {

        List<DefaultEdge> path = new ArrayList<>();
        int source = getInitialState();
        boolean finished = false;
        if(automaton.getOutgoingTransitions(source).isEmpty())
            finished = true;
        while (!finished & path.size()<targetDepth){
            List<DefaultEdge> outgoing = new ArrayList<>();
            for(DefaultEdge outg : automaton.getOutgoingTransitions(source)){
                if(accepting | path.size()<targetDepth-1) {
                    if (automaton.getAccept(automaton.getTransitionTarget(outg)) != TraceDFA.Accept.REJECT) {
                        outgoing.add(outg);
                    }
                }
                else if (automaton.getAccept(automaton.getTransitionTarget(outg)) == TraceDFA.Accept.REJECT) {
                    outgoing.add(outg);
                }
            }
            Collections.shuffle(outgoing);
            double point = rand.nextDouble();
            double probabilityMass = 0D;
            Map<DefaultEdge, Double> probLoad = new HashMap<>();
            for(DefaultEdge de : outgoing){
                double coeff = automaton.getTransitionData(de).getPayLoad();
                if(path.size()>0) {
                    for (DefaultEdge key : path.subList(0, path.size() - 1)) {
                        //This will simply set the coefficient to the last previous one in the trace.
                        if (transitionsToCoefficient.contains(key, de)) {
                            coeff = transitionsToCoefficient.get(key, de);
                        }
                    }
                }
                //coeff = Math.min(1D,coeff); //TODO - is this even right??
                //coeff = Math.max(0D,coeff);
                probabilityMass= probabilityMass + coeff;
                probLoad.put(de,coeff);
            }
            double current = 0D;
            DefaultEdge currentEdge = null;
            for(DefaultEdge de : outgoing) {
                current = current + (probLoad.get(de)/probabilityMass);
                if (current > point) {
                    currentEdge = de;
                    break;
                }
            }
            path.add(currentEdge);
            source = automaton.getTransitionTarget(currentEdge);
            if(automaton.getOutgoingTransitions(source).isEmpty())
                finished = true;
        }
        return path;
    }
}
