package mint.visualise;

import mint.Configuration;
import mint.inference.BaseClassifierInference;
import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.prefixtree.EFSMPrefixTreeFactory;
import mint.model.prefixtree.PrefixTreeFactory;
import mint.tracedata.TraceSet;
import mint.tracedata.readers.TraceReader;
import mint.visualise.d3.Tree2JSONTransformer;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class Tree2JSONTransformerTests {
	
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

	@Test
	public void testBranchTransformation() throws IOException {
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



        TraceSet result = TraceReader.readTraceFile(tempFile.toPath().toString(), configuration.TOKENIZER);

        BaseClassifierInference bci = null;
		PrefixTreeFactory<?> tptg;
		bci = new BaseClassifierInference(result, configuration.ALGORITHM);
		tptg = new EFSMPrefixTreeFactory(new PayloadMachine(), bci.getClassifiers(),bci.getElementsToInstances());
		
		Machine tree = tptg.createPrefixTree(result);
        
		Tree2JSONTransformer builder = new Tree2JSONTransformer();
        builder.buildTree(tree, false);
	}

}
