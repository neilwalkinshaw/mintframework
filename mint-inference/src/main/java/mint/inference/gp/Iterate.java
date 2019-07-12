package mint.inference.gp;

import org.apache.log4j.Logger;
import mint.inference.evo.AbstractIterator;
import mint.inference.evo.Chromosome;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.nonterminals.booleans.RootBoolean;
import mint.inference.gp.tree.nonterminals.doubles.RootDouble;
import mint.inference.gp.tree.nonterminals.integer.RootInteger;
import mint.inference.gp.tree.nonterminals.lists.RootListNonTerminal;

import java.util.*;

/**
 * Responsible for creating the offspring in an iteration by applying mutation and crossover.
 * Also retains a small number (3) elite offspring from the previous generation.
 *
 * Created by neilwalkinshaw on 06/03/15.
 */
public class Iterate extends AbstractIterator {
    protected Generator gen;
    protected int maxDepth;

    private final static Logger LOGGER = Logger.getLogger(Iterate.class.getName());


    public Iterate(List<Chromosome> population, double crossOver, double mutation, Generator g, int maxD, Random r, Collection<Chromosome> elites) {
        super(population, crossOver, mutation,r, elites);
        this.gen = g;
        this.maxDepth = maxD;
    }

    protected void mutate(int numberCrossover,int mutation) {

        for(int i = numberCrossover; i<numberCrossover+mutation; i++){

            //chromosome to be mutated
            Node<?> aNode = (Node<?>)population.get(i);
            List<Node<?>> nt = new ArrayList<Node<?>>();


            addAllChildren(aNode,nt, null, maxDepth);
            removeUnviableNodes(nt);
            if(nt.isEmpty())
                continue;
            Node<?> toMutate = pickRandomBiasEarly(nt,rand.nextDouble());

            offSpring.add(aNode);

            toMutate.mutate(gen,rand.nextInt(maxDepth-toMutate.depth()));
        }
    }

    protected void removeUnviableNodes(List<Node<?>> nt) {
        Collection toRemove = new HashSet();
        for(Node n : nt){
            if(n instanceof RootDouble || n instanceof RootBoolean || n instanceof RootInteger || n instanceof RootListNonTerminal)
                toRemove.add(n);
            else if(n.depth() >= maxDepth)
                toRemove.add(n);
        }
        nt.removeAll(toRemove);
    }

    protected int select(List<Integer> avoid, int limit){
        List<Integer> possibilities = new ArrayList<Integer>();
        for(int i = 0; i<limit; i++){
            possibilities.add(i);
        }
        possibilities.removeAll(avoid);
        if(possibilities.size() == 0)
            return -1;
        int parent = possibilities.get(rand.nextInt(possibilities.size()));
        return parent;
    }

    protected void crossOver(List<Chromosome> pop, int number) {
        int count = 0;
        //TODO can end up in an infinite loop
        while(count<number){

            int parentA = select(new ArrayList(), number);
            if(parentA<0)
                break; //no more crossovers possible.
            ArrayList<Integer> avoid = new ArrayList<Integer>();
            avoid.add(parentA);
            boolean completedParentA = false;
            while(!completedParentA) {
                int parentB = select(avoid, number);
                if (parentB < 0) {
                    break;
                }
                Node<?> parentBNode = (Node<?>)pop.get(parentB);
                Node<?> aCopy = (Node<?>)pop.get(parentA).copy();


                if (aCopy.getType().equals("string")) {
                    offSpring.add(aCopy);
                    break; //have no parents, so cannot do crossover.
                }
                Node<?> bCopy = parentBNode.copy();

                Node<?> crossOverA = null;
                Node<?> crossOverB = null;
                try {
                    crossOverA = selectCrossOverPoint(aCopy, null);
                    crossOverB = selectCrossOverPoint(bCopy, crossOverA);
                    if (crossOverB == null) {
                        avoid.add(parentB);
                        continue;
                    }

                    crossOverA.swapWith(crossOverB);


                } catch (Exception e) {
                    if(crossOverA == null){
                        LOGGER.debug("null crossover for children of "+aCopy);
                    }
                    LOGGER.debug(crossOverA + ", " + crossOverB);
                    continue;
                }
                count = count + 1;
                offSpring.add(aCopy);
                completedParentA = true;
            }

        }

    }

    /**
     * Select a crossover point in tree that will be transplanted into target.
     * Although both are Node types, tree is treated as a whole subtree from that node,
     * whereas target is a specific node that is the target of a cross-over.
     * @param tree
     * @param target
     * @return
     */
    protected Node<?> selectCrossOverPoint(Node<?> tree, Node<?> target) {
        List<Node<?>> nt = new ArrayList<Node<?>>();
        int depth = maxDepth;
        if(target !=null)
            depth = maxDepth - target.depth();
        addAllChildren(tree,nt, target, depth); //only add nodes that are same type as target
        Node<?> picked = null;
        removeUnviableNodes(nt);
        double which = rand.nextDouble();
        if(nt.isEmpty())
            return null;
        picked =  pickRandomBiasEarly(nt, which);
        //picked = nt.get(rand.nextInt(nt.size()));
        return picked;
    }

    /**
     * Pick random element from a list, but prefer earlier elements
     * (these correspond to elements higher up the tree).
     * @param coll
     * @param which
     * @return
     */
    protected Node<?> pickRandomBiasEarly(List<Node<?>> coll, double which){
        double collSize = (double)coll.size();
        double[] probs = calculateProbs(collSize);
        double sum = 0;
        Node<?> retVal = null;
        for(int i = 0; i<coll.size(); i++){
            double current = probs[i];
            sum +=current;
            if(which<=sum) {
                retVal = coll.get(i);
                break;
            }
        }
        return retVal;
    }


    /**
     * Calculate probabilities to bias towards earlier elements.
     *
     * This should favour the selection of nodes that are higher up in the tree (assumign tree
     * is in breadth-first order).
     * @param collSize
     * @return
     */
    private double[] calculateProbs(double collSize) {
        double inc = collSize/5;
        double[] probs = new double[(int)collSize];
        int counter = 0;
        double sum = 0;
        for(int i = (int)collSize; i>0; i--){
            probs[counter] = i * inc;
            sum+=(i*inc);
            counter++;
        }
        for(int i = 0; i<probs.length; i++){
            probs[i] = probs[i] / sum;
        }
        return probs;
    }


    /**
     * Add children to to nt that could feasibly be candidates for crossover.
     * For this their subtree depth must be < childMaxDepth, and they must be of the same type as target.
     *
     *
     * @param tree
     * @param nt
     * @param target
     * @param depth
     */
    protected void addAllChildren(Node<?> tree, List<Node<?>> nt, Node<?> target, int depth) {
        int currentDepth = 1;
        Stack<Node> worklist = new Stack<Node>();
        for(Node<?> child: tree.getChildren()) {
            if((child.subTreeMaxdepth() - currentDepth) > depth)
                continue;
            worklist.push(child);
            if(target!=null){
                if(!target.getType().equals(child.getType()))
                    continue;
            }
            nt.add(child);
            Collections.shuffle(nt);
        }
        while(!worklist.isEmpty()){
            List<Node<?>> toAdd = new ArrayList<Node<?>>();
            List<Node<?>> forThisDepth = new ArrayList<Node<?>>();
            Node<?> c = worklist.pop();
            for (Node<?> child : c.getChildren()) {
                if((child.subTreeMaxdepth() - currentDepth) > depth)
                    continue;
                toAdd.add(child);
                if(target!=null){
                    if(!target.getType().equals(child.getType()))
                        continue;
                }
                forThisDepth.add(child);
            }
            Collections.shuffle(forThisDepth);
            nt.addAll(forThisDepth);
            worklist.addAll(toAdd);
            currentDepth++;
        }

    }

}
