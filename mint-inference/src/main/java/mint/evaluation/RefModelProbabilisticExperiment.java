package mint.evaluation;

import mint.Configuration;
import mint.evaluation.kfolds.ProbabilisticExperiment;
import mint.evaluation.kfolds.Result;
import mint.evaluation.kfolds.SimpleResult;
import mint.model.Machine;
import mint.model.ProbabilisticMachine;
import mint.model.dfa.TraceDFA;
import mint.model.soa.ProbabilisticMachineDecorator;
import mint.model.soa.SOResult;
import mint.model.soa.SubjectiveOpinion;
import mint.model.soa.SubjectiveOpinionResult;
import mint.model.walk.WalkResult;
import mint.model.walk.probabilistic.ProbabilisticMachineAnalysis;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import org.apache.log4j.Logger;

import java.util.*;

public class RefModelProbabilisticExperiment extends ProbabilisticExperiment {

    protected ProbabilisticMachine reference;

    private final static Logger LOGGER = Logger.getLogger(RefModelProbabilisticExperiment.class.getName());


    public RefModelProbabilisticExperiment(String name, Random r, Collection<List<TraceElement>> trace, int folds, Configuration.Data algo, int seed, int tail, boolean data, Configuration.Strategy strategy, ProbabilisticMachine reference) {
        super(name, r, trace, folds, algo, seed, tail, data, strategy);
        this.reference = reference;
    }

    @Override
    public List<Result> call() {
        LOGGER.info("Running experiment for:"+name+","+algo.toString()+","+seed+","+data+","+strategy);
        setConfiguration();
        List<Set<List<TraceElement>>> f = computeFolds(folds);
        SubjectiveOpinionResult res = new SubjectiveOpinionResult(name,algo.toString(),seed,tail,data,strategy);
        for(int i = 0; i< folds; i++){
            TraceSet testing = new TraceSet(f.get(i));
            TraceSet training = new TraceSet();
            for(int j = 0; j<folds;j++){
                if(j==i)
                    continue;
                training.getPos().addAll(f.get(j));
            }


            ProbabilisticMachineDecorator  model = null;
            try {
                TraceSet ev = new TraceSet();
                for (List<TraceElement> tes : testing.getPos()) {
                    ev.addPos(tes);
                }
                eval = ev;
                model = (ProbabilisticMachineDecorator)learnModel(training);
                if(model == null) {
                    LOGGER.info("Skipping null model");
                    continue;
                }

                for(List<TraceElement> test : ev.getPos()){
                    WalkResult result = model.walk(test);
                    SubjectiveOpinion so = model.walkOpinion(result);
                    TraceDFA.Accept accept = result.isAccept(model.getAutomaton());
                    ProbabilisticMachineAnalysis referenceModelAnalysis = new ProbabilisticMachineAnalysis(reference);
                    double predictedOpinion = referenceModelAnalysis.getProbabilityOfWalk(test);
                    res.addResult(new SOResult(so,predictedOpinion,accept, model.getStates().size(), model.getAutomaton().getTransitions().size()));
                }
            }
            catch(Exception e){
                LOGGER.error(e.toString());
                e.printStackTrace();
                System.exit(0);
            }
        }
        results.add(res);

        return results;
    }



}
