package mint.inference.filter;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MultiValuedMap;

import mint.tracedata.types.VariableAssignment;

/**
 * Takes in a training set, and filters out instances according to some
 * criterion
 *
 * Created by neilwalkinshaw on 22/03/2016.
 */
public interface Filter {

	void filter(Map<String, MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>>> map);
}
