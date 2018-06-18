package mint.visualise.d3;

import mint.model.Machine;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Machine2JSONTransformer {
	
	public class Transition {
		String from,to,label;
		
		public Transition(String from, String to, String label){
			this.from = from;
			this.to=to;
			this.label=label;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((from == null) ? 0 : from.hashCode());
			result = prime * result + ((to == null) ? 0 : to.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Transition other = (Transition) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (from == null) {
				if (other.from != null)
					return false;
			} else if (!from.equals(other.from))
				return false;
			if (to == null) {
				if (other.to != null)
					return false;
			} else if (!to.equals(other.to))
				return false;
			return true;
		}

		private Machine2JSONTransformer getOuterType() {
			return Machine2JSONTransformer.this;
		}
		
		
	}
	
	private Set<Transition> doneTransitions;
	
	public Machine2JSONTransformer(){
		doneTransitions = new HashSet<Transition>();
	}

	public void buildMachine(Machine m, File output){
		try {
			FileWriter fw = new FileWriter(output);
			PrintWriter out = new PrintWriter(fw);
			
			JSONObject root = buildMachine(m);
			root.writeJSONString(out);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public JSONObject buildMachine(Machine<Set<TraceElement>> machine){
		JSONObject m = new JSONObject();
		JSONArray nodes = new JSONArray();
		for(Integer state : machine.getStates()){
			nodes.add(node(state));
		}
		JSONArray links = new JSONArray();
        for(Integer state : machine.getStates()){
            Map<Integer,Transition> done = new HashMap<Integer,Transition>();
            for(DefaultEdge de : machine.getAutomaton().getOutgoingTransitions(state)){

                Transition newTrans = null;
                if(done.containsKey(machine.getAutomaton().getTransitionTarget(de))) {
                    newTrans = done.get(machine.getAutomaton().getTransitionTarget(de));
                    newTrans.label = newTrans.label+"\n"+machine.getLabel(de).trim();

                }
                else {
                    newTrans = new Transition(machine.getAutomaton().getTransitionSource(de).toString(),
                            machine.getAutomaton().getTransitionTarget(de).toString(),
                            machine.getLabel(de).trim());
                    done.put(machine.getAutomaton().getTransitionTarget(de),newTrans);
                }
            }
            for(Transition t: done.values()){
                links.add(transition(t));
            }


        }

		m.put("states", nodes);
		m.put("edges", links);
		return m;
	}
	
	private JSONObject node(Integer state) {
		JSONObject ob = new JSONObject();
		ob.put("id", Integer.toString(state));
		ob.put("value", generateLabel(Integer.toString(state)));
		return ob;
	}


    private JSONObject transition(Transition trans){
        JSONObject t = new JSONObject();
        t.put("u", trans.from);
        t.put("v", trans.to);
        t.put("value", generateLabel(trans.label));
        return t;
    }
	
	private JSONObject generateLabel(String lab){
		JSONObject l = new JSONObject();
		l.put("label", lab);
		return l;
	}
	
	

}
