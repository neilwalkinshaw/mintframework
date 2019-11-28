package mint.inference.text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;

/**
 * Created by neilwalkinshaw on 25/06/2018.
 */
public class SingleInputNumericalOutputLearnerPDBTest {

	final static Logger LOGGER = Logger.getLogger(SingleInputNumericalOutputLearnerPDBTest.class.getName());

	/*
	 * @Test public void testWithPDBData(){ BasicConfigurator.resetConfiguration();
	 * BasicConfigurator.configure(); Logger.getRootLogger().setLevel(Level.ALL);
	 * List<String> compounds = getCompounds(); List<String> trainingSet = new
	 * ArrayList<>(); List<String> testSet = new ArrayList<>(); try { for (int i =
	 * 0; i < 40; i++) {
	 * 
	 * String compound = compounds.get(i); LOGGER.info("reading "+compound); String
	 * protein = new Scanner(new
	 * URL("http://files.rcsb.org/view/"+compound+".cif").openStream(),
	 * "UTF-8").useDelimiter("\\A").next(); trainingSet.add(protein); } }catch
	 * (IOException e) { e.printStackTrace(); }
	 * 
	 * Map<TestIO,TestIO> training = runInputs(trainingSet); Map<TestIO,TestIO>
	 * testing = runInputs(testSet); LOGGER.info("Learning");
	 * SingleInputNumericalOutputLearner sino = new
	 * SingleInputNumericalOutputLearner(false); sino.train(training,null);
	 * 
	 * try { Evaluation eval = new Evaluation(sino.buildDataSet(training,null));
	 * //eval.crossValidateModel(sino.getWekaModel(),
	 * SingleInputNumericalOutputLearner.buildDataSet(testing,sino.getTextModel()),4
	 * ,new Random(2));
	 * eval.crossValidateModel(sino.getWekaModel(),sino.trainInstances,10,new
	 * Random(0)); System.out.println(eval.correlationCoefficient()); } catch
	 * (Exception e) { e.printStackTrace(); } }
	 * 
	 * private Map<TestIO,TestIO> runInputs(List<String> inputs){ Map<TestIO,TestIO>
	 * runs = new HashMap<TestIO,TestIO>(); int counter = 0; for(String s : inputs){
	 * 
	 * double numAtoms = BioJavaSUT.countAtoms(s); StringVariableAssignment
	 * inputString = new StringVariableAssignment(Integer.toString(counter),s);
	 * List<VariableAssignment<?>> vars = new ArrayList<>(); vars.add(inputString);
	 * TestIO input = new TestIO(Integer.toString(counter),vars);
	 * DoubleVariableAssignment count = new
	 * DoubleVariableAssignment("count",numAtoms); List<VariableAssignment<?>> outs
	 * = new ArrayList<>(); outs.add(count); TestIO output = new
	 * TestIO("output",outs); runs.put(input,output); counter++; } return runs; }
	 */

	private List<String> getCompounds() {
		List<String> compounds = new ArrayList<>();
		try (Scanner sc = new Scanner(new File("src/tests/resources/compound.idx"))) {
			while (sc.hasNextLine()) {
				compounds.add(sc.next());
				sc.nextLine();// consume rest of text from that line
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return compounds;
	}

}