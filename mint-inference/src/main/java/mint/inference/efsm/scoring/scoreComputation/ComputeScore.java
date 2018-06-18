package mint.inference.efsm.scoring.scoreComputation;

import mint.Configuration;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.statepair.StatePair;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * A callable score-computer. Ought to independently compute the score for a state-pair. Can be run
 * concurrently with other scorers.
 *
 * Created by neilwalkinshaw on 17/10/2014.
 */
public class ComputeScore< T extends SimpleMergingState<?>> implements Callable<Score> {

    protected  T mergingState;
    private Set<StatePair> seen;
    private StatePair sp;
    private Map<StatePair,Score> checked;
    private boolean carefulDet;

    /*
    In addition to computing a primary score, should we compute a secondary score?
    This would count the number of joint trace elements for pairs of transitions
    in suffixes of two states.
    */
    private boolean secondaryScoring = false;


    public ComputeScore(){

    }

    public ComputeScore(T mergingState, StatePair sp){
        this.mergingState = mergingState;
        seen = new HashSet<StatePair>();
        this.sp = sp;
        checked = new HashMap<StatePair,Score>();
        carefulDet = Configuration.getInstance().CAREFUL_DETERMINIZATION;
    }

    public ComputeScore newInstance(T mergingState,StatePair sp){
        return new ComputeScore(mergingState,sp);
    }

    @Override
    public Score call() throws Exception {
        return recurseScore(sp);
    }

    private boolean scoreKnown(T rms, StatePair sp){
        if(rms.alreadyAttempted(new StatePair(sp.getFirstState(), sp.getSecondState()))) {
            checked.put(new StatePair(sp.getFirstState(),sp.getSecondState()),new Score(0));
            return true;
        }
        else if(checked.keySet().contains(new StatePair(sp.getFirstState(),sp.getSecondState())))
            return true;
        return false;
    }

    private Score getKnownScore(T rms, StatePair sp){
        if(rms.alreadyAttempted(sp))
            return new Score(-1);
        else if(checked.keySet().contains(new StatePair(sp.getFirstState(),sp.getSecondState())))
            return checked.get(new StatePair(sp.getFirstState(),sp.getSecondState()));
        return new Score(0);
    }


    /**
     * This function will check the score for a pair of states recursively, using the scoring approach used for EDSM state merging.
     *
     * It will only give a negative score (i.e. incompatible) if the two states have been previously proven to be unmergable.
     * Otherwise the lowest score is a zero.
     * @param sp
     * @return
     */
    protected Score recurseScore(StatePair sp){

        if(scoreKnown(mergingState, sp)){
            return getKnownScore(mergingState,sp);
        }
        Score score = new Score(0);

        Integer a = sp.getFirstState();
        Integer b = sp.getSecondState();
        TraceDFA automaton = mergingState.getCurrent().getAutomaton();
		if(!automaton.compatible(a,b)){
            checked.put(new StatePair(a,b),new Score(-1));
			return new Score(-1);
		}
        if(automaton.getOutgoingTransitions(a).size() == 0 || automaton.getOutgoingTransitions(b).size() == 0) {
            checked.put(new StatePair(a,b),new Score(0));
            return new Score(0);
        }
        Set<DefaultEdge> fromTransitions = new HashSet<DefaultEdge>();
        fromTransitions.addAll(automaton.getOutgoingTransitions(b));
        Iterator<DefaultEdge> bIt = fromTransitions.iterator();
        while(bIt.hasNext()){
            DefaultEdge current = bIt.next();
            TransitionData<Set<TraceElement>> data=automaton.getTransitionData(current);
            Set<DefaultEdge> aTrans = automaton.getOutgoingTransitions(a);
            if(!aTrans.isEmpty()){
                for (DefaultEdge aTran : aTrans) {
                    //For every pair of outgoing transitions from the respective states:
                    TransitionData<Set<TraceElement>> aTranData=automaton.getTransitionData(aTran);
                    Score transScore = new Score(0);

                    //work out a score transScore.

                    if(aTran.equals(current))
                        continue;
                    if(!data.getLabel().equals(aTranData.getLabel()))
                        continue;
                    Integer currentDest = automaton.getTransitionTarget(current);

                    Integer transDest = automaton.getTransitionTarget(aTran);

                    //get their respective target states

                    //If the labels on the transitions are the same, we need to recurse to destination states.
                    if(mergingState.getCurrent().compatible(current,aTran)){

                        transScore.incrementPrimaryScore();
                        transScore.setSecondaryScore(transScore.getSecondaryScore() + compatibilityWeight(aTranData,data));
                        StatePair p = new StatePair(currentDest,transDest);
                        Score recursiveScore;
                        if(scoreKnown(mergingState,p)) {
                            Score knownSc = getKnownScore(mergingState,p);
                            if(knownSc.getPrimaryScore()<0)
                                return new Score(-1);
                            else {
                                recursiveScore = knownSc;
                            }
                        }
                        else {
                            if(seen.contains(p))
                                continue;
                            seen.add(p);
                            recursiveScore = recurseScore(p);

                            if (recursiveScore.getPrimaryScore() < 0) {

                                return new Score(-1);
                            }
                            checked.put(p, recursiveScore);
                        }
                        transScore.setPrimaryScore(transScore.getPrimaryScore() + recursiveScore.getPrimaryScore());
                        transScore.setSecondaryScore(transScore.getSecondaryScore() + recursiveScore.getSecondaryScore());
                    }
                    else{ //labels on transitions are not compatible
                        Score s = new Score(0);
                        if(carefulDet)
                            s.setPrimaryScore(-1);
                        checked.put(new StatePair(a,b),s);
                        transScore = s;
                    }
                    score.setPrimaryScore(score.getPrimaryScore() + transScore.getPrimaryScore());
                    score.setSecondaryScore(score.getSecondaryScore() + transScore.getSecondaryScore());
                }

            }
        }
        checked.put(new StatePair(a,b),score);
        return score;
    }

    private int compatibilityWeight(TransitionData<Set<TraceElement>> aTranData, TransitionData<Set<TraceElement>> aTranData1) {
        return Math.min(aTranData.getPayLoad().size(),aTranData1.getPayLoad().size());
    }


}
