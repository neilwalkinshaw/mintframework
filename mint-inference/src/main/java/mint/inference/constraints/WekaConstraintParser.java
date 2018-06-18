package mint.inference.constraints;

import org.apache.commons.lang.StringUtils;
import mint.inference.constraints.expression.*;
import mint.tracedata.types.VariableAssignment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class WekaConstraintParser {
	
	
		
		
		/*
		 * Take the decision tree model produced by the J48.toString() method in WEKA,
		 * and return a set of constraints from the tree that are specific to the outcome parameter.
		 * 
		 */
		public static Expression parseJ48Expression(String model, String outcome, Set<VariableAssignment<?>> vars){
			Stack<Expression> constraintStack = new Stack<Expression>();
			BufferedReader br = new BufferedReader(new StringReader(model));
			String line;
			Vector<Expression> constraints = new Vector<Expression>();
			try {
				line = br.readLine();
				while(line!=null){
					if(line.startsWith("==") || line.startsWith("J48")||line.startsWith("MODEL")|| line.startsWith("JRIP")
							|| line.startsWith("--")|| line.startsWith("Number") || line.isEmpty() || line.startsWith("Size")){
						line = br.readLine();
						continue;
					}
					int endInd = line.indexOf(":");
					int barInd = line.lastIndexOf("|");
					int bars = StringUtils.countMatches(line, "|");
					while(constraintStack.size()>bars)
						constraintStack.pop();
					boolean prediction = true;
					if(endInd<0){
						endInd = line.length();
						prediction = false;
					}
					String constraint = line.substring(barInd+1,endInd).trim();
					if(constraint.isEmpty()){
						line = br.readLine();
						continue;
					}
					Atom a = buildAtom(constraint, vars);
					constraintStack.push(a);
					if(prediction == true){
						String predicted = line.substring(endInd+1).trim();
						int parenthesis = predicted.lastIndexOf("(");
						if(parenthesis>0)
						predicted = predicted.substring(0, parenthesis);
						if(predicted.trim().equals(outcome))
							constraints.add(compound(constraintStack,Compound.Rel.AND));
						constraintStack.pop();
					}
					line = br.readLine();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return compound(constraints,Compound.Rel.OR);
		}

		
		/*
		 * Take the decision tree model produced by the J48.toString() method in WEKA,
		 * and return a set of constraints from the tree that are specific to the outcome parameter.
		 * 
		 */
		public static Collection<Expression> parseJ48ExpressionForAllOutcomes(String model, Collection<VariableAssignment<?>> vars){
			Stack<Expression> constraintStack = new Stack<Expression>();
			BufferedReader br = new BufferedReader(new StringReader(model));
			String line;
			Vector<Expression> constraints = new Vector<Expression>();
			try {
				line = br.readLine();
				while(line!=null){
					if(line.startsWith("==") || line.startsWith("J48")||line.startsWith("MODEL")|| line.startsWith("JRIP")
							|| line.startsWith("--")|| line.startsWith("Number") || line.isEmpty() || line.startsWith("Size")){
						line = br.readLine();
						continue;
					}
					int endInd = line.indexOf(":");
					int barInd = line.lastIndexOf("|");
					int bars = StringUtils.countMatches(line, "|");
					while(constraintStack.size()>bars)
						constraintStack.pop();
					boolean prediction = true;
					if(endInd<0){
						endInd = line.length();
						prediction = false;
					}
					String constraint = line.substring(barInd+1,endInd).trim();
					if(constraint.isEmpty()){
						line = br.readLine();
						continue;
					}
					Atom a = buildAtom(constraint, vars);
					constraintStack.push(a);
					if(prediction == true){
						String predicted = line.substring(endInd+1).trim();
						int parenthesis = predicted.lastIndexOf("(");
						if(parenthesis>0)
						predicted = predicted.substring(0, parenthesis);
						constraints.add(compound(constraintStack,Compound.Rel.AND));
						constraintStack.pop();
					}
					line = br.readLine();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return constraints;
		}


		private static Atom buildAtom(String constraint, Collection<VariableAssignment<?>> vars) {
			constraint = constraint.replaceAll(" +", " ");
			String[] cons = constraint.split(" ");
			String identifier = cons[0];
			VariableAssignment<?> found = findVariableAssignment(identifier, vars);
			if(found == null)
				return null;
			String rel = cons[1];
			String value = cons[2];
			VariableAssignment<?> var = found.createNew(identifier, value);
			return new Atom(var,rel);
		}


		private static VariableAssignment<?> findVariableAssignment(String identifier, Collection<VariableAssignment<?>> el) {
			VariableAssignment<?> found = null;
			for (VariableAssignment<?> v : el) {
				String id = v.getName();
				if(id.equals(identifier)){
					found = v; 
					break;
				}
			}
			return found;
			
		}


		/*
		 * Take the decision tree model produced by the JRIP.toString() method in WEKA,
		 * and return a set of constraints from the tree that are specific to the outcome parameter.
		 * 
		 */
		public static Expression parseJRIPExpression(String model, String outcome, Set<VariableAssignment<?>> vars){
			BufferedReader br = new BufferedReader(new StringReader(model));
			String line;
			Vector<Expression> constraints = new Vector<Expression>();
			try {
				line = br.readLine();
				while(line!=null){
					if(line.startsWith("==") || line.startsWith("JRIP")||line.startsWith("Number")
							|| line.startsWith("--")|| line.isEmpty()){
						line = br.readLine();
						continue;
					}
					int endInd = line.indexOf("=>");
					
					String constraint = line.substring(0,endInd);
					if(constraint.isEmpty()){
						line = br.readLine();
						continue;
					}
					constraint = constraint.trim();
					String[] split = constraint.split("and");
					List<Expression> atoms = new ArrayList<Expression>();
					for (String s : split) {
						if(s.isEmpty())
							continue;
						s= s.trim();
						atoms.add(buildAtom(s.substring(1,s.length()-1),vars));
					}
					Expression c;
					if(atoms.size()>1)
						c = new Compound(atoms,Compound.Rel.AND);
					else if(atoms.isEmpty()){
						line = br.readLine();
						continue;
					}
					else
						c = atoms.get(0);
					
					
					String predicted = line.substring(endInd+1).trim();
					int parenthesis = predicted.indexOf("(");
					if(parenthesis>0)
					predicted = predicted.substring(0, parenthesis);
					if(predicted.trim().substring(predicted.indexOf("=")+1).trim().equals(outcome))
						constraints.add(c);
					
					line = br.readLine();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return compound(constraints, Compound.Rel.OR);
		}

		/*
		 * Take the decision tree model produced by the J48.toString() method in WEKA,
		 * and return a set of constraints from the tree that are specific to the outcome parameter.
		 * 
		 */
		public static Collection<Expression> parseJRIPExpressionForAllOutcomes(String model, Collection<VariableAssignment<?>> vars){
			BufferedReader br = new BufferedReader(new StringReader(model));
			String line;
			Vector<Expression> constraints = new Vector<Expression>();
			try {
				line = br.readLine();
				while(line!=null){
					if(line.startsWith("==") || line.startsWith("JRIP")||line.startsWith("Number")
							|| line.startsWith("--")|| line.isEmpty()){
						line = br.readLine();
						continue;
					}
					int endInd = line.indexOf("=>");
					
					String constraint = line.substring(0,endInd);
					if(constraint.isEmpty()){
						line = br.readLine();
						continue;
					}
					constraint = constraint.trim();
					String[] split = constraint.split("and");
					List<Expression> atoms = new ArrayList<Expression>();
					for (String s : split) {
						if(s.isEmpty())
							continue;
						s= s.trim();
						atoms.add(buildAtom(s.substring(1,s.length()-1),vars));
					}
					Expression c;
					if(atoms.size()>1)
						c = new Compound(atoms,Compound.Rel.AND);
					else if(atoms.isEmpty()){
						line = br.readLine();
						continue;
					}
					else
						c = atoms.get(0);
					
					
					String predicted = line.substring(endInd+1).trim();
					int parenthesis = predicted.indexOf("(");
					if(parenthesis>0)
					predicted = predicted.substring(0, parenthesis);
					constraints.add(c);
					
					line = br.readLine();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return constraints;
		}
		
		/*
		 * Take the decision tree model produced by the J48.toString() method in WEKA,
		 * and return a set of constraints from the tree that are specific to the outcome parameter.
		 * 
		 */
		public static Collection<Expression> parseM5ExpressionForAllOutcomes(String model, Collection<VariableAssignment<?>> vars){
			Stack<Expression> constraintStack = new Stack<Expression>();
			BufferedReader br = new BufferedReader(new StringReader(model));
			String line;
			Vector<Expression> constraints = new Vector<Expression>();
			try {
				line = br.readLine();
				while(line!=null){
					if(line.startsWith("==") || line.startsWith("M5")||line.startsWith("MODEL")|| line.startsWith("JRIP")
							|| line.startsWith("--")|| line.startsWith("Number") || line.isEmpty() || line.startsWith("Size")
							|| line.startsWith("LM") || line.startsWith(" ") || line.startsWith("(using")|| line.startsWith("output =")){
						line = br.readLine();
						continue;
					}
					int endInd = line.indexOf(":");
					int barInd = line.lastIndexOf("|");
					int bars = StringUtils.countMatches(line, "|");
					while(constraintStack.size()>bars)
						constraintStack.pop();
					boolean prediction = true;
					if(endInd<0){
						endInd = line.length();
						prediction = false;
					}
					String constraint = line.substring(barInd+1,endInd).trim();
					if(constraint.isEmpty()){
						line = br.readLine();
						continue;
					}
					Atom a = buildAtom(constraint, vars);
					if(a == null){
						line = br.readLine();
						continue;
					}
					constraintStack.push(a);
					if(prediction == true){
						//String predicted = line.substring(endInd+1).trim();
						//int parenthesis = predicted.lastIndexOf("(");
						//if(parenthesis>0)
						//predicted = predicted.substring(0, parenthesis);
						constraints.add(compound(constraintStack,Compound.Rel.AND));
						constraintStack.pop();
					}
					line = br.readLine();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return constraints;
		}
		
		private static Expression compound(List<Expression> constraints, Compound.Rel relationship) {
			Compound c = new Compound(relationship);
			for(Expression e:constraints){
				c.add(e);
			}
			return c;
		}

		


		
}
