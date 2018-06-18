package mint.tracedata.readers.ngram;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NgrammerTester {


	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNGramTokenizerWorking() {

		Ngrammer ng = new Ngrammer("And then there were two were two",2);
		assert(ng.ngramDistribution.get("tw").equals(2));
	}



}
