package mint.evaluation;

import mint.Configuration;
import mint.evaluation.kfolds.ProbabilisticExperiment;
import mint.model.Machine;
import mint.model.ProbabilisticMachine;
import mint.model.walk.probabilistic.ProbabilisticMachineAnalysis;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;

import java.awt.image.AreaAveragingScaleFilter;
import java.util.*;

public class RefModelProbabilisticExperiment extends ProbabilisticExperiment {

    protected ProbabilisticMachine reference;

    public RefModelProbabilisticExperiment(String name, Random r, Collection<List<TraceElement>> trace, int folds, Configuration.Data algo, int seed, int tail, boolean data, Configuration.Strategy strategy, ProbabilisticMachine reference) {
        super(name, r, trace, folds, algo, seed, tail, data, strategy);
        this.reference = reference;
    }

    protected Double score(Machine model, TraceSet pos) {

        ProbabilisticMachine pm = buildProbabilisticMachine(model);

        ProbabilisticMachineAnalysis trainingModelAnalysis = new ProbabilisticMachineAnalysis(pm);
        ProbabilisticMachineAnalysis referenceModelAnalysis = new ProbabilisticMachineAnalysis(reference);

        List<Double> inferredDist =  getDistribution(trainingModelAnalysis, pos);
        List<Double> testDist = getDistribution(referenceModelAnalysis,pos);

        normalise(inferredDist);
        normalise(testDist);
        return KLDivergencee(inferredDist,testDist);
    }

    private List<Double> getDistribution(ProbabilisticMachineAnalysis modelAnalysis, TraceSet pos) {
        List<Double> distribution = new ArrayList<>();
        Iterator<List<TraceElement>> posIt = pos.getPos().iterator();
        while(posIt.hasNext()){
            List<TraceElement> trace = posIt.next();
            double probability = modelAnalysis.getProbabilityOfWalk(trace);
            distribution.add(probability);
        }
        return distribution;
    }


}
