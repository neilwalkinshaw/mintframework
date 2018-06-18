package mint.inference.evo.pfsm.matrix;

import org.apache.log4j.Logger;
import mint.evaluation.kfolds.NGram;
import mint.inference.evo.Chromosome;
import mint.inference.evo.ChromosomeComparator;
import mint.inference.evo.GPConfiguration;
import mint.inference.evo.TournamentSelection;
import mint.inference.gp.fitness.Fitness;
import mint.tracedata.TraceElement;

import java.util.*;

/**
 * Created by neilwalkinshaw on 18/05/2016.
 */
public class PFSMTournamentSelection extends TournamentSelection {

    private final static Logger LOGGER = Logger.getLogger(PFSMTournamentSelection.class.getName());

    List<List<TraceElement>> training;
    NGram ngrams;
    List<Double> targetDist;

    public PFSMTournamentSelection(List<Chromosome> totalPopulation, List<List<TraceElement>> training) {
        super(totalPopulation, 0);
        Collection<String> alphabet = getAlphabet(training);
        ngrams = new NGram(alphabet,3);
        targetDist = getTestCoords(training,ngrams.getNgrams());
        this.training = training;
    }

    private Collection<String> getAlphabet(List<List<TraceElement>> training) {
        Collection<String> alphabet = new HashSet<String>();
        for(List<TraceElement> list : training){
            for(TraceElement el : list){
                alphabet.add(el.getName());
            }
        }
        return alphabet;
    }

    private List<Double> getTestCoords(List<List<TraceElement>> pos, List<List<String>> ngrams) {
        List<Double> dist = new ArrayList<Double>();
        for(List<String> ngram : ngrams){
            Double ngramTot = 0D;
            for(List<TraceElement> trace : pos){
                for(int i = 0; i < trace.size(); i++){
                    boolean matched = false;
                    for(int j = 0; j< ngram.size(); j++){
                        if(i+j >= trace.size()) {
                            matched = false;
                            break;
                        }
                        String traceEl = trace.get(i+j).getName();
                        if(!ngram.get(j).equals(traceEl)) {
                            matched = false;
                            break;
                        }
                        else
                            matched = true;
                    }
                    if(matched)
                        ngramTot++;
                }
            }
            dist.add(ngramTot);
        }
        return dist;
    }

    @Override
    public List<Chromosome> select(GPConfiguration config){
        List<Chromosome> best =  super.select(config);
        MergingTable mt = (MergingTable)best.get(0);
        LOGGER.debug("BEST: " +mt.latest);
        return best;
    }

    @Override
    public Fitness getFitness(Chromosome toEvaluate) {
        return new PFSMFitness((MergingTable)toEvaluate, training, ngrams, targetDist);
    }

    @Override
    protected Comparator<Chromosome> getComparator() {
        return new ChromosomeComparator(this);
    }
}
