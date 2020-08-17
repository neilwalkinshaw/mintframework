package mint.model.dfa.reader;

import mint.model.dfa.TraceDFA;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.nio.GraphImporter;
import org.jgrapht.nio.dot.DOTImporter;
import org.junit.Test;

import java.io.StringReader;
import java.nio.file.FileSystems;

import static org.junit.Assert.*;

public class DotReaderTest {

    @Test
    public void testReader(){
        DotReader reader = new DotReader(FileSystems.getDefault().getPath("src/tests/resources/GnuTLS_3.3.12_client_full.dot"));
        TraceDFA model = reader.getImported();
        System.out.println(model);
    }


}