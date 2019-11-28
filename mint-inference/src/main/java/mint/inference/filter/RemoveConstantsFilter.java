package mint.inference.filter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.MultiValuedMap;

import mint.tracedata.types.VariableAssignment;

/**
 *
 * Removes attributes where the value remains constant
 *
 * Created by neilwalkinshaw on 22/03/2016.
 */

public class RemoveConstantsFilter implements Filter {

	@Override
	public void filter(Map<String, MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>>> trainingSet) {

		for (String key : trainingSet.keySet()) {
			Set<List<VariableAssignment<?>>> toRemove = new HashSet<List<VariableAssignment<?>>>();
			MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> t = trainingSet.get(key);

			for (Entry<List<VariableAssignment<?>>, VariableAssignment<?>> current : t.entries()) {
				VariableAssignment<?> output = current.getValue();
				List<VariableAssignment<?>> inputs = current.getKey();
				VariableAssignment<?> input = find(output.getName(), inputs);
				if (output.getValue() == null)
					toRemove.add(inputs);
				else if (input == null)
					continue;
				else if (input.isNull() && output.isNull())
					toRemove.add(inputs);
				else if (input.getValue() == null)
					continue;
				else if (input.getValue().equals(output.getValue()))
					toRemove.add(inputs);
			}
			for (List<VariableAssignment<?>> inputs : toRemove) {
				t.remove(inputs);
			}

		}

		// return filtered;
	}

	private VariableAssignment<?> find(String val, List<VariableAssignment<?>> params) {
		for (VariableAssignment<?> p : params) {
			if (p.getName().equals(val))
				return p;
		}
		return null;
	}

}
