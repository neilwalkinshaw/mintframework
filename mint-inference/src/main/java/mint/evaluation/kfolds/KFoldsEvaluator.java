/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mint.evaluation.kfolds;

import org.apache.log4j.Logger;
import mint.Configuration;
import mint.tracedata.TraceElement;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;


public class KFoldsEvaluator {
	
	private final static Logger LOGGER = Logger.getLogger(KFoldsEvaluator.class.getName());

	
	protected final Collection<List<TraceElement>> trace, negTrace,eval;
	protected int seed,tail;
	protected final String name;

	
	public KFoldsEvaluator(String name, Collection<List<TraceElement>> trace, Collection<List<TraceElement>> negTrace, int seed, int tail, Collection<List<TraceElement>> eval){
		this.trace = new HashSet<List<TraceElement>>();
		this.trace.addAll(trace);
		this.seed = seed;
		this.name = name;
		this.tail = tail;
		this.negTrace = negTrace;
		this.eval = eval;
	}
	
	
	public void kfolds(int folds, boolean data){
		LOGGER.info("Running K-Folds experiments for k="+tail);
		if(folds>trace.size()){
			LOGGER.error("Incorrect number of folds specified.");
		}
        List<List> results = new ArrayList<List>();
        Configuration.Data[] algos = new Configuration.Data[]{Configuration.Data.J48,
                Configuration.Data.AdaBoostDiscrete, Configuration.Data.JRIP, Configuration.Data.NaiveBayes};
		if(data){

            for(int i = 0; i<algos.length;i++){
                for(Configuration.Strategy s: Configuration.Strategy.values()) {
					if(s.equals(Configuration.Strategy.exhaustive)) // skip exhaustive :-)
						continue;
                    Experiment a = generateExperiment(folds, algos, i, true,s);
                    results.add(a.call());
                }
            }
        }
		else{
            for(Configuration.Strategy s: Configuration.Strategy.values()) {
				if(!s.equals(Configuration.Strategy.redblue)) // skip exhaustive :-)
					continue;
                Experiment a = generateExperiment(folds, algos, 0, false,s);
                results.add(a.call());
            }
        }
		//Experiment nodata = generateExperiment(folds, algos, 0, false);
		//results.add(nodata.call());

		
		for(int i = 0; i< results.size();i++){
			List<Object> outcomes = results.get(i);
			output(outcomes);	
		}
		LOGGER.info("Completed K-Folds experiments for k="+tail);
	}

	protected Experiment generateExperiment(int folds,
			Configuration.Data[] algos, int i, boolean data, Configuration.Strategy strategy) {
        return new ProbabilisticExperiment(name, new Random(seed),trace,folds,algos[i],seed, tail, data,strategy);
	}
	
	private void output(List res) {
		FileWriter fWriter = null;
	    BufferedWriter writer = null;
	    try {
	        fWriter = new FileWriter(name+".csv",true);
	        writer = new BufferedWriter(fWriter);
	        for (Object result : res) {

				writer.append(result.toString()+"\n");
	        	LOGGER.debug("WRITING RESULT" + result.toString());
			}
	        writer.close();
	    } catch (Exception e) {
			e.printStackTrace();
	    }
		
	}
	
	

	

}
