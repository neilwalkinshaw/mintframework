package mint.model.prefixtree;

import mint.Configuration;
import mint.inference.BaseClassifierInference;
import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.dfa.TraceDFA;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.readers.TraceReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TracePrefixTreeGeneratorTests {

	File tempFile;
    PrintWriter file;
    Configuration configuration;

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
     * Test of for a simple, linear prefix tree.
     */
    @Test
    public void testSimpleAcceptingTrace() throws Exception {
        file.println("types");
        file.println("foo a:S b:N");
        file.println("bar x:N y:S");
        file.println("trace");
        file.println("foo abc 2");
        file.println("foo abc 2");
        file.println("bar 400 abc");
        file.flush();


        configuration.PREFIX_CLOSED = false;
        TraceSet result = TraceReader.readTraceFile(tempFile.toPath().toString(), configuration.TOKENIZER);

        BaseClassifierInference bci = null;
		PrefixTreeFactory<?> tptg;
		bci = new BaseClassifierInference(result, configuration.ALGORITHM);
		tptg = new EFSMPrefixTreeFactory(new PayloadMachine(), bci.getClassifiers(),bci.getElementsToInstances());
		
		Machine<Set<TraceElement>> tree = tptg.createPrefixTree(result);
        
        assertEquals("There should be four states in the resulting `tree'. ", 4, tree.getStates().size());
        for(Integer i: tree.getStates()){
        	if(tree.getAutomaton().getOutgoingTransitions(i).isEmpty() || tree.getAutomaton().getInitialState().equals(i))
        		assertEquals("Final and initial states are accept states", true, tree.getAutomaton().getAccept(i));
        	else
        		assertEquals("Non-final / initial states are not accepting states", false, tree.getAutomaton().getAccept(i));
        }
    }
    
    /**
     * Test of for a negative, linear prefix tree.
     */
    @Test
    public void testSimpleRejectingTrace() throws Exception {
        file.println("types");
        file.println("foo a:S b:N");
        file.println("bar x:N y:S");
        file.println("negtrace");
        file.println("foo abc 2");
        file.println("foo abc 2");
        file.println("bar 400 abc");
        file.flush();



        TraceSet result = TraceReader.readTraceFile(tempFile.toPath().toString(), configuration.TOKENIZER);

        BaseClassifierInference bci = null;
		PrefixTreeFactory<?> tptg;
		bci = new BaseClassifierInference(result, configuration.ALGORITHM);
		tptg = new EFSMPrefixTreeFactory(new PayloadMachine(), bci.getClassifiers(),bci.getElementsToInstances());
		
		Machine<Set<TraceElement>> tree = tptg.createPrefixTree(result);
        
        assertEquals("There should be four states in the resulting `tree'. ", 4, tree.getStates().size());
        for(Integer i: tree.getStates()){
        	if(tree.getAutomaton().getInitialState().equals(i))
        		assertEquals("Initial state is an accept state", true, tree.getAutomaton().getAccept(i));
        	else
        		assertEquals("Final and intermediate states are not accepting states", false, tree.getAutomaton().getAccept(i));
        }
    }
    
    /**
     * Test of for a positive and negative trace.
     */
    @Test
    public void testBranchingTraces() throws Exception {
        file.println("types");
        file.println("foo a:S b:N");
        file.println("bar x:N y:S");
        file.println("negtrace");
        file.println("foo abc 2");
        file.println("foo abc 2");
        file.println("bar 400 abc");
        file.println("trace");
        file.println("bar 20 a");
        file.println("foo abc 2");
        file.println("bar 400 abc");
        file.flush();

        configuration.PREFIX_CLOSED = false;

        TraceSet result = TraceReader.readTraceFile(tempFile.toPath().toString(), configuration.TOKENIZER);

        BaseClassifierInference bci = null;
		PrefixTreeFactory<?> tptg;
		bci = new BaseClassifierInference(result, configuration.ALGORITHM);
		tptg = new EFSMPrefixTreeFactory(new PayloadMachine(), bci.getClassifiers(),bci.getElementsToInstances());
		
		Machine<Set<TraceElement>> tree = tptg.createPrefixTree(result);
        assertEquals("There should be seven states in the resulting `tree'. ", 7, tree.getStates().size());
        int acceptstates = 0;
        int rejectstates = 0;
        for(Integer i: tree.getStates()){
        	boolean accepting = tree.getAutomaton().getAccept(i).equals(TraceDFA.Accept.ACCEPT);
        	if(accepting)
        		acceptstates++;
        	else
        		rejectstates++;
        	if(tree.getAutomaton().getInitialState().equals(i))
        		assertEquals("Initial state is an accept state", true, tree.getAutomaton().getAccept(i));
        	else if(!tree.getAutomaton().getOutgoingTransitions(i).isEmpty())
        		assertEquals("Intermediate states should be rejecting", false, tree.getAutomaton().getAccept(i));
        }
        assertEquals("There should be two accepting states - initial and one final", 2, acceptstates);
        assertEquals("There should be five rejecting states", 5, rejectstates);
    }

}
