package mint.evaluation.kfolds;

import mint.Configuration;
import mint.evaluation.RefModelProbabilisticExperiment;
import mint.model.RawProbabilisticMachine;
import mint.model.soa.SubjectiveOpinionResult;
import mint.tracedata.TraceElement;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class RefModelKFoldsEvaluator extends StrategicKFoldsEvaluator {

    public RawProbabilisticMachine refModel;
    protected double negProp;
    private final static Logger LOGGER = Logger.getLogger(RefModelKFoldsEvaluator.class.getName());


    public RefModelKFoldsEvaluator(String name, Collection<List<TraceElement>> trace, Collection<List<TraceElement>> negTrace,
                                   int seed, int tail, RawProbabilisticMachine refModel, double negProp) {
        super(name, trace, negTrace, seed, tail);
        this.negProp = negProp;
        this.refModel = refModel;
    }

    protected Experiment generateExperiment(int folds,
                                            Configuration.Data[] algos, int i, boolean data, Configuration.Strategy strategy) {
        //return new ProbabilisticExperiment(name, new Random(seed),trace,folds,algos[i],seed, tail, data,strategy);
        return new RefModelProbabilisticExperiment(name, new Random(seed),negTrace,trace,folds,algos[i],seed, tail, data,strategy, refModel, negProp);

    }


    protected void output(List res) {
        FileWriter fWriter = null;
        BufferedWriter writer = null;
        try {
            fWriter = new FileWriter(name+".csv",true);
            writer = new BufferedWriter(fWriter);
            for (Object result : res) {
                SubjectiveOpinionResult sor = (SubjectiveOpinionResult) result;
                writer.append(sor.toString());
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
