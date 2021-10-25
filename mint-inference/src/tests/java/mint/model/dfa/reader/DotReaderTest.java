package mint.model.dfa.reader;

import mint.model.Machine;
import mint.model.dfa.TraceDFA;
import mint.visualise.dot.DotGraphWithLabels;
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
        DotReader reader = new DotReader(FileSystems.getDefault().getPath("src/tests/resources/GnuTLS_3.3.12_client_full.dot"),"2");
        Machine model = reader.getImported();
        System.out.println(DotGraphWithLabels.summaryDotGraph(model));
    }

    @Test
    public void testReader2(){
        DotReader reader = new DotReader(FileSystems.getDefault().getPath("/Users/neil/Google Drive/Research/Software/SubjectSystems/DynamicLstarM/mealyInference/Benchmark/MQTT/ActiveMQ/single_client.dot"),"2");
        Machine<Integer> model = reader.getImported();
        for(Integer state : model.getStates()){
            if(model.getAutomaton().getOutgoingTransitions(state).size() == 0)
                System.out.println("STATE "+state+" is a problem");
            //assertTrue(model.getAutomaton().getOutgoingTransitions(state).size()>0);
        }
        //System.out.println(DotGraphWithLabels.summaryDotGraph(model));
    }




}