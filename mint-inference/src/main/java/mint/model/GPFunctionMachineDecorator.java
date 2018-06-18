package mint.model;

import org.apache.log4j.Logger;
import mint.Configuration;
import mint.inference.evo.GPConfiguration;
import mint.inference.filter.Filter;
import mint.inference.filter.RemoveConstantsFilter;
import mint.inference.gp.Generator;
import mint.inference.gp.NodeExecutor;
import mint.inference.gp.SingleOutputGP;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.nonterminals.booleans.*;
import mint.inference.gp.tree.nonterminals.doubles.*;
import mint.inference.gp.tree.nonterminals.integer.CastIntegersOperator;
import mint.inference.gp.tree.terminals.*;
import mint.model.dfa.TransitionData;
import mint.tracedata.TraceElement;
import mint.tracedata.types.*;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

public class GPFunctionMachineDecorator extends MachineDecorator {


	protected Map<DefaultEdge,Set<Node<?>>> edgesToFunctions;
	protected final int minElements, iterations;
    protected final GPConfiguration conf;
    protected Map<Node<?>,String> expressionsToVarNames;

	private final static Logger LOGGER = Logger.getLogger(GPFunctionMachineDecorator.class.getName());


	public GPFunctionMachineDecorator(Machine decorated, int minElements, GPConfiguration conf, int iterations) {
		super(decorated);
		this.minElements = minElements;
        this.conf = conf;
		this.edgesToFunctions = new HashMap<DefaultEdge,Set<Node<?>>>();
        this.iterations = iterations;
        this.expressionsToVarNames = new HashMap<Node<?>,String>();
	}

    /**
     * Get variable name corresponding to function.
     * @param function
     * @return
     */
    public String getVarNameForFunction(Node<?> function){
        return expressionsToVarNames.get(function);
    }

    /**
     * Assign outcome of function to given variable name.
     * @param function
     * @return
     */
    public String setVarToFunction(Node<?> function, String var){
        return expressionsToVarNames.put(function, var);
    }

    /**
     * Get the inferred functions for the data attached to de.
     * @param de
     * @return
     */
    public Set<Node<?>> getFunctions(DefaultEdge de){
        return edgesToFunctions.get(de);
    }

    /**
     * Set the inferred functions for the data attached to de.
     * @param de
     * @return
     */
    public Set<Node<?>> setFunctions(DefaultEdge de, Set<Node<?>> funcs){
        return edgesToFunctions.put(de,funcs);
    }

    @Override
    public void postProcess() {
        Iterator<DefaultEdge> edgeIt = component.getAutomaton().getTransitions().iterator();
        int counter=0;
        while(edgeIt.hasNext()){
            counter ++;
            LOGGER.debug(counter + " out of "+component.getAutomaton().getTransitions().size());
            Set<Node<?>> functions = new HashSet<Node<?>>();
            DefaultEdge current = edgeIt.next();
            LOGGER.debug("current label: "+component.getAutomaton().getTransitionSource(current)+"->"+
                    component.getAutomaton().getTransitionTarget(current)+":"+
                    component.getLabel(current));
            TransitionData<Set<TraceElement>> traceData = component.getAutomaton().getTransitionData(current);

            Generator gpGenerator = makeGenerator(traceData.getPayLoad());
            Filter removeIdentical = new RemoveConstantsFilter();
            Map<String,Map<List<VariableAssignment<?>>, VariableAssignment<?>>> trainingData = getTrainingData(traceData);// removeIdentical.filter(getTrainingData(traceData));
            removeIdentical.filter(getTrainingData(traceData));
            for(String outputVar : trainingData.keySet()){
                LOGGER.debug("Variable: "+outputVar+", Training set size: "+trainingData.get(outputVar).size());
                Map<List<VariableAssignment<?>>, VariableAssignment<?>> train = trainingData.get(outputVar);
                if(train.size()<this.minElements)
                    continue;
                SingleOutputGP gp = new SingleOutputGP(gpGenerator, train,conf);

                Node<?> evolved = (Node<?>)gp.evolve(iterations);
                simplify(evolved,train);
                LOGGER.debug("Simplified: "+evolved.toString());
                expressionsToVarNames.put(evolved,outputVar);
                functions.add(evolved);
            }
            edgesToFunctions.put(current,functions);
        }

    }

    private void simplify(Node<?> evolved, Map<List<VariableAssignment<?>>, VariableAssignment<?>> train) {
        evolved.reset();
        NodeExecutor nex = new NodeExecutor(evolved);
        for(List<VariableAssignment<?>> key : train.keySet()){
            try{
                nex.execute(key);
            }
            catch(InterruptedException e){
                LOGGER.debug("Execution interrupted during simplification.");
            }
        }
        try {
            evolved.simplify();
        }
        catch(Exception e){
            LOGGER.debug("Failed during simplification...");
        }
    }

    private Generator makeGenerator(Set<TraceElement> payload) {
        Configuration config = Configuration.getInstance();
        Generator gpGenerator = new Generator(new Random(config.SEED));

        List<NonTerminal<?>> doubleNonTerms = new ArrayList<NonTerminal<?>>();
        doubleNonTerms.add(new AddDoublesOperator());
        doubleNonTerms.add(new SubtractDoublesOperator());
        doubleNonTerms.add(new MultiplyDoublesOperator());
        doubleNonTerms.add(new CastDoublesOperator());
        doubleNonTerms.add(new DivideDoublesOperator());
        doubleNonTerms.add(new PwrDoublesOperator());
        //doubleNonTerms.add(new ForWhileOperator());
        doubleNonTerms.add(new IfThenElseOperator());
        //doubleNonTerms.add(new WriteAuxOperator());
        doubleNonTerms.add(new CosDoublesOperator());
        doubleNonTerms.add(new ExpDoublesOperator());
        doubleNonTerms.add(new LogDoublesOperator());
        gpGenerator.setDoubleFunctions(doubleNonTerms);

        List<VariableTerminal<?>> doubleTerms = generateTerms(payload.iterator().next());
        //doubleTerms.add(new ReadAuxTerminal());
        gpGenerator.setDoubleTerminals(doubleTerms);

        List<NonTerminal<?>> boolNonTerms = new ArrayList<NonTerminal<?>>();
        boolNonTerms.add(new AndBooleanOperator());
        boolNonTerms.add(new OrBooleanOperator());
        boolNonTerms.add(new LTBooleanDoublesOperator());
        boolNonTerms.add(new GTBooleanDoublesOperator());
        boolNonTerms.add(new EQBooleanOperator());
        boolNonTerms.add(new EQArithOperator());
        boolNonTerms.add(new EQStringOperator());
        gpGenerator.setBooleanFunctions(boolNonTerms);

        List<NonTerminal<?>> intNonTerms = new ArrayList<NonTerminal<?>>();
        intNonTerms.add(new CastIntegersOperator());
        gpGenerator.setIntegerFunctions(intNonTerms);
        gpGenerator.setIntegerTerminals(generateIntTerms(payload.iterator().next()));



        gpGenerator.setBooleanTerminals(generateBooleanTerms(payload.iterator().next()));


        gpGenerator.setStringTerminals(generateStringTerms(payload));
        return gpGenerator;
    }

    private List<VariableTerminal<?>> generateBooleanTerms(TraceElement data) {
        List<VariableTerminal<?>> intTerms = new ArrayList<VariableTerminal<?>>();

        for(VariableAssignment<?> var : data.getData()){
            if(var.typeString().equals(":B")) {
                BooleanVariableAssignment iv = new BooleanVariableAssignment(var.getName());
                iv.setParameter(true);
                intTerms.add(new BooleanVariableAssignmentTerminal(iv, false));
            }
        }
        VariableAssignment<Boolean> truevar = new BooleanVariableAssignment("true", true);
        BooleanVariableAssignmentTerminal trueterm = new BooleanVariableAssignmentTerminal(truevar, true);
        VariableAssignment<Boolean> falsevar = new BooleanVariableAssignment("false", false);
        BooleanVariableAssignmentTerminal falseterm = new BooleanVariableAssignmentTerminal(falsevar, true);
        intTerms.add(trueterm);
        intTerms.add(falseterm);
        return intTerms;
    }

    private List<VariableTerminal<?>> generateIntTerms(TraceElement data) {
        List<VariableTerminal<?>> intTerms = new ArrayList<VariableTerminal<?>>();

        for(VariableAssignment<?> var : data.getData()){
            if(var.typeString().equals(":I")) {
                IntegerVariableAssignment iv = new IntegerVariableAssignment(var.getName());
                iv.setParameter(true);
                intTerms.add(new IntegerVariableAssignmentTerminal(iv, false));
            }
        }
        IntegerVariableAssignment dvar = new IntegerVariableAssignment("randA",-200);
        dvar.setParameter(false);
        dvar.setMax(2000);
        dvar.setMin(-2000);
        IntegerVariableAssignment dvar2 = new IntegerVariableAssignment("randB",0);
        dvar2.setParameter(false);
        dvar2.setMax(2000);
        dvar2.setMin(-200);
        IntegerVariableAssignment dvar3 = new IntegerVariableAssignment("randC",200);
        dvar3.setParameter(false);
        dvar3.setMax(2000);
        dvar3.setMin(-2000);
        //doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("0.1",0.1D), true));
        intTerms.add(new IntegerVariableAssignmentTerminal(dvar, true));
        intTerms.add(new IntegerVariableAssignmentTerminal(dvar2, true));
        intTerms.add(new IntegerVariableAssignmentTerminal(dvar3, true));
        return intTerms;
    }

    private List<VariableTerminal<?>> generateStringTerms(Set<TraceElement> next) {
        List<VariableTerminal<?>> stringTerms = new ArrayList<VariableTerminal<?>>();
        Set<String> doneVars = new HashSet<String>();
        Set<String> values = new HashSet<String>();
        values.add("null");
        for(TraceElement el : next){
            for(VariableAssignment<?> var : el.getData()){
                if(var instanceof StringVariableAssignment){
                    //extract value and add to list of seed-values
                    values.add(var.printableStringOfValue());
                    //generate parameter variable.
                    if(!doneVars.contains(var.getName())) {
                        StringVariableAssignment param = new StringVariableAssignment(var.getName());
                        param.setParameter(true);
                        stringTerms.add(new StringVariableAssignmentTerminal(param, true));
                        doneVars.add(var.getName());
                    }
                }
            }
            //do the same for following element in the trace
            TraceElement follow = el.getNext();
            if(follow!=null){
                for(VariableAssignment<?> var : follow.getData()){
                    if(var instanceof StringVariableAssignment){
                        values.add(var.printableStringOfValue());
                    }
                }
            }
        }
        //generate concrete String terminals from seed values.
        for (String value: values) {
            stringTerms.add(new StringVariableAssignmentTerminal(new StringVariableAssignment(value,value), true));
        }
        return stringTerms;
    }


    private List<VariableTerminal<?>> generateTerms(TraceElement data) {
        List<VariableTerminal<?>> doubleTerms = new ArrayList<VariableTerminal<?>>();

        if(data!=null) {
            for (VariableAssignment<?> var : data.getData()) {
                if(var.typeString().equals(":D")) {
                    DoubleVariableAssignment dvar = new DoubleVariableAssignment(var.getName());
                    dvar.setParameter(true);
                    DoubleVariableAssignmentTerminal param = new DoubleVariableAssignmentTerminal(dvar, false);
                    doubleTerms.add(param);
                }
            }
        }
        doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("0.5",0.5D), true));
        doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("0",0D), true));
        //doubleTerms.add(new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("2",2D), true));
        DoubleVariableAssignment dvar = new DoubleVariableAssignment("randA",-1D);
        dvar.setParameter(false);
        dvar.setMax(2000D);
        dvar.setMin(-2000D);
        doubleTerms.add(new DoubleVariableAssignmentTerminal(dvar, true));
        return doubleTerms;
    }

    private Map<String,Map<List<VariableAssignment<?>>, VariableAssignment<?>>> getTrainingData(TransitionData<Set<TraceElement>> traceData) {
        Map<String,Map<List<VariableAssignment<?>>, VariableAssignment<?>>> outputsToTrainingSet =
                new HashMap<String,Map<List<VariableAssignment<?>>, VariableAssignment<?>>>();
        for(TraceElement current : traceData.getPayLoad()) {
           
            TraceElement next = current.getNext();
            if(next == null)
                continue;
            for(VariableAssignment<?> output: next.getData()){
                if(output.isParameter())
                    continue;
                Map<List<VariableAssignment<?>>, VariableAssignment<?>> trainingData = obtainDataFor(output.getName(), outputsToTrainingSet);
                ArrayList<VariableAssignment<?>> training = new ArrayList<VariableAssignment<?>>();
                if(!allNull(current.getData()) && !output.isNull()) {
                    training.addAll(current.getData());
                    trainingData.put(training, output);
                }
            }
        }
        return outputsToTrainingSet;
    }

    private boolean allNull(Set<VariableAssignment<?>> data) {
        for(VariableAssignment<?> va : data){
            if(!va.isNull())
                return false;
        }
        return true;
    }

    private Map<List<VariableAssignment<?>>, VariableAssignment<?>> obtainDataFor(String name, Map<String, Map<List<VariableAssignment<?>>, VariableAssignment<?>>> outputsToTrainingSet) {
        if(outputsToTrainingSet.containsKey(name))
            return outputsToTrainingSet.get(name);
        else{
            Map<List<VariableAssignment<?>>, VariableAssignment<?>> ts = new HashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();
            outputsToTrainingSet.put(name,ts);
            return ts;
        }

    }


    public String getLabel(DefaultEdge de){
		String retEdge = null;
		Configuration configuration = Configuration.getInstance();
		if(configuration.STRATEGY != Configuration.Strategy.gktails)
			retEdge = component.getLabel(de);
		else
			retEdge = getAutomaton().getTransitionData(de).getLabel();
        //if(!retEdge.isEmpty())
          //  retEdge+="\\n";
		Set<Node<?>> function = edgesToFunctions.get(de);

		if(function==null)
			return retEdge;
        for(Node<?> n : function){
            String var = expressionsToVarNames.get(n);
            String val = n.toString();
            if(var.equals(val))
                continue; //ignore identities.
            retEdge+="\\n"+var+"="+val;
        }

		return retEdge;
	}

	


}
