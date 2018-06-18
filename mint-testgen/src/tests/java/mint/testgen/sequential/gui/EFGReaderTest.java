package mint.testgen.sequential.gui;

import mint.testgen.sequential.gui.efg.EFGDFSTester;
import mint.testgen.sequential.gui.efg.EFGReader;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.junit.Test;

import java.io.File;
import java.util.Set;

/**
 * Created by neilwalkinshaw on 31/08/2017.
 */
public class EFGReaderTest {
    @Test
    public void readFile() throws Exception {
        File f = new File("src/tests/resources/Rachota-Guitar.EFG");
        EFGReader efgReader = new EFGReader();
        efgReader.readFile(f);
        DirectedPseudograph<String,DefaultEdge> graph = efgReader.getGraph();
        Set<String> initial = efgReader.getInitNodes();
        EFGDFSTester tester = new EFGDFSTester(graph,initial,true);

    }

}