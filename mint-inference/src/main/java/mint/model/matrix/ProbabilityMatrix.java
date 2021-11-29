package mint.model.matrix;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import mint.model.RawProbabilisticMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.model.walk.WalkResult;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Build a transition probability matrix corresponding to a state machine.
 *
 * Created by neilwalkinshaw on 16/05/2016.
 */


public class ProbabilityMatrix {

    protected Map<Integer,Integer> statesToIndices = new HashMap<Integer,Integer>();
    private Integer indexCounter = 0; // keeps track of index we're on for state machine
    protected RealMatrix fundamentalMatrix;
    protected RawProbabilisticMachine machine;

    public ProbabilityMatrix(RawProbabilisticMachine machine){
        this.machine = machine;
        initialise(machine);
    }



    private void initialise(RawProbabilisticMachine machine) {
        double[][] fundamental = new double[machine.getStates().size()][machine.getStates().size()];
        generateAbsoluteCounts(fundamental, machine);
        normalise(fundamental, true);
        fundamentalMatrix = new Array2DRowRealMatrix(fundamental);
        fundamentalMatrix = createFundamentalMatrix(fundamentalMatrix);
    }

    private RealMatrix createFundamentalMatrix(RealMatrix transitionMatrix){
        RealMatrix ident = MatrixUtils.createRealIdentityMatrix(transitionMatrix.getColumnDimension());
        RealMatrix subtract = ident.subtract(transitionMatrix);
        RealMatrix inverse = new LUDecomposition(subtract).getSolver().getInverse();
        return inverse;
    }

    /**
     * Change counts to probabilities, by normalising the transition matrix.
     * Rows do not add up to 1, because an additional imaginary transition
     * is added, leading to the imaginary"absorbing" state.
     * @param matrix
     */
    private void normalise(double[][] matrix, boolean absorbing) {
        for(int i = 0; i< matrix.length; i++){
            double[] row = matrix[i];
            double sum = 0D;
            for(int j = 0; j<row.length; j++){
                sum+=row[j];
            }
            for(int j = 0; j<row.length; j++){
                if(absorbing)
                    row[j] = row[j]/(sum+1);
                else
                    row[j] = row[j]/(sum+1);
            }
        }

    }

    /**
     * Generate absolute counts of number of times a transition between a pair
     * of states is traversed. If there are dual-transitions, then these are added
     * together to form a single transition count.
     *
     * @param matrix
     * @param machine
     */
    private void generateAbsoluteCounts(double[][] matrix, RawProbabilisticMachine machine) {
        TraceDFA<Double> automaton = machine.getAutomaton();
        initialiseMatrix(matrix);
        for(Integer state : machine.getStates()){
            Integer stateIndex = getState(state);
            for(DefaultEdge de : automaton.getOutgoingTransitions(state)){
                TransitionData<Double> transData = automaton.getTransitionData(de);
                Integer targetIndex = getState(automaton.getTransitionTarget(de));
                matrix[stateIndex][targetIndex] =  matrix[stateIndex][targetIndex] + transData.getPayLoad();
            }
        }
    }



    /**
     * Initialises array with zero probabilities.
     * @param matrix
     */
    private void initialiseMatrix(double[][] matrix) {
        for(int i = 0; i<matrix.length; i++){
            for(int j = 0; i<matrix.length; i++){
                matrix[i][j]=0D;
            }
        }
    }

    public double getCount(WalkResult result){
        Double ret = 0D;
        double transitionProbability = 1;
        List<DefaultEdge> transitions = result.getWalk();
        int baseIndex = 0;
        for(int from = 0; from<transitions.size(); from++){
            DefaultEdge de = transitions.get(from);
            assert(machine!=null);
            TraceDFA automaton = machine.getAutomaton();
            assert(automaton !=null);
            Integer fromIndex = getState(automaton.getTransitionSource(de));
            double stateTimes = fundamentalMatrix.getEntry(baseIndex,fromIndex);
            ret+=stateTimes * transitionProbability;
            transitionProbability = machine.getProbability(transitions.get(from));
            if(transitionProbability == 0D)
                return 0D;
        }
        return ret;
    }



    private Integer getState(Integer machineState){
        Integer index;
        if(statesToIndices.containsKey(machineState)){
            index = statesToIndices.get(machineState);
        }
        else{
            index = indexCounter;
            statesToIndices.put(machineState,indexCounter);
            indexCounter++;
        }
        return index;
    }
}
