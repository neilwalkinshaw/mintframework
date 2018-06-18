package mint.tracedata;

import org.apache.log4j.Logger;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.NumberVariableAssignment;
import mint.tracedata.types.VariableAssignment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Outputs a set of traces to a trace file (of the type readable again by
 * EFSMTool. Particularly useful when the trace was read-in from another
 * trace format - e.g. Daikon DTrace.
 *
 * Created by neilwalkinshaw on 31/08/2014.
 */

public class TraceToFile {

    private Map<String,List<VariableAssignment<?>>> decs;
    private List<String> traceStrings;
    private boolean addLimits = false;
    private VariableRangeTracker tracker = new VariableRangeTracker();

    private final static Logger LOGGER = Logger.getLogger(TraceToFile.class.getName());


    public TraceToFile(Collection<List<TraceElement>> traces, boolean limit){
        addLimits = limit;
        decs = new HashMap<String,List<VariableAssignment<?>>>();
        traceStrings = new ArrayList<String>();

        for(List<TraceElement> trace:traces){
            String traceString = "trace\n";

            for(TraceElement element:trace){
                if(!decs.keySet().contains(element.getName())){
                    decs.put(element.getName(),getDeclaration(element));
                }
                if(addLimits) {
                    for (VariableAssignment<?> v : element.getData()) {
                        if (v instanceof DoubleVariableAssignment) {
                            DoubleVariableAssignment dvar = (DoubleVariableAssignment) v;
                            if(dvar.isNull())
                                continue;
                            double var = dvar.getValue();
                            tracker.addVar(element.getName(), v.getName(), var);


                        } else if (v instanceof IntegerVariableAssignment) {
                            IntegerVariableAssignment ivar = (IntegerVariableAssignment) v;
                            if(ivar.isNull())
                                continue;
                            double var = (double) ivar.getValue();
                            tracker.addVar(element.getName(), v.getName(), var);
                        }
                    }
                }
                traceString += generateStringInstance(element)+ "\n";
            }
            traceStrings.add(traceString);
        }
    }

    private String generateStringInstance(TraceElement element) {
        String instance = element.getName();
        List<VariableAssignment<?>> varNames = decs.get(element.getName());
        if(varNames == null)
            return instance;
        for(VariableAssignment<?> param:varNames){
            String name = param.getName();
            VariableAssignment<?> equivalent = find(name,element.getData());
            assert(equivalent!=null);
            Object val = equivalent.getValue();
            String stringVal = "null";
            if(val != null) {
                stringVal = val.toString();
                stringVal = stringVal.replaceAll(" ", ",");
            }
            instance+=" "+stringVal;
        }
        return instance;
    }

    private VariableAssignment<?> find(String name, Set<VariableAssignment<?>> data) {
        for(VariableAssignment<?> candidate : data){
            if(candidate.getName().equals(name))
                return candidate;
        }
        LOGGER.error("Could not find declared variable "+name);
        return null;

    }

    private List<VariableAssignment<?>> getDeclaration(TraceElement element) {
        List<VariableAssignment<?>> vars = new ArrayList<VariableAssignment<?>>();
        for(VariableAssignment<?> var : element.getData()){
            vars.add(var);
        }
        return vars;
    }

    public void writeToFile(File f) throws IOException {
        LOGGER.debug("Writing to: "+f.getPath());
        FileWriter fw = new FileWriter(f);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("types\n");
        for(String dec : decs.keySet()){
            bw.write(dec);
            for(VariableAssignment<?> var : decs.get(dec)){
                bw.write(" "+var.getName()+var.typeString());
                if(addLimits) {
                    try {
                        Object min = tracker.getMin(dec, var.getName());
                        Object max = tracker.getMax(dec, var.getName());
                        if (min == null || checkNan(min)) {
                            NumberVariableAssignment nvar = (NumberVariableAssignment) var;
                            min = nvar.getMin();

                        }
                        if (max == null || checkNan(max)) {

                            NumberVariableAssignment nvar = (NumberVariableAssignment) var;
                            max = nvar.getMax();

                        }

                        bw.write("[" + min + ":" + max + "]");
                    }
                    catch(Exception e) {
                        continue;
                    }
                }
            }
            bw.write("\n");
        }
        for(String trace:traceStrings){
            bw.write(trace);
        }
        bw.close();
        fw.close();
    }

    private boolean checkNan(Object num) {
        if(num instanceof Double){
            Double numd = (Double)num;
            if(numd.isInfinite() || numd.isNaN())
                return true;
        }
        return false;
    }

}
