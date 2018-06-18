package mint.tracedata.types;

import mint.tracedata.readers.ngram.Ngrammer;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates an N-Gram distribution VariableAssignment (a list of doubles
 * representing the frequencies of the various n-grams in the given text).
 *
 * Created by neilwalkinshaw on 23/03/2017.
 */

public class NGramVariableAssignment extends ListVariableAssignment {

    Ngrammer ng;

    /**
     *
     * @param name
     * @param sourceText
     * @param n - size of the n-grams
     * @param normalised - normalise distribution to interval of [0..1]
     */
    public NGramVariableAssignment(String name, String sourceText, int n, boolean normalised) {
        super(name);
        ng = new Ngrammer(sourceText,n);
        List<Double> distribution = new ArrayList<Double>();
        for(Integer count : ng.getOrderedValues()){
            distribution.add((double)count.intValue());
        }
        if(normalised)
            normalise(distribution);
        this.value = distribution;
        setNull(false);
    }

    public NGramVariableAssignment(String name, boolean normalised, List<Double> distribution) {
        super(name);
        if(normalised)
            normalise(distribution);
        this.value = distribution;
        setNull(false);
    }

    private void normalise(List<Double> distribution) {
        double max = 0D;
        for(Double d : distribution){
            if(d > max)
                max = d;
        }
        for(int i = 0; i<distribution.size();i++){
            distribution.set(i,distribution.get(i)/max);
        }
    }
}
