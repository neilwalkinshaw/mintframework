package mint.inference.efsm.scoring.scoreComputation;

import mint.Configuration;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.model.dfa.TraceDFA;
import mint.model.statepair.StatePair;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;


public class LinearScoreComputer extends KTailsScorecomputer {


    boolean includeBalance = false;
    protected Map<Integer,Integer> stateDepths;
    protected double gapPenalty = 0.5;

    public LinearScoreComputer(){
        super(Configuration.getInstance().K);
        stateDepths = new HashMap<Integer, Integer>();

    }

    public LinearScoreComputer(boolean includeBalance){
        super(Configuration.getInstance().K);
        this.includeBalance = includeBalance;
        stateDepths = new HashMap<Integer, Integer>();

    }

    public LinearScoreComputer(SimpleMergingState mergingState, StatePair sp, boolean includeBalance, Map<Integer,Integer> depths) {
        super(mergingState, sp, Configuration.getInstance().K);
        Configuration config = Configuration.getInstance();
        this.includeBalance = includeBalance;
        this.stateDepths = depths;
        config.CAREFUL_DETERMINIZATION = false;
    }



   @Override
    protected Score recurseScore(StatePair sp) {

       TraceDFA td = mergingState.getCurrent().getAutomaton();

       if(!mergingState.allowed(sp))
           return new Score(-1);
       //Integer stateA = sp.getFirstState();
       Integer stateB = sp.getSecondState();
       //if (td.reachableFrom(stateA, stateB))
         //  return new Score(-1);
       //if (td.reachableFrom(stateB, stateA))
        //   return new Score(-1);
       if(!includeBalance)
        return super.recurseScore(sp);
       //else return new Score(Math.max((super.recurseScore(sp).getPrimaryScore()-prefixDiff(sp)),0));
       else{
           double diff = (double)prefixDiff(sp);
           //int penalty = (int)(diff * gapPenalty);
           int origscore = super.recurseScore(sp).getPrimaryScore();

           double doubScore = origscore / ((gapPenalty*diff)+1);
           int score = (int)doubScore;
           //if(diff < 5)
           //     score = score

           return new Score(score);
       }

    }

    /**
     * Return the difference between prefix lengths for the two states in sp.
     * @param sp
     * @return
     */
    private int prefixDiff(StatePair sp) {
        int first = stateDepths.get(sp.getFirstState());
        int second = stateDepths.get(sp.getSecondState());
        int retScore = Math.abs(first - second);
        return retScore;
    }

    /**
     * Do a BFS traversal of the state machine, store the depth of each state
     * in the stateDepths attribute.
     */
    private void bfsStates(SimpleMergingState mState) {
        List<Integer> done = new ArrayList<Integer>();
        Queue<Integer> todo = new LinkedList<Integer>();
        Integer initialState= mState.getCurrent().getInitialState();
        todo.add(initialState);
        done.add(initialState);
        stateDepths.put(initialState,0);
        while(!todo.isEmpty()){
            Integer current = todo.poll();
            for(DefaultEdge outgoing : (Set<DefaultEdge>)mState.getCurrent().getAutomaton().getOutgoingTransitions(current)) {
                Integer target = mState.getCurrent().getAutomaton().getTransitionTarget(outgoing);
                Integer newDepth = stateDepths.get(current)+1;
                if(stateDepths.containsKey(target))
                    newDepth = Math.min(newDepth,stateDepths.get(target)); //depth is shortest path from init.
                stateDepths.put(target,newDepth);
                if(!done.contains(target)) {
                    done.add(target);
                    todo.add(target);
                }
            }

        }
    }

    public void computeStateDepths(SimpleMergingState mergingState){

        bfsStates(mergingState);
    }

    public ComputeScore newInstance(SimpleMergingState mergingState,StatePair sp){
        //if(stateDepths.isEmpty())
        //   computeStateDepths(mergingState);
        return new LinearScoreComputer(mergingState,sp, includeBalance, stateDepths);
    }


}
