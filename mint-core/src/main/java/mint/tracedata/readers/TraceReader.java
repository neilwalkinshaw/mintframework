/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mint.tracedata.readers;


import org.apache.log4j.Logger;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.types.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


/**
 * Assumes trace file is structured as follows:
 * 
 * types
 * elementa name:type_1 name:type_2 name:type 3
 * elementb name:type_2 name:type_4 name:type 3
 * ...
 * trace
 * elements 
 * elementa 4 d beta
 * elementb d 1.0 alpha
 * ...
 * trace
 * elementa 4 d beta
 * elementb d 1.0 alpha
 * ...
 * ...
 * 
 * where the types can be "N" for number and S for "string"
 * 
 * Note that returns are not explicitly modelled in the trace. However, they can
 * be modelled by inserting a line "return [return value]" if needed.
 */


public class TraceReader {
	
	private final static Logger LOGGER = Logger.getLogger(TraceReader.class.getName());

	public static TraceSet readTraceFile(String traceFile, String splitter) throws IOException {
	 	File f = new File(traceFile);
        BufferedReader r = new BufferedReader(new FileReader(f));
        TraceSet traces = readTrace(r, splitter);
        r.close();
        return traces;
    }

	/**
	 * @param r
	 * @return
	 */
	protected static TraceSet readTrace(BufferedReader r, String splitter) {
		TraceSet traces = new TraceSet();
        try {
        	Map<String,VariableAssignment<?>[]> types = new HashMap<String,VariableAssignment<?>[]>();
			String line;
			List<TraceElement> trace = new ArrayList<TraceElement>();
			TraceElement last = null;
			while((line = r.readLine())!=null){
                if(line.startsWith("#")){
                    continue;
                }

				if(line.startsWith("types")){
					types = new HashMap<String,VariableAssignment<?>[]>();
					while((line = r.readLine())!=null && !(line.startsWith("trace"))&& !(line.startsWith("negtrace"))){
						if(line.startsWith("#")) {
							continue; //Skip comments in type definitions.
						}
						String[] tokens = line.split(" ");
						String element = tokens[0];
						VariableAssignment<?>[] typeArray = getTypes(Arrays.copyOfRange(tokens, 1, tokens.length));
						types.put(element, typeArray);
					}
					
				}
                if(line == null)
                    continue;
				if(line.startsWith("trace")){
                    trace = new ArrayList<TraceElement>();
                    traces.addPos(trace);

					last = null;
					continue;
				}
				else if(line.startsWith("negtrace")){
                    trace = new ArrayList<TraceElement>();
                    traces.addNeg(trace);

					last = null;
					continue;
				}
				else{
					String[] tokens = line.split(splitter);
					if(tokens==null||tokens.length==0)
						continue;
					else if(tokens[0].isEmpty())
						continue;
					SimpleTraceElement el = generateSimpleTraceElement(tokens, types);
					trace.add(el);
					if(last !=null)
						last.setNext(el);
					last = el;
				}
			}

			r.close();
		} catch (IOException e) {
			LOGGER.error("File reading error: "+e.toString());
			
		}
		return traces;
	}

	private static VariableAssignment<?>[] getTypes(String[] strings) {
		VariableAssignment<?>[] typeArray = new VariableAssignment[strings.length];
		for (int i = 0; i<strings.length;i++) {
			String current = strings[i];
			int splitIndex = current.indexOf(':');
			String name = current.substring(0,splitIndex);
			String type = current.substring(splitIndex+1);
			typeArray[i] = createVar(type, name);
		}
		return typeArray;
	}

    private static VariableAssignment<?> createVar(String type, String name) {
        VariableAssignment<?> ret = null;
        if(type.equals("NI")){
            ret = new DoubleVariableAssignment(name);
            ret.setParameter(true);
        }
        else if(type.equals("SI")) {
            ret = new StringVariableAssignment(name);
            ret.setParameter(true);
        }
        else if(type.equals("II")) {
            ret = new IntegerVariableAssignment(name);
            ret.setParameter(true);
        }
        else if((type.startsWith("N"))||(type.startsWith("D"))){

            if(type.indexOf("[")>=0){
                double upperLimit = upperLimDouble(type.substring(type.indexOf("[")+1,type.indexOf("]")));
                double lowerLimit = lowerLimDouble(type.substring(type.indexOf("[")+1,type.indexOf("]")));
                ret =  new DoubleVariableAssignment(name, lowerLimit, upperLimit);
            }
            else
                ret =  new DoubleVariableAssignment(name);
        }
        else if(type.equals("S"))
            ret = new StringVariableAssignment(name);
		else if(type.equals("B"))
			ret = new BooleanVariableAssignment(name);
        else if(type.startsWith("I"))
            if(type.indexOf("[")>=0){
                int upperLimit = upperLimInteger(type.substring(type.indexOf("[")+1,type.indexOf("]")));
                int lowerLimit = lowerLimInteger(type.substring(type.indexOf("[")+1,type.indexOf("]")));
                ret =  new IntegerVariableAssignment(name, lowerLimit, upperLimit);
            }
            else
            ret = new IntegerVariableAssignment(name);

        return ret;
    }


    /**
     * Takes an input String of form "XX:YY", returns "YY".
     * @param s
     * @return
     */
    private static double upperLimDouble(String s) {
        return Double.parseDouble(s.substring(s.indexOf(":")+1));
    }

    /**
     * Takes an input String of form "XX:YY", returns "XX".
     * @param s
     * @return
     */
    private static double lowerLimDouble(String s) {
        return Double.parseDouble(s.substring(0,s.indexOf(":")));
    }

    /**
     * Takes an input String of form "XX:YY", returns "YY".
     * @param s
     * @return
     */
    private static int upperLimInteger(String s) {
        Double doub =  Double.parseDouble(s.substring(s.indexOf(":")+1));
		return doub.intValue();
    }

    /**
     * Takes an input String of form "XX:YY", returns "XX".
     * @param s
     * @return
     */
    private static int lowerLimInteger(String s) {
        Double doub = Double.parseDouble(s.substring(0,s.indexOf(":")));
		return doub.intValue();
    }

    /**
	 * Generate a SimpleTraceElement. The name is the first token, any subsequent
	 * tokens represent variable values. The map of strings to VariableAssignment returns 
	 * an array of empty VariableAssignments (representing types) for each function.
	 */
	private static SimpleTraceElement generateSimpleTraceElement(String[] tokens, Map<String,
			VariableAssignment<?>[]> types) {
		
		String name = tokens[0];
		VariableAssignment<?>[] sig = types.get(name);
		if(sig == null) {
		    System.err.println("There is no type signature for event \"" + name + "\". Make sure that you included a line with the word \"types\" above your type delcarations.");
		    for(String t : tokens) {
		    	System.out.print(t + " ");
		    }
		    System.out.println("");
		    System.exit(1);
		}
		VariableAssignment<?>[] params = new VariableAssignment[sig.length];
		for(int i = 0; i<sig.length;i++){
			try{

				VariableAssignment<?> va = null;
                if(tokens.length == 1)
                    va = generateNewVariableAssignment(sig[i],null);
                else
                    va = generateNewVariableAssignment(sig[i],tokens[i+1]);
				params[i] = va;
			}
			catch(Exception e){
				e.printStackTrace();
				System.out.println("There has been a mismatch between the specified types and the trace contents. " +
						"\nElement "+i+" in the following line was not expected:");
				for (String token : tokens) {
					System.out.print(token+" ");
				}
				System.out.print("\n\nExpecting:\n" + name);
				for(VariableAssignment<?> s: sig) {
				    System.out.print(" " + s.toString());
				}
				System.out.println("");
				System.exit(2);
			}
			
		}
		return new SimpleTraceElement(name,params);
		
	}

	private static VariableAssignment<?> generateNewVariableAssignment(
			VariableAssignment<?> variableAssignment, String value) {

		return variableAssignment.createNew(variableAssignment.getName(), value);
	}
	
	
	 
}
