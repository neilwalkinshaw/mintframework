package mint.evaluation.kfolds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Generates all possible NGrams for a given alphabet and a given N.
 *
 * Created by neilwalkinshaw on 08/05/2016.
 */
public class NGram<T> {


    protected List<List<T>> ngrams;

    protected Collection<T> alphabet;

    protected int k;

    public Collection<T> getAlphabet(){
        return alphabet;
    }

    public NGram(Collection<T> alphabet, int k){
        this.alphabet = alphabet;
        ngrams = new ArrayList<List<T>>();
        List alph = new ArrayList<T>();
        alph.addAll(alphabet);
        for(Object ngram : getAllLists(alph,k)){
            ngrams.add((List<T>)ngram);
        }
    }

    public List<List<T>> getNgrams(){
        return ngrams;
    }


    protected Collection<List<T>> getAllLists(List<T> elements, int lengthOfList)
    {
        //initialize our returned list with the number of elements calculated above
        Collection<List<T>> output = new ArrayList<List<T>>();

        //lists of length 1 are just the original elements
        if(lengthOfList == 1){
            for(T element : elements){
                List<T> newList = new ArrayList<T>();
                newList.add(element);
                output.add(newList);
            }

            return output;
        }
        else
        {
            //the recursion--get all lists of length 3, length 2, all the way up to 1
            Collection<List<T>> allSublists = getAllLists(elements, lengthOfList - 1);

            for(int i = 0; i < elements.size(); i++)
            {
                for(List<T> list : allSublists)
                {
                    List<T> newList = new ArrayList<T>();
                    newList.addAll(list);
                    newList.add(0,elements.get(i));
                    //add the newly appended combination to the list
                    output.add(newList);
                }
            }

            return output;
        }
    }

    /*public Collection<List<T>> permute(List<T> input) {
        Collection<List<T>> output = new ArrayList<List<T>>();
        if (input.isEmpty()) {
            output.add(new ArrayList<T>());
            return output;
        }
        List<T> list = new ArrayList<T>(input);
        T head = list.get(0);
        List<T> rest = list.subList(1, list.size());
        for (List<T> permutations : permute(rest)) {
            List<List<T>> subLists = new ArrayList<List<T>>();
            for (int i = 0; i <= permutations.size(); i++) {
                List<T> subList = new ArrayList<T>();
                subList.addAll(permutations);
                subList.add(i, head);
                subLists.add(subList);
            }
            output.addAll(subLists);
        }
        return output;
    }*/
}
