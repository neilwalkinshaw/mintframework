package mint.model.walk.probabilistic;

import mint.model.walk.WalkResult;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;

/**
 * Created by neilwalkinshaw on 27/04/2016.
 */
public class ProbabilisticWalkResult extends WalkResult {

    public ProbabilisticWalkResult(Integer target, List<DefaultEdge> walk) {
        super(target, walk);
    }


}
