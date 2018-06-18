package mint.model.statepair;

import mint.inference.efsm.scoring.scoreComputation.Score;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

/**
 * Created by neilwalkinshaw on 18/02/15.
 */

public class OrderedStatePairWithScoreTester {

    @Before
    public void setup() throws IOException {

    }

    @After
    public void teardown() {
    }

    /**
     * Test that OrderedStatePairWithScore objects are ordered correctly in a TreeSet.
     */
    @Test
    public void testSimpleAcceptingTrace() throws Exception {
        TreeSet<OrderedStatePairWithScore> pairs = new TreeSet<OrderedStatePairWithScore>();
        Random r = new Random(0);
        for(int i = 0; i<1000; i++){
            OrderedStatePairWithScore pair = new OrderedStatePairWithScore(i,i+1);
            pair.setScore(new Score(r.nextInt(12)));
            pairs.add(pair);
        }
        Iterator<OrderedStatePairWithScore> pairIt = pairs.descendingIterator();
        int lastScore = 13;
        while(pairIt.hasNext()){
            OrderedStatePairWithScore current = pairIt.next();
            Assert.assertTrue(current.getScore().getPrimaryScore() <= lastScore);
            lastScore = current.getScore().getPrimaryScore();
        }
    }

    /**
     * Test that OrderedStatePairWithScore objects are ordered correctly in a TreeSet, with secondary scores.
     */
    @Test
    public void testSimpleAcceptingTraceWithSecondaryScores() throws Exception {
        TreeSet<OrderedStatePairWithScore> pairs = new TreeSet<OrderedStatePairWithScore>();
        Random r = new Random(0);
        for(int i = 0; i<1000; i++){
            OrderedStatePairWithScore pair = new OrderedStatePairWithScore(i,i+1);
            pair.setScore(new Score(r.nextInt(12),r.nextInt(12)));
            pairs.add(pair);
        }
        Iterator<OrderedStatePairWithScore> pairIt = pairs.descendingIterator();
        int lastScore = 13;
        int lastSecondaryScore = 13;
        while(pairIt.hasNext()){
            OrderedStatePairWithScore current = pairIt.next();
            Assert.assertTrue(current.getScore().getPrimaryScore() <= lastScore);
            if(current.getScore().getPrimaryScore() == lastScore) {
                Assert.assertTrue(current.getScore().getSecondaryScore() <= lastSecondaryScore);
                lastSecondaryScore = current.getScore().getSecondaryScore();
            }
            else{
                lastSecondaryScore = 13;
            }
            lastScore = current.getScore().getPrimaryScore();

        }
    }

}
