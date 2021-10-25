package mint.evaluation;

import mint.tracedata.TraceElement;

import java.util.List;

public class SortableTrace implements Comparable<SortableTrace>{
    List<TraceElement> trace;
    Double score;
    Double belief;

    public SortableTrace(List<TraceElement> trace, Double score) {
        this.trace = trace;
        this.score = score;
        this.belief = 0D;
    }

    public SortableTrace(List<TraceElement> trace, Double score, Double belief) {
        this.trace = trace;
        this.score = score;
        this.belief = belief;
    }

    public double getScore(){
        return score;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
     * for all {@code x} and {@code y}.  (This
     * implies that {@code x.compareTo(y)} must throw an exception iff
     * {@code y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
     * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
     * all {@code z}.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(SortableTrace o) {
        /*Double ths = 1-belief;
        Double othr = 1-o.belief;
        int beliefComparison = ths.compareTo(othr);
        if(beliefComparison==0){
            Double tu = score;
            Double ou = o.score;
            beliefComparison = tu.compareTo(ou);
        }*/
        /*Double ths = score;
        Double othr = o.score;
        int beliefComparison = ths.compareTo(othr);
        if(beliefComparison==0){
            Double tu = 1-belief;
            Double ou = 1-o.belief;
            beliefComparison = tu.compareTo(ou);
        }*/

        if(trace.size() == o.trace.size()){
            Double thisScore = score + (1-belief);
            Double otherScoer = o.score+(1-o.belief);
            return thisScore.compareTo(otherScoer);
        }
        else{
            Double thisScore = score ;
            Double otherScoer = o.score;
            return thisScore.compareTo(otherScoer);
        }


    }

    public List<TraceElement> getTrace() {
        return trace;
    }
}
