package mint.inference.efsm.scoring.scoreComputation;

import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.statepair.StatePair;
import mint.tracedata.TraceElement;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class KTailsScorecomputer extends ComputeScore {

	final protected int min_score;
    protected boolean limitToDecisionOnK = false;
	
	public KTailsScorecomputer(SimpleMergingState mergingState, StatePair sp, int min_score) {
        super(mergingState,sp);
        this.min_score = min_score;
	}

    public void setLimitToDecisionOnK(boolean decision){
        limitToDecisionOnK = decision;
    }

    public KTailsScorecomputer(int min_score){
        this.min_score = min_score;
    }



    protected Score recurseScore(StatePair sp){
        Stack<StatePair> todo = new Stack<StatePair>();
		todo.add(sp);

		return recurseScore(todo,new HashSet<StatePair>(), 0);
	}

    public ComputeScore newInstance(SimpleMergingState mergingState,StatePair sp){
        KTailsScorecomputer newInst = new KTailsScorecomputer(mergingState,sp,min_score);
        newInst.setLimitToDecisionOnK(limitToDecisionOnK);
        return newInst;
    }

    protected Score recurseScore(Stack<StatePair> todo, Set<StatePair> done, int depth){

        if(limitToDecisionOnK && (depth >= min_score))
            return new Score(depth);
        Stack<StatePair> newTodo = new Stack<StatePair>();
        TraceDFA automaton = mergingState.getCurrent().getAutomaton();
        todo.removeAll(done);

        if(todo.isEmpty())
            return new Score(depth);
        while(!todo.isEmpty()){
            StatePair sp = todo.pop();
            done.add(sp);
            Integer a = sp.getFirstState();
            Integer b = sp.getSecondState();
            if((!automaton.compatible(a,b)))
                continue;
            Set<DefaultEdge> fromTransitions = new HashSet<DefaultEdge>();
            fromTransitions.addAll(automaton.getOutgoingTransitions(a));
            Iterator<DefaultEdge> aIt = fromTransitions.iterator();
            while(aIt.hasNext()){
                DefaultEdge current = aIt.next();
                Set<DefaultEdge> bTrans = automaton.getOutgoingTransitions(b);
                TransitionData<Set<TraceElement>> currentData = automaton.getTransitionData(current);
                boolean found = false;
                for (DefaultEdge bTran : bTrans) {
                    TransitionData<Set<TraceElement>> bTranData = automaton.getTransitionData(bTran);
                    if(bTran.equals(current))
                        continue;
                    if(!currentData.getLabel().equals(bTranData.getLabel()))
                        continue;
                    Integer currentDest = automaton.getTransitionTarget(current);
                    Integer transDest = automaton.getTransitionTarget(bTran);
                    if(!automaton.compatible(currentDest,transDest)) //why - shouldn't this be !compatible?
                        continue;
                    if(mergingState.getCurrent().compatible(current, bTran)){
                        found = true;
                        StatePair osp = new StatePair(automaton.getTransitionTarget(current),automaton.getTransitionTarget(bTran));
                        newTodo.push(osp);
                        break;
                    }
                }
                if(!found)
                    return new Score(depth);
            }
        }
        if(newTodo.isEmpty())
            return new Score(depth);
        return recurseScore(newTodo, done, depth + 1);
    }

}
