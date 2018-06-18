package mint.evaluation;

import mint.Configuration;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.readers.TraceReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class NegativePairMutatorTest {
	
	File tempFile;
    PrintWriter file;
    Configuration configuration;


	@Before
	public void setUp() throws Exception {
		Configuration.reset();
		configuration = Configuration.getInstance();
		tempFile = File.createTempFile("testtrace", ".txt");
        tempFile.deleteOnExit();
        file = new PrintWriter(tempFile);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws IOException {
		file.println("types");
        file.println("a a:S b:N");
        file.println("b x:S y:N");
        file.println("c x:S y:N");
        file.println("trace");
        file.println("a abc 2");
        file.println("b abc 2");
        file.println("c abc 2");
        file.println("trace");
        file.println("c abc 2");
        file.println("a abc 2");
        file.println("b abc 2");
        file.flush();

        
        configuration.PREFIX_CLOSED = true;
        TraceSet result = TraceReader.readTraceFile(tempFile.toPath().toString(), configuration.TOKENIZER);
        int[] lengths = new int[result.getPos().size()];
        Collection<List<TraceElement>> posTraces = result.getPos();
        int counter = 0;
        for(List<TraceElement> trace:posTraces){
        	lengths[counter] = trace.size();
        	counter++;
        }
        Map<String,Set<String>> negPairs = new HashMap<String,Set<String>>();
        Set<String> notAfterA = new HashSet<String>();
        notAfterA.add("c");
        notAfterA.add("a");
        negPairs.put("a", notAfterA);
        NegativePairMutator npm = new NegativePairMutator(negPairs,result.getPos());
        for(List<TraceElement> neg : npm.getNegatives()){
        	System.out.println(neg);
        }
        int[] lengths2 = new int[result.getPos().size()];
        counter = 0;
        for(List<TraceElement> trace:posTraces){
        	lengths2[counter] = trace.size();
        	counter++;
        }
        assertEquals(lengths.length,lengths2.length);
        for(int i = 0; i<lengths.length;i++){
        	assertEquals(lengths[i],lengths2[i]);
        }
	}

}
