package mint.tracedata;

import mint.tracedata.readers.DaikonTraceReader;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class DaikonTraceReaderTester {

	@Before
	public void setUp() throws Exception {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}

	@After
	public void tearDown() throws Exception {
	}


    @Test
    public void jeditReaderWritertest() throws IOException {
        DaikonTraceReader dtr = new DaikonTraceReader(new File("src/tests/resources/jEdit2.dtrace"),"java.util.StringTokenizer.","java.util.StringTokenizer.StringTokenizer(java.lang.String):::ENTER");
        TraceSet traces = dtr.getTraces();
        TraceToFile ttf = new TraceToFile(traces.getPos(),false);
        ttf.writeToFile(new File(System.getProperty("java.io.tmpdir")+ System.getProperty("path.separator")+"jedit.trace"));

    }


}
