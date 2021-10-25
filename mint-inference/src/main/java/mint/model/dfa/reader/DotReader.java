package mint.model.dfa.reader;

import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;

import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.dot.DOTImporter;
import org.jgrapht.util.SupplierUtil;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DotReader {

    DirectedPseudograph<String, DefaultEdge> imported;
    Map<DefaultEdge, Map<String, Attribute>> edgeAttrs;
    String init = "";
    Integer initState = 0;
    boolean removeOutput = false;

    public void setRemoveOutput(boolean val){
        removeOutput = val;
    }

    public DotReader(Path path, String initialState){
        this.init = initialState;
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



    public Machine getImported(){
        Machine machine = new PayloadMachine();
        machine.getAutomaton().removeState(machine.getInitialState());
        for(DefaultEdge e : imported.edgeSet()){
            String source = imported.getEdgeSource(e);
            String destination = imported.getEdgeTarget(e);
            Integer sourceState = Integer.parseInt(imported.getEdgeSource(e));
            Integer destinationState = Integer.parseInt(imported.getEdgeTarget(e));

            if(source.equals(init)){
                initState = sourceState;
            }
            else if(destination.equals(init)){
                initState = destinationState;
            }




            TransitionData<String> td = null;
            if(!edgeAttrs.containsKey(e)){
                // edge to initial state
                initState = destinationState;
                machine.getAutomaton().addState(destinationState);
                machine.getAutomaton().setAccept(initState, TraceDFA.Accept.ACCEPT);
            }
            else {
                machine.getAutomaton().addState(sourceState);
                machine.getAutomaton().addState(destinationState);
                machine.getAutomaton().setAccept(sourceState, TraceDFA.Accept.ACCEPT);
                machine.getAutomaton().setAccept(destinationState, TraceDFA.Accept.ACCEPT);
                Attribute label = edgeAttrs.get(e).get("label");
                Set<TraceElement> element = new HashSet<>();
                td = new TransitionData(getLabel(label.toString()), element);
                machine.getAutomaton().addTransition(sourceState,destinationState, td);
            }

        }
        machine.getAutomaton().setInitialState(initState);
        return machine;
    }

    private String getLabel(String toString) {
        if(removeOutput){
            toString = toString.substring(0,toString.indexOf('/'));
        }
        return toString.trim();
    }


}
