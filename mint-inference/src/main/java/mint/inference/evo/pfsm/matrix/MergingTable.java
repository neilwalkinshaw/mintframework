package mint.inference.evo.pfsm.matrix;

import org.apache.log4j.Logger;
import mint.inference.efsm.EDSMMerger;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.inference.evo.Chromosome;
import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.prefixtree.FSMPrefixTreeFactory;
import mint.model.prefixtree.PrefixTreeFactory;
import mint.model.statepair.OrderedStatePair;
import mint.model.statepair.StatePair;
import mint.model.statepair.StatePairComparator;
import mint.tracedata.TraceSet;

import java.util.*;

/**
 * Created by neilwalkinshaw on 19/05/2016.
 */
public class MergingTable implements Chromosome {

    private final static Logger LOGGER = Logger.getLogger(MergingTable.class.getName());

    static int counter=0;

    protected int id;

    List<StatePair> merges;

    protected String latest;

    protected TraceSet base;
    int baseStates = 0;


    public void setMerges(List<StatePair> merges){
        this.merges = merges;
    }

    public MergingTable(TraceSet prefixTree, int baseStates){
        this.baseStates = baseStates;
        merges = new ArrayList<StatePair>();
        id = counter;
        counter++;
        base = prefixTree;
    }

    /**
     *
     * @param rand
     */

    public void randomise(Random rand, int num){
        for(int j = 0; j<num; j++) {
            int index = rand.nextInt(baseStates);
            int jindex = rand.nextInt(baseStates);
            merges.add(new StatePair(index,jindex));
        }

    }

    public void sortMerges(){
        SimpleMergingState<PayloadMachine> sm = new SimpleMergingState<PayloadMachine>(getTree());
        Collections.sort(merges,new StatePairComparator(sm));
    }

    protected PayloadMachine getTree(){
        PrefixTreeFactory<?> tptg;
        tptg = new FSMPrefixTreeFactory(new PayloadMachine());
        PayloadMachine tree = (PayloadMachine)tptg.createPrefixTree(base);
        return tree;
    }


    public PayloadMachine getMergedMachine(){
        SimpleMergingState<PayloadMachine> sm = new SimpleMergingState<PayloadMachine>(getTree());
        EDSMMerger<PayloadMachine,SimpleMergingState<PayloadMachine>> merger = new EDSMMerger<PayloadMachine, SimpleMergingState<PayloadMachine>>(null,sm);
        Collection<StatePair> toRemove = new HashSet<StatePair>();
        int counter = 0;
        for(StatePair sp : merges){
            counter++;
            if(StatePairComparator.getPairScore(sp)<1) {
                if(merges.size()-toRemove.size()>(baseStates/4)) {
                    toRemove.add(sp);
                }
                continue;
            }
            Machine current = sm.getCurrent();
            if(current.getStates().contains(sp.getFirstState()) && current.getStates().contains(sp.getSecondState()) ){
                if(current.getInitialState().equals(sp.getSecondState()))
                    sp = new StatePair(sp.getSecondState(), sp.getFirstState());
                merger.merge(new OrderedStatePair(sp.getFirstState(),sp.getSecondState()));
            }
            else{
                if(merges.size()-toRemove.size()>(baseStates/4)) {
                    toRemove.add(sp);
                }
            }
        }
        merges.removeAll(toRemove);
        latest = sm.getCurrent().getStates().size()+" states "+merges.size()+" merges";
        return sm.getCurrent();
    }


    public void mutate(Random r) {
        sortMerges();
        int choice = r.nextInt(2);
        int stateA, stateB;
        stateA = r.nextInt(baseStates);
        stateB = r.nextInt(baseStates);

        if(choice == 0) {
            double proportion = r.nextDouble();
            int number = (int) (proportion * merges.size());
            for (int i = (merges.size() - 1); i > Math.max(merges.size() - number, 0); i--) {
                StatePair sp = merges.get(i);
                if (r.nextBoolean()) {
                    sp.setFirstState(stateA);
                } else {
                    sp.setSecondState(stateB);
                }
            }
        }
        else if(choice == 1){
            double proportion = r.nextDouble();
            int number = (int) (proportion * merges.size());
            for (int i = 0; i< number; i++) {
                merges.add(new StatePair(stateA,stateB));
            }
        }

    }

    @Override
    public Chromosome copy() {
        MergingTable copy = new MergingTable(base,baseStates);
        List<StatePair> newMerges = new ArrayList<StatePair>();
        for(StatePair sp : merges){
            newMerges.add(new StatePair(sp.getFirstState(),sp.getSecondState()));
        }
        copy.setMerges(newMerges);
        return copy;
    }
}
