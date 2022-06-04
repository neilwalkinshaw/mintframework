/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mint.model;

import org.apache.log4j.Logger;
import mint.Configuration;
import mint.inference.BaseClassifierInference;
import mint.inference.constraints.WekaConstraintParser;
import mint.inference.constraints.expression.Expression;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;
import weka.classifiers.Classifier;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;


public class WekaGuardMachineDecorator extends MachineDecorator {

	protected Map<TraceElement,Instance> elementMap;
	protected Map<String,Classifier> modelMap;
	protected boolean data;	
	protected Map<DefaultEdge,Expression> edgesToConstraints;

    private final static Logger LOGGER = Logger.getLogger(WekaGuardMachineDecorator.class.getName());

    /**
     * Get the component wrapped by this decorator. Bad design.
     * %TODO: refactor somehow.
     * @return
     */
    public Machine getWrapped(){
        return component;
    }

    public WekaGuardMachineDecorator(Machine decorated, boolean data){
		super(decorated);
		this.data = data;
		elementMap = new HashMap<TraceElement,Instance>();
		modelMap = new HashMap<String,Classifier>();
		edgesToConstraints = new HashMap<DefaultEdge,Expression>();
	}

	
	public Instance getInstance(TraceElement e){
		return elementMap.get(e);
	}


	public void setModelMap(Map<String,Classifier> map){
		this.modelMap = map;
	}
	
	public Classifier getClassifier(String label){
		try{
			Classifier c =  modelMap.get(label);
			return c;
		}
		catch(NullPointerException e){
			return null;
		}
	}

	
	
	/**
	 *Return label corresponding to edge - this includes any constraints (if the classifiers are J48 and JRIP).
	 */
	public String getLabel(DefaultEdge t) {
		Expression c = edgesToConstraints.get(t);
		String constraint = "";
		if(c!=null)
			constraint = c.toString();
		if(constraint == null || constraint.isEmpty())
			return component.getLabel(t);
		else{
			return component.getLabel(t) + lineBreak() + constraint;
		}
	}
	
	public Expression getConstraint(DefaultEdge ed){
		return edgesToConstraints.get(ed);
	}

	private static String lineBreak() {
		Configuration configuration = Configuration.getInstance();
		if(configuration.VIS==Configuration.Visualise.graphical)
			return "\n";
		else return "\\n";
	}

	
	
	

	
	/*
	 * If any predictions are inconsistent, the merge will not go ahead.
	 */
	public boolean compatible(DefaultEdge transitionA, DefaultEdge transitionB) {
		if(!component.compatible(transitionA, transitionB))
			return false;
		if(!data)
			return true;
		Set<Instance> insA = getInstancesForTransition(transitionA);
		Set<Instance> insB = getInstancesForTransition(transitionB);
		String label = getAutomaton().getTransitionData(transitionA).getLabel();
		boolean retVal = compatible(insA, insB, label);
		return retVal;
	}


    public boolean compatible(Set<Instance> ins, Set<Instance> ins2,
							  String label) {
        boolean retVal = false;
        if(ins.isEmpty() || ins2.isEmpty())
            retVal =  true;
        else
            retVal = checkTransitionCompatibility(label, ins, ins2);
        return retVal;
    }

    /*
         * Two transitions are compatible if the instances attached to the transitions lead to the same sets of classes.
         */
    protected boolean checkTransitionCompatibility(String label,
												   Set<Instance> ins, Set<Instance> ins2) {
        Classifier c = getClassifier(label);
        if(c == null)
            return true;
        if(ins.isEmpty() || ins2.isEmpty())
            return true;
        Instances instancesA = BaseClassifierInference.makeInstances(ins, label);
        Instances instancesB = BaseClassifierInference.makeInstances(ins2,label);
        try {
            Set<Integer> predictionsA = getPredictions(c,instancesA);
            Set<Integer> predictionsB = getPredictions(c,instancesB);

            //predictionsA.retainAll(predictionsB);
            //return !predictionsA.isEmpty();
            return predictionsA.containsAll(predictionsB) || predictionsB.containsAll(predictionsA);
            //return predictionsA.equals(predictionsB);

        } catch (Exception e) {
            LOGGER.error("Error with classifier when comparing pairs of transitions.");
        }
        return false;
    }

    private Set<Integer> getPredictions(Classifier c, Instances i) throws Exception {
        Set<Integer> predictions = new HashSet<Integer>();
        Iterator<Instance> instanceIt = i.iterator();
        while(instanceIt.hasNext()){
            int pred = (int) c.classifyInstance(instanceIt.next());
            predictions.add(pred);

        }
        return predictions;
    }
	
	
	
	public Set<Instance> getInstancesForTransition(DefaultEdge e){
		Set<Instance> ins2 = new HashSet<Instance>();
		TransitionData<Set<TraceElement>> eData = component.getAutomaton().getTransitionData(e);
		for (TraceElement simpleTraceElement : eData.getPayLoad()) {
			Instance i = getInstance(simpleTraceElement);
			if(i!=null)
				ins2.add(i);
		}
		return ins2;
	}


	
	
	
	public String modelStrings(){
		String models = new String();
		Iterator<String> modelIt = modelMap.keySet().iterator();
		while(modelIt.hasNext()){
			String title = modelIt.next();
			models = models + "\n=========================== MODEL FOR:"+title+" =========================== \n";
			models = models + modelMap.get(title).toString();
		}
		return models;
	}

	

	public void setElementsToInstances(
			Map<TraceElement, Instance> elementsToInstances) {
		this.elementMap = elementsToInstances;
		
	}

	@Override
	public void postProcess() {
		super.postProcess();
		for(DefaultEdge de:getAutomaton().getTransitions()){
			Expression l = null;
			TransitionData<Set<TraceElement>> tData =component.getAutomaton().getTransitionData(de);
			Set<TraceElement> payload = tData.getPayLoad();
			for (TraceElement traceElement : payload) {
				Classifier c = getClassifier(traceElement.getName());
				if(c == null)
					continue;
				Instance i = getInstance(traceElement);
				if(i == null)
					continue;
				try {
					double d = c.classifyInstance(i);
					String label = i.classAttribute().value((int)d);
					if (c instanceof J48){
						l = WekaConstraintParser.parseJ48Expression(c.toString(), label, traceElement.getData());
					}
					else if(c instanceof JRip){
						l = WekaConstraintParser.parseJRIPExpression(c.toString(), label, traceElement.getData());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
			edgesToConstraints.put(de, l);
		}
	}


	public Map<TraceElement, Instance> getElementMap() {
		return elementMap;
	}


	public Map<String, Classifier> getModelMap() {
		return modelMap;
	}

    @Override
    public Collection<DefaultEdge> findCompatible(
			Set<DefaultEdge> possibleTransitions, TraceElement element) {
        Set<DefaultEdge> poss = new HashSet<DefaultEdge>();
        if(possibleTransitions.isEmpty())
            return poss;
        Classifier classifier = getClassifier(element.getName());
        Instance ins = getInstance(element);
        if(classifier!=null && ins !=null){ //only return transitions where attached data produces same predictions as element
            buildSuccessors(possibleTransitions, classifier, ins, poss);
        }
        else
            poss.addAll(possibleTransitions);
        return poss;
    }


    /**
     * compare outputs on classifier from instances attached to transitions to the supplied instance
     */
    protected void buildSuccessors(Set<DefaultEdge> possibleTrans,
                                   Classifier classifier, Instance ins, Collection<DefaultEdge> poss) {
        try {
            TraceDFA automaton = getAutomaton();
            String required = extractPredictedClass(classifier, ins);
            for(DefaultEdge t: possibleTrans){
                Set<String> provided = new HashSet<String>();
                TransitionData<Set<TraceElement>> traceData = automaton.getTransitionData(t);
                for(TraceElement element: traceData.getPayLoad()){
                    Instance j = getInstance(element);
                    if(j == null)
                        continue;
                    provided.add(extractPredictedClass(classifier, j));
                }
                if(provided.isEmpty() || provided.contains(required))
                    poss.add(t);
            }
        } catch (Exception e) {
            LOGGER.error("Error when trying to build successors: "+e.toString());
        }
    }

    /**
     * Extract the class predicted by classifier for instance.
     * @param classifier
     * @param ins
     * @return
     * @throws Exception
     */
    protected String extractPredictedClass(Classifier classifier, Instance ins)
            throws Exception {
        double d = classifier.classifyInstance(ins);
        return ins.dataset().classAttribute().value((int)d);
    }

    /**
     * Is trace element td compatible with transitionB?
     * @param td
     * @param transitionB
     * @return
     */
    public boolean compatible(TraceElement td,
                              DefaultEdge transitionB) {
        assert(getAutomaton().consistentTransitions());
        assert(getAutomaton().getTransitions().contains(transitionB));
        //if(!compatible(td, transitionB))
        //    return false;
        return checkCompatible(td, transitionB);
    }

    /**
     * is the data attached to aData compatible with transitionB?
     * @param aData
     * @param transitionB
     * @return
     */
    protected boolean checkCompatible(TraceElement aData,
                                      DefaultEdge transitionB) {
        TransitionData<Set<TraceElement>> bData = getAutomaton().getTransitionData(transitionB);
        Set<Instance> ins = new HashSet<Instance>();
        Instance i = getInstance(aData);
        if(i!=null)
            ins.add(i);
        Set<Instance> ins2 = getInstancesForTransition(transitionB);
        String label = bData.getLabel();
        boolean retVal = compatible(ins, ins2, label);
        return retVal;
    }



}
