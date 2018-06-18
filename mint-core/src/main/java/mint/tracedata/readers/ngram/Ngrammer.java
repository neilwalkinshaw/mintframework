package mint.tracedata.readers.ngram;

import java.util.*;

/**
 * Build an N-gram distribution for a string.
 *
 * Created by neilwalkinshaw on 17/03/2017.
 */
public class Ngrammer {

    protected Map<String,Integer> ngramDistribution;
    protected String originalString;
    protected static int counter = 0;
    protected int id;
    protected int n;

    public Ngrammer(String toTokenize, int n){
        this.originalString = toTokenize;
        this.n = n;
        this.id = counter;
        counter++;
        ngramDistribution = new HashMap<String, Integer>();
        try {
            List<String> tokenized = extractNgrams(toTokenize,n);
            for(int i = 0; i< tokenized.size(); i++){
                if(!ngramDistribution.containsKey(tokenized.get(i)))
                    ngramDistribution.put(tokenized.get(i),1);
                else{
                    int current = ngramDistribution.get(tokenized.get(i));
                    ngramDistribution.put(tokenized.get(i),current+1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public int getN(){
        return n;
    }

    public Integer getValue(String key){
        return ngramDistribution.get(key);
    }

    public List<String> getOrderedKeys(){
        List<String> keys = new ArrayList<String>();
        keys.addAll(ngramDistribution.keySet());
        Collections.sort(keys);
        return keys;
    }

    /**
     * Returns values ordered according to ngram ordering (alphabetical).
     * @return
     */
    public List<Integer> getOrderedValues(){
        List<Integer> values = new ArrayList<Integer>();
        for(String key : getOrderedKeys()){
            values.add(ngramDistribution.get(key));
        }
        return values;
    }

    private static List<String> extractNgrams(String tokenize, int n){
        List<String> ngrams = new ArrayList<String>();
        for(int i = 0; i< tokenize.length(); i++){
            if(tokenize.length()-i-n>0){
                ngrams.add(tokenize.substring(i,i+n));
            }
        }
        return ngrams;
    }

}
