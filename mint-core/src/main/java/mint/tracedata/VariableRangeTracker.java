package mint.tracedata;

import java.util.*;

/**
 * Created by neilwalkinshaw on 18/03/2016.
 */



public class VariableRangeTracker {

    Map<String,Map<String,List<Double>>> tracker;

    public VariableRangeTracker(){
        tracker = new HashMap<String, Map<String, List<Double>>>();
    }

    public void addVar(String methodName, String varName, double value){
        Map<String,List<Double>> varsForMethod = getVarsForMethod(methodName);
        List<Double> range = getRange(varName,varsForMethod);
        range.add(value);
    }

    private List<Double> getRange(String var, Map<String, List<Double>> varsForMethod) {
        if(varsForMethod.containsKey(var))
            return varsForMethod.get(var);
        List<Double> ranges = new ArrayList<Double>();
        varsForMethod.put(var,ranges);
        return ranges;
    }

    private Map<String, List<Double>> getVarsForMethod(String methodName) {
        if(tracker.containsKey(methodName))
            return tracker.get(methodName);
        Map<String,List<Double>> ranges = new HashMap<String,List<Double>>();
        tracker.put(methodName,ranges);
        return ranges;
    }

    public Number getMin(String methodName, String variable){
        List<Double> vals = tracker.get(methodName).get(variable);
        if(vals == null)
            return null;
        return Collections.min(vals);
    }

    public Number getMax(String methodName, String variable){
        List<Double> vals = tracker.get(methodName).get(variable);
        if(vals == null)
            return null;
        return Collections.max(vals);
    }

}
