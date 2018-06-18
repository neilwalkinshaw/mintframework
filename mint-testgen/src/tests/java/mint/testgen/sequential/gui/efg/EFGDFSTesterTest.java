package mint.testgen.sequential.gui.efg;

import org.junit.Test;

import java.io.File;

/**
 * Created by neilwalkinshaw on 01/09/2017.
 */
public class EFGDFSTesterTest {

    @Test
    public void pathGenerationTest() throws Exception {
        File f = new File("src/tests/resources/Rachota-Guitar.EFG");
        EFGReader efgReader = new EFGReader();
        efgReader.readFile(f);
        EFGDFSTester tester = new EFGDFSTester(efgReader.getGraph(),efgReader.getInitNodes(),true);

    }

}