package mint.inference.evo.pfsm.matrix;

import mint.Configuration;
import mint.inference.evo.*;
import mint.model.PayloadMachine;
import mint.model.prefixtree.FSMPrefixTreeFactory;
import mint.model.prefixtree.PrefixTreeFactory;
import mint.tracedata.TraceSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by neilwalkinshaw on 19/05/2016.
 */
public class GP extends AbstractEvo {

    protected Random random;
    protected TraceSet traceData;

    public GP(GPConfiguration gpConf, TraceSet traceData) {
        super(gpConf);
        random = new Random(Configuration.getInstance().SEED);
        this.traceData = traceData;
    }

    @Override
    protected AbstractIterator getIterator(Selection selection) {
        return new Iterate(population, gpConf.getCrossOver(), gpConf.getMutation(), random, selection.getElites());

    }

    @Override
    protected Selection buildSelection(List<Chromosome> population) {
        return new PFSMTournamentSelection(population,traceData.getPos());
    }

    @Override
    protected List<Chromosome> select(List<Chromosome> population, Selection selection) {
        List<Chromosome> sel = selection.select(gpConf);
        return sel;
    }

    @Override
    protected List<Chromosome> generatePopulation(int i) {
        List<Chromosome> population = new ArrayList<Chromosome>();
        PrefixTreeFactory<?> tptg;
        tptg = new FSMPrefixTreeFactory(new PayloadMachine());
        PayloadMachine tree = (PayloadMachine)tptg.createPrefixTree(traceData);
        int total = tree.getStates().size()/4;

        for(int j = 0; j<i; j++){
            MergingTable mt = new MergingTable(traceData,tree.getStates().size());
            mt.randomise(random,total);
            population.add(mt);
        }
        return population;
    }
}
