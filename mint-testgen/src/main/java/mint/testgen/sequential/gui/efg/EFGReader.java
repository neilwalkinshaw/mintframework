package mint.testgen.sequential.gui.efg;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by neilwalkinshaw on 31/08/2017.
 */
public class EFGReader {

    DirectedPseudograph<String,DefaultEdge> graph = new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
    Set<String> initNodes = new HashSet<String>();
    List<String> events = new ArrayList<String>();

    public DirectedPseudograph<String, DefaultEdge> getGraph() {
        return graph;
    }

    public Set<String> getInitNodes() {
        return initNodes;
    }

    public void readFile(File fXmlFile){

        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();

            extractEvents(doc, events);

            NodeList nList = doc.getElementsByTagName("Row");
            System.out.println("----");
            for (int from = 0; from < nList.getLength(); from++) {

                assert(nList.getLength()<=events.size());
                Node nNode = nList.item(from);


                NodeList destList = nNode.getChildNodes();
                List<Node> toNodes = new ArrayList<Node>();
                for(int to = 0; to < destList.getLength(); to++) {
                    Node child = destList.item(to);
                    if (!child.getNodeName().equals("E"))
                        continue;
                    else
                        toNodes.add(child);
                }
                for(int to = 0; to < toNodes.size(); to++){
                    Node child = toNodes.get(to);
                    Node valNode = child.getFirstChild();
                    if(valNode == null)
                        continue;
                    String val = valNode.getNodeValue();

                    if(!val.equals("0")){
                        graph.addEdge(events.get(from),events.get(to));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void extractEvents(Document doc, List<String> events) {
        NodeList nList = doc.getElementsByTagName("Event");
        System.out.println(nList.getLength());
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            Node idNode = nNode.getChildNodes().item(1).getFirstChild();
            String eventID = idNode.getNodeValue();

            Node initNode = nNode.getChildNodes().item(7).getFirstChild();
            String initial = initNode.getNodeValue();
            if(initial.equals("true"))
                initNodes.add(eventID);
            events.add(eventID);
            graph.addVertex(eventID);

        }
    }
}



