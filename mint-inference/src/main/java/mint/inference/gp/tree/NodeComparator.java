package mint.inference.gp.tree;

import mint.inference.evo.Chromosome;
import mint.inference.evo.TournamentSelection;

import java.util.Comparator;

/**
 * Created by neilwalkinshaw on 09/03/15.
 */
public class NodeComparator implements Comparator<Chromosome> {

    protected TournamentSelection sel;

    public NodeComparator(TournamentSelection evaluator){
        this.sel = evaluator;
    }


    /**
     * Returns -1 if fitness of o1 is worse than o2, returns 1
     * if fitness of o1 is better, and 0 otherwise.
     *
     * Note that "fitness" is a minimisation function, so a lower fitness
     * is better...
     *
     * @param co1
     * @param co2
     * @return
     */
    @Override
    public int compare(Chromosome co1, Chromosome co2) {
        Node<?> o1 = (Node<?>)co1;
        Node<?> o2 = (Node<?>)co2;

        double fitVal = 0;
        try {
            fitVal = sel.computeFitness(o2) - sel.computeFitness(o1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(fitVal == 0D){
            int size = o2.size() - o1.size();
            if(size == 0){
                return o2.numVarsInTree()-o1.numVarsInTree();
            }
            else
                return (o2.size() - o1.size());
        }
        else if(fitVal < 0D)
            return 1;
        else
            return -1;
    }
}
