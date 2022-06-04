package mint; /*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

import org.apache.log4j.Level;
import weka.classifiers.meta.Bagging;


public class Configuration {
	
	protected static Configuration instance;


	public enum Data {
		J48, //decision tree learner for discrete outputs
		M5, //decision tree learner for numerical outputs
		NaiveBayes, //Bayesian learner for discrete outputs
		//M5Rules, //Neural net learner 
		JRIP, //JRIP learner
		//AdditiveRegression, //Ensemble learner for numerical outputs
		AdaBoost, //Ensemble learner for nominal outputs
		Bagging //Ensemble learner for numeric outputs
	}
	
	public enum Strategy {
		noloops, 
		redblue, 
		exhaustive,
		gktails,
		ktails
	}
	
	public enum TestSelection {
		art,
		qbc,
		bagging,
		qbcClustered,
		decisionTree,
		random
	}

	/**
	 * If using the AdaptiveTester, should tests be limited by
	 * time, or the number of test-iterations?
	 */
	public enum TestMode{
		timeLimited,
		iterationLimited
	}
	
	public static Configuration getInstance(){
		if(instance == null)
			instance = new Configuration();
		return instance;
	}
	
	public static void reset(){
		instance = null;
	}
	
	protected Configuration(){
		
	}
	
	/*
	 * If traces distinguish between I/O, ensure that a trace can never
	 * start with an output, and that an output has to be preceded immediately by
	 * an input.
	 */
	public boolean CONSISTENT_RETURNS = false;

	public boolean SUBJECTIVE_OPINIONS=false;

	public double CONFIDENCE_THRESHOLD=Double.MAX_VALUE;
		
	public enum Visualise{text,graphical}

    public boolean CAREFUL_DETERMINIZATION = false;
	
	public String TOKENIZER = "[ \t]";
	
	public String TMP_PREFIX =  "tmp"+ System.getProperty("file.separator");
	
	public String JSON_FILE = "tree.json";
	
	public Visualise VIS = Visualise.text;

	public String VIS_OUTPUT = null;

	public TestMode TEST_MODE = TestMode.iterationLimited;

    public long TEST_TIMEOUT = 600000; //Timeout for an individual test (not tied to USE_TIMEOUT)

	public String PROCESS = null;

	public boolean SEQ_FLAG=false;
			
	public String[] WEKA_OPTIONS = new String[0];
	
	public TestSelection TEST_SELECTION = TestSelection.qbc;
	
	public boolean PREFIX_CLOSED = false;

    public boolean GP = false;

	// data learner
	public Data ALGORITHM = Data.J48;
	
	public Strategy STRATEGY = Strategy.redblue;
	
	// use data for inference?
	public boolean DATA = true;

	//K
	public int K = 0;

	// Input file
	public String INPUT = "";

	// Input file
	public String COMPARETO = "";
		
	public Level LOGGING = Level.INFO;
	
	public int SEED = 0;
	
	public boolean DAIKON = false;
		
	public int MINDAIKON = 2;
	
	public String TARGET_EXECUTABLE = "";
	
	public String TEST_PLAN = "";
	
	public boolean RESPECT_LIMITS = false;
	
	public int ITERATIONS = 60;

	/*
	Number of test-select iterations in a QBC testing cycle.
	 */
	public int QBC_ITERATIONS = 1;

	/*
	Size of the QBC Committee
	 */
	public int QBC_COMMITTEE = 10;

	/*
	 Number of random inputs for a pool of candidate tests.
	  Used for QBC and ART.
	 */
	public int RANDOM_POOL = 100;


	/**
	 * The following are necessary if the SUT is a Java target,
	 * and we wish to generate JUnit test cases for it.
	 *
	 * To be supplied as a comma-separated string of the format:
	 *
	 * PACKAGE_PREFIX,CLASS,METHOD
	 */

	public String JAVA_SUT="";


	/**
	 * When decorating a machine from traces, should the occurrence of an element in a trace be counted multiple
	 * times whenever it occurs within a trace, or just the once?
	 */
	public boolean ONE_WEIGHT_PER_TRACE=false;


}
