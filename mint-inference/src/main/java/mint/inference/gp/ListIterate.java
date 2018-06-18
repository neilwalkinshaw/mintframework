package mint.inference.gp;

import org.apache.log4j.Logger;
import mint.inference.evo.Chromosome;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.nonterminals.lists.RootListNonTerminal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * If a GP infers a list of different types of elements, crude conventional crossover and
 * mutation may not be well suited. List elements that are less deep, for example, can be missed
 * out.
 *
 * In this version, when a list occurs, a crossover operation will cross every element in the list
 * with a corresponding element in the other list.
 *
 * If the chromosome in question is not a list, it simply defaults to the Iterator behaviour.
 *
 * Created by neilwalkinshaw on 07/03/2018.
 */
public class ListIterate extends Iterate {

    boolean isList = true;
    private final static Logger LOGGER = Logger.getLogger(ListIterate.class.getName());


    public ListIterate(List<Chromosome> population, double crossOver, double mutation, Generator g, int maxD, Random r, Collection<Chromosome> elites) {
        super(population, crossOver, mutation, g, maxD, r, elites);
        if(population.isEmpty())
            isList = false;
        else {
            Chromosome chrom = population.get(0);
            if(chrom instanceof RootListNonTerminal)
                isList = true;
            else
                isList = false;
        }

    }


    protected Node<?> selectCrossOverPoint(Node<?> tree, Node<?> target) {
        List<Node<?>> nt = new ArrayList<Node<?>>();
        int depth = maxDepth;
        if(target !=null)
            depth = maxDepth - target.depth();
        nt.add(tree);
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


    @Override
    protected void crossOver(List<Chromosome> pop, int number) {
        if(!isList)
            super.crossOver(pop, number);
        else{

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
                    RootListNonTerminal aCopy = (RootListNonTerminal)pop.get(parentA).copy();
                    RootListNonTerminal bCopy = (RootListNonTerminal)parentBNode.copy();
                    int i = rand.nextInt(aCopy.getChildren().size());

                    Node<?> childA = aCopy.getChildren().get(i);
                    Node<?> childB = bCopy.getChildren().get(i);
                    if (childA.getType().equals("string")) {
                        continue; //have no parents, so cannot do crossover.
                    }
                    Node<?> crossOverA = null;
                    Node<?> crossOverB = null;
                    try {
                        crossOverA = selectCrossOverPoint(childA, null);
                        crossOverB = selectCrossOverPoint(childB, crossOverA);
                        if (crossOverB == null) {
                            avoid.add(parentB);
                            continue;
                        }

                        crossOverA.swapWith(crossOverB);
                        //LOGGER.debug("Crossed-over child "+i);

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
    }
}
