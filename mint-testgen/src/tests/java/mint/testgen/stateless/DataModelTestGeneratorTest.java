/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package mint.testgen.stateless;

import com.microsoft.z3.Z3Exception;
import org.apache.log4j.BasicConfigurator;
import mint.Configuration;
import mint.Configuration.Data;
import mint.inference.constraints.WekaConstraintParser;
import mint.inference.constraints.expression.Expression;
import mint.tracedata.TestIO;
import mint.testgen.stateless.runners.execution.TestRunner;
import mint.testgen.stateless.weka.DataModelTestGenerator;
import mint.tracedata.types.BooleanVariableAssignment;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.VariableAssignment;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class DataModelTestGeneratorTest {
	
	Set<VariableAssignment<?>> vars;

	@Before
	public void setUp() throws Exception {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void complexTest() throws Z3Exception {
		vars = new HashSet<VariableAssignment<?>>();
		vars.add(new BooleanVariableAssignment("pump"));
		vars.add(new DoubleVariableAssignment("methane"));
		vars.add(new DoubleVariableAssignment("water"));
		String complexModel = readModelString("src/tests/resources/complexTree");
		DataModelTestGenerator dmt = new DataModelTestGenerator("complexTest",complexModel,vars,Configuration.Data.J48);
		Collection<TestIO> tests = dmt.generateTestCases(10);
		for(TestIO io:tests){
			System.out.println(io);
		}
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
	
	@Test
	public void FaultyParseTest2(){
		Configuration config = Configuration.getInstance();
		config.RESPECT_LIMITS = true;
		JSONParser jp = new JSONParser();
		vars = new HashSet<VariableAssignment<?>>();
		try {
			JSONObject o = (JSONObject)jp.parse(new FileReader("src/tests/resources/gcc.json"));
			vars.addAll(TestRunner.readVariables((JSONArray) o.get("parameters")));
		}
		catch(Exception e){e.printStackTrace();}
		String complexModel = readModelString("src/tests/resources/gcc-tree");
		Collection<Expression> exp = WekaConstraintParser.parseJ48ExpressionForAllOutcomes(complexModel, vars);
		for(Expression e:exp){
			System.out.println(e);
		}
		DataModelTestGenerator tester = new DataModelTestGenerator("name", complexModel, vars, Data.J48);
		tester.generateTestCases();
		
	}

    @Test
    public void BMITest(){
        Configuration config = Configuration.getInstance();
        config.RESPECT_LIMITS = true;
        JSONParser jp = new JSONParser();
        vars = new HashSet<VariableAssignment<?>>();
        vars.add(new DoubleVariableAssignment("height"));
        vars.add(new DoubleVariableAssignment("weight"));

        String complexModel = readModelString("src/tests/resources/BMITree");
        Collection<Expression> exp = WekaConstraintParser.parseJ48ExpressionForAllOutcomes(complexModel, vars);
        for(Expression e:exp){
            System.out.println(e);
        }
        DataModelTestGenerator tester = new DataModelTestGenerator("name", complexModel, vars, Data.J48);
        List<TestIO> inputs = tester.generateTestCases();
        System.out.println(inputs);

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
