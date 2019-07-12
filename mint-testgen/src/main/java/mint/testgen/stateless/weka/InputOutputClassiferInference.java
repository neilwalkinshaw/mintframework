package mint.testgen.stateless.weka;

import mint.Configuration;
import mint.inference.BaseClassifierInference;
import mint.tracedata.TestIO;
import mint.tracedata.types.*;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a Weka classifier and its associated configuration and training set.
 * @author neilwalkinshaw
 *
 */
public class InputOutputClassiferInference extends WekaClassifierInstantiator{
	
	protected Classifier classifier;
	protected List<Attribute> attributes;
	protected Instances trainingSet;
	protected Configuration.Data algo;

	/**
	 * Build a training set from observed input, output events and create a classifier for it.
	 * @param testInputs a List observed inputs.
     * @param testOutputs a List of observed outputs corresponding to testInputs.
	 * @param algorithm the Weka classifier to use
	 */
	public InputOutputClassiferInference(List<TestIO> testInputs, List<TestIO> testOutputs, Configuration.Data algorithm) throws Exception {
		this.algo = algorithm;
		buildInput(testInputs, testOutputs);

        String range = getStringAttributeIndices(trainingSet,false);
        if(range.equals("")){
            trainingSet.setClassIndex(trainingSet.numAttributes()-1);
        }
        TestIO output = testOutputs.iterator().next();
        VariableAssignment<?> val = output.getVals().get(0);


        if(val instanceof StringVariableAssignment){
            Filter stringToNominal = createStringToNominalFilter(range);
            stringToNominal.setInputFormat(trainingSet);
            trainingSet = Filter.useFilter(trainingSet, stringToNominal);

        }
        /*//else {


            PKIDiscretize discretize = new PKIDiscretize();
            discretize.setOptions(new String[]{"-O"});
            discretize.setAttributeIndicesArray(new int[]{trainingSet.numAttributes()-1});
            discretize.setIgnoreClass(true);
            discretize.setInputFormat(trainingSet);
            trainingSet = Filter.useFilter(trainingSet, discretize);

        //}*/
        classifier = makeClassifier(algo);
        classifier.buildClassifier(trainingSet);

	}
	
	
	public Instances getInstances(){
		return trainingSet;
	}
	
	public List<Attribute> getAttributes(){
		return attributes;
	}
	
	public void buildInput(List<TestIO> inputs, List<TestIO> outputs){
		TestIO first = inputs.iterator().next();
		TestIO out = outputs.iterator().next();
		
		List<VariableAssignment<?>> params = new ArrayList<VariableAssignment<?>>();
		params.addAll(first.getVals());
		params.addAll(out.getVals());
		attributes = BaseClassifierInference.buildAttributeList(params);
		
		trainingSet = buildInstances(attributes,inputs, outputs);
	}
	

	public Classifier getClassifier(){
		return classifier;
	}
	
	public static Instances buildInstances(List<Attribute> attributes, List<TestIO> inputs, List<TestIO> outputs) {
		Iterator<TestIO> inputIt = inputs.iterator();
		Instances ret = new Instances("testIO",(ArrayList<Attribute>) attributes,inputs.size());
        int counter = 0;
		while(inputIt.hasNext()){
			TestIO input = inputIt.next();
			TestIO output= outputs.get(counter);
			List<VariableAssignment<?>> params = new ArrayList<VariableAssignment<?>>();
			params.addAll(input.getVals());
			params.addAll(output.getVals());
			Instance ins = new DenseInstance(params.size());
			
			convertToInstance(params,ins,attributes);
			ret.add(ins);
            counter++;
		}
		ret.setClassIndex(ret.numAttributes()-1);
		return ret;
	}
	
	public static void convertToInstance(Collection<VariableAssignment<?>> variableVals, Instance ins, List<Attribute> attributes) {
		for (VariableAssignment<?> var : variableVals) {
			if(var.isNull())
				continue;
			
			if (var instanceof DoubleVariableAssignment){
				DoubleVariableAssignment dvar = (DoubleVariableAssignment)var;
				ins.setValue(BaseClassifierInference.findAttribute(var.getName(),attributes), dvar.getValue());
			}
			else if (var instanceof StringVariableAssignment){
				StringVariableAssignment sva = (StringVariableAssignment)var;
				ins.setValue(BaseClassifierInference.findAttribute(var.getName(),attributes), sva.getValue());
			}
			else if (var instanceof IntegerVariableAssignment){
				IntegerVariableAssignment sva = (IntegerVariableAssignment)var;
				ins.setValue(BaseClassifierInference.findAttribute(var.getName(),attributes), sva.getValue());
			}
			else if(var instanceof BooleanVariableAssignment){
				BooleanVariableAssignment bva = (BooleanVariableAssignment)var;
				ins.setValue(BaseClassifierInference.findAttribute(var.getName(),attributes), bva.getValue() ? 1.0 : 0.0);
			
			}

		}
	}

	

}
