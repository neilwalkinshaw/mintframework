package mint.evaluation.kfolds;

import mint.Configuration;
import mint.evaluation.RefModelProbabilisticExperiment;
import mint.model.ProbabilisticMachine;
import mint.tracedata.TraceElement;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class RefModelKFoldsEvaluator extends StrategicKFoldsEvaluator {

    public ProbabilisticMachine refModel;
    private final static Logger LOGGER = Logger.getLogger(RefModelKFoldsEvaluator.class.getName());


    public RefModelKFoldsEvaluator(String name, Collection<List<TraceElement>> trace, Collection<List<TraceElement>> negTrace,
                                   int seed, int tail, ProbabilisticMachine refModel) {
        super(name, trace, negTrace, seed, tail);
        this.refModel = refModel;
    }

    protected Experiment generateExperiment(int folds,
                                            Configuration.Data[] algos, int i, boolean data, Configuration.Strategy strategy) {
        //return new ProbabilisticExperiment(name, new Random(seed),trace,folds,algos[i],seed, tail, data,strategy);
        return new RefModelProbabilisticExperiment(name, new Random(seed),trace,folds,algos[i],seed, tail, data,strategy, refModel);

    }



}
