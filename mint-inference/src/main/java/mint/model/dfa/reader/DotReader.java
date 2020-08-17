package mint.model.dfa.reader;

import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.dot.DOTImporter;
import org.jgrapht.util.SupplierUtil;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DotReader {

    DirectedPseudograph<String, DefaultEdge> imported;
    Map<DefaultEdge, Map<String, Attribute>> edgeAttrs;

    public DotReader(Path path){
        String dotCode = null;
        try {
            dotCode = Files.readString(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        edgeAttrs = new HashMap<>();
        importer.addEdgeAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = edgeAttrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                edgeAttrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });




        imported = new DirectedPseudograph<>(SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        importer.importGraph(imported,
                new StringReader(dotCode));

    }



    public TraceDFA<String> getImported(){
        TraceDFA<String> machine = new TraceDFA<>();
        for(DefaultEdge e : imported.edgeSet()){
            Attribute label = edgeAttrs.get(e).get("label");
            TransitionData<String> td = new TransitionData<>(label.toString(),label.toString());
            machine.addState(Integer.parseInt(imported.getEdgeSource(e)));
            machine.addState(Integer.parseInt(imported.getEdgeTarget(e)));
            machine.addTransition(Integer.parseInt(imported.getEdgeSource(e)),
                    Integer.parseInt(imported.getEdgeTarget(e)),td);
        }
        machine.setInitialState(0);
        return machine;
    }

}
