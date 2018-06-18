package mint.inference.evo;

import java.util.Comparator;

/**
 * Created by neilwalkinshaw on 09/03/15.
 */
public class ChromosomeComparator implements Comparator<Chromosome> {

    protected TournamentSelection sel;

    public ChromosomeComparator(TournamentSelection evaluator){
        this.sel = evaluator;
    }


    /**
     * Returns -1 if fitness of o1 is worse than o2, returns 1
     * if fitness of o1 is better, and 0 otherwise.
     *
     * Note that "fitness" is a minimisation function, so a lower fitness
     * is better...
     *
     * @param o1
     * @param o2
     * @return
     */
    @Override
    public int compare(Chromosome o1, Chromosome o2) {

        double fitVal = 0;
        try {
            fitVal = sel.computeFitness(o2) - sel.computeFitness(o1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
       if(fitVal < 0D)
            return 1;
        else if(fitVal>0D)
            return -1;
        else return 0;
    }
}
