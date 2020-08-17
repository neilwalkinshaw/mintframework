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
import mint.evaluation.kfolds.RefModelKFoldsEvaluator;
import mint.model.Machine;
import mint.model.ProbabilisticMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.types.VariableAssignment;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.jgrapht.graph.DefaultEdge;

import java.io.File;
import java.util.*;

public class SLProbabilitiesEvaluator {
	
	public static void main(String[] args){
		BasicConfigurator.configure();
		Configuration configuration = Configuration.getInstance();
		Machine dfa = getMachineFromSMVFile(args[2]);
		ProbabilisticMachine pdfa = createProbabilisticMachine(dfa);
		TraceSet traces = createTraces(pdfa,Integer.parseInt(args[3]));
		runExperiment(args[0],Integer.parseInt(args[1]), configuration, traces, pdfa);

		
	}

	private static TraceSet createTraces(ProbabilisticMachine pdfa, int numTraces) {
		int targetDepth = getDepth(pdfa)+5; //This is taken from the STAMINA random walk algorithm
		Random rand = new Random(Configuration.getInstance().SEED);
		TraceSet posSet = new TraceSet();
		for(int i = 0; i< numTraces; i++){
			Integer currentState = pdfa.getInitialState();
			int currentDepth = 0;
			List<TraceElement> trace = new ArrayList<>();
			while(currentDepth < targetDepth){
				ArrayList<DefaultEdge> outgoing = new ArrayList<>();
				outgoing.addAll(pdfa.getAutomaton().getOutgoingTransitions(currentState));
				if(outgoing.isEmpty())
					break;
				DefaultEdge selected = selectRandomAccordingToDistribution(pdfa,outgoing,rand);
				//DefaultEdge selected = selectRandom(outgoing,rand);
				assert(selected!=null);
				String label = pdfa.getAutomaton().getTransitionData(selected).getLabel();
				trace.add(new SimpleTraceElement(label,new VariableAssignment[]{}));
				currentDepth++;
			}
			posSet.addPos(trace);
		}
		return posSet;
	}

	private static DefaultEdge selectRandomAccordingToDistribution(ProbabilisticMachine pdfa, ArrayList<DefaultEdge> outgoing, Random rand) {
		Collections.shuffle(outgoing);
		double target = rand.nextDouble();
		double sumProb = 0;
		DefaultEdge toReturn = null;
		for(int i = 0; i<outgoing.size(); i++){
			Double currentProb = pdfa.getAutomaton().getTransitionData(outgoing.get(i)).getPayLoad();
			sumProb+=currentProb;
			if(target < sumProb){
				toReturn = outgoing.get(i);
				break;
			}
		}
		return toReturn;
	}

	private static DefaultEdge selectRandom(ArrayList<DefaultEdge> outgoing, Random rand) {
		return outgoing.get(rand.nextInt(outgoing.size()));
	}

		private static int getDepth(ProbabilisticMachine pdfa) {
		Integer depth = 0;
		Integer startState = pdfa.getAutomaton().getInitialState();
		for(Integer state : pdfa.getAutomaton().getStates()){
			Integer stateDepth = pdfa.getAutomaton().shortestPath(startState,state).getLength();
			if(stateDepth > depth)
				depth = stateDepth;
		}
		return depth;
	}

	private static ProbabilisticMachine createProbabilisticMachine(Machine dfa) {
		ProbabilisticMachine pdfa = new ProbabilisticMachine();
		pdfa.setAutomaton(dfa.getAutomaton());
		for(Integer state : pdfa.getAutomaton().getStates()){
			int numOutgoing = pdfa.getAutomaton().getOutgoingTransitions(state).size();
			double[] distribution = createDistribution(numOutgoing);
			int i = 0;
			for(DefaultEdge edge : pdfa.getAutomaton().getOutgoingTransitions(state)){
				pdfa.getAutomaton().getTransitionData(edge).setPayLoad(distribution[i]);
				i++;
			}
		}
		return pdfa;
	}

	private static double[] createDistribution(int numOutgoing) {
		double[] dist = new double[numOutgoing];
		Random rand = new Random(Configuration.getInstance().SEED);
		for(int i = 0; i<numOutgoing; i++){
			dist[i] = rand.nextDouble();
		}
		dist = normalise(dist);
		return dist;
	}

	private static double[] normalise(double[] dist) {
		double total = 0;
		for(int i = 0; i<dist.length; i++){
			total+=dist[i];
		}
		for(int i = 0; i<dist.length; i++){
			dist[i] = dist[i]/total;
		}
		return dist;
	}

	/**
	 * Read state machine from NUSMV file.
	 * Add random probabilities to transitions.
	 * @param arg
	 * @return
	 */
	private static Machine getMachineFromSMVFile(String arg) {
		NuSMVFSMReader reader = new NuSMVFSMReader();
		reader.readFile(new File(arg));
		Machine dfa = reader.getMachine();
		return dfa;
	}

	private static void runExperiment(String label, int folds, Configuration configuration, TraceSet posSet,
									  ProbabilisticMachine pdfa) {
		Collection<List<TraceElement>> pos = posSet.getPos();
		Collection<List<TraceElement>> neg = new HashSet<List<TraceElement>>();
		configuration.PREFIX_CLOSED = true;
		configuration.SUBJECTIVE_OPINIONS=true;
		//configuration.CONFIDENCE_THRESHOLD=60;
		configuration.LOGGING = Level.ALL;
		for(int k = 1; k<10; k++) {
			Set<List<TraceElement>> sizeP = new HashSet<List<TraceElement>>();
			sizeP.addAll(pos);
			RefModelKFoldsEvaluator kfolds = new RefModelKFoldsEvaluator(label, sizeP, neg, 0, k, pdfa);
			kfolds.kfolds(folds,false);
		}
	}
}
