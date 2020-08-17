package mint.inference.filter;

import mint.tracedata.types.VariableAssignment;

import java.util.*;

/**
 *
 * For any attributes where the value does not change from input to output, these are removed.
 *
 * Created by neilwalkinshaw on 22/03/2016.
 */

public class RemoveConstantsFilter implements Filter {


    @Override
    public void filter(Map<String, Map<List<VariableAssignment<?>>, VariableAssignment<?>>> trainingSet) {
        Set<String> toRemove = new HashSet<String>();
        for(String key: trainingSet.keySet()){
            Map<List<VariableAssignment<?>>, VariableAssignment<?>> t = trainingSet.get(key);

            boolean inputOutputDifference = false;
            VariableAssignment outputVar = null;
            for(List<VariableAssignment<?>> inputs: t.keySet()){
                VariableAssignment<?> output = t.get(inputs);
                if(outputVar == null){
                    outputVar = output;
                }

                VariableAssignment input = find(output.getName(),inputs);
                if(output.getValue() == null)
                    continue;
                else if(input == null)
                    continue;
                else if(input.isNull() && output.isNull())
                    continue;
                else if(input.getValue() == null)
                    continue;
                else if(!input.getValue().equals(output.getValue()))
                    inputOutputDifference=true;
            }
            if(!inputOutputDifference){
                toRemove.add(key);
            }

        }
        for(String key : toRemove){
            trainingSet.remove(key);
        }
       // return filtered;
    }



    private VariableAssignment<?> find(String val, List<VariableAssignment<?>> params) {
        for(VariableAssignment<?> p  :params) {
            if (p.getName().equals(val))
                return p;
        }
        return null;
    }



}
