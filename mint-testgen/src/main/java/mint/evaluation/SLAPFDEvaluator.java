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

import citcom.subjectiveLogic.BinomialOpinion;
import fr.vergne.pareto.ParetoComparator;
import fr.vergne.pareto.ParetoHelper;
import mint.Configuration;
import mint.evaluation.mutation.MutationOperator;
import mint.evaluation.mutation.StateMachineMutator;
import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.reader.DotReader;
import mint.model.soa.*;
import mint.model.walk.SimpleMachineAnalysis;
import mint.model.walk.WalkResult;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.FileSystems;
import java.util.*;

import static org.junit.Assert.assertTrue;

public class SLAPFDEvaluator {

	private final static Logger LOGGER = Logger.getLogger(SLAPFDEvaluator.class.getName());

	protected static PayloadMachine dfa;
	protected static int mutations = 1000;
	protected static final int maxStep = 5;

	protected static List<Machine> mutated;


	public static void main(String[] args){


		String label = args[0];
		String referenceMachine = args[1];
		Configuration.getInstance().CONFIDENCE_THRESHOLD = 100;

		for(int i = 0; i<30;i++) {
			Configuration.getInstance().SEED = i;
			run(label, referenceMachine, args[2]);
		}

	}

	private static void run(String label,  String referenceMachine, String initial){
		BasicConfigurator.configure();
		DotReader dr = new DotReader(FileSystems.getDefault().getPath(referenceMachine),initial);
		dfa = (PayloadMachine)dr.getImported();

		StateMachineMutator smm = new StateMachineMutator(dfa);
		mutated = new ArrayList<>();
		for(MutationOperator mo : smm.generateMutated(mutations)){
			Machine toAdd = null;
			try {
				toAdd = mo.applyMutation();
			} catch (MutationOperator.NonDeterministicException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(toAdd!=null)
				mutated.add(toAdd);
		}

		dfa.getAutomaton().completeWithRejects();

		//WMethodSMTester tester = new WMethodSMTester();
		//tester.setK(1);
		//List<List<TraceElement>> baseTests2 = tester.generateTests(dfa);

		TraceSet baseTests = SLProbabilitiesEvaluator.createTraces2(dfa,500,500);

		Collections.shuffle(baseTests.getPos());
		Collections.shuffle(baseTests.getNeg());
		List<List<TraceElement>> prioritisedTest = new ArrayList<>();
		List<List<TraceElement>> coverageTest = new ArrayList<>();
		List<List<TraceElement>> randomTests = new ArrayList<>();
		List<List<TraceElement>> infoDistTests = new ArrayList<>();

		randomTests.addAll(baseTests.getPos());
		randomTests.addAll(baseTests.getNeg());

		prioritisedTest.addAll(baseTests.getPos());
		prioritisedTest.addAll(baseTests.getNeg());
		prioritisedTest = prioritiseSM2(prioritisedTest);

		coverageTest.addAll(baseTests.getPos());
		coverageTest.addAll(baseTests.getNeg());
		coverageTest = prioritiseCoverage(coverageTest);

		infoDistTests.addAll(baseTests.getPos());
		infoDistTests.addAll(baseTests.getNeg());
		infoDistTests = prioritiseInfoDist(infoDistTests);




		TraceSet ts = new TraceSet();
		for(List<TraceElement> test : prioritisedTest){
			ts.addPos(test);
		}
		assert(prioritisedTest.containsAll(randomTests));
		assert(randomTests.containsAll(prioritisedTest));
		List<Double>  apfd = mutationScore(ts.getPos());
		output(apfd,label+"_prioritised");

		ts = new TraceSet();
		for(List<TraceElement> test : randomTests){
			ts.addPos(test);
		}
		List<Double> apfd2 = mutationScore(ts.getPos());
		output(apfd2,label+"_random");

		ts = new TraceSet();
		for(List<TraceElement> test : coverageTest){
			ts.addPos(test);
		}
		apfd = mutationScore(ts.getPos());
		output(apfd,label+"_coverage");

		ts = new TraceSet();
		for(List<TraceElement> test : infoDistTests){
			ts.addPos(test);
		}
		apfd = mutationScore(ts.getPos());
		output(apfd,label+"_gower");


		assertTrue(apfd.get(apfd.size()-1) - apfd2.get(apfd2.size()-1) == 0D);

	}



	private static List<List<TraceElement>> prioritiseInfoDist(List<List<TraceElement>> infoDistTests) {

		List<List<TraceElement>> finalTests = new ArrayList<>();

		TraceSet ts = new TraceSet();

		SimpleMachineAnalysis sma = new SimpleMachineAnalysis(dfa);
		Comparator<List<TraceElement>> dCom = new DistanceComparator(ts,sma);
		ParetoComparator<List<TraceElement>> traceComparator = new ParetoComparator<>();
		traceComparator.add(dCom);


		while(!infoDistTests.isEmpty()) {

			Collection<List<TraceElement>> subset = ParetoHelper.getMinimalFrontierOf(infoDistTests, traceComparator);

			List<List<TraceElement>> list = new ArrayList<>();
			list.addAll(subset);

			int lim = Math.min(maxStep,list.size());

			for(int i = 0; i<lim; i++) {

				List<TraceElement> selected = list.get(i);

				ts.addPos(selected);

				infoDistTests.remove(selected);
				finalTests.add(selected);
			}

			dCom = new DistanceComparator(ts, sma);
			traceComparator = new ParetoComparator<>();
			traceComparator.add(dCom);

		}
		return finalTests;
	}

	private static List<List<TraceElement>> prioritiseCoverage(List<List<TraceElement>> coverageTest) {

		List<List<TraceElement>> finalTests = new ArrayList<>();
		Set<DefaultEdge> covered = new HashSet<>();

		SimpleMachineAnalysis sma = new SimpleMachineAnalysis(dfa);
		Comparator<List<TraceElement>> cCom = new CoverageComparator(sma,covered);
		ParetoComparator<List<TraceElement>> traceComparator = new ParetoComparator<>();
		traceComparator.add(cCom);

		while(!coverageTest.isEmpty()) {

			Collection<List<TraceElement>> subset = ParetoHelper.getMaximalFrontierOf(coverageTest, traceComparator);

			List<List<TraceElement>> list = new ArrayList<>();
			list.addAll(subset);

			int lim = Math.min(maxStep,list.size());

			for(int i = 0; i<lim; i++) {

				List<TraceElement> selected = list.get(i);
				WalkResult wr1 = sma.walk(selected,dfa.getInitialState(),new ArrayList<>(),dfa.getAutomaton());
				covered.addAll(wr1.getWalk());
				coverageTest.remove(selected);
				finalTests.add(selected);
			}
		}
		return finalTests;

	}

	protected static void output(List res, String name) {
		FileWriter fWriter = null;
		BufferedWriter writer = null;
		try {
			fWriter = new FileWriter(name+".csv",true);
			writer = new BufferedWriter(fWriter);
			for(int i = 0; i<res.size(); i++){
				writer.append(Configuration.getInstance().SEED+","+i+","+res.get(i)+"\n");
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static List<List<TraceElement>> prioritiseSM2(List<List<TraceElement>> coverageTest) {

		List<List<TraceElement>> finalTests = new ArrayList<>();
		Set<DefaultEdge> covered = new HashSet<>();

		while(!coverageTest.isEmpty()) {

			TraceSet ts = new TraceSet();
			ts.getPos().addAll(finalTests);



			MultinomialOpinionMachineDecorator pmd = new MultinomialOpinionMachineDecorator(dfa,ts,Configuration.getInstance().CONFIDENCE_THRESHOLD, false);
			pmd.postProcess();

			Comparator<List<TraceElement>> cCom = new UncertaintyComparator(pmd, covered);

			ParetoComparator<List<TraceElement>> traceComparator = new ParetoComparator<>();
			traceComparator.add(cCom);

			Collection<List<TraceElement>> subset = ParetoHelper.getMaximalFrontierOf(coverageTest, traceComparator);

			List<List<TraceElement>> list = new ArrayList<>();
			list.addAll(subset);

			int lim = Math.min(maxStep,list.size());

			for(int i = 0; i<lim; i++) {

				List<TraceElement> selected = list.get(i);

				WalkResult wr1 = pmd.walk(selected);
				covered.addAll(wr1.getWalk());

				coverageTest.remove(selected);
				finalTests.add(selected);
			}
		}
		return finalTests;

	}







	private static Double distance(BinomialOpinion m1, BinomialOpinion m2) {
		double m1prob = m1.getProjectedProbability();
		double m2prob = m2.getProjectedProbability();
		if(m1prob > m2prob)
			return 1-(m2prob / m1prob);
		else if(m2prob > m1prob)
			return 1-(m1prob / m2prob);
		else return 0D;
	}

	private static class SequenceRating implements Comparable{

		private List<TraceElement> sequence;
		private Double score;

		public SequenceRating(List<TraceElement> sequence, Double score){
			this.sequence=sequence;
			this.score = score;
		}

		/**
		 * Compares this object with the specified object for order.  Returns a
		 * negative integer, zero, or a positive integer as this object is less
		 * than, equal to, or greater than the specified object.
		 *
		 * <p>The implementor must ensure
		 * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
		 * for all {@code x} and {@code y}.  (This
		 * implies that {@code x.compareTo(y)} must throw an exception iff
		 * {@code y.compareTo(x)} throws an exception.)
		 *
		 * <p>The implementor must also ensure that the relation is transitive:
		 * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
		 * {@code x.compareTo(z) > 0}.
		 *
		 * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
		 * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
		 * all {@code z}.
		 *
		 * <p>It is strongly recommended, but <i>not</i> strictly required that
		 * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
		 * class that implements the {@code Comparable} interface and violates
		 * this condition should clearly indicate this fact.  The recommended
		 * language is "Note: this class has a natural ordering that is
		 * inconsistent with equals."
		 *
		 * <p>In the foregoing description, the notation
		 * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
		 * <i>signum</i> function, which is defined to return one of {@code -1},
		 * {@code 0}, or {@code 1} according to whether the value of
		 * <i>expression</i> is negative, zero, or positive, respectively.
		 *
		 * @param o the object to be compared.
		 * @return a negative integer, zero, or a positive integer as this object
		 * is less than, equal to, or greater than the specified object.
		 * @throws NullPointerException if the specified object is null
		 * @throws ClassCastException   if the specified object's type prevents it
		 *                              from being compared to this object.
		 */
		@Override
		public int compareTo(Object o) {
			SequenceRating other = (SequenceRating) o;

			return other.score.compareTo(score);
		}
	}





	protected static List<Double> mutationScore(List<List<TraceElement>>tests) {
		List<Double> apfd = new ArrayList<>();
		double total = mutated.size();
		SimpleMachineAnalysis sma = new SimpleMachineAnalysis(dfa);
		Set<Machine> killed = new HashSet<>();
		List<Machine> mut = new ArrayList<>();
		mut.addAll(mutated);

		double count =0;
		for(List<TraceElement> test : tests){
			count++;
			LOGGER.info("Mutation testing: "+(count/(double)tests.size())*100+"% done (out of "+tests.size()+" tests)");
			WalkResult wr = sma.walk(test,dfa.getInitialState(),new ArrayList<>(),dfa.getAutomaton());
			TraceDFA.Accept accept = wr.isAccept(dfa.getAutomaton());
			if(accept == TraceDFA.Accept.UNDEFINED)
				accept = TraceDFA.Accept.REJECT;
			mut.removeAll(killed);
			for(Machine mutated : mut){

					SimpleMachineAnalysis mutantAnalysis = new SimpleMachineAnalysis(mutated);
					WalkResult mwr = mutantAnalysis.walk(test,mutated.getInitialState(),new ArrayList<>(),mutated.getAutomaton());
					TraceDFA.Accept maccept = mwr.isAccept(mutated.getAutomaton());
					if(maccept == TraceDFA.Accept.UNDEFINED)
						maccept = TraceDFA.Accept.REJECT;
					if(accept != maccept) {
						killed.add(mutated);
					}
			}
			apfd.add((double)killed.size()/(total));
		}
		return apfd;
	}



	private static class BinomialBeliefComparator implements Comparator<List<TraceElement>>{

		MultinomialOpinionMachineDecorator pmd;
		TraceSet ts;
		HashMap<List<TraceElement>,Double> scoreMap;

		public BinomialBeliefComparator(MultinomialOpinionMachineDecorator pmd, TraceSet ts, Collection<List<TraceElement>> sequences) {
			this.pmd = pmd;
			this.ts = ts;
			scoreMap = new HashMap<>();
			for(List<TraceElement> trace : sequences){
				scoreMap.put(trace,computeScore(trace));
			}
		}


		@Override
		public int compare(List<TraceElement> o1, List<TraceElement> o2) {
			Double o1Score = scoreMap.get(o1);
			Double o2Score = scoreMap.get(o2);
			return o1Score.compareTo(o2Score);
		}

		private double computeScore(List<TraceElement> test){
			WalkResult wr1 = pmd.walk(test);
			//BinomialOpinion m1 = pmd.binomialFusedWalkOpinion(wr1,test.size());
			BinomialOpinion m1 = pmd.binomialWalkOpinion(wr1,test.size(),true);

			TraceSet addedTrace = new TraceSet();
			addedTrace.getPos().addAll(ts.getPos());
			addedTrace.addPos(test);

			MultinomialOpinionMachineDecorator pmd2 = new MultinomialOpinionMachineDecorator(dfa,addedTrace,Configuration.getInstance().CONFIDENCE_THRESHOLD, false);
			pmd2.postProcess();

			WalkResult wr2 = pmd2.walk(test);
			//BinomialOpinion m2 = pmd2.binomialFusedWalkOpinion(wr2,test.size());
			BinomialOpinion m2 = pmd2.binomialWalkOpinion(wr2,test.size(),true);
			Double score = distance(m1,m2);
			return score;
		}

		@Override
		public boolean equals(Object obj) {
			return false;
		}
	}


	private static class UncertaintyComparator implements Comparator<List<TraceElement>>{

		MultinomialOpinionMachineDecorator pmd;
		protected Set<DefaultEdge> covered;

		public UncertaintyComparator(MultinomialOpinionMachineDecorator pmd, Set<DefaultEdge> covered) {
			this.pmd = pmd;
			this.covered = covered;
		}


		@Override
		public int compare(List<TraceElement> o1, List<TraceElement> o2) {
			WalkResult wr1 = pmd.walk(o1);
			WalkResult wr2 = pmd.walk(o2);

			HashSet<DefaultEdge> walkEdges1 = new HashSet<>();
			walkEdges1.addAll(wr1.getWalk());
			walkEdges1.removeAll(covered);

			HashSet<DefaultEdge> walkEdges2 = new HashSet<>();
			walkEdges2.addAll(wr2.getWalk());
			walkEdges2.removeAll(covered);

			Double o1U = (double) walkEdges1.size();
			Double o2U = (double) walkEdges2.size();

			if(!o1U.equals(o2U))
				return o1U.compareTo(o2U);

			else{
				Double sumUncertaintyW1 = minUncertainty(wr1);
				Double sumUncertaintyW2 = minUncertainty(wr2);
				return sumUncertaintyW2.compareTo(sumUncertaintyW1); //we want the lower one to be picked...
			}
		}

		// Could ensure only unique occurrences of states?
		private Double sumUncertainties(WalkResult wr1) {
			Integer initialState = pmd.getInitialState();
			Double uncertainty = pmd.getOpinion(initialState).getUncertainty();
			for(DefaultEdge de : wr1.getWalk()){
				Integer target = pmd.getAutomaton().getTransitionTarget(de);
				uncertainty += pmd.getOpinion(target).getUncertainty();
			}
			return uncertainty;
		}

		private Double minUncertainty(WalkResult wr1) {
			Integer initialState = pmd.getInitialState();
			Double uncertainty = pmd.getOpinion(initialState).getUncertainty();
			for(DefaultEdge de : wr1.getWalk()){
				Integer target = pmd.getAutomaton().getTransitionTarget(de);
				double newUncertainty = pmd.getOpinion(target).getUncertainty();
				if(newUncertainty < uncertainty)
					uncertainty = newUncertainty;
			}
			return uncertainty;
		}

		@Override
		public boolean equals(Object obj) {
			return false;
		}
	}

	private static class BeliefComparator implements Comparator<List<TraceElement>>{

		MultinomialOpinionMachineDecorator pmd;

		public BeliefComparator(MultinomialOpinionMachineDecorator pmd) {
			this.pmd = pmd;
		}

		@Override
		public int compare(List<TraceElement> o1, List<TraceElement> o2) {
			WalkResult wr1 = pmd.walk(o1);
			WalkResult wr2 = pmd.walk(o2);

			BinomialOpinion m1 = pmd.binomialWalkOpinion(wr1,o1.size(), true);
			BinomialOpinion m2 = pmd.binomialWalkOpinion(wr2,o2.size(), true);
			Double o1U = m1.getUncertainty();
			Double o2U = m2.getUncertainty();
			return o1U.compareTo(o2U);
		}

		@Override
		public boolean equals(Object obj) {
			return false;
		}
	}


	private static class CoverageComparator implements Comparator<List<TraceElement>>{

		protected SimpleMachineAnalysis sma;

		protected Set<DefaultEdge> covered;

		public CoverageComparator(SimpleMachineAnalysis sma, Set<DefaultEdge> covered){
			this.sma = sma;
			this.covered = covered;
		}

		@Override
		public int compare(List<TraceElement> o1, List<TraceElement> o2) {
			WalkResult wr1 = sma.walk(o1,dfa.getInitialState(),new ArrayList<>(),dfa.getAutomaton());
			WalkResult wr2 = sma.walk(o2,dfa.getInitialState(),new ArrayList<>(),dfa.getAutomaton());

			HashSet<DefaultEdge> walkEdges1 = new HashSet<>();
			walkEdges1.addAll(wr1.getWalk());
			walkEdges1.removeAll(covered);

			HashSet<DefaultEdge> walkEdges2 = new HashSet<>();
			walkEdges2.addAll(wr2.getWalk());
			walkEdges2.removeAll(covered);

			Double o1U = (double) walkEdges1.size();
			Double o2U = (double) walkEdges2.size();
			return o1U.compareTo(o2U);
		}

		@Override
		public boolean equals(Object obj) {
			return false;
		}
	}

	private static class DistanceComparator implements Comparator<List<TraceElement>>{

		protected List<List<String>> done;
		protected SimpleMachineAnalysis sma;

		public DistanceComparator(TraceSet doneTraces, SimpleMachineAnalysis sma){
			done = new ArrayList<>();
			this.sma = sma;
			for(List<TraceElement> trace : doneTraces.getPos()){
				done.add(getString(trace));
			}

		}

		private double computeSimilarity(List<String> strings, List<String> strings1) {
			List<String> numerator = new ArrayList<>();
			numerator.addAll(strings);
			numerator.retainAll(strings1);
			double numSize = (double) numerator.size();
			List<String> union = new ArrayList<>();
			union.addAll(strings);
			union.addAll(strings1);
			double unionSize = (double) union.size();
			double gower = (numSize / (numSize + .5*(unionSize-numSize)));
			return gower;
		}

		private List<String> getString(List<TraceElement> te){
			List<String> stringSequence = new ArrayList<>();
			WalkResult wr = sma.walk(te,dfa.getInitialState(),new ArrayList<>(),dfa.getAutomaton());
			for(DefaultEdge de : wr.getWalk()){
				stringSequence.add(dfa.getAutomaton().getTransitionSource(de).toString());
				stringSequence.add(dfa.getAutomaton().getTransitionData(de).getLabel());
				stringSequence.add(dfa.getAutomaton().getTransitionTarget(de).toString());
			}
			return stringSequence;
		}

		@Override
		public int compare(List<TraceElement> o1, List<TraceElement> o2) {

			Double o1Total = 0D;
			List<String> strings = getString(o1);
			for(int i = 0; i<done.size(); i++){
				double similarity = computeSimilarity(done.get(i),strings);
				o1Total = o1Total + similarity;

			}

			Double o2Total = 0D;
			List<String> strings2 = getString(o2);
			for(int i = 0; i<done.size(); i++){
				double similarity = computeSimilarity(done.get(i),strings2);
				o2Total = o2Total + similarity;

			}
			return o1Total.compareTo(o2Total);
		}

		@Override
		public boolean equals(Object obj) {
			return false;
		}
	}


}
