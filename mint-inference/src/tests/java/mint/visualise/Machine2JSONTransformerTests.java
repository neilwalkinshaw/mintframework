package mint.visualise;

import mint.Configuration;
import mint.app.Mint;
import mint.model.Machine;
import mint.tracedata.TraceSet;
import mint.tracedata.readers.TraceReader;
import mint.visualise.d3.Machine2JSONTransformer;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Machine2JSONTransformerTests {
	
	Machine m;
	Configuration configuration;

    @Before
    public void setup() throws FileNotFoundException, IOException {
    	Configuration.reset();
		configuration = Configuration.getInstance();
    	TraceSet posSet = TraceReader.readTraceFile("testlogs"
                + System.getProperty("file.separator")+"pump"+ System.getProperty("file.separator")
                +"minePump2", configuration.TOKENIZER);
		m = Mint.infer(posSet);
		
    }

	@Test
	public void testMachineTransformation() throws IOException {
		
		Machine2JSONTransformer builder = new Machine2JSONTransformer();
        builder.buildMachine(m, new File(configuration.TMP_PREFIX+"test.json"));
	}

}
