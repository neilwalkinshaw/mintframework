package mint.tracedata.readers.ngram;

import mint.tracedata.types.NGramVariableAssignment;
import mint.tracedata.types.VariableAssignment;

import java.util.*;

/**
 * For a collection of Ngrammers (representing individual documents),
 * this merges all of the ngrams into a single collection.
 *
 * Created by neilwalkinshaw on 23/03/2017.
 */
public class NgramMerger {

    List<String> orderedGlobalKeys;
    Map<String,Integer> ngramInstances;
    Map<String,Integer> stringOccurrences;
    List<Ngrammer> ngrammers;


    public NgramMerger(){
        orderedGlobalKeys = new ArrayList<String>();
        ngrammers = new ArrayList<Ngrammer>();
        ngramInstances = new HashMap<String,Integer>();
        stringOccurrences = new HashMap<String,Integer>();

    }


    public void addNgramCounts( Ngrammer ng) {
        if(ngrammers.contains(ng))
            return;
        for(String key : ng.getOrderedKeys()){
            if(ngramInstances.containsKey(key)){
                ngramInstances.put(key, ngramInstances.get(key)+ng.getValue(key));
            }
            else
                ngramInstances.put(key,ng.getValue(key));
            if(stringOccurrences.containsKey(key))
                stringOccurrences.put(key,stringOccurrences.get(key)+1);
            else
                stringOccurrences.put(key,1);
        }
        ngrammers.add(ng);
    }


    /**
     * From the collection of n-gram distributions, produce a term-document matrix
     * according to the given list of headers. For each n-gram distribution, reduce it
     * to a list of numbers where, for each header n-gram, the number corresponds to the number
     * of times that header occurs in the given n-gram. Repeated for a set of n-gram distributions,
     * this results in a "matrix" - a list of distributions.
     * @param headers
     * @return
     */
    public List<List<Double>> getTermDocumentMatrix(List<String> headers){
        List<List<Double>> documentMatrix = new ArrayList<List<Double>>();
        for(Ngrammer ngrammer : ngrammers){
            List<Double> occurrences = new ArrayList<Double>();
            occurrences.addAll(getNumericalDistribution(ngrammer,headers));
            documentMatrix.add(occurrences);
        }
        return documentMatrix;
    }

    public List<VariableAssignment<?>> getPCADistribution(){
        List<String> headers = new ArrayList<String>();
        for(String s : stringOccurrences.keySet()){
            headers.add(s);
        }
        PCAReducer pcar = new PCAReducer(getTermDocumentMatrix(headers),headers);
        List<List<Double>> transformed = pcar.getDistribution();
        List<VariableAssignment<?>> training = new ArrayList<VariableAssignment<?>>();
        for(List<Double> t : transformed){
            training.add(new NGramVariableAssignment("output",false,t));
        }
        return training;
    }

    /*public List<VariableAssignment<?>> getUselessReducedDistribution(){
        List<String> headers = new ArrayList<String>();
        for(String s : stringOccurrences.keySet()){
            headers.add(s);
        }
        UselessReducer pcar = new UselessReducer(getTermDocumentMatrix(headers),headers);
        List<List<Double>> transformed = pcar.getDistribution(25);
        List<VariableAssignment<?>> training = new ArrayList<VariableAssignment<?>>();
        for(List<Double> t : transformed){
            training.add(new NGramVariableAssignment("output",false,t));
        }
        return training;
    }*/

    /**
     * Returns a list of Ngram distributions, trimmed to the top few, as parameterised by
     * reduced.
     *
     * The removeUseless option will run it through the WEKA removeUseless filter to remove
     * features that do not contribute anything to the outcome (e.g. constants).
     *
     * @param removeUseless
     * @param reduced
     * @return
     */
    public List<VariableAssignment<?>> getDistribution(boolean removeUseless, int reduced){


        List<VariableAssignment<?>> training = new ArrayList<VariableAssignment<?>>();
        List<List<Double>> transformed = null;
        if(removeUseless){
            List<String> headers = new ArrayList<String>();
            for(String s : stringOccurrences.keySet()){
                headers.add(s);
            }
            UselessReducer pcar = new UselessReducer(getTermDocumentMatrix(headers),headers);
            int distSize = getNumKeys();
            transformed = pcar.getDistribution(Math.min(reduced,distSize));
        }

        if(!removeUseless || training.isEmpty()){
            List<String> topKeys = getTopGlobalKeys(reduced);
            transformed = getTermDocumentMatrix(topKeys);
        }

        for(List<Double> t : transformed){
            training.add(new NGramVariableAssignment("output",false,t));
        }

        return training;
    }

    //Remove constants
    //pick most changeable.

    public VariableAssignment<?> getDistributionFor(String name, Ngrammer ngrammer, List<String> newDistribution) {
        List<Double> distribution = getNumericalDistribution(ngrammer, newDistribution);
        return new NGramVariableAssignment(name,false,distribution);
    }

    private List<Double> getNumericalDistribution(Ngrammer ngrammer, List<String> newDistribution) {
        List<Double> distribution = new ArrayList<Double>();
        List<String> ngramKeys = ngrammer.getOrderedKeys();
        for(String key : newDistribution){
            if(ngramKeys.contains(key))
                distribution.add((double)ngrammer.getValue(key));
            else
                distribution.add(0D);
        }
        return distribution;
    }


    public int getNumKeys(){
        return orderedGlobalKeys.size();
    }

    /**
     * Return ngrams that occur in filter or more strings.
     * @param filter
     * @return
     */
    public List<String> getOrderedGlobalKeys(int filter){
        List<String> ngramList = new ArrayList<String>();
        for(String key : orderedGlobalKeys){
            if(stringOccurrences.get(key) >= filter)
                ngramList.add(key);
        }
        return ngramList;
    }

    public List<String> getTopGlobalKeys(int num){
        ValueComparator bvc = new ValueComparator(stringOccurrences,ngramInstances);
        TreeMap<String,Integer> orderedSet = new TreeMap<String,Integer>(bvc);
        orderedSet.putAll(stringOccurrences);
        Iterator<String> keyIt = orderedSet.descendingKeySet().descendingIterator();
        int counter = 0;
        List<String> retList = new ArrayList<String>();
        while(keyIt.hasNext() && counter<num){
            retList.add(keyIt.next());
            counter++;
        }
        return retList;
    }
}

class ValueComparator implements Comparator<String> {
    Map<String, Integer> base, secondary;

    public ValueComparator(Map<String, Integer> base, Map<String, Integer> secondary) {
        this.base = base;
        this.secondary = secondary;
    }

    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    public int compare(String a, String b) {
        if (base.get(a) > base.get(b)) {
            return -1;
        } else if(base.get(a) == base.get(b)) {
            if(secondary.get(a) > secondary.get(b))
                return -1;
            else if(secondary.get(a) == secondary.get(b))
                return 0;
            else
                return 1;
        }
        else{
            return 1;
        } // returning 0 would merge keys
    }
}