package mint.inference.filter;

import mint.tracedata.types.VariableAssignment;

import java.util.List;
import java.util.Map;

/**
 * Takes in a training set, and filters out instances according to some criterion
 *
 * Created by neilwalkinshaw on 22/03/2016.
 */
public interface Filter {

    void filter(Map<String, Map<List<VariableAssignment<?>>, VariableAssignment<?>>> trainingSet);
}
