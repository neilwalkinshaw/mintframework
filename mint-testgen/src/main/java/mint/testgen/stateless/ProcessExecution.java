package mint.testgen.stateless;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import mint.tracedata.TestIO;
import mint.tracedata.types.FilePointerVariableAssignment;
import mint.tracedata.types.NGramVariableAssignment;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Executes the test subject and records the output. Assumes that the output is a single value
 * on the command line. If no single value is returned, it is recorded as having failed, in
 * an extra variable.
 *
 * Created by neilwalkinshaw on 27/11/14.
 */
public class ProcessExecution implements Callable<TestIO>{


    private final static Logger LOGGER = Logger.getLogger(ProcessExecution.class.getName());


    protected List<String> commands;
    protected File redirectInput;
    protected boolean time;
    protected List<VariableAssignment<?>> output;

    public ProcessExecution(List<String> commands, File redirectInput, boolean time, List<VariableAssignment<?>> output){
        this.commands = commands;
        this.redirectInput = redirectInput;
        this.time = time;
        this.output = output;
        addErrorCategory();
    }

    /**
     * Adds a special variable to the output variables to signify whether or not
     * an execution ends in a failure / error.
     *
     */
    private void addErrorCategory() {

        StringVariableAssignment var = new StringVariableAssignment("Error");

        Set<String> range = new HashSet<String>();
        range.add("OK");
        range.add("Error");

        var.setRange(range);
        output.add(var);
    }

    /**
     * Executes the test invocation.
     * @return
     * @throws Exception
     */
    @Override
    public TestIO call() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.redirectErrorStream(true);
        if(redirectInput!=null){
            pb.redirectInput();
        }
        Process process = pb.start();
        final long start = System.currentTimeMillis();

        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);


        String line= readLine(br);
        TestIO res = null;
        if(time){

            final long end = System.currentTimeMillis();
            long duration = end-start;
            List<VariableAssignment<?>> outs = new ArrayList<VariableAssignment<?>>();
            VariableAssignment<?> out = output.get(0).createNew("output", Long.toString(duration));
            outs.add(out);
            res = new TestIO("output",outs,false);
        }
        else {
            if (output.get(0) instanceof FilePointerVariableAssignment) {
                FilePointerVariableAssignment pointer = (FilePointerVariableAssignment) output.get(0);
                List<VariableAssignment<?>> outs = new ArrayList<VariableAssignment<?>>();
                LOGGER.debug("Output to file: " + pointer.getName());
                String fileString = FileUtils.readFileToString(new File(pointer.getName()));
                if (fileString.isEmpty()) {
                    LOGGER.info("Test case resulted in empty file");
                    res = null;
                } else {
                    VariableAssignment<?> outputNGram = new StringVariableAssignment("output", fileString);
                    outs.add(outputNGram);

                    res = new TestIO("output", outs, false);
                }
            } else if (exception(line))
                res = null;

            else if (line != null) { //Better at detecting errors.
                List<VariableAssignment<?>> outs = new ArrayList<VariableAssignment<?>>();
                StringTokenizer st = new StringTokenizer(line,",");
                int count = 0;
                while(st.hasMoreElements()){
                    VariableAssignment<?> out = output.get(count);
                    out = out.createNew(out.getName(),st.nextElement().toString());
                    count++;
                    outs.add(out);
                }
                //VariableAssignment<?> error = output.get(1).createNew("output", "OK");
                //outs.add(error);
                res = new TestIO("output", outs, false);

                LOGGER.debug("input" + commands + outs);
            }
            if (res == null) {
                LOGGER.error("Error output for " + commands + " " + line);
                List<VariableAssignment<?>> outs = new ArrayList<VariableAssignment<?>>();
                VariableAssignment<?> out = output.get(0).createNew("output", "E");
                outs.add(out);
                //VariableAssignment<?> error = output.get(1).createNew("output", "Error");
                //outs.add(error);
                res = new TestIO("output", outs, false);
                res.setValid(false);
            }
        }
        process.destroy();
        return res;
    }

    private boolean exception(String line) {
        if(line == null)
            return true;
        else if(line.isEmpty())
            return true;
        else if(line.contains("Exception"))
            return true;
        else if(line.contains("by zero"))
            return true;
        else
            return false;
    }

    //Used to remove alphanumeric characters - why?
    private String readLine(BufferedReader br) throws IOException {
        int c;
        StringBuilder response= new StringBuilder();

        while ((c = br.read()) != -1) {
            response.append( (char)c ) ;
        }
        String line = response.toString().trim();
        //line = line.replaceAll("[^a-zA-Z0-9]", "");
        return line;
    }
}
