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

import mint.Configuration;
import mint.evaluation.kfolds.KFoldsEvaluator;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SLProbabilitiesEvaluator {
	
	public static void main(String[] args){
		BasicConfigurator.configure();
		Configuration configuration = Configuration.getInstance();
		TraceSet posSet = getTracesFromSMVFile(args[2]);
		runExperiment(args[0],Integer.parseInt(args[1]), configuration, posSet);

		
	}

	private static TraceSet getTracesFromSMVFile(String arg) {
		NuSMVFSMReader reader = new NuSMVFSMReader();
		reader.readFile(new File(arg));
	return null;
	}

	private static void runExperiment(String label, int folds, Configuration configuration, TraceSet posSet) {
		Collection<List<TraceElement>> pos = posSet.getPos();
		Collection<List<TraceElement>> neg = new HashSet<List<TraceElement>>();
		Collection<List<TraceElement>> eval = pos;
		configuration.PREFIX_CLOSED = true;
		configuration.LOGGING = Level.ALL;
		for(int k = 1; k<15; k++) {
			Set<List<TraceElement>> sizeP = new HashSet<List<TraceElement>>();
			sizeP.addAll(pos);
			KFoldsEvaluator kfolds = new KFoldsEvaluator(label, sizeP, neg, 0, k, eval);
			kfolds.kfolds(folds,false);
		}
	}
}
