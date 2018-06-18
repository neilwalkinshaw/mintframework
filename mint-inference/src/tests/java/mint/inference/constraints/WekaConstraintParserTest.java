package mint.inference.constraints;

import mint.inference.constraints.expression.Expression;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.VariableAssignment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class WekaConstraintParserTest {
	
	Set<VariableAssignment<?>> vars;

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void complexTest() {
		vars = new HashSet<VariableAssignment<?>>();
		vars.add(new BooleanVariableAssignment("pump"));
		vars.add(new DoubleVariableAssignment("methane"));
		vars.add(new DoubleVariableAssignment("water"));
		String complexModel = readModelString("src/tests/resources/complexTree");
		WekaConstraintParser.parseJ48Expression(complexModel, "critical",vars);
	}
	
	@Test
	public void complexExpressionTest() {
		vars = new HashSet<VariableAssignment<?>>();
		vars.add(new BooleanVariableAssignment("pump"));
		vars.add(new DoubleVariableAssignment("methane"));
		vars.add(new DoubleVariableAssignment("water"));
		String complexModel = readModelString("src/tests/resources/complexTree");
		Expression exp = WekaConstraintParser.parseJ48Expression(complexModel, "critical",vars);
		System.out.println(exp);
	}
	
	@Test
	public void JRIPParseTest() {
		vars = new HashSet<VariableAssignment<?>>();
		vars.add(new BooleanVariableAssignment("pump"));
		vars.add(new DoubleVariableAssignment("methane"));
		vars.add(new DoubleVariableAssignment("water"));
		String complexModel = readModelString("src/tests/resources/jripExample");
		Expression exp = WekaConstraintParser.parseJRIPExpression(complexModel, "critical",vars);
		System.out.println(exp);
	}
	
	//(above.value<=0.0)&&(above.value>0.0)	
	@Test
	public void FaultyParseTest(){
		vars = new HashSet<VariableAssignment<?>>();
		vars.add(new DoubleVariableAssignment("above.value"));
		vars.add(new DoubleVariableAssignment("below.value"));
		String complexModel = readModelString("src/tests/resources/complexTree2");
		Expression exp = WekaConstraintParser.parseJ48Expression(complexModel, "push(I)Z",vars);
		System.out.println(exp);
		assertEquals(false,exp.toString().contains("(above.value<=0)&&(above.value>0)"));
	}

	
	public static String readModelString(String name){
		File f = new File(name);
		String ret = new String();
		try {
			BufferedReader br =  new BufferedReader(new FileReader(f));
			String line = br.readLine();
			while(line !=null){
				ret+=line+"\n";
				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	

}
