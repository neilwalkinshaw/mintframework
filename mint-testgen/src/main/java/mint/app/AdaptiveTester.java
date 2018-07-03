/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mint.app;


import mint.testgen.stateless.text.TextIORunner;
import org.apache.commons.cli.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import mint.Configuration;
import mint.testgen.stateless.art.ARTTestRunner;
import mint.testgen.stateless.gp.ClusterGPTestRunner;
import mint.testgen.stateless.gp.GPTestRunner;
import mint.testgen.stateless.random.RandomTestRunner;
import mint.testgen.stateless.runners.execution.Command;
import mint.testgen.stateless.runners.execution.IDParameterExecutor;
import mint.testgen.stateless.runners.execution.SequenceIDExecutor;
import mint.testgen.stateless.runners.execution.TestRunner;
import mint.testgen.stateless.weka.WekaClassifierTestRunner;


/**
 * Generate test cases iteratively, from an inferred model. This entry-point will not infer EFSMs, just simple WEKA models
 * (by default J48 decision trees).
 *
 * As input, it takes a JSON file.
 * 
 * @author neilwalkinshaw
 *
 */

public class AdaptiveTester {

	private final static Logger LOGGER = Logger.getLogger(AdaptiveTester.class.getName());

	
	/**
	 * Parse arguments and instantiate a TestRunner with the corresponding options.
	 * @param args
	 */
	@SuppressWarnings("static-access")
	public void parseCommandLine(String[] args) {
		Options options = new Options();

		Option help = new Option("help", "print this message");
		Option csv = OptionBuilder.withArgName("input").hasArg().withDescription("Directory of base-inputs").create("input");
		Option algorithm = OptionBuilder.withArgName("algorithm").hasArg().withDescription("J48, M5, JRIP").create("algorithm");
		Option wekaOptions = OptionBuilder.withArgName("wekaOptions").hasArgs().withDescription("WEKA options for specific learning algorithms (See WEKA documentation)").create("wekaOptions");
		wekaOptions.setArgs(6);
		Option target = OptionBuilder.withArgName("target").hasArg().withDescription("Test target").create("target");
		Option testPlan = OptionBuilder.withArgName("testPlan").hasArg().withDescription("Test plan").create("testPlan");
		Option respectConstraints = OptionBuilder.withArgName("respectConstraints").withDescription("Always respect limits on input parameters").create("respectConstraints");
		Option testSelection = OptionBuilder.withArgName("testSelection").hasArg().withDescription("art, qbc, qbcClustered, decisionTree, random, bagging").create("testSelection");
		Option iterations = OptionBuilder.withArgName("iterations").hasArg().withDescription("Number of test iterations (excludes individual QBC iterations - an entire QBC cycle is counted as a single iteration.)").create("iterations");
		Option limited = OptionBuilder.withArgName("terminationMode").hasArg().withDescription("Either \"time-limited\" or \"iterations\".").create("terminationMode");
		Option qbcIterations = OptionBuilder.withArgName("qbcIterations").hasArg().withDescription("Number of iterations in a single QBC cycle.)").create("qbcIterations");
		Option qbcCommittee = OptionBuilder.withArgName("qbcCommittee").hasArg().withDescription("Number of members in a QBC committee.)").create("qbcCommittee");
		Option randomPool = OptionBuilder.withArgName("randomPool").hasArg().withDescription("Number of tests in a pool that a random tester (e.g. ART or QBC) can choose from.)").create("randomPool");
		Option seed = OptionBuilder.withArgName("seed").hasArg().withDescription("Random seed").create("seed");
		Option addProcessID = OptionBuilder.withArgName("addProcessID").hasArg().withDescription("[For experiments] Identifier for process added as first parameter to SUT").create("addProcessID");
		Option addSeqID = OptionBuilder.withArgName("addSequenceID").withDescription("[For experiments] Identifier for test number added as second parameter to SUT").create("addSequenceID");

		options.addOption(help);
		options.addOption(csv);
		options.addOption(target);
		options.addOption(algorithm);
		options.addOption(wekaOptions);
		options.addOption(testPlan);
		options.addOption(respectConstraints);
		options.addOption(testSelection);
		options.addOption(iterations);
		options.addOption(limited);
		options.addOption(qbcIterations);
		options.addOption(qbcCommittee);
		options.addOption(randomPool);
		options.addOption(seed);
		options.addOption(addProcessID);
		options.addOption(addSeqID);

		// create the parser
		CommandLineParser parser = new GnuParser();
		Configuration configuration = Configuration.getInstance();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("help") || !line.hasOption("target")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("AdaptiveTester", options);
                return;
			}
			if (line.hasOption("input"))
				configuration.INPUT = line.getOptionValue("input");
			if (line.hasOption("algorithm"))
				configuration.ALGORITHM = Configuration.Data.valueOf(line.getOptionValue("algorithm"));
			if (line.hasOption("testSelection"))
				configuration.TEST_SELECTION = Configuration.TestSelection.valueOf(line.getOptionValue("testSelection"));
			
			if (line.hasOption("target"))
				configuration.TARGET_EXECUTABLE = line.getOptionValue("target");
			if (line.hasOption("testPlan"))
				configuration.TEST_PLAN = line.getOptionValue("testPlan");
			if (line.hasOption("wekaOptions")){
				String[] opt = line.getOptionValues("wekaOptions");
				configuration.WEKA_OPTIONS =opt;
			}
			if (line.hasOption("respectConstraints"))
				configuration.RESPECT_LIMITS = true;
			if (line.hasOption("iterations")){				
				configuration.ITERATIONS = (Integer.valueOf(line.getOptionValue("iterations")));
			}
			if (line.hasOption("terminationMode")){
				configuration.TEST_MODE = Configuration.TestMode.valueOf(line.getOptionValue("terminationMode"));
			}
			if (line.hasOption("qbcIterations")){
				configuration.QBC_ITERATIONS = (Integer.valueOf(line.getOptionValue("qbcIterations")));
			}
			if (line.hasOption("qbcCommittee")){
				configuration.QBC_COMMITTEE = (Integer.valueOf(line.getOptionValue("qbcCommittee")));
			}
			if (line.hasOption("randomPool")){
				configuration.RANDOM_POOL = (Integer.valueOf(line.getOptionValue("randomPool")));
			}
			if (line.hasOption("seed")){
				configuration.SEED = (Integer.valueOf(line.getOptionValue("seed")));
			}
			if(line.hasOption("addSequenceID")) {
				Configuration.getInstance().SEQ_FLAG=true;
			}
			if(line.hasOption("addProcessID")) {
				Configuration.getInstance().PROCESS = line.getOptionValue("addProcessID");
			}

			TestRunner ti = null;
			switch(configuration.TEST_SELECTION){
				case art: ti = new ARTTestRunner(configuration.TARGET_EXECUTABLE,configuration.TEST_PLAN);
					break;
				case qbc: ti = new GPTestRunner(configuration.TARGET_EXECUTABLE,configuration.TEST_PLAN);
					break;
				case qbcClustered: ti = new ClusterGPTestRunner(configuration.TARGET_EXECUTABLE,configuration.TEST_PLAN);
					break;
				case decisionTree: ti = new WekaClassifierTestRunner(configuration.TARGET_EXECUTABLE,configuration.TEST_PLAN, configuration.ALGORITHM);
					break;
				case bagging: ti = new TextIORunner(configuration.TARGET_EXECUTABLE,configuration.TEST_PLAN, configuration.INPUT);
					break;
				case random: ti = new RandomTestRunner(configuration.TARGET_EXECUTABLE,configuration.TEST_PLAN);
			}
            Command comBuilder = ti.getCommandBuilder();
			if(line.hasOption("addSequenceID")) {
                comBuilder = new SequenceIDExecutor(ti,comBuilder);
            }
            if(line.hasOption("addProcessID")) {
                comBuilder = new IDParameterExecutor(comBuilder,line.getOptionValue("addProcessID"));
            }
            ti.setCommandBuilder(comBuilder);
			//TestRunner ti = new WekaClassifierTestRunner(configuration.TARGET_EXECUTABLE,configuration.TEST_PLAN, configuration.ALGORITHM, nonMutatedExecPath, configuration.ITERATIONS);
			//TestRunner ti = new GPTestRunner(configuration.TARGET_EXECUTABLE,configuration.TEST_PLAN);
			ti.run();

		} catch (ParseException exp) {
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("ExperimentDriver", options);
		}

	}
	
	
	

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ALL);
		AdaptiveTester mint = new AdaptiveTester();
		mint.parseCommandLine(args);

	}


}
