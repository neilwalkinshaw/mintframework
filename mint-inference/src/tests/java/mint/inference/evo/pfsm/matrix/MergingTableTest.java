package mint.inference.evo.pfsm.matrix;

import org.apache.log4j.BasicConfigurator;
import mint.Configuration;
import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.prefixtree.FSMPrefixTreeFactory;
import mint.model.prefixtree.PrefixTreeFactory;
import mint.tracedata.TraceSet;
import mint.tracedata.readers.TraceReader;
import mint.visualise.dot.DotGraphWithLabels;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

/**
 * Created by neilwalkinshaw on 19/05/2016.
 */
public class MergingTableTest {

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


    @Test
    public void testMergingTable() throws IOException {

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

        TraceSet result = TraceReader.readTraceFile(tempFile.toPath().toString(), configuration.TOKENIZER);

        PrefixTreeFactory<?> tptg;
        tptg = new FSMPrefixTreeFactory(new PayloadMachine());
        PayloadMachine tree = (PayloadMachine)tptg.createPrefixTree(result);

        MergingTable mt = new MergingTable(result,tree.getStates().size());

        mt.randomise(new Random(0),10);

        PayloadMachine payLoadMachine = mt.getMergedMachine();

        System.out.println(DotGraphWithLabels.summaryDotGraph(payLoadMachine));



    }

}
