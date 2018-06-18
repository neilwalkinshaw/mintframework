package mint.testgen.stateless.weka;

import mint.Configuration;
import mint.tracedata.TestIO;
import mint.tracedata.types.VariableAssignment;
import org.apache.log4j.Logger;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

/**
 * Generates tests based on an estimate of uncertainty of the model.
 * 
 * When a new input is required, this class generates a pool of random solutions,
 * the size of this pool is set by the <code>candidatePoolSize</code>. From this pool,
 * the test with the highest uncertainty is chosen.
 * 
 * @author neilwalkinshaw
 *
 */
public class UncertaintySamplingTestGenerator extends WekaModelTestGenerator {

	protected final Classifier classifier;
	protected final int candidatePool;
	protected final Random random;
	protected final Instances instances;
	
	private final static Logger LOGGER = Logger.getLogger(UncertaintySamplingTestGenerator.class.getName());

	
	/**
	 * 
	 * @param name 
	 * @param types the types of the inputs to the program
	 * @param c the Weka classifier
	 * @param candidatePoolSize the number of random tests to generate
	 * @param ins observations (inputs and outputs) so far 
	 */
	public UncertaintySamplingTestGenerator(String name,
			Collection<VariableAssignment<?>> types, Classifier c, int candidatePoolSize, Instances ins) {
		super(name, types, c);
		Configuration conf = Configuration.getInstance();
		classifier = c;
		candidatePool = candidatePoolSize;
		random = new Random(conf.SEED);
		instances = ins;
	}

	@Override
	public List<TestIO> generateTestCases(int howMany) {
		List<TestIO> finalSet = new ArrayList<TestIO>();
		for(int i = 0; i< howMany; i++){
			List<TestIO> candidates = new ArrayList<TestIO>();
			for(int j = 0; j<candidatePool; j++){
				candidates.add(generateRandomTestIO());
			}
			finalSet.add(pickLeastCertain(candidates));
		}
		return finalSet;
	}

	/**
	 * Find the candidate test case with the lowest certainty according to the current model.
	 * @param candidates a pool of test cases
	 */
	private TestIO pickLeastCertain(List<TestIO> candidates) {
		LOGGER.debug("Picking least certain");
		Map<Instance,TestIO> instanceToTestCase = new HashMap<Instance,TestIO>();
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		for(int i = 0; i< instances.numAttributes(); i++){
			attributes.add(instances.attribute(i));
		}
		Instances candInstances = new Instances("testIO",(ArrayList<Attribute>) attributes,candidates.size());
		for(int i = 0; i< candidates.size(); i++ ){
			TestIO inputs = candidates.get(i);
			List<VariableAssignment<?>> params = new ArrayList<VariableAssignment<?>>();
			params.addAll(inputs.getVals());
			
			Instance ins = new DenseInstance(instances.numAttributes());
			InputOutputClassiferInference.convertToInstance(params,ins,attributes);
			
			candInstances.add(ins);
			instanceToTestCase.put(candInstances.get(i), inputs);

		}
		candInstances.setClassIndex(candInstances.numAttributes()-1);
		Instance leastCertain = candInstances.firstInstance();
		double lowestMargin = computeMargin(classifier,leastCertain);
		for(int i = 1; i< candInstances.numInstances();i++){
			Instance current = candInstances.get(i);
			assert(instanceToTestCase.containsKey(current));
			double currentMargin = computeMargin(classifier,current);
			if(currentMargin < lowestMargin){
				lowestMargin = currentMargin;
				leastCertain = current;
			}
			if(currentMargin == 0D)
				break; //can't go any lower, so not worth continuing with the loop.
		}
		TestIO ret = instanceToTestCase.get(leastCertain);
		LOGGER.debug("Selected input with certainty score of "+(1-lowestMargin));
		assert(ret!=null);
		return ret;
	}

	@Override
	public List<TestIO> generateTestCases() {
		LOGGER.debug("Generating random tests");
		return generateTestCases(random.nextInt(10)+1);
	}
	
	/**
	 * Compute the "margin" of a test instance.
	 * Calculates the maximum difference between the two (predicted) most likely labels for the instance.
	 * If there is only one class, then the method returns 0.
	 */
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


}
