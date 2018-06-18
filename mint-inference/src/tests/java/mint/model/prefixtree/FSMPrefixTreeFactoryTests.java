/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013,2014 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package mint.model.prefixtree;

import mint.Configuration;
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

public class FSMPrefixTreeFactoryTests {

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

        FSMPrefixTreeFactory inst = new FSMPrefixTreeFactory(new PayloadMachine());
        Machine<Set<TraceElement>> tree = inst.createPrefixTree(result);
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

        FSMPrefixTreeFactory inst = new FSMPrefixTreeFactory(new PayloadMachine());
        Machine<Set<TraceElement>> tree = inst.createPrefixTree(result);
        
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

        FSMPrefixTreeFactory inst = new FSMPrefixTreeFactory(new PayloadMachine());
        Machine<Set<TraceElement>> tree = inst.createPrefixTree(result);
        
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
