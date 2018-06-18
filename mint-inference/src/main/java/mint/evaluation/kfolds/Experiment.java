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
import mint.Configuration.Data;
import mint.inference.BaseClassifierInference;
import mint.inference.efsm.AbstractMerger;
import mint.inference.efsm.EDSMDataMerger;
import mint.inference.efsm.EDSMMerger;
import mint.inference.efsm.mergingstate.RedBlueMergingState;
import mint.inference.efsm.scoring.RedBlueScorer;
import mint.inference.efsm.scoring.Scorer;
import mint.inference.efsm.scoring.scoreComputation.ComputeScore;
import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.SimpleMachine;
import mint.model.WekaGuardMachineDecorator;
import mint.model.prefixtree.EFSMPrefixTreeFactory;
import mint.model.prefixtree.FSMPrefixTreeFactory;
import mint.model.prefixtree.PrefixTreeFactory;
import mint.model.walk.EFSMAnalysis;
import mint.model.walk.SimpleMachineAnalysis;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;

import java.util.*;
import java.util.concurrent.*;

public class Experiment implements Callable<List<Result>> {

	private final static Logger LOGGER = Logger.getLogger(Experiment.class.getName());


	final protected Random rand;
	final protected TraceSet trace;
    protected TraceSet negTrace;
    protected TraceSet eval;
	final protected int folds,seed, tail;
	final protected Data algo;
	final protected String name;
	final protected boolean data;
	final protected List results;
    final protected Configuration.Strategy strategy;
	
	public Experiment(String name, Random r, Collection<List<TraceElement>> trace, Collection<List<TraceElement>> negTrace, int folds, Data algo, int seed, int tail, boolean data, Configuration.Strategy strategy){
		this.rand = r;
		this.trace = new TraceSet(trace);
		this.folds = folds;
		this.algo = algo;
		this.name = name;
		this.seed = seed;
		this.tail = tail;
		this.data = data;
		results = new ArrayList<Result>();
        if(negTrace!=null)
		    this.negTrace =new TraceSet(negTrace);
        this.strategy = strategy;
	}

	public Collection<List<TraceElement>> getTrace() {
		return trace.getPos();
	}



	public Data getAlgo() {
		return algo;
	}

	public String getName() {
		return name;
	}



	public boolean isData() {
		return data;
	}



	@Override
	public List<Result> call() {
		LOGGER.info("Running experiment for:"+name+","+algo.toString()+","+seed+","+data);
		setConfiguration();
		List<Set<List<TraceElement>>> f = computeFolds(folds);
		//Collections.shuffle(f, rand);
		List<Score> scores = new ArrayList<Score>();
		List<Double> states = new ArrayList<Double>();
		List<Double> transitions = new ArrayList<Double>();
		for(int i = 0; i< folds; i++){
			TraceSet testing = new TraceSet(f.get(i));
			TraceSet training = new TraceSet();
			for(int j = 0; j<folds;j++){
				if(j==i)
					continue;
				training.getPos().addAll(f.get(j));
			}
			
			final long startTime = System.currentTimeMillis();
			final long endTime;
			Machine model = null;
			try {
				TraceSet ev = new TraceSet();
				for (List<TraceElement> tes : testing.getPos()) {
					ev.addPos(tes);
				}
				eval = ev;
				model = learnModel(training);
                if(model == null)
                    continue;
				states.add((double) model.getStates().size());
				transitions.add((double)model.getAutomaton().transitionCount());
				endTime = System.currentTimeMillis();
				final long duration = endTime - startTime;
				Score score = score(model,testing, negTrace);
				score.setDuration(duration);
				scores.add(score);
			} 
			catch(Exception e){
				LOGGER.error(e.toString());
				e.printStackTrace();
				System.exit(0);
			}
		}
		Score means = calculateMeans(scores);
		double meanStates = calculateMean(states);
		double meanTransitions = calculateMean(transitions);
		final Object res = new Result(name,algo.toString(),scores.size(),means.getSensitivity(),means.getSpecificity(),means.getKappa(),means.getDuration(),seed,tail,data,meanStates,meanTransitions,strategy);
		results.add(res);	
		LOGGER.info("Results for:"+name+","+algo.toString()+","+seed+","+data+"\n"+res);
		
		return results;
	}


    private void setConfiguration() {
		Configuration config = Configuration.getInstance();
		config.ALGORITHM = algo;
		config.SEED = seed;
		config.K = tail;
		config.DATA = data;
		config.STRATEGY = strategy;
		
	}

	protected Score calculateMeans(List<Score> scores) {
		Score mean = new Score(0,0,0,0);
		double totalSensitivity = 0D, totalSpecificity = 0D, totalBCR = 0D, totalKappa = 0D,totalDuration = 0D;
		for (Score score : scores) {
			totalSensitivity = totalSensitivity + score.getSensitivity();
			totalSpecificity = totalSpecificity + score.getSpecificity();
			totalBCR = totalBCR + score.getBCR();
			totalDuration = totalDuration + score.getDuration();
			totalKappa = totalKappa + score.getKappa();
		}
        if(scores.size() == 0){

            mean.setDuration(0L);
        }
        else {
            mean.setSensitivity(totalSensitivity / scores.size());
            mean.setSpecificity(totalSpecificity / scores.size());
            mean.setBCR(totalBCR / scores.size());
            mean.setDuration((long) totalDuration / scores.size());
			mean.setKappa(totalKappa / scores.size());
        }
		return mean;
	}

	static private Double calculateMean(List<Double> from){
		Double sum = 0D;
		for (Double d : from) {
			sum+=d;
		}
		return sum/from.size();
	}

	/*
	 * Computes the sensitivity - the probability of a positive prediction given 
	 * that a test should be positive.
	 */
	protected Score score(Machine model, TraceSet pos, TraceSet neg) {
		double tp=0.0D; double fn = 0.0D; double tn = 0.0D; double fp = 0.0D;
		SimpleMachineAnalysis<?> analysis = null;
		if(data){
			//if(algo == Configuration.Data.J48)
			//	analysis = new ConstraintEFSMAnalysis((WekaGuardMachineDecorator)model);
			//else
				analysis = new EFSMAnalysis((WekaGuardMachineDecorator)model);
		}
		else
			analysis = new SimpleMachineAnalysis<Machine<Set<TraceElement>>>(model);
		for (List<TraceElement> trace : pos.getPos()) {
			assert(model.getAutomaton().consistentTransitions());
			
			boolean accepted = analysis.walk(trace, true,model.getAutomaton());
			if(accepted)
				tp++;
			else{
                if(data && algo == Data.J48){
                    LOGGER.debug("FN: "+trace);
                }
				fn++;
			}
		}
		for (List<TraceElement> trace : neg.getPos()) {
			boolean accepted = analysis.walk(trace,true,model.getAutomaton());
			if(accepted) {
                if(data && algo == Data.J48){
                    LOGGER.debug("FP: "+trace);
                }
                fp++;
            }
			else{
				tn++;
			}
		}
		Score ret = new Score(tp,tn,fp,fn);

		if(data)
			LOGGER.debug("tp:"+tp+", tn:"+tn+", fp:"+fp+", fn:"+fn+", RESULT: "+ret);
		return ret;
	}

	
	
	protected Machine learnModel(TraceSet pos) throws InterruptedException {
        AbstractMerger<?,?> inference = getInference(pos);
        Machine inferred = null;
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<? extends Machine> future = executor.submit(inference);
        executor.shutdown(); // This does not cancel the already-scheduled task.
        try { inferred = future.get(70, TimeUnit.MINUTES); }
        catch (InterruptedException ie) {
            future.cancel(true);
            LOGGER.error("Inference interrupted.");
        }
        catch (ExecutionException ee) {
            future.cancel(true);
            LOGGER.error("Exception during inference.");
        }
        catch (TimeoutException te) {
            future.cancel(true);
            LOGGER.error("TIMEOUT");
        }


		return inferred;

	}


	protected List<Set<List<TraceElement>>> computeFolds(int folds) {
		List<Set<List<TraceElement>>> folded = new ArrayList<Set<List<TraceElement>>>();
		for(int i = 0; i< folds; i++){
			Set<List<TraceElement>> traceSet = new HashSet<List<TraceElement>>();
			folded.add(i,traceSet);
		}
		int counter = 0;
		Iterator<List<TraceElement>> traceIt = this.trace.getPos().iterator();
		while(traceIt.hasNext()){
			if(counter==folds)
				counter = 0;
			Set<List<TraceElement>> traces = folded.get(counter++);
			traces.add(traceIt.next());
		}
		return folded;
	}
	
	public AbstractMerger<?, ?> getInference(TraceSet posSet) { 
		AbstractMerger<?,?> inference = null;
		
		if(this.data){
			BaseClassifierInference bci = new BaseClassifierInference(posSet,eval, algo);
			
			PrefixTreeFactory<WekaGuardMachineDecorator> tptg = new EFSMPrefixTreeFactory(new PayloadMachine(),bci.getClassifiers(),bci.getElementsToInstances());

			RedBlueMergingState<WekaGuardMachineDecorator> ms = new RedBlueMergingState<WekaGuardMachineDecorator>(tptg.createPrefixTree(posSet));
			Scorer<RedBlueMergingState<WekaGuardMachineDecorator>> scorer  = new RedBlueScorer<RedBlueMergingState<WekaGuardMachineDecorator>>(tail, new ComputeScore());
			

			inference = new EDSMDataMerger<RedBlueMergingState<WekaGuardMachineDecorator>>(scorer,ms);				
				
			
		}
		else{
			PrefixTreeFactory<SimpleMachine> tptg = new FSMPrefixTreeFactory(new PayloadMachine());
			RedBlueMergingState<Machine> ms = new RedBlueMergingState<Machine>(tptg.createPrefixTree(posSet));
			Scorer<RedBlueMergingState<Machine>> scorer  = new RedBlueScorer<RedBlueMergingState<Machine>>(tail, new ComputeScore());
			inference = new EDSMMerger<Machine,RedBlueMergingState<Machine>>(scorer,ms);	
			
		}		
			
		
		return inference;
	}
	

	
	
	
}
