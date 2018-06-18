package mint.inference.efsm.scoring.scoreComputation;

/**
 * Created by neilwalkinshaw on 27/04/2016.
 */
public class Score implements Comparable<Score> {
    int primaryScore = 0;
    int secondaryScore = 0;

    public Score(int primaryScore) {
        this.primaryScore = primaryScore;
    }

    public Score(int primaryScore, int secondaryScore) {
        this.primaryScore = primaryScore;
        this.secondaryScore = secondaryScore;
    }

    public int getPrimaryScore() {
        return primaryScore;
    }

    public void setPrimaryScore(int primaryScore) {
        this.primaryScore = primaryScore;
    }

    public int getSecondaryScore() {
        return secondaryScore;
    }

    public void setSecondaryScore(int secondaryScore) {
        this.secondaryScore = secondaryScore;
    }

    public void incrementPrimaryScore(){
        primaryScore++;
    }


    @Override
    public int compareTo(Score o) {
        Integer primFrom = (Integer)getPrimaryScore();
        Integer primTo = (Integer)o.getPrimaryScore();
        Integer secFrom = (Integer)getSecondaryScore();
        Integer secTo = (Integer)o.getSecondaryScore();
        int comp = primFrom.compareTo(primTo);
        if(comp == 0)
            return secFrom.compareTo(secTo);
        return comp;
    }

    public String toString(){
        return primaryScore+", "+secondaryScore;
    }
}
