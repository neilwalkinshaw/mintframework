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


import mint.Configuration;

public class Result extends SimpleResult{
	
	protected final String name,algo, strategy;
	protected final double sensitivity,specificity,bcr, states, transitions;
	protected final long duration;
	protected final int seed, tail,numObs;
	protected final boolean data;
	
	public Result(String name, String algo, int numTraces, double sensitivity,
				  double specificity, double bcr, long duration, int seed, int tail, boolean data, double states, double transitions, Configuration.Strategy strategy) {
		super(name,algo,seed,tail,data,states,transitions,strategy, bcr);
		this.numObs = numTraces;
		this.name = name;
		this.algo = algo;
		this.sensitivity = sensitivity;
		this.specificity = specificity;
		this.bcr = bcr;
		this.duration = duration;
		this.seed = seed;
		this.tail = tail;
		this.data = data;
		this.states = states;
		this.transitions = transitions;
        this.strategy = strategy.name();
	}

	public String getName() {
		return name;
	}

	public double getStates() {
		return states;
	}

	public double getTransitions() {
		return transitions;
	}

	@Override
	public String toString() {
		return name + "," + algo +","+numObs+","+seed+","+tail+","+strategy+","+data+","
				+ sensitivity + "," + specificity + "," + bcr
				+ "," + states
				+ "," + transitions
				+ "," + duration;
	}
	
	
	
	

}
