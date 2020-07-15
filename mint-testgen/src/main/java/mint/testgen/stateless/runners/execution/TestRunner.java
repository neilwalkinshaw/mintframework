package mint.testgen.stateless.runners.execution;

import mint.testgen.stateless.output.BasicTextRecorder;
import mint.testgen.stateless.output.TestRecorder;
import mint.testgen.stateless.output.junit.JUnitTestRecorder;
import org.apache.log4j.Logger;
import mint.Configuration;
import mint.tracedata.TestIO;
import mint.testgen.stateless.ProcessExecution;
import mint.testgen.stateless.runners.termination.FixedIterationsRunner;
import mint.testgen.stateless.runners.termination.RepeatRunner;
import mint.testgen.stateless.runners.termination.TimedRunner;
import mint.tracedata.readers.ngram.NgramMerger;
import mint.tracedata.readers.ngram.Ngrammer;
import mint.tracedata.types.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Runs the system under test with inputs.
 * @author neilwalkinshaw
 *
 */
public abstract class TestRunner {

	protected String command, setupFile;
	protected List<VariableAssignment<?>> params,output;

    protected Command commandBuilder;

    /*
    testInputs and testOutputs record the pairs of inputs and outputs that can be used for learning.
     */
    protected List<TestIO> testInputs, testOutputs;

    //allTestInputs records *all* inputs attempted, even those that cause failures.
    protected List<TestIO> allTestInputs;

    protected Map<String,Ngrammer> stringsToNgrams = new HashMap<String,Ngrammer>();

    protected int minTests = 10;
    protected int maxTests = 80000;
    protected boolean time = false;
    protected Random rand;
    protected String testPlan;
    //protected RepeatRunner repRunner;
    protected NgramMerger ngramMerger;

    protected int listLength = -1;

    protected JUnitDetails jud = null;
	
	private final static Logger LOGGER = Logger.getLogger(TestRunner.class.getName());

	/**
	 *
	 * @param setupFile
	 * @param testPlan
	 */
	public TestRunner(String setupFile, String testPlan){
        this.setupFile = setupFile;

        this.testPlan = testPlan;
        this.commandBuilder = new SimpleCommand();
        rand = new Random(Configuration.getInstance().SEED);

        testInputs = new ArrayList<TestIO>();
        allTestInputs = new ArrayList<TestIO>();
        testOutputs = new ArrayList<TestIO>();

		JSONParser jp = new JSONParser();

        ngramMerger = new NgramMerger();

        if(!Configuration.getInstance().JAVA_SUT.isEmpty()){
            setJUD();
        }

		try {
			JSONObject o = (JSONObject)jp.parse(new FileReader(setupFile));
			command = o.get("command").toString();
			params = readVariables((JSONArray) o.get("parameters"));


            output = readVariables( (JSONArray)o.get("output"));


		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    /**
     * set JUnit description jud to contents of JAVA_SUT, as set in configuration.
     */
    private void setJUD() {
	    String sut = Configuration.getInstance().JAVA_SUT;
	    String[] elements = sut.split(",");
	    if(elements.length<3){
	        LOGGER.error("JAVA_SUT not configured properly. Only has "+elements.length+" elements.");
        }
	    jud = new JUnitDetails(elements[0],elements[0],elements[1],elements[2]);
    }


    public void setCommandBuilder(Command commandBuilder){
        this.commandBuilder = commandBuilder;
    }

    public Command getCommandBuilder(){
        return commandBuilder;
    }


    protected RepeatRunner getRepRunner(){
        Configuration config = Configuration.getInstance();
        if(config.TEST_MODE == Configuration.TestMode.iterationLimited){
            return new FixedIterationsRunner(this,config.ITERATIONS);
        }
        else
            return new TimedRunner(this,config.TEST_TIMEOUT);
    }

    public List<TestIO> getAllTestInputs() {
        return allTestInputs;
    }

    public void run(){
        List<TestIO> toInfer = null;

        if(testPlan !=null && !testPlan.isEmpty()){
            toInfer = readTestInputs(testPlan);
            if(toInfer.size()<minTests){
                toInfer.addAll(generateRandomTestInputs(minTests-toInfer.size()));
            }
            while(toInfer.size() > maxTests){
                toInfer.remove(rand.nextInt(toInfer.size()));
            }
        }
        else{
            toInfer = generateRandomTestInputs(minTests);
        }
        getRepRunner().runTests(toInfer);
    }

    public List<TestIO> getTestInputs() {
        return testInputs;
    }

    public List<TestIO> getTestOutputs() {
        return testOutputs;
    }

    public abstract String getLabel();




    public abstract List<TestIO> generateTests();


	protected List<TestIO> generateRandomTestInputs(int number) {
		List<TestIO> tests = new ArrayList<TestIO>();
		for(int i = 0; i< number; i++){
			tests.add(parse(""));
		}
		return tests;
	}

	/**
	 * Reads in a JSONArray of variable specifications and returns a
     * corresponding list of VariableAssignments.
	 * @param jsonArray
	 * @return
	 */
	public static List<VariableAssignment<?>> readVariables(JSONArray jsonArray) {
		List<VariableAssignment<?>> ret = new ArrayList<VariableAssignment<?>>();
		for(int i = 0; i < jsonArray.size(); i++)
		{
		      JSONObject object = (JSONObject)jsonArray.get(i);
		      String name = object.get("name").toString();
		      String type = object.get("type").toString();
		      if(type.equals("integer")){
		    	  IntegerVariableAssignment var = new IntegerVariableAssignment(name);
		    	  String max = (String) object.get("max");
		    	  if(max!=null){
		    		  var.setMax(Integer.valueOf(max));
		    	  }
		    	  String min = (String) object.get("min");
		    	  if(min!=null)
		    		  var.setMin(Integer.valueOf(min));

                  JSONArray restrictions = (JSONArray)object.get("restrictions");
                  if(restrictions !=null){
                      Set<Integer> range = new HashSet<Integer>();
                      for(Object element : restrictions){
                          String val = (String) element;
                          range.add(Integer.parseInt(val));
                      }
                      var.setRange(range);
                  }
		    	  LOGGER.debug(name +": min - "+min+", max - "+max);
		    	  assert(var.getMax()>0);
		    	  ret.add(var);
		      }
		      else if(type.equals("double")){
		    	  DoubleVariableAssignment var = new DoubleVariableAssignment(name);
		    	  String max = (String) object.get("max");
		    	  if(max!=null)
		    		  var.setMax(Double.valueOf(max));
		    	  String min = (String) object.get("min");
		    	  if(min!=null)
		    		  var.setMin(Double.valueOf(min));
                  JSONArray restrictions = (JSONArray)object.get("restrictions");
                  if(restrictions !=null){
                      Set<Double> range = new HashSet<Double>();
                      for(Object element : restrictions){
                          String val = (String) element;
                          range.add(Double.parseDouble(val));
                      }
                      var.setRange(range);
                  }
		    	  ret.add(var);
		      }
		      else if(type.equals("bool")){
		    	  ret.add(new BooleanVariableAssignment(name));
		      }
		      else if(type.equals("string")){
		    	  StringVariableAssignment var = new StringVariableAssignment(name);
                  JSONArray restrictions = (JSONArray)object.get("restrictions");
                  if(restrictions !=null){
                      Set<String> range = new HashSet<String>();
                      for(Object element : restrictions){
                          String val = (String) element;
                          range.add(val);
                      }
                      var.setRange(range);


                  }
                  ret.add(var);
		      }
              else if(type.equals("input_redirect")) {
                  String prefix = "outputs"+File.separator;
                  Configuration config = Configuration.getInstance();
                  if(config.PROCESS!=null){
                      prefix = prefix + Configuration.getInstance().PROCESS;
                  }
                  FilePointerVariableAssignment var = new FilePointerVariableAssignment(prefix+name);
                  JSONArray restrictions = (JSONArray) object.get("restrictions");
                  if (restrictions != null) {
                      Set<String> range = new HashSet<String>();
                      for (Object element : restrictions) {
                          String val = (String) element;
                          range.add(val);
                      }
                      var.setRange(range);


                  }
                  ret.add(var);
              }
		}
		return ret;
	}
	
	/**
	 * Read in a text file of test inputs into a list of TestIO objects.
     *
	 * @param testPlan
	 * @return
	 */
	public List<TestIO> readTestInputs(String testPlan){
		List<TestIO> plan = new ArrayList<TestIO>();
		
		try {
			FileInputStream fis = new FileInputStream(testPlan);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

			String line;
			line = reader.readLine();
			while(line!=null) {
                if(line.isEmpty()){
                    line = reader.readLine();

                }
                else {
                    plan.add(parse(line));
                    line = reader.readLine();
                }
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return plan;
	}

	/**
	 * Take a line of text and return a corresponding set of TestIO objects.
     * If the line is empty or partial, missing parameters will be replaced by
     * random values.
     *
	 * @param line
	 * @return
	 */
	protected TestIO parse(String line) {
		String[] split = line.trim().split("\\s+");
		List<VariableAssignment<?>> inputParams = new ArrayList<VariableAssignment<?>>();
		boolean diffLengths = false;
		if(split.length != params.size()){
			diffLengths = true;
		}
		for(int i = 0; i<params.size(); i++){

			VariableAssignment<?> type = params.get(i);
			VariableAssignment<?> in = null;
			if(diffLengths == false)
				in = type.createNew(type.getName(), split[i].trim());
			else{
				type.setToRandom();
				in = type.createNew(type.getName(), type.getValue().toString());
			}
			inputParams.add(in);
		}
		TestIO input = new TestIO(command,inputParams);
		return input;
	}
	
	/**
	 * Executes a set of inputs in testPlan, expects return type output, and records the input-outputs
     * in record.
	 * @param testPlan

	 */
	public void run(Collection<TestIO> testPlan){
        assert(testPlan !=null);
		for(TestIO test : testPlan) {
            List<String> commands = new ArrayList<String>();
            String name = test.getName();
            StringTokenizer st = new StringTokenizer(name);
            while(st.hasMoreElements()){
                commands.add(st.nextToken());
            }
            File redirectFile = null;



            boolean mvnExecution = false;
            if(test.getName().startsWith("mvn") || test.getName().startsWith("/usr/local/bin/mvn"))
                mvnExecution = true;
            if(mvnExecution)
                commands.add("-Dexec.args="+getArgString(test.getVals()));
            else {

                for (VariableAssignment<?> assignment : test.getVals()) {
                    if (!(assignment instanceof FilePointerVariableAssignment))
                        commands.add(assignment.getValue().toString());
                    else {
                        FilePointerVariableAssignment fpv = (FilePointerVariableAssignment) assignment;
                        redirectFile = new File(fpv.getValue());
                        if (redirectFile.isDirectory()) {
                            redirectFile = selectRandomFileFrom(redirectFile);
                            fpv.setValue(redirectFile.getAbsolutePath());
                        }
                    }
                }
            }

            execute(test, commands, redirectFile);
        }
        if(testOutputs.size()>0 && isOutputString())
            postProcessStrings();
	}

    private boolean isOutputString() {
        VariableAssignment out = testOutputs.get(0).getValWithName("output");
        if(out!=null) {
            if(out.typeString().equals(":S"))
                return true;
        }
        return false;
    }

    private String getArgString(List<VariableAssignment<?>> vals) {
        String argString = "";
        for (VariableAssignment<?> assignment : vals) {
            if(!argString.isEmpty())
                argString+= " ";
            if (!(assignment instanceof FilePointerVariableAssignment))
                argString = argString+(assignment.getValue().toString());

        }

        return argString;
    }


    /**
     * Turn text output into ngram distributions that can be subject to
     * Ngram-based analysis techniques.
     */
    protected void postProcessStrings() {
        Collection<String> stringOutputs = new HashSet<String>();
        List<String> rawOutputs = new ArrayList<String>();

        //Store all raw string outputs.
        //int testOutputCounter = 0;
        for(TestIO io : testOutputs){
            VariableAssignment<?> var = io.getVals().get(0);
            LOGGER.debug("output var type: "+var.typeString());
            /*if(!(var instanceof StringVariableAssignment)) {
                testOutputCounter++;
                continue;
            }
            StringVariableAssignment sva = (StringVariableAssignment)var;
            stringOutputs.add(sva.getValue());
            rawOutputs.add(testOutputCounter,sva.getValue());*/
            rawOutputs.add(var.getValue().toString());
            //testOutputCounter++;
        }
        assert(rawOutputs.size() == testOutputs.size());

        //Compute N-Grams, and map to string outputs.
        for(String s : rawOutputs) {
            Ngrammer ngrammer = new Ngrammer(s, 2);
            stringsToNgrams.put(s,ngrammer);
            ngramMerger.addNgramCounts(ngrammer);
        }
        listLength = 25;
        //List<VariableAssignment<?>> reduced = ngramMerger.getUselessReducedDistribution();
        List<VariableAssignment<?>> reduced = ngramMerger.getDistribution(true,25);
        if(reduced.isEmpty()){
            LOGGER.error("empty output distribution.");
        }
        NGramVariableAssignment nvar = (NGramVariableAssignment) reduced.get(0);
        listLength = nvar.getValue().size();
        int count = 0;
        for(TestIO io : testOutputs) {
            //assuming that testOutputs corresponds in order to reduced
            io.getVals().set(0,reduced.get(count));

            count++;
        }

    }

    public void execute(TestIO test, List<String> commands, File redirectFile) {
        commandBuilder.setCore(commands);

        TestIO res = timedCall(new ProcessExecution(commandBuilder.getCommand(), redirectFile, time, output));

        allTestInputs.add(test);
        if(res==null) {
            LOGGER.debug("NULL return");
            return;
        }
        else if(!error(res)){
            testInputs.add(test);
            testOutputs.add(res);
        }
    }


    protected boolean error(TestIO res) {
        VariableAssignment<?> errorVar = res.getValWithName("Error");
        if(errorVar == null){
            if(res.getVals() == null)
                return true;
            else if(res.getVals().get(0) == null)
                return true;
            else if(res.getVals().get(0).getValue() == null)
                return true;
            else if(res.getVals().get(0).getValue().toString().trim().isEmpty())
                return true;
            if(res.isValid())
                return false;
            else
                return true;
        }
        else
            return true;
    }

    protected static <T> T timedCall(Callable<T> c)
    {
        Configuration config = Configuration.getInstance();

        ExecutorService es = Executors.newCachedThreadPool();
        T retVal = null;
        try{
            FutureTask<T> task = new FutureTask<T>(c);
            es.execute(task);
            retVal = task.get(config.TEST_TIMEOUT, TimeUnit.MILLISECONDS);
            task.cancel(true);
        }
        catch (TimeoutException e) {
            LOGGER.info("Timeout hit for test "+c.toString());
        }
        catch (InterruptedException e) {
            LOGGER.error(e.toString());
            e.printStackTrace();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
            LOGGER.info("Exception triggered!");
        }
        catch (Exception e){
            LOGGER.error("Unexpected exception caught for: "+c.toString());
            e.printStackTrace();
        }
        finally {

            es.shutdownNow();
        }
        return retVal;
    }
	
	/**
	 * 
	 * @param redirectFile
	 * @return
	 */
    protected File selectRandomFileFrom(File redirectFile) {
        assert(redirectFile.isDirectory());
        File[] files = redirectFile.listFiles();
        return files[rand.nextInt(files.length)];
    }

    public TestRecorder getTestRecorder() {
        if(jud == null)
            return new BasicTextRecorder(this);
        else{
            return new JUnitTestRecorder(jud);
        }
    }

    /**
     * If the SUT in question is a Java unit, we can generate JUnit tests automatically.
     * However, for this we need some additional details, which are stored in the following object.
     */
    public class JUnitDetails{

        private String path,packageName,clazz,method;

        public JUnitDetails(String path, String packageName, String clazz, String method) {
            this.path = path;
            this.packageName = packageName;
            this.clazz = clazz;
            this.method = method;
        }

        public String getPath() {
            return path;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getClazz() {
            return clazz;
        }

        public String getMethod() {
            return method;
        }
    }
}
