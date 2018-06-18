/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mint.inference;

import org.apache.log4j.Logger;
import mint.Configuration.Data;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.types.*;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.RemoveUseless;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.*;

/*
 * This will infer a set of classifiers (with WEKA) for a given trace. Intended for use in conjunction with EFSM 
 * inference - uses the subsequent event as an outcome label.
 */

public class BaseClassifierInference extends WekaClassifierInstantiator{
	
	private final static Logger LOGGER = Logger.getLogger(BaseClassifierInference.class.getName());
	private final static String final_name = "CLASS_NEXT";

    /**
     * TODO: Include map for single-follower classifiers, so that this can be enforced
     * in model.
     */

	protected TraceSet traces, evalSet;
	protected Map<String,Classifier> classifiers;
	protected HashMap<TraceElement,Instance> elementsToInstances;
	protected Map<Instance,TraceElement> instancesToElements;
	protected Map<String,Instances> testingData;
	protected Data inferenceAlgorithm;
	
	public HashMap<TraceElement, Instance> getElementsToInstances() {
		return elementsToInstances;
	}
	
	/**
	 * Infers the relevant data models for trace.
	 * @param trace
	 * @param algo
	 */
	public BaseClassifierInference(TraceSet trace, Data algo){
		this.traces = trace;
		this.inferenceAlgorithm = algo;
		Map<String,Instances> trainingData = new HashMap<String,Instances>();
		elementsToInstances = new HashMap<TraceElement,Instance>();
		evalSet = new TraceSet();
		testingData = new HashMap<String,Instances>();
		instancesToElements = new HashMap<Instance,TraceElement>();
		Map<String,Set<TraceElement>> functionsToTraceElements = new HashMap<String,Set<TraceElement>>();
		for (List<TraceElement> simpleTrace : trace.getPos()) {
			mapFunctionsToTraceElements(simpleTrace, functionsToTraceElements);
		}
		addTrainingData(functionsToTraceElements,trainingData);
		buildClassifiers(trainingData);
	}

	/**
	 * This infers the data models for trace. It ensures that any instances in 
	 * the evaluation eval set are not used to infer the models.
	 * @param trace
	 * @param evalSet
	 * @param algo
	 */
	public BaseClassifierInference(TraceSet trace, TraceSet evalSet, Data algo){
		this.traces = trace;
		this.evalSet = evalSet;
		this.inferenceAlgorithm = algo;
		Map<String,Instances> trainingData = new HashMap<String,Instances>();
		elementsToInstances = new HashMap<TraceElement,Instance>();
		instancesToElements = new HashMap<Instance,TraceElement>();
		testingData = new HashMap<String,Instances>();
		Map<String,Set<TraceElement>> functionsToTraceElements = new HashMap<String,Set<TraceElement>>();
		for (List<TraceElement> simpleTrace : trace.getPos()) {
			mapFunctionsToTraceElements(simpleTrace, functionsToTraceElements);
		}
		for (List<TraceElement> simpleTrace : evalSet.getPos()) {
			mapFunctionsToTraceElements(simpleTrace, functionsToTraceElements);
		}
		addTrainingData(functionsToTraceElements,trainingData);

		buildClassifiers(trainingData);
	}
	
	public Map<String,Classifier> getClassifiers(){
		return classifiers;
	}
	

	private Map<String, Instances> addTrainingData(Map<String, Set<TraceElement>> functionsToTraceElements, Map<String, Instances> trainingData) {
		Iterator<String> functionIterator = functionsToTraceElements.keySet().iterator();
		while(functionIterator.hasNext()){
			String element = functionIterator.next();
			Instances i = buildInstances(functionsToTraceElements.get(element));
			transferToTestingData(i,element);
			if(trainingData.containsKey(element)){
				Instances existing = trainingData.get(element);
				i.addAll(existing);
			}
			trainingData.put(element, i);
		}
		
		return trainingData;
	}


	private void transferToTestingData(Instances i, String element) {
		Iterator<Instance> instanceIt = i.iterator();
		Set<Instance> toBeRemoved = new HashSet<Instance>();
		while(instanceIt.hasNext()){
			Instance ins = instanceIt.next();
			TraceElement el = instancesToElements.get(ins);
			if(contains(evalSet.getPos(),el)){
				
				if(testingData.containsKey(element)){
					testingData.get(element).add(ins);
					toBeRemoved.add(ins);
				}
				else{
					int index = i.indexOf(ins);
					Instances instances = new Instances(i,index,1);
					testingData.put(element, instances);
					toBeRemoved.add(ins);
				}
				
			}
			
		}
		i.removeAll(toBeRemoved);
			
		
	}

	private boolean contains(Collection<List<TraceElement>> traceSet, TraceElement el) {
		for (List<TraceElement> simpleTrace : traceSet) {
			if(simpleTrace.contains(el))
				return true;
		}
		return false;
	}

	private void buildClassifiers(Map<String, Instances> trainingData) {
		classifiers = new HashMap<String,Classifier>();
		Iterator<String> functionIterator = trainingData.keySet().iterator();
		while(functionIterator.hasNext()){
			String function = functionIterator.next();
			Instances instances = trainingData.get(function);
			try {
				Classifier algo = makeClassifier(inferenceAlgorithm);
				algo.buildClassifier(instances);
				classifiers.put(function, algo);
			} catch (Exception e) {
				LOGGER.debug(function+" only has one possible following function, no need for classifier.");
			}
		}
	}
	

	

	public Instances buildInstances(Set<TraceElement> elementSet) {
		SimpleTraceElement first = (SimpleTraceElement)elementSet.toArray()[0];
		ArrayList<Attribute> attributes = buildAttributeList(first.getData());
		Attribute a = new Attribute(final_name,(List) null);
		attributes.add(a);
		Instances ret = new Instances(first.getName(),attributes,elementSet.size());
		Iterator<TraceElement> listIt = elementSet.iterator();
		List<TraceElement> addedElements = new ArrayList<TraceElement>();
		while(listIt.hasNext()){
			TraceElement element = listIt.next();
			if(element.getNext()!=null){
				Set<VariableAssignment<?>> vars = element.getData();
				Instance ins = new DenseInstance(vars.size()+1);
				if(element.getNext() == null)
					continue;
				convertToInstance(vars,element.getNext(),ins,attributes);
				ret.add(ins);
				addedElements.add(element);
			}
			
		}
		try {
			String range = getStringAttributeIndices(ret,false);
			if(range.equals("")){
				ret.setClassIndex(ret.numAttributes()-1);
				return ret;
			}
			
			//Filter f = createStringToWordFilter(range);
			//Instances filtered = filterStringToWordSequence(ret);
			Instances filtered = filterStringToNominal(ret, range);
			for(int i = 0; i< filtered.size();i++){
				TraceElement el = addedElements.get(i);
				assert(el!=null);
				elementsToInstances.put(el, filtered.get(i));
				instancesToElements.put(filtered.get(i), el);
			}
			return filtered;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assert(elementsToInstances.size() == elementSet.size());
		return ret;
	}

	protected Instances filterStringToNominal(Instances ret, String range)
			throws Exception {
		ret.setClassIndex(-1);
		Filter f = createStringToNominalFilter(range);
		f.setInputFormat(ret);
		Filter removeUseless = new RemoveUseless();
		removeUseless.setInputFormat(ret);
		Instances filtered = applyFilter(removeUseless,ret);
		filtered = applyFilter(f,ret);
		filtered.setClassIndex(ret.numAttributes()-1);
		return filtered;
	}

	protected Instances filterStringToWordSequence(Instances ret)
			throws Exception {
		String range = getStringAttributeIndices(ret, true);
		Filter f = createStringToWordFilter(range);
		f.setInputFormat(ret);
		Filter removeUseless = new RemoveUseless();
		removeUseless.setInputFormat(ret);
		Instances filtered = applyFilter(removeUseless,ret);
		filtered = applyFilter(f,ret);
		Filter reorder = createReorderFilter();
		reorder.setInputFormat(filtered);
		filtered = applyFilter(reorder,filtered);
		return filtered;
	}
	
	private Filter createStringToWordFilter(String range) {
		StringToWordVector filter = new StringToWordVector();
		String[] args = {"-R", range};
		try {
			filter.setOptions(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return filter;
	}
	
	private Filter createReorderFilter() {
		StringToWordVector filter = new StringToWordVector();
		String[] args = {"-R", "last-first"};
		try {
			filter.setOptions(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return filter;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ArrayList<Attribute> buildAttributeList(Collection<VariableAssignment<?>> vars) {
		ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
		for (VariableAssignment var : vars) {
			String name = var.getName();
			Attribute a = null;
			
			if(var instanceof DoubleVariableAssignment || var instanceof IntegerVariableAssignment ||
					var instanceof BooleanVariableAssignment)
				a = new Attribute(name);
			else
				 a = new Attribute(name,(List) null);

			attributeList.add(a);
		}
		return attributeList;
	}

	
	private static void convertToInstance(Set<VariableAssignment<?>> variableVals, TraceElement next, Instance ins, List<Attribute> attributes) {
		for (VariableAssignment<?> var : variableVals) {
			if(var.isNull())
				continue;
			
			if (var instanceof DoubleVariableAssignment){
				DoubleVariableAssignment dvar = (DoubleVariableAssignment)var;
				ins.setValue(findAttribute(var.getName(),attributes), dvar.getValue());
			}
			else if (var instanceof IntegerVariableAssignment){
                if(var instanceof IntegerBooleanVariableAssignment){
                    IntegerBooleanVariableAssignment ibv = (IntegerBooleanVariableAssignment) var;
                    ins.setValue(findAttribute(var.getName(),attributes), ibv.getValue());
                }
                else {
                    IntegerVariableAssignment iva = (IntegerVariableAssignment) var;
                    Attribute att = findAttribute(var.getName(), attributes);
                    ins.setValue(att, iva.getValue());
                }
			}
			else if (var instanceof StringVariableAssignment){
				StringVariableAssignment sva = (StringVariableAssignment)var;
				ins.setValue(findAttribute(var.getName(),attributes), sva.getValue());
			}
			else if(var instanceof BooleanVariableAssignment){
				BooleanVariableAssignment bva = (BooleanVariableAssignment)var;
				ins.setValue(findAttribute(var.getName(),attributes), bva.getValue() ? 1.0 : 0.0);
			
			}
		}
		ins.setValue(findAttribute(BaseClassifierInference.final_name,attributes), next.getName());
	}
	
	

	private void mapFunctionsToTraceElements(List<TraceElement> trace, Map<String, Set<TraceElement>> map) {
		for(int i = 0; i< trace.size();i++){
			TraceElement element = trace.get(i);
			String name = element.getName();
			Set<TraceElement> elements = map.get(name);
			if(elements == null){
				elements = new HashSet<TraceElement>();
				map.put(name, elements);
			}
			elements.add(element);
		}
	}

	public static double computeMargin(Classifier m, Instance instance) {
		double margin = 0.0;
		try {
			double[] distribution = m.distributionForInstance(instance);
			if (distribution.length >= 2) {
				Arrays.sort(distribution);
				double most = distribution[distribution.length - 1];
				double secondMost = distribution[distribution.length - 2];
				margin = most - secondMost;
			} else
				margin = 0D;
	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return margin;
	}

	public static Instances makeInstances(Set<Instance> ins, String name) {
		Instance first = (Instance) ins.toArray()[0];
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		for(int a = 0;a<first.numAttributes();a++){
			attributes.add(first.attribute(a)); // might be shifted
		}
		Instances ret = new Instances(name,attributes,ins.size());
		Iterator<Instance> listIt = ins.iterator();
		while(listIt.hasNext()){
			ret.add(listIt.next());
		}
		ret.setClassIndex(ret.numAttributes()-1);
		return ret;
	}
	
	

}
