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
import org.apache.log4j.Logger;
import mint.Configuration;
import mint.evaluation.kfolds.KFoldsEvaluator;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.readers.TraceReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EDSMSimpleDataEvaluator {

    /**
     * Carry out k-folds evaluation, where positive traces are read from one trace file, and negative traces
     * are read from the other.
     */
	
	private final static Logger LOGGER = Logger.getLogger(EDSMSimpleDataEvaluator.class.getName());
	
	
	public static void main(String[] args){
		BasicConfigurator.configure();
		Configuration configuration = Configuration.getInstance();
		Logger.getRootLogger().setLevel(Level.DEBUG);
		try {
			LOGGER.info("Reading trace files");
			TraceSet posSet = TraceReader.readTraceFile(args[0],configuration.TOKENIZER);

            List<List<TraceElement>> pos = new ArrayList<List<TraceElement>>();
            pos.addAll(posSet.getPos());
            Collections.shuffle(pos);
            if(args.length>3)
                reduceToSize(pos, Integer.parseInt(args[3]));

			List<List<TraceElement>> neg = new ArrayList<List<TraceElement>>();
            neg.addAll(readNegs(args[1]));
            Collections.shuffle(neg);

			List<List<TraceElement>> eval = pos;
			configuration.PREFIX_CLOSED = true;
			int folds = Integer.parseInt(args[2]);

			if(args.length>4)
				reduceToSize(neg, Integer.parseInt(args[4]));
			for(int j = 0;j<5;j++){
				LOGGER.info("k="+j);
				KFoldsEvaluator kfolds = new KFoldsEvaluator(args[0],pos,neg, 0,j);
				kfolds.kfolds(folds,true);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	protected static Collection<List<TraceElement>> readNegs(String negFile) throws IOException {
		Collection<List<TraceElement>> negs = TraceReader.readTraceFile(negFile,Configuration.getInstance().TOKENIZER).getPos();

		return negs;
	}



	public static void reduceToSize(Collection<List<TraceElement>> pos, int i) {
		
		List<List<TraceElement>> temp = new ArrayList<List<TraceElement>>();
		temp.addAll(pos);
		Collections.shuffle(temp);
		pos.clear();
		pos.addAll(temp.subList(0, i));
		
		assert(pos.size() == i);
		
	}

}
