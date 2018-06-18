package mint.tracedata.types;

import java.util.Comparator;

/**
 * To order VariableAssignments alphabetically according to the variable name.
 *
 * Created by neilwalkinshaw on 09/03/15.
 */
public class VariableAssignmentComparator implements Comparator<VariableAssignment<?>> {
    @Override
    public int compare(VariableAssignment<?> o1, VariableAssignment<?> o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
