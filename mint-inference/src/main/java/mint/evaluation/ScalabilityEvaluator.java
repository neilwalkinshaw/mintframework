/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mint.evaluation;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import mint.Configuration;
import mint.evaluation.kfolds.KFoldsEvaluator;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.readers.TraceReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScalabilityEvaluator {
	
	public static void main(String[] args){
		BasicConfigurator.configure();
		Configuration configuration = Configuration.getInstance();
		try {
			TraceSet posSet = TraceReader.readTraceFile(args[0],configuration.TOKENIZER);
			TraceSet negSet = TraceReader.readTraceFile(args[1],configuration.TOKENIZER);
			Collection<List<TraceElement>> pos = posSet.getPos();
			Collection<List<TraceElement>> neg = negSet.getPos();
			Collection<List<TraceElement>> eval = pos;
			configuration.PREFIX_CLOSED = true;
			configuration.LOGGING = Level.ALL;
			int folds = Integer.parseInt(args[2]);
			
			for(int size = 25; size<500; size+=25){
                for(int k = 0; k<3; k++) {
                    Set<List<TraceElement>> sizeP = new HashSet<List<TraceElement>>();
                    sizeP.addAll(pos);
                    EDSMSimpleDataEvaluator.reduceToSize(sizeP, size);
                    KFoldsEvaluator kfolds = new KFoldsEvaluator(args[0] + "scale", sizeP, neg, 0, k);
                    kfolds.kfolds(folds,true);
                }
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
