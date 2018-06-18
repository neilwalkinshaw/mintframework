package mint.inference.efsm;

import org.apache.log4j.BasicConfigurator;
import mint.Configuration;
import mint.Configuration.Strategy;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.inference.efsm.scoring.BasicScorer;
import mint.inference.efsm.scoring.scoreComputation.ComputeScore;
import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.prefixtree.FSMPrefixTreeFactory;
import mint.model.prefixtree.PrefixTreeFactory;
import mint.tracedata.TraceSet;
import mint.tracedata.readers.TraceReader;
import mint.visualise.dot.DotGraphWithLabels;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class EDSMMergerTester {

	Machine m;
	File tempFile;
    PrintWriter file;
    Configuration configuration;

	@Before
	public void setUp() throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
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
	public void testBigBranchExhaustive() throws IOException, InterruptedException {
		file.println("types");
        file.println("a a:S b:N");
        file.println("b x:S y:N");
        file.println("c x:S y:N");
        file.println("d x:S y:N");
        file.println("trace");
        file.println("a abc 2");
        file.println("a abc 2");
        file.println("trace");
        file.println("a abc 2");
        file.println("c abc 2");
        file.println("trace");
        file.println("b abc 2");
        file.println("c abc 2");
        file.println("trace");
        file.println("b abc 2");
        file.println("a abc 2");
        file.flush();


        configuration.PREFIX_CLOSED = true;
        configuration.K = 1;
        configuration.STRATEGY = Strategy.exhaustive;
        TraceSet result = TraceReader.readTraceFile(tempFile.toPath().toString(), configuration.TOKENIZER);

		PrefixTreeFactory<?> tptg;

		tptg = new FSMPrefixTreeFactory(new PayloadMachine());
		
		Machine tree = tptg.createPrefixTree(result);
		
		BasicScorer<SimpleMergingState<Machine>,ComputeScore> rbs = new BasicScorer<SimpleMergingState<Machine>,ComputeScore>(configuration.K, new ComputeScore());
		
		SimpleMergingState<Machine> ms = new SimpleMergingState<Machine>(tree);



        EDSMMerger<Machine,SimpleMergingState<Machine>> merger = new EDSMMerger<Machine,SimpleMergingState<Machine>>(rbs,ms);
		
		
		System.out.println(DotGraphWithLabels.summaryDotGraph(merger.infer()));
		//assertTrue(score == 4);

	}


}
