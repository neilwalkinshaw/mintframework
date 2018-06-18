package mint.evaluation;

import mint.Configuration;
import mint.inference.BaseClassifierInference;
import mint.model.PayloadMachine;
import mint.model.WekaGuardMachineDecorator;
import mint.model.prefixtree.EFSMPrefixTreeFactory;
import mint.model.prefixtree.PrefixTreeFactory;
import mint.model.walk.EFSMAnalysis;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.readers.TraceReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class    NegativePairDataMutatorTest {
	
	File tempFileA, tempFileB;
    PrintWriter fileA, fileB;
    Configuration configuration;


	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSynthesiseNegatives() throws IOException {
        Configuration.reset();
        configuration = Configuration.getInstance();
        tempFileA = File.createTempFile("testtrace", ".txt");
        tempFileA.deleteOnExit();
        fileA = new PrintWriter(tempFileA);

        tempFileB = File.createTempFile("testnegs", ".txt");
        tempFileB.deleteOnExit();
        fileB = new PrintWriter(tempFileB);
		fileA.println("types");
        fileA.println("a a:S b:N");
        fileA.println("b x:S y:N");
        fileA.println("c x:S y:N");
        fileA.println("trace");
        fileA.println("a abc 1");
        fileA.println("b abc 2");
        fileA.println("c abc 3");
        fileA.println("trace");
        fileA.println("c abc 3");
        fileA.println("a abc 2");
        fileA.println("b abc 1");
        fileA.flush();


        fileB.println("types");
        fileB.println("a a:S b:N");
        fileB.println("b x:S y:N");
        fileB.println("c x:S y:N");
        fileB.println("trace");
        fileB.println("a * 1");
        fileB.println("c * *");
        fileB.println("trace");
        fileB.println("c abc 3");
        fileB.println("b abc 2");
        fileB.flush();
        
        configuration.PREFIX_CLOSED = true;
        TraceSet result = TraceReader.readTraceFile(tempFileA.toPath().toString(), configuration.TOKENIZER);
        TraceSet negs = TraceReader.readTraceFile(tempFileB.toPath().toString(),configuration.TOKENIZER);
        int[] lengths = new int[result.getPos().size()];
        Collection<List<TraceElement>> posTraces = result.getPos();

        NegativePairDataMutator npm = new NegativePairDataMutator(negs.getPos(),result.getPos());
        npm.buildNegs();
        for(List<TraceElement> neg : npm.getNegatives()){
        	System.out.println(neg);
        }

	}

    @Test
    public void testFindMatches() throws IOException {
        Configuration.reset();
        configuration = Configuration.getInstance();
        tempFileA = File.createTempFile("testtrace", ".txt");
        tempFileA.deleteOnExit();
        fileA = new PrintWriter(tempFileA);

        tempFileB = File.createTempFile("testnegs", ".txt");
        tempFileB.deleteOnExit();
        fileB = new PrintWriter(tempFileB);
        fileA.println("types");
        fileA.println("a a:S b:N");
        fileA.println("b x:S y:N");
        fileA.println("c x:S y:N");
        fileA.println("trace");
        fileA.println("a abc 1");
        fileA.println("b abc 2");
        fileA.flush();


        fileB.println("types");
        fileB.println("a a:S b:N");
        fileB.println("b x:S y:N");
        fileB.println("c x:S y:N");
        fileB.println("trace");
        fileB.println("a * 1");
        fileB.println("c abc 2");
        fileB.flush();

        configuration.PREFIX_CLOSED = true;
        TraceSet result = TraceReader.readTraceFile(tempFileA.toPath().toString(), configuration.TOKENIZER);
        TraceSet negs = TraceReader.readTraceFile(tempFileB.toPath().toString(),configuration.TOKENIZER);

        NegativePairDataMutator npm = new NegativePairDataMutator(negs.getPos(),result.getPos());
        List<TraceElement> p = result.getPos().iterator().next();
        List<TraceElement> n = negs.getPos().iterator().next();
        Collection<Integer> indices = npm.findMatches(p,n);
        assertFalse(indices.isEmpty());
    }

    @Test
    public void testTrueNegatives() throws IOException {
        Configuration.reset();
        configuration = Configuration.getInstance();
        tempFileA = File.createTempFile("testtrace", ".txt");
        tempFileA.deleteOnExit();
        fileA = new PrintWriter(tempFileA);

        tempFileB = File.createTempFile("testnegs", ".txt");
        tempFileB.deleteOnExit();
        fileB = new PrintWriter(tempFileB);
        fileA.println("types");
        fileA.println("a a:S b:N");
        fileA.println("b x:S y:N");
        fileA.println("c x:S y:N");
        fileA.println("trace");
        fileA.println("a abc 1");
        fileA.println("b abc 2");
        fileA.println("c abc 3");
        fileA.println("trace");
        fileA.println("c abc 3");
        fileA.println("a abc 2");
        fileA.println("b abc 1");
        fileA.flush();


        fileB.println("types");
        fileB.println("a a:S b:N");
        fileB.println("b x:S y:N");
        fileB.println("c x:S y:N");
        fileB.println("trace");
        fileB.println("a * 1");
        fileB.println("c * *");
        fileB.println("trace");
        fileB.println("c abc 3");
        fileB.println("b abc 2");
        fileB.flush();

        configuration.PREFIX_CLOSED = true;
        TraceSet result = TraceReader.readTraceFile(tempFileA.toPath().toString(), configuration.TOKENIZER);
        TraceSet negs = TraceReader.readTraceFile(tempFileB.toPath().toString(), configuration.TOKENIZER);

        BaseClassifierInference bci = new BaseClassifierInference(result, configuration.ALGORITHM);


        PrefixTreeFactory<WekaGuardMachineDecorator> tptg = new EFSMPrefixTreeFactory(new PayloadMachine(), bci.getClassifiers(), bci.getElementsToInstances());

        NegativePairDataMutator npm = new NegativePairDataMutator(negs.getPos(),result.getPos());
        npm.buildNegs();
        Collection<List<TraceElement>> synthNegs = npm.getNegatives();

        WekaGuardMachineDecorator m = tptg.createPrefixTree(result);

        EFSMAnalysis analysis = new EFSMAnalysis(m);

        for(List<TraceElement> trace : synthNegs){
            assertTrue("Trace does not belong to Machine",!analysis.walk(trace,false,m.getAutomaton()));
        }

    }

    @Test
    public void testBlankNegatives() throws IOException {
        Configuration.reset();
        configuration = Configuration.getInstance();
        tempFileA = File.createTempFile("testtrace", ".txt");
        tempFileA.deleteOnExit();
        fileA = new PrintWriter(tempFileA);

        tempFileB = File.createTempFile("testnegs", ".txt");
        tempFileB.deleteOnExit();
        fileB = new PrintWriter(tempFileB);
        fileA.println("types");
        fileA.println("a a:S b:N");
        fileA.println("b x:S y:N");
        fileA.println("c x:S y:N");
        fileA.println("trace");
        fileA.println("a abc 1");
        fileA.println("b abc 2");
        fileA.println("c abc 3");
        fileA.println("trace");
        fileA.println("c abc 3");
        fileA.println("a abc 2");
        fileA.println("b abc 1");
        fileA.flush();


        fileB.println("types");
        fileB.println("a a:S b:N");
        fileB.println("b x:S y:N");
        fileB.println("c x:S y:N");
        fileB.println("trace");
        fileB.println("a * 1");
        fileB.println("c");
        fileB.println("trace");
        fileB.println("c abc 3");
        fileB.println("b");
        fileB.flush();

        configuration.PREFIX_CLOSED = true;
        TraceSet result = TraceReader.readTraceFile(tempFileA.toPath().toString(), configuration.TOKENIZER);
        TraceSet negs = TraceReader.readTraceFile(tempFileB.toPath().toString(), configuration.TOKENIZER);

        BaseClassifierInference bci = new BaseClassifierInference(result, configuration.ALGORITHM);


        PrefixTreeFactory<WekaGuardMachineDecorator> tptg = new EFSMPrefixTreeFactory(new PayloadMachine(), bci.getClassifiers(), bci.getElementsToInstances());

        NegativePairDataMutator npm = new NegativePairDataMutator(negs.getPos(),result.getPos());
        npm.buildNegs();
        Collection<List<TraceElement>> synthNegs = npm.getNegatives();

        WekaGuardMachineDecorator m = tptg.createPrefixTree(result);

        EFSMAnalysis analysis = new EFSMAnalysis(m);

        for(List<TraceElement> trace : synthNegs){
            assertTrue("Trace does not belong to Machine",!analysis.walk(trace,false,m.getAutomaton()));
        }

    }

    @Test
    public void testFalseNegatives() throws IOException {
        Configuration.reset();
        configuration = Configuration.getInstance();
        tempFileA = File.createTempFile("testtrace", ".txt");
        tempFileA.deleteOnExit();
        fileA = new PrintWriter(tempFileA);

        tempFileB = File.createTempFile("testnegs", ".txt");
        tempFileB.deleteOnExit();
        fileB = new PrintWriter(tempFileB);
        fileA.println("types");
        fileA.println("a a:S b:N");
        fileA.println("b x:S y:N");
        fileA.println("c x:S y:N");
        fileA.println("trace");
        fileA.println("a abc 1");
        fileA.println("b abc 2");
        fileA.println("c abc 1");
        fileA.println("trace");
        fileA.println("c abc 3");
        fileA.println("a abc 2");
        fileA.println("b abc 1");
        fileA.flush();


        fileB.println("types");
        fileB.println("a a:S b:N");
        fileB.println("b x:S y:N");
        fileB.println("c x:S y:N");
        fileB.println("trace");
        fileB.println("a * 1");
        fileB.println("b");
        fileB.println("trace");
        fileB.println("c abc 3");
        fileB.println("a");
        fileB.flush();

        configuration.PREFIX_CLOSED = true;
        TraceSet result = TraceReader.readTraceFile(tempFileA.toPath().toString(), configuration.TOKENIZER);
        TraceSet negs = TraceReader.readTraceFile(tempFileB.toPath().toString(), configuration.TOKENIZER);

        BaseClassifierInference bci = new BaseClassifierInference(result, configuration.ALGORITHM);


        PrefixTreeFactory<WekaGuardMachineDecorator> tptg = new EFSMPrefixTreeFactory(new PayloadMachine(), bci.getClassifiers(), bci.getElementsToInstances());

        NegativePairDataMutator npm = new NegativePairDataMutator(negs.getPos(),result.getPos());
        npm.buildNegs();
        Collection<List<TraceElement>> synthNegs = npm.getNegatives();

        WekaGuardMachineDecorator m = tptg.createPrefixTree(result);

        EFSMAnalysis analysis = new EFSMAnalysis(m);

        for(List<TraceElement> trace : synthNegs){
            boolean belongs = analysis.walk(trace,false,m.getAutomaton());
            assertTrue("Trace belongs to Machine",belongs);
        }

    }

}
