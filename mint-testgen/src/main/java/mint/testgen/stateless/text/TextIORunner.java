package mint.testgen.stateless.text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;

import mint.Configuration;
import mint.inference.text.SingleInputNumericalOutputLearner;
import mint.testgen.stateless.runners.execution.TestRunner;
import mint.tracedata.TestIO;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;
import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;

/**
 * Vectorise input files with doc2vec.
 *
 * Created by neilwalkinshaw on 04/06/2018.
 */
public class TextIORunner extends TestRunner {

	protected final String pool;

	protected SingleInputNumericalOutputLearner sino;

	protected Map<TestIO, TestIO> testSet;
	protected List<TestIO> orderedTestSet;

	protected boolean randomTests = false;

	/**
	 * @param setupFile
	 * @param testPlan
	 */
	public TextIORunner(String setupFile, String testPlan, String pool) {
		super(setupFile, testPlan);
		minTests = 0;
		maxTests = 10000000;
		this.pool = pool;
		sino = new SingleInputNumericalOutputLearner(true);
		sino.setTokenizerChoice(SingleInputNumericalOutputLearner.TokenizerChoice.NGram);
		sino.setClassifierChoice(SingleInputNumericalOutputLearner.ClassifierChoice.GaussianProcess);
	}

	public void setRandomTests(boolean randomTests) {
		this.randomTests = randomTests;
	}

	public void recordPerformance() {
		sino.setRecordFitness(true);
	}

	@Override
	public String getLabel() {
		return "TextInput";
	}

	@Override
	public List<TestIO> generateTests() {
		assert (testInputs != null);
		Classifier testModel = inferModel();
		TextIOGenerator generator = new TextIOGenerator(command, params, Configuration.getInstance().ITERATIONS,
				testModel, orderedTestSet, sino);
		generator.setRandom(randomTests);
		return generator.generateTestCases();
	}

	/**
	 * For this, testPlan is taken to be a directory with test files in it (not a
	 * file with inputs on a per-line basis).
	 *
	 * @param testPlan
	 * @return
	 */
	@Override
	public List<TestIO> readTestInputs(String testPlan) {

		List<TestIO> plan = new ArrayList<TestIO>();

		Map<String, String> tests = readTextFiles(new File(testPlan));
		for (String s : tests.keySet()) {
			String input = tests.get(s);
			StringVariableAssignment inputString = new StringVariableAssignment(s, input);
			List<VariableAssignment<?>> vars = new ArrayList<>();
			vars.add(inputString);
			TestIO inputIO = new TestIO(command, vars);
			plan.add(inputIO);
		}
		return plan;
	}

	protected static Map<String, String> readTextFiles(File directory) {

		Map<String, String> data = new HashMap<>();

		File[] listOfFiles = directory.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			File file = listOfFiles[i];
			if (file.isFile()) {
				try {
					String content = FileUtils.readFileToString(file);
					String label = file.getParent() + File.separator + file.getName();
					data.put(label, content);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return data;
	}

	@Override
	public void run(Collection<TestIO> testPlan) {
		assert (testPlan != null);
		for (TestIO test : testPlan) {
			List<String> commands = new ArrayList<String>();
			String name = test.getName();
			StringTokenizer st = new StringTokenizer(name);
			while (st.hasMoreElements()) {
				commands.add(st.nextToken());
			}
			for (VariableAssignment<?> assignment : test.getVals()) {
				commands.add(assignment.getName().toString());

			}

			execute(test, commands, null);
		}
	}

	@Override
	public void run() {
		super.run();
		if (sino.isRecordFitness()) {
			List<List<Double>> fitnessHistory = sino.getFitnessHistory();
			writeToCSV(fitnessHistory);
		}
	}

	/**
	 * Write fitness history from inference to csv file. Currently name of csv is
	 * hard-coded. TODO change this.
	 * 
	 * @param fitnessHistory
	 */
	private void writeToCSV(List<List<Double>> fitnessHistory) {
		try {
			String label = getLabel();
			if (randomTests)
				label += "R";
			PrintWriter out = new PrintWriter(label);
			String toWrite = "";
			for (List<Double> scores : fitnessHistory) {
				for (int i = 0; i < scores.size(); i++) {
					toWrite += scores.get(i);
					if (i < scores.size() - 1)
						toWrite += ",";
					else
						toWrite += "\n";
				}
			}
			out.println(toWrite);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private Map<TestIO, TestIO> buildTrainingSetWithoutOutputs(List<TestIO> candidates) {
		Map<TestIO, TestIO> ret = new HashMap<>();
		orderedTestSet = new ArrayList<>();
		for (TestIO key : candidates) {
			ret.put(key, null);
			orderedTestSet.add(key);
		}
		return ret;
	}

	private FilteredClassifier inferModel() {

		Map<TestIO, TestIO> trainingSet = new HashMap<>();
		for (int i = 0; i < testInputs.size(); i++) {
			trainingSet.put(testInputs.get(i), testOutputs.get(i));
		}

		testSet = buildTrainingSetWithoutOutputs(readTestInputs(pool));

		sino.train(trainingSet, testSet);
		FilteredClassifier fc = (FilteredClassifier) sino.getWekaModel();

		return fc;
	}

}
