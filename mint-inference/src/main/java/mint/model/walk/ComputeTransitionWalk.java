package mint.model.walk;

import org.apache.log4j.Logger;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.Terminal;
import mint.model.GPFunctionMachineDecorator;
import mint.tracedata.TraceElement;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.NumberVariableAssignment;
import mint.tracedata.types.VariableAssignment;
import mint.tracedata.types.VariableAssignmentComparator;
import org.jgrapht.graph.DefaultEdge;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Given a walk and a GPFunctionMachineDecorator, compute the variable values for the walk.
 *
 * Created by neilwalkinshaw on 19/03/15.
 */
public class ComputeTransitionWalk {

    protected final GPFunctionMachineDecorator inferred;
    protected final WalkResult wr;
    private final static Logger LOGGER = Logger.getLogger(ComputeTransitionWalk.class.getName());
    private List<String> commonVars;


    public ComputeTransitionWalk(GPFunctionMachineDecorator model, WalkResult wr, List<String> commonVariables){
        this.inferred = model;
        this.wr = wr;
        commonVars = commonVariables;
    }

    protected boolean checkCommon(String varName){
        if(commonVars == null)
            return true;
        else{
            return commonVars.contains(varName);
        }
    }


    /**
     * Walks path and transforms values in assignments to relevant values. Starts
     * from a single initial assignment, assumes that the rest of the variables throughout the
     * run are computed without further user input.
     * @param assignments
     */
    public Collection<VariableAssignment<?>> compute(Collection<VariableAssignment<?>> assignments){
        List<DefaultEdge> walk = wr.getWalk();
        if(walk.isEmpty())
            return assignments;
        Iterator<DefaultEdge> walker = wr.getWalk().iterator();

        while(walker.hasNext()){
            DefaultEdge current = walker.next();
            if(!walker.hasNext())
                break;
            Collection<Node<?>> functions = inferred.getFunctions(current);
            assignInitialVars(functions,assignments);
            runAndAssign(functions,assignments);
        }
        return assignments;
    }


    /**
     * Walks path and transforms values in assignments to relevant values. assignmentInputs is a list
     * of variable assignments, where the index corresponds to the step in the walk at which external
     * input is received.
     * @param assignmentInputs
     */
    public Collection<VariableAssignment<?>> compute(List<TraceElement> assignmentInputs){
        List<DefaultEdge> walk = wr.getWalk();
        if(walk.isEmpty())
            return new HashSet<VariableAssignment<?>>();
        Iterator<DefaultEdge> walker = wr.getWalk().iterator();
        Collection<VariableAssignment<?>> assignments = assignmentInputs.get(0).getData();
        int index = 0;
        while(walker.hasNext()){
            DefaultEdge current = walker.next();
            Collection<Node<?>> functions = inferred.getFunctions(current);
            assignInitialVars(functions,assignments);
            runAndAssign(functions, assignments);
            index++;
            if(index<assignmentInputs.size()) {
                overlayAssignments(assignments, assignmentInputs.get(index).getData());
            }
        }
        return assignments;
    }

    /**
     * Walks path and transforms values in assignments to relevant values. assignmentInputs is a list
     * of variable assignments, where the index corresponds to the step in the walk at which external
     * input is received.
     * @param assignmentInputs
     */
    public Collection<VariableAssignment<?>> compute(int tracesDone, List<TraceElement> assignmentInputs, String logFile){
        List<DefaultEdge> walk = wr.getWalk();
        if(walk.isEmpty())
            return new HashSet<VariableAssignment<?>>();
        Iterator<DefaultEdge> walker = wr.getWalk().iterator();
        Collection<VariableAssignment<?>> assignments = assignmentInputs.get(0).getData();
        int index = 0;
        try{
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
            List<VariableAssignment<?>> initAss = new ArrayList<VariableAssignment<?>>();
            initAss.addAll(assignments);
            addMissing(initAss);
            Collections.sort(initAss, new VariableAssignmentComparator());
            //out.println(lineString(tracesDone, "start", initAss, initAss));
            while(walker.hasNext()){
                DefaultEdge current = walker.next();
                Collection<Node<?>> functions = inferred.getFunctions(current);
                String label = inferred.getLabel(current);
                if(label.indexOf("\\n")>0)
                    label = label.substring(0,(label.indexOf("\\n")));
                assignInitialVars(functions,assignments);
                runAndAssign(functions, assignments);
                index++;
                if(index<assignmentInputs.size()) {
                    overlayAssignments(assignments, assignmentInputs.get(index).getData());
                }
                if(walker.hasNext()) {
                    List<VariableAssignment<?>> inferredData = new ArrayList<VariableAssignment<?>>();
                    inferredData.addAll(assignments);
                    addMissing(inferredData);
                    Collections.sort(inferredData, new VariableAssignmentComparator());
                    List<VariableAssignment<?>> referenceData = new ArrayList<VariableAssignment<?>>();
                    referenceData.addAll(assignmentInputs.get(index).getData());
                    addMissing(referenceData);
                    Collections.sort(referenceData, new VariableAssignmentComparator());
                    out.println(lineString(tracesDone, label, referenceData, inferredData));
                }
            }
            out.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return assignments;
    }

    private void addMissing(List<VariableAssignment<?>> referenceData) {
        for(String common : this.commonVars){
            boolean found = false;
            for(VariableAssignment var : referenceData){
                if(var.getName().equals(common)){
                    found = true;
                }
            }
            if(!found){
                DoubleVariableAssignment doub = new DoubleVariableAssignment(common);
                doub.setNull(true);
                referenceData.add(doub);
            }
        }
    }

    private String lineString(int index, String lab, List<VariableAssignment<?>> referenceData, List<VariableAssignment<?>> inferredData) {
        String retString = Integer.toString(index);
        retString = retString + "," + strip(lab) +",";
        Set<String> done = new HashSet<String>();
        for( int i = 0; i < referenceData.size(); i++){
            VariableAssignment v = referenceData.get(i);
            if(!done.contains(v.getName()) && containsName(inferredData, v.getName()) && checkCommon(v.getName())) {

                retString = retString + v.getValue() + ",";
                done.add(v.getName());
            }
        }
        for(VariableAssignment v : inferredData){
            if(done.contains(v.getName()) &&containsName(referenceData, v.getName()) && checkCommon(v.getName())) {
                retString = retString + v.getValue() + ",";
                done.add(v.getName());
            }
        }
        return retString;
    }

    private boolean containsName(Collection<VariableAssignment<?>> inferredData, String name) {
        for(VariableAssignment<?> var : inferredData){
            if(var.getName().equals(name))
                return true;
        }
        return false;
    }

    private String strip(String lab) {
        return lab.replaceAll(",","-");
    }


    /**
     * Replaces computed assignment variable values with input variable assignments where applicable.
     * Otherwise computed values are retained.
     * @param assignments
     * @param inputAssignments
     */
    private void overlayAssignments(Collection<VariableAssignment<?>> assignments, Collection<VariableAssignment<?>> inputAssignments) {

        for(VariableAssignment<?> input : inputAssignments){
            if(input.isParameter()){
                for(VariableAssignment<?> ass: assignments){
                    if(ass.getName().equals(input.getName())){
                        ass.setStringValue(input.getValue().toString());
                    }
                }
            }

        }

    }

    /**
     * A highly inefficient way to set initial vars to terminal in a tree.
     * TODO - make more efficient.
     * @param functions
     * @param assignments
     */
    private void assignInitialVars(Collection<Node<?>> functions, Collection<VariableAssignment<?>> assignments) {
        for(Node<?> function:functions){
            Collection<Terminal<?>> terminals = getTerminals(function);
            for(VariableAssignment v : assignments){
                if(v.isNull())
                    continue;
                for(Terminal t : terminals){
                    if(v.getName().equals(t.getTerminal().getName())){
                        t.getTerminal().setStringValue(v.getValue().toString());
                    }
                    if(v instanceof NumberVariableAssignment){
                        NumberVariableAssignment nrVar = (NumberVariableAssignment) v;
                        nrVar.setEnforcing(true);
                    }
                    else{
                        LOGGER.debug("Variable type: " + v.getClass() + " Container type: " + t.getClass());
                    }
                }

            }
        }
    }

    /**
     * Run functions and assign results to variables in assignments
     * @param functions
     * @param assignments
     */
    private void runAndAssign(Collection<Node<?>> functions, Collection<VariableAssignment<?>> assignments){
        for(Node<?> function:functions) {
            String functionName = inferred.getVarNameForFunction(function);
            for(VariableAssignment v : assignments) {
                if(v.getName().equals(functionName)){
                    try {
                        v.setValue(function.evaluate().getValue());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private Collection<Terminal<?>> getTerminals(Node<?> function) {
        Collection<Terminal<?>> terminals = new HashSet<Terminal<?>>();
        List<Node<?>> worklist = new ArrayList<Node<?>>();
        worklist.add(function);
        while(!worklist.isEmpty()){
            Node<?> current = worklist.remove(worklist.size()-1);
            worklist.addAll(current.getChildren());
            if(current.getChildren().isEmpty()) {
                assert(current instanceof Terminal<?>);
                terminals.add((Terminal<?>) current);
            }
        }
        return terminals;
    }


}
