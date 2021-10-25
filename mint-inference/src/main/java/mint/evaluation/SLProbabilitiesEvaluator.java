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
import mint.evaluation.kfolds.RefModelKFoldsEvaluator;
import mint.evaluation.mutation.MutationOperator;
import mint.evaluation.mutation.StateMachineMutator;
import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.ProbabilisticMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.reader.DotReader;
import mint.model.prefixtree.FSMPrefixTreeFactory;
import mint.model.soa.SubjectiveOpinionResult;
import mint.model.walk.SimpleMachineAnalysis;
import mint.model.walk.WalkResult;
import mint.model.walk.probabilistic.ProbabilisticMachineAnalysis;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.types.VariableAssignment;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.FileSystems;
import java.util.*;

public class SLProbabilitiesEvaluator {

	private final static Logger LOGGER = Logger.getLogger(SLProbabilitiesEvaluator.class.getName());


	enum FileType {SMV,DOT};
	
	public static void main(String[] args){
		String state = args[4].replaceAll("\'","");
		Configuration.getInstance().CONFIDENCE_THRESHOLD = Double.parseDouble(args[5]);
		for(int i = 0; i<30; i++) {
			Configuration.getInstance().SEED = i;
			run(args[0], args[1], args[2], args[3], FileType.DOT, state);
		}
	}

	private static void run(String label, String folds, String referenceMachine, String numtraces, FileType type,String initial){
		BasicConfigurator.configure();
		Configuration configuration = Configuration.getInstance();
		Machine dfa = null;

		if(type == FileType.SMV)
			dfa = getMachineFromSMVFile(referenceMachine);
		else{
			DotReader dr = new DotReader(FileSystems.getDefault().getPath(referenceMachine),initial);
			dr.setRemoveOutput(false);
			dfa = dr.getImported();
		}
		dfa.getAutomaton().completeWithRejects();
		ProbabilisticMachine pdfa = createProbabilisticMachine(dfa);

		int targetSize = Integer.parseInt(numtraces);
		//int targetSize = pdfa.getAutomaton().getStates().size() * pdfa.getAutomaton().getAlphabet().size();
		int numNeg = targetSize;
		TraceSet traces = createTraces2(pdfa,targetSize,numNeg);
		Collections.shuffle(traces.getPos());
		Collections.shuffle(traces.getNeg());
		while(traces.getPos().size()>targetSize){
			traces.getPos().remove(0);
		}
		while(traces.getNeg().size()>numNeg){
			traces.getNeg().remove(0);
		}
		runExperiment(label,Integer.parseInt(folds), configuration, traces, pdfa);
	}

	public static void checkTransitions(ProbabilisticMachine m){
		for(DefaultEdge transition : m.getAutomaton().getTransitions()){
			assert(m.getAutomaton().getTransitionData(transition).getPayLoad() instanceof Double);
		}
	}


	public static TraceSet createTraces2(Machine pdfa, int posNum, int negNum) {


		FSMPrefixTreeFactory prefixTreeFactory = new FSMPrefixTreeFactory(new PayloadMachine());

		int targetDepth = pdfa.getAutomaton().getDepth()+2;
		int posTraces = 0;
		Random rand = new Random(Configuration.getInstance().SEED);
		while(posTraces < posNum){
			List<DefaultEdge> walk = pdfa.getAutomaton().randomAcceptingWalk(targetDepth,rand);
			List<TraceElement> trace = new ArrayList<>();
			for (int i = 0; i < walk.size(); i++) {
				DefaultEdge de = walk.get(i);

				String label = pdfa.getAutomaton().getTransitionData(de).getLabel();
				trace.add(new SimpleTraceElement(label, new VariableAssignment[]{}));
			}
			prefixTreeFactory.addSequence(trace,true);
			posTraces = prefixTreeFactory.numSequences(true);
		}


		int negTraces = 0;

		while(negTraces<negNum){

			List<DefaultEdge> walk = null;
			while(walk == null){
				walk = pdfa.getAutomaton().randomRejectingWalk(targetDepth,rand);
			}
			List<TraceElement> trace = new ArrayList<>();
			for (int i = 0; i < walk.size(); i++) {
				DefaultEdge de = walk.get(i);

				String label = pdfa.getAutomaton().getTransitionData(de).getLabel();
				trace.add(new SimpleTraceElement(label, new VariableAssignment[]{}));
			}
			prefixTreeFactory.addSequence(trace,false);
			negTraces = prefixTreeFactory.numSequences(false);
		}

		return prefixTreeFactory.getTraces();


	}


		private static TraceSet createTracesD(ProbabilisticMachine pdfa, int posNum, int negNum) {
		Integer rejectState = 0;

		for(Integer s : pdfa.getStates()){
			if(pdfa.getAutomaton().getAccept(s) == TraceDFA.Accept.REJECT){
				rejectState = s;
			}
		}

		FSMPrefixTreeFactory prefixTreeFactory = new FSMPrefixTreeFactory(new PayloadMachine());


		List<GraphPath<Integer,DefaultEdge>> paths = pdfa.getAutomaton().allPaths(pdfa.getInitialState(),pdfa.getAutomaton().getDepth()+2);

		Collections.shuffle(paths);

		int posTraces = 0;
		int negTraces = 0;

		for(GraphPath<Integer,DefaultEdge> p : paths){
			int negInd = p.getVertexList().indexOf(rejectState);
			List<TraceElement> trace = new ArrayList<>();

			if(negInd >=0 && negTraces < negNum) {

				for (int i = 0; i < negInd; i++) {
					DefaultEdge de = p.getEdgeList().get(i);

					String label = pdfa.getAutomaton().getTransitionData(de).getLabel();
					trace.add(new SimpleTraceElement(label, new VariableAssignment[]{}));
				}
				prefixTreeFactory.addSequence(trace,false);
			}
			else if (posTraces<posNum && negInd < 0){
				for (int i = 0; i < p.getLength(); i++) {
					DefaultEdge de = p.getEdgeList().get(i);

					String label = pdfa.getAutomaton().getTransitionData(de).getLabel();
					trace.add(new SimpleTraceElement(label, new VariableAssignment[]{}));
				}
				prefixTreeFactory.addSequence(trace,true);
			}
			posTraces = prefixTreeFactory.numSequences(true);
			negTraces = prefixTreeFactory.numSequences(false);
			if(posTraces>=posNum && negTraces >= negNum)
				break;

		}
		return prefixTreeFactory.getTraces();
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
		Collection<List<TraceElement>> neg = posSet.getNeg();
		configuration.PREFIX_CLOSED = true;
		configuration.SUBJECTIVE_OPINIONS=true;
		configuration.CAREFUL_DETERMINIZATION=false;
		configuration.LOGGING = Level.ALL;
		outputStats(label,pdfa);
		//for(int k = 0; k<3; k++) {
			Set<List<TraceElement>> sizeP = new HashSet<List<TraceElement>>();
			sizeP.addAll(pos);
			Set<List<TraceElement>> sizeN = new HashSet<List<TraceElement>>();
			sizeN.addAll(neg);
			RefModelKFoldsEvaluator kfolds = new RefModelKFoldsEvaluator(label, sizeP, sizeN, configuration.SEED, 0, pdfa, 0.05);
			kfolds.kfolds(folds,false);
		//}

	}

	protected static void outputStats(String label, ProbabilisticMachine pdfa) {
		FileWriter fWriter = null;
		BufferedWriter writer = null;
		try {
			fWriter = new FileWriter(label+"_stats.csv",true);
			writer = new BufferedWriter(fWriter);

			String stats = pdfa.getStates().size()+","+pdfa.getAutomaton().getTransitions().size()+","+pdfa.getAutomaton().getAlphabet().size()+"\n";

			writer.append(stats);

			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


}
