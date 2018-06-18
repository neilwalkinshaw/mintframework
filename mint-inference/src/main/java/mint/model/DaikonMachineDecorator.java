package mint.model;

import com.microsoft.z3.Z3Exception;
import daikon.FileIO;
import daikon.PptMap;
import daikon.inv.Invariant;
import mint.Configuration;
import mint.inference.constraints.InvariantsToZ3Constraints;
import mint.model.dfa.TransitionData;
import mint.tracedata.TraceElement;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.VariableAssignment;
import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class DaikonMachineDecorator extends MachineDecorator {
	
	protected Map<DefaultEdge,String> edgesToDecls;
	protected Map<DefaultEdge,String> edgesToDTraces;
	protected Map<DefaultEdge,Set<Invariant>> edgesToInvariants;
    protected Map<String,List<VariableAssignment<?>>> config;
	protected boolean postProcess;
	protected final int minElementsForDaikon;
	protected Set<DefaultEdge> newEdges;
	
	private final static Logger LOGGER = Logger.getLogger(DaikonMachineDecorator.class.getName());
	
	/*
	 * Writes a declaration file, where each transition corresponds to a unique declaration point.
	 */
	
	public DaikonMachineDecorator(Machine decorated, int minElements, boolean postProcess) {
		super(decorated);
		this.postProcess = postProcess;
		minElementsForDaikon = minElements;
		edgesToDecls = new HashMap<DefaultEdge,String>();
		edgesToDTraces = new HashMap<DefaultEdge,String>();
		edgesToInvariants = new HashMap<DefaultEdge,Set<Invariant>>();
        config = new HashMap<String,List<VariableAssignment<?>>>();
		newEdges = new HashSet<DefaultEdge>();
		newEdges.addAll(component.getAutomaton().getTransitions());
		computeDTraceFile();
	}

	private void computeDTraceFile() {
		Iterator<DefaultEdge> deIt = newEdges.iterator();
		LOGGER.debug(newEdges.size()+" NEW TRANSITIONS");
		buildDtrace(deIt);
	}

	private void buildDtrace(Iterator<DefaultEdge> deIt) {
		while(deIt.hasNext()){
			DefaultEdge te = deIt.next();
			TransitionData<Set<TraceElement>> td = component.getAutomaton().getTransitionData(te);
			if(td.getPayLoad().size()<minElementsForDaikon)
				continue;
			computeForEdge(te);
		}
		writeTraceFile();
	}
	
	private void computeDTraceFileFromAutomaton() {
		Iterator<DefaultEdge> deIt = getAutomaton().getTransitions().iterator();
		buildDtrace(deIt);
	}


	private void writeTraceFile() {
        PrintWriter declOut = null;
        PrintWriter dtraceOut = null;
        try {
			declOut = new PrintWriter(new FileWriter("model.decls"));
			Iterator<String> declIt = edgesToDecls.values().iterator();
			declOut.append("decl-version 2.0\n");
			while(declIt.hasNext()){
				String decl = declIt.next();
				declOut.append(decl);
			}
			dtraceOut = new PrintWriter(new FileWriter("model.dtrace"));
			Iterator<String> dTraceIt = edgesToDTraces.values().iterator();
			while(dTraceIt.hasNext()){
				String dtrace = dTraceIt.next();
                dtraceOut.append(dtrace);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        finally{
            try {
                declOut.close();
                dtraceOut.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
		
		
	}
	
	/*
	 * Should return a unique identifier for an edge.  
	 */
	protected String getName(String label, DefaultEdge te){
		return label+"."+component.getAutomaton().getTransitionSource(te)+"."+
				component.getAutomaton().getTransitionTarget(te)+te.hashCode()+":::OBJECT";
	}

	protected void computeForEdge(DefaultEdge te) {
		TransitionData<Set<TraceElement>> td = component.getAutomaton().getTransitionData(te);
		String name = getName(td.getLabel(),te);
        Iterator<TraceElement> teIt = td.getPayLoad().iterator();
        edgesToDecls.put(te, getDecl(name, td));
        while(teIt.hasNext()) {
            TraceElement tel = teIt.next();
            if(edgesToDTraces.get(te) == null)
                edgesToDTraces.put(te, getDTrace(name, tel));
            else{
                String existing = edgesToDTraces.get(te);
                existing = existing + "\n"+getDTrace(name,tel);
                edgesToDTraces.put(te,existing);
            }
        }
	}

	private String getDTrace(String name, TraceElement te) {
		StringBuffer dtrace = new StringBuffer();

        dtrace.append(name + "\n");
        Set<VariableAssignment<?>> vars = te.getData();
        List<VariableAssignment<?>> decls = config.get(name);
        for (VariableAssignment<?> dec : decls) {
            VariableAssignment<?> var = findInVars(dec.getName(),vars);
            dtrace.append(var.getName() + "\n");
            if (var instanceof BooleanVariableAssignment)
                dtrace.append((Boolean) var.getValue() + "\n1\n");
            else if (var instanceof DoubleVariableAssignment)
                dtrace.append((Double) var.getValue() + "\n1\n");
            else dtrace.append("\"" + (String) var.getValue() + "\"\n1\n");

        }
        dtrace.append("\n");

		return dtrace.toString();
	}

    private VariableAssignment<?> findInVars(String name, Set<VariableAssignment<?>> vars) {
        for(VariableAssignment<?> var : vars){
            if(var.getName().equals(name))
                return var;
        }
        return null;
    }

    public Set<Invariant> getInvariants(DefaultEdge e){
		return edgesToInvariants.get(e);
	}

	private String getDecl(String name, TransitionData<Set<TraceElement>> data) {
		StringBuffer decl = new StringBuffer();
        TraceElement te = data.getPayLoad().iterator().next();
		decl.append("ppt "+ name+"\nppt-type object\n");
		List<VariableAssignment<?>> vars = new ArrayList<VariableAssignment<?>>();
        vars.addAll(te.getData());
        config.put(name,vars);
		for (VariableAssignment<?> var : vars) {
			decl.append("variable " + var.getName()+"\nvar-kind variable\n");
			
			if(var instanceof BooleanVariableAssignment)
				decl.append("dec-type boolean\nrep-type boolean\ncomparability -1\n");
			else if(var instanceof DoubleVariableAssignment)
				decl.append("dec-type double\nrep-type double\ncomparability -1\n");
			else decl.append("dec-type java.lang.String\nrep-type java.lang.String\ncomparability -1\n");
			
		}
		decl.append("\n");
		return decl.toString();
	}

	public DefaultEdge  mergeTransitions(Integer source, DefaultEdge a,
										 DefaultEdge b){

		edgesToDecls.remove(a);
		edgesToDecls.remove(b);
		edgesToDTraces.remove(a);
		edgesToDTraces.remove(b);
		edgesToInvariants.remove(a);
		edgesToInvariants.remove(b);
		DefaultEdge merged = super.mergeTransitions(source, a, b);
		//if(!postProcess)
			//recomputeDtrace(merged);
		newEdges.add(merged);
		return merged;
	}
	
	public void postProcessMerge(){
		edgesToDTraces.clear();
		edgesToDecls.clear();
		computeDTraceFile();
		if(!newEdges.isEmpty())
			computeInvariants(false);
		newEdges.clear();
	}
	
	public void postProcess(){
		computeDTraceFileFromAutomaton();
		computeInvariants(true);
	}
	
	
	public boolean compatible(DefaultEdge transitionA, DefaultEdge transitionB) {
		Configuration configuration = Configuration.getInstance();
		if(configuration.STRATEGY != Configuration.Strategy.gktails)
			return component.compatible(transitionA, transitionB);
		TransitionData<Set<TraceElement>> aData = component.getAutomaton().getTransitionData(transitionA);
		TransitionData<Set<TraceElement>> bData = component.getAutomaton().getTransitionData(transitionB);

		if(!aData.getLabel().equals(bData.getLabel()))
			return false;
		return true;
		
	}
	
	public boolean constraintCompatible(DefaultEdge transitionA, DefaultEdge transitionB) {
		
		if(getInvariants(transitionA) == null||getInvariants(transitionB) == null)
			return true;
		InvariantsToZ3Constraints ic;
		try {
			ic = new InvariantsToZ3Constraints();
			Iterator<Invariant> invIt = getInvariants(transitionA).iterator();
			while(invIt.hasNext()){
				Invariant cur = invIt.next();
				ic.addInvariant(cur);
			}
			invIt = getInvariants(transitionB).iterator();
			while(invIt.hasNext()){
				Invariant cur = invIt.next();
				ic.addInvariant(cur);
			}
			if(! ic.solve()){
				return false;
			}
		} catch (Z3Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public boolean compatible(TraceElement td,
			DefaultEdge transitionB) {
		if(!component.compatible(td, transitionB))
			return false;
		return checkCompatible(td, transitionB);
	}
	
	
	private boolean checkCompatible(TraceElement td, DefaultEdge transitionB) {
		
		InvariantsToZ3Constraints ic;
		try {
			ic = new InvariantsToZ3Constraints();
			Iterator<Invariant> invIt = getInvariants(transitionB).iterator();
			while(invIt.hasNext()){
				Invariant cur = invIt.next();
				ic.addInvariant(cur);
			}
			Set<VariableAssignment<?>> vars = td.getData();
			for (VariableAssignment<?> var : vars) {
				ic.addVariableAssignment(var);
			}
			if(!ic.solve()){
				return false;
			}
		} catch (Z3Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	protected Set<DefaultEdge> elementsForEdge(DefaultEdge e, Map<DefaultEdge,Set<DefaultEdge>> map){
		Set<DefaultEdge> retSet = new HashSet<DefaultEdge>();
		if(map.containsKey(e)){
			retSet.addAll(map.get(e));
		}
		return retSet;
	}
	
	public String getLabel(DefaultEdge de){
		String retEdge = null;
		Configuration configuration = Configuration.getInstance();
		if(configuration.STRATEGY != Configuration.Strategy.gktails)
			retEdge = component.getLabel(de)+"\\n";
		else
			retEdge = getAutomaton().getTransitionData(de).getLabel()+"\\n";
		Set<Invariant> invariants = edgesToInvariants.get(de);
		if(invariants==null)
			return retEdge;
		Set<String> invLabels = new HashSet<String>();
		Iterator<Invariant> invIt = invariants.iterator();
		//Lots of duplicate invariants - want to have only unique labels - use set to eliminate duplicates.
		while(invIt.hasNext()){
			Invariant inv = invIt.next();
			invLabels.add("("+inv.format()+")\\n");
		}
		Iterator<String> labelIt = invLabels.iterator();
		StringBuffer buf = new StringBuffer();
        while(labelIt.hasNext()){
            buf.append(labelIt.next());

		}
        retEdge+=buf.toString();
		return retEdge;
	}

	
	
	
	
	protected void computeInvariants(boolean finished){
		if(finished){
			LOGGER.debug("Running daikon without restrictions.");
			daikon.Daikon.main(new String[]{"model.decls","model.dtrace","--nohierarchy","--no_text_output","--no_show_progress","--noversion"});
		}
		else
			daikon.Daikon.main(new String[]{"model.decls","model.dtrace","--nohierarchy","--no_text_output","--no_show_progress","--noversion"});
		try {
			PptMap ppts = FileIO.read_serialized_pptmap(new File("model.inv.gz"),
			        true // use saved config
			        );
			Iterator<DefaultEdge> edgeIt = component.getAutomaton().getTransitions().iterator();
			
			while(edgeIt.hasNext()){
				DefaultEdge de = edgeIt.next();
				TransitionData<Set<TraceElement>> data = component.getAutomaton().getTransitionData(de);
				String pptLabel = getName(data.getLabel(),de);
				Set<Invariant> invariants = edgesToInvariants.get(de);
				if(invariants == null)
					invariants = new HashSet<Invariant>();
				if(!ppts.containsName(pptLabel))
					continue;
				Iterator<Invariant> invIt = ppts.get(pptLabel).invariants_iterator();
				while(invIt.hasNext()){
					Invariant inv = invIt.next();
					if(!inv.isWorthPrinting())
						continue;
					invariants.add(inv);
				}
				edgesToInvariants.put(de, invariants);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
