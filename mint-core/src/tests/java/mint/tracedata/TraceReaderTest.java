/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mint.tracedata;

import mint.Configuration;
import mint.tracedata.readers.TraceReader;
import mint.tracedata.types.VariableAssignment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author mat
 */
public class TraceReaderTest {

    File tempFile;
    PrintWriter file;
    Configuration configuration;
    
    public TraceReaderTest() {
    }

    @Before
    public void setup() throws FileNotFoundException, IOException {
    	Configuration.reset();
		configuration = Configuration.getInstance();
    	tempFile = File.createTempFile("testtrace", ".txt");
        tempFile.deleteOnExit();
        file = new PrintWriter(tempFile);
    }

    @After
    public void teardown() {
    }

    /**
     * Test of readTraceFile method, of class TraceReader.
     */
    @Test
    public void testReadTraceFile() throws Exception {
        file.println("types");
        file.println("foo a:S b:N");
        file.println("bar x:N y:S");
        file.println("trace");
        file.println("foo abc 2");
        file.println("foo abc 2");
        file.println("bar 400 abc");
        file.flush();



        TraceSet result = TraceReader.readTraceFile(tempFile.toPath().toString(), configuration.TOKENIZER);

        assertEquals("There should only be one trace in the result", 1, result.getPos().size());

        List<TraceElement> element = new ArrayList<List<TraceElement>>(result.getPos()).get(0);

        assertEquals("The trace should have three elements in it", 3, element.size());

        assertEquals("The first element should be foo", "foo", element.get(0).getName());


    }
    
    @Test
    public void testReadNegTraceFile() throws Exception {
        file.println("types");
        file.println("foo a:S b:N");
        file.println("bar x:N y:S");
        file.println("trace");
        file.println("foo abc 2");
        file.println("foo abc 2");
        file.println("bar 400 abc");
        file.println("negtrace");
        file.println("foo abc 2");
        file.println("bar 2 abc");
        file.println("bar 400 abc");
        file.flush();



        TraceSet result = TraceReader.readTraceFile(tempFile.toPath().toString(), configuration.TOKENIZER);

        assertEquals("There should only be one positive trace in the result", 1, result.getPos().size());
        assertEquals("There should only be one negative trace in the result", 1, result.getNeg().size());

        List<TraceElement> poselement = new ArrayList<List<TraceElement>>(result.getPos()).get(0);
        List<TraceElement> negelement = new ArrayList<List<TraceElement>>(result.getNeg()).get(0);


        assertEquals("The positive trace should have three elements in it", 3, poselement.size());
        assertEquals("The negative trace should have three elements in it", 3, negelement.size());

        assertEquals("The first element should be foo", "foo", poselement.get(0).getName());


    }

    @Test
    public void testReadTraceFileWithComments() throws Exception {
        file.println("types");
        file.println("foo a:S b:N");
        file.println("bar x:N y:S");
        file.println("trace");
        file.println("foo abc 2");
        file.println("foo abc 2");
        file.println("bar 400 abc");
        file.flush();

        TraceSet noCommentResult = TraceReader.readTraceFile(tempFile.toPath().toString(), configuration.TOKENIZER);

        File secondFile = File.createTempFile("testtrace", ".txt");
        secondFile.deleteOnExit();
        file = new PrintWriter(secondFile);

        file.println("#a comment");
        file.println("types");
        file.println("foo a:S b:N");
        file.println("bar x:N y:S");
        file.println("trace");
        file.println("foo abc 2");
        file.println("#foo abc 2 <- this is a comment, so shouldn't be parsed.");
        file.println("foo abc 2");
        file.println("bar 400 abc");
        file.flush();

        TraceSet withCommentResult = TraceReader.readTraceFile(tempFile.toPath().toString(), configuration.TOKENIZER);

        
        List<TraceElement> noCommentTrace = new ArrayList<List<TraceElement>>(noCommentResult.getPos()).get(0);
        List<TraceElement> withCommentTrace = new ArrayList<List<TraceElement>>(withCommentResult.getPos()).get(0);
        
        
        Iterator<TraceElement> noComment = noCommentTrace.iterator();
        Iterator<TraceElement> withComments = withCommentTrace.iterator();
        
        while(noComment.hasNext() && withComments.hasNext()){
            TraceElement wc = withComments.next();
            TraceElement nc = noComment.next();
            
            assertEquals(wc.getName(),nc.getName());
            
            Set<VariableAssignment<?>> ncAssignments = nc.getData();
            Set<VariableAssignment<?>> wcAssignments = wc.getData();
            assertEquals(ncAssignments.size(), wcAssignments.size());
            
  
        }
    }
}
