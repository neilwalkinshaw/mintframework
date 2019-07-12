package mint.testgen.stateless.output;

import mint.Configuration;
import mint.testgen.stateless.runners.execution.TestRunner;
import mint.testgen.stateless.text.TextIORunner;
import mint.tracedata.TestIO;
import mint.tracedata.types.VariableAssignment;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

public class BasicTextRecorder implements TestRecorder {

    protected TestRunner host;

    public BasicTextRecorder(TestRunner host){

        this.host = host;
    }

    protected String getLogFileName(){
        Configuration config = Configuration.getInstance();
        java.util.Date date= new java.util.Date();
        String testName = host.getLabel()+config.TEST_PLAN+config.TEST_MODE+new Timestamp(date.getTime());
        testName = testName.replaceAll("[^A-Za-z0-9 ]", "");
        testName = testName.replaceAll("\\s", "");
        return testName;
    }

    @Override
    public void record(List<TestIO> testInputs, List<Integer> iterations) {
        PrintWriter writer;
        Iterator<Integer> iterIt = iterations.iterator();
        int nextBreak = testInputs.size();
        int counter = 0;
        if(iterIt.hasNext())
            nextBreak = iterIt.next();
        try {
            writer = new PrintWriter(getLogFileName());
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
                    writer.println("# ");
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
