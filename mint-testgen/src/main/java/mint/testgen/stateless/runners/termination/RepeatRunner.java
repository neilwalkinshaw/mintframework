package mint.testgen.stateless.runners.termination;

import mint.Configuration;
import mint.testgen.stateless.runners.execution.TestRunner;
import mint.testgen.stateless.text.TextIORunner;
import mint.tracedata.TestIO;
import mint.tracedata.types.VariableAssignment;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by neilwalkinshaw on 25/08/2016.
 */
public abstract class RepeatRunner {

    protected TestRunner host;

    protected List<Integer> indexList;
    protected List<Long> timeList;
    protected Long time;

    public RepeatRunner(TestRunner host){

        indexList = new ArrayList<Integer>();
        timeList = new ArrayList<Long>();
        this.host = host;
        time = System.currentTimeMillis();
    }

    protected void runTest(List< TestIO > toInfer){
        host.run(toInfer);

        indexList.add(host.getTestSize()-1);

        timeList.add(System.currentTimeMillis()-time);
        assert(toInfer !=null);
    }

    public abstract void runTests(List< TestIO > toInfer);

    protected String getLogFileName(){
        Configuration config = Configuration.getInstance();
        java.util.Date date= new java.util.Date();
        String testName = host.getLabel()+config.TEST_PLAN+config.TEST_MODE+new Timestamp(date.getTime());
        testName = testName.replaceAll("[^A-Za-z0-9 ]", "");
        testName = testName.replaceAll("\\s", "");
        return testName;
    }

    /**
     * Write testInputs to a text file. Each iteration will be marked by a
     * separate line with a `#' in it.
     *
     * @param testInputs
     * @param iterations
     */
    protected void recordTestSet(List<TestIO> testInputs, List<Integer> iterations) {
        PrintWriter writer;
        String testName = getLogFileName();
        Iterator<Integer> iterIt = iterations.iterator();
        int nextBreak = testInputs.size();
        int counter = 0;
        if(iterIt.hasNext())
            nextBreak = iterIt.next();
        try {
            writer = new PrintWriter(testName);
            for(TestIO io:testInputs){

                String line = "";
                for(VariableAssignment var : io.getVals()){
                    String val = var.getValue().toString();
                    if(host instanceof TextIORunner)
                        val = var.getName();
                    line += val + " ";
                }
                writer.println(line);
                if(counter == nextBreak){
                    writer.println("# "+timeList.get(iterations.indexOf(nextBreak)));
                    if(iterIt.hasNext())
                        nextBreak = iterIt.next();
                    else
                        nextBreak = testInputs.size();
                }
                counter++;


            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
