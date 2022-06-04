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
import mint.model.Machine;
import mint.model.RawProbabilisticMachine;
import mint.model.dfa.reader.DotReader;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import java.nio.file.FileSystems;
import java.util.*;

public class MachineStats {

	private final static Logger LOGGER = Logger.getLogger(MachineStats.class.getName());

	public static void main(String[] args){
		run(args[0], args[1]);

	}

	private static void run( String referenceMachine, String initial){
		BasicConfigurator.configure();

		DotReader dr = new DotReader(FileSystems.getDefault().getPath(referenceMachine),initial);
		dr.setRemoveOutput(false);
		Machine dfa = dr.getImported();

		//dfa.getAutomaton().completeWithRejects();
		RawProbabilisticMachine pdfa = createProbabilisticMachine(dfa);

		outputStats(" ", pdfa);
	}


	private static RawProbabilisticMachine createProbabilisticMachine(Machine dfa) {
		RawProbabilisticMachine pdfa = new RawProbabilisticMachine();
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







	protected static void outputStats(String label, RawProbabilisticMachine pdfa) {


			String stats = pdfa.getStates().size()+","+pdfa.getAutomaton().getTransitions().size()+","+pdfa.getAutomaton().getAlphabet().size()+"\n";
			LOGGER.info(stats);
	}


}
