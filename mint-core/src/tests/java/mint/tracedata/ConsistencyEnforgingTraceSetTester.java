package mint.tracedata;

import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.VariableAssignment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ConsistencyEnforgingTraceSetTester {


	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConsistencyEnforcingTestSet() {
		ConsistencyEnforcingTraceSet cets = new ConsistencyEnforcingTraceSet();
		cets.addPos(generateTraceA());
		cets.addPos(generateTraceB());
		for(List<TraceElement> pos : cets.getPos()){
			for(TraceElement te: pos){
				if(te.getName().equals("x")){
					assertTrue(te.getData().size() == 3);
				}
				else if(te.getName().equals("y")){
					assertTrue(te.getData().size() == 2);
				}
			}
		}
	}

	private List<TraceElement> generateTraceA() {
		List<TraceElement> list = new ArrayList<TraceElement>();
		List<VariableAssignment<?>> vars = new ArrayList<VariableAssignment<?>>();
		vars.add(new DoubleVariableAssignment("a",7D));
		vars.add(new DoubleVariableAssignment("b",8D));
		vars.add(new DoubleVariableAssignment("c",9D));
		TraceElement te = new SimpleTraceElement("x",vars);
		list.add(te);
		List<VariableAssignment<?>> vars2 = new ArrayList<VariableAssignment<?>>();
		
		vars2.add(new DoubleVariableAssignment("j",8D));
		TraceElement te2 = new SimpleTraceElement("y",vars2);
		list.add(te2);
		return list;
	}
	
	private List<TraceElement> generateTraceB() {
		List<TraceElement> list = new ArrayList<TraceElement>();
		List<VariableAssignment<?>> vars = new ArrayList<VariableAssignment<?>>();
		vars.add(new DoubleVariableAssignment("a",7D));
		vars.add(new DoubleVariableAssignment("b",8D));
		TraceElement te = new SimpleTraceElement("x",vars);
		list.add(te);
		List<VariableAssignment<?>> vars2 = new ArrayList<VariableAssignment<?>>();
		vars2.add(new DoubleVariableAssignment("j",8D));
		vars2.add(new DoubleVariableAssignment("k",7D));
		TraceElement te2 = new SimpleTraceElement("y",vars2);
		list.add(te2);
		return list;
	}

}
