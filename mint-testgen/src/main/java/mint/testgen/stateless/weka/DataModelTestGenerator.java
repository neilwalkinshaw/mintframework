/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package mint.testgen.stateless.weka;

import com.microsoft.z3.Z3Exception;
import org.apache.log4j.Logger;
import mint.Configuration;
import mint.inference.constraints.WekaConstraintParser;
import mint.inference.constraints.expression.Expression;
import mint.inference.constraints.expression.convertors.ExpressionToZ3;
import mint.tracedata.TestIO;
import mint.tracedata.types.VariableAssignment;
import weka.classifiers.Classifier;

import java.util.*;


public class DataModelTestGenerator extends WekaModelTestGenerator {
	
	protected final Collection<Expression> outcomeExpressions;
	protected Map<Expression,ExpressionToZ3> contextMap;
	
	
	private final static Logger LOGGER = Logger.getLogger(DataModelTestGenerator.class.getName());

	public DataModelTestGenerator(String name, Classifier model, Collection<VariableAssignment<?>> types, Configuration.Data algo){
		super(name,types,model);
		if(algo.equals(Configuration.Data.J48))
			outcomeExpressions = WekaConstraintParser.parseJ48ExpressionForAllOutcomes(model.toString(), types);
		else if(algo.equals(Configuration.Data.JRIP))
			outcomeExpressions = WekaConstraintParser.parseJRIPExpressionForAllOutcomes(model.toString(), types);
		else if(algo.equals(Configuration.Data.M5))
			outcomeExpressions = WekaConstraintParser.parseM5ExpressionForAllOutcomes(model.toString(), types);
		else{
			outcomeExpressions = new HashSet<Expression>();
			LOGGER.warn("No suitable model inference engine selected.");
		}
		contextMap = new HashMap<Expression,ExpressionToZ3>();

	}
	
	public DataModelTestGenerator(String name, String model, Collection<VariableAssignment<?>> types, Configuration.Data algo){
		super(name,types,null);
		if(algo.equals(Configuration.Data.J48))
			outcomeExpressions = WekaConstraintParser.parseJ48ExpressionForAllOutcomes(model, types);
		else if(algo.equals(Configuration.Data.JRIP))
			outcomeExpressions = WekaConstraintParser.parseJRIPExpressionForAllOutcomes(model, types);
		else if(algo.equals(Configuration.Data.M5))
			outcomeExpressions = WekaConstraintParser.parseM5ExpressionForAllOutcomes(model, types);
		else{
			outcomeExpressions = new HashSet<Expression>();
			LOGGER.warn("No suitable model inference engine selected.");
		}
		contextMap = new HashMap<Expression,ExpressionToZ3>();

	}
	
	public List<TestIO> generateTestCases(int howMany){
		List<TestIO> tests = new ArrayList<TestIO>();
		List<Expression> expList = new ArrayList<Expression>();
		expList.addAll(outcomeExpressions);
		int size = outcomeExpressions.size();
		int i = 0;
		for(int j = 0; j < expList.size(); j++){
			assert(outcomeExpressions.size()==size);
			if(j == expList.size()-1)
				j = 0;
			
			Expression target = expList.get(j);
			ExpressionToZ3 convertor;
			try {
				convertor = getConvertor(target);
				boolean solved = convertor.solve(true);
				//target.setNegated(!target.isNegated());
				if(!solved){
					continue;
				}
				i++;
				if(i == howMany)
					break;
				Collection<VariableAssignment<?>> sol = convertor.getVars();
				tests.add(generateTestIO(sol));
			} catch (Z3Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	
		return tests;
	}
	
	public List<TestIO> generateTestCases(){
		List<TestIO> tests = new ArrayList<TestIO>();
		List<Expression> expList = new ArrayList<Expression>();
		expList.addAll(outcomeExpressions);
		for(int j = 0; j < expList.size(); j++){
			Expression target = expList.get(j);
			ExpressionToZ3 convertor;
			try {
				convertor = getConvertor(target);
				boolean solved = convertor.solve(true);
				//target.setNegated(!target.isNegated());
				if(!solved){
					continue;
				}			
				Collection<VariableAssignment<?>> sol = convertor.getVars();
				tests.add(generateTestIO(sol));
			} catch (Z3Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	
		return tests;
	}

	

	private ExpressionToZ3 getConvertor(Expression target) throws Z3Exception {
		if(contextMap.containsKey(target))
			return contextMap.get(target);
		else
		{
			Configuration configuration = Configuration.getInstance();

			ExpressionToZ3 context = new ExpressionToZ3(target,configuration.RESPECT_LIMITS);
			contextMap.put(target, context);
			return context;
		}
	}
	
	
	
}
