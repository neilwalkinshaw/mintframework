package mint.model.statepair;

import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.inference.efsm.scoring.scoreComputation.ComputeScore;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by neilwalkinshaw on 20/05/2016.
 */

public class StatePairComparator<T extends SimpleMergingState<?>> implements Comparator<StatePair> {

    private static Map<StatePair,Integer> statesToScores = new ConcurrentHashMap<StatePair,Integer>();

    protected T ms;

    public static int getPairScore(StatePair sp){
        Integer score = statesToScores.get(sp);
        if(score == null)
            return -1;
        else
            return score;
    }


    public StatePairComparator(T ms){
        this.ms = ms;
    }

    /**
     * Negative if 01 < o2, positive if o1 > o2, 0 otherwise
     * @param o1
     * @param o2
     * @return
     */
    @Override
    public int compare(StatePair o1, StatePair o2) {
        Integer scoreo1 = findScore(o1);
        Integer scoreo2 = findScore(o2);

        return scoreo1.compareTo(scoreo2);
    }

    private Integer findScore(StatePair o1) {
        Integer score = null;
        if(statesToScores.containsKey(o1))
            score =  statesToScores.get(o1);
        else {
            score = computeScore(o1);
            statesToScores.put(o1,score);
        }
        if(score == null)
            return 0;
        else return score;
    }

    private Integer computeScore(StatePair o1) {
        ComputeScore<T> cs = new ComputeScore<T>(ms,o1);
        try {
            return cs.call().getPrimaryScore();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
