package mint.inference.gp.fitness;

/**
 * Created by neilwalkinshaw on 20/08/15.
 */
public class InvalidDistanceException extends Exception {


    @Override
    public String getMessage() {
        return "Distance involved comparison to NaN or Infinite values.";
    }
}
