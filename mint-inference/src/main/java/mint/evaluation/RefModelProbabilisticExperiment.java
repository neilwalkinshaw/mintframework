package mint.evaluation;

import citcom.subjectiveLogic.BinomialOpinion;
import mint.Configuration;
import mint.evaluation.kfolds.ProbabilisticExperiment;
import mint.evaluation.kfolds.Result;
import mint.model.ProbabilisticMachine;
import mint.model.dfa.TraceDFA;
import mint.model.soa.*;
import mint.model.walk.SimpleMachineAnalysis;
import mint.model.walk.WalkResult;
import mint.model.walk.probabilistic.ProbabilisticMachineAnalysis;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import org.apache.log4j.Logger;

import java.util.*;

public class RefModelProbabilisticExperiment extends ProbabilisticExperiment {

    protected ProbabilisticMachine reference;
    protected double proportionNeg = 1;

    private final static Logger LOGGER = Logger.getLogger(RefModelProbabilisticExperiment.class.getName());


    public RefModelProbabilisticExperiment(String name, Random r, Collection<List<TraceElement>> negtrace, Collection<List<TraceElement>> trace, int folds, Configuration.Data algo, int seed, int tail, boolean data, Configuration.Strategy strategy, ProbabilisticMachine reference, double proportionNeg) {
        super(name, r, trace, negtrace, folds, algo, seed, tail, data, strategy);
        this.reference = reference;
        this.proportionNeg = proportionNeg;
    }

    @Override
    public List<Result> call() {
        LOGGER.info("Running experiment for:"+name+","+algo.toString()+","+seed+","+data+","+strategy);
        setConfiguration();
        List<TraceSet> f = computeFolds(folds);
        ProbabilisticMachineAnalysis referenceModelAnalysis = new ProbabilisticMachineAnalysis(reference);
        for(TraceSet ts : f){
            for(List<TraceElement> p : ts.getPos()){
                assert (referenceModelAnalysis.walkAccept(p, false, reference.getAutomaton()) != TraceDFA.Accept.REJECT);
            }
        }
        SubjectiveOpinionResult res = new SubjectiveOpinionResult(name,algo.toString(),seed,tail,data,strategy);
        for(int i = 0; i< folds; i++){
            TraceSet testing = f.get(i);
            TraceSet training = new TraceSet();
            for(int j = 0; j<folds;j++){
                if(j==i)
                    continue;
                training.getPos().addAll(f.get(j).getPos());
                training.getNeg().addAll(proportion(f.get(j).getNeg()));

            }


            MultinomialOpinionMachineDecorator model = null;
            try {
                TraceSet ev = new TraceSet();
                for (List<TraceElement> tes : testing.getPos()) {
                    ev.addPos(tes);
                }
                for (List<TraceElement> tes : testing.getNeg()) {
                    ev.addNeg(tes);
                }
                eval = ev;
                model = (MultinomialOpinionMachineDecorator)learnModel(training);
                //model.setStrict(false);
                if(model == null) {
                    LOGGER.info("Skipping null model");
                    continue;
                }
                processSequence(res, model, ev.getPos());
                processSequence(res, model, ev.getNeg());
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

    private Collection<? extends List<TraceElement>> proportion(List<List<TraceElement>> neg) {
        Collections.shuffle(neg);
        int num = (int) (neg.size()*this.proportionNeg);
        Collection<List<TraceElement>> reduced = new ArrayList<>();
        for(int i = 0; i<num; i++){
            reduced.add(neg.get(i));
        }
        return reduced;
    }


    public void processSequence(SubjectiveOpinionResult res, MultinomialOpinionMachineDecorator model, List<List<TraceElement>> all) {
        for(List<TraceElement> test : all){
            if(test.size()==0)
                continue;
            SimpleMachineAnalysis predAnalysis = new SimpleMachineAnalysis(model);
            WalkResult wr = predAnalysis.walk(test,model.getInitialState(),new ArrayList<>(),model.getAutomaton());
            WalkResult moWalk = model.walk(test);
            //MultinomialOpinion so = model.walkOpinion(moWalk,test.size());
            BinomialOpinion so = model.binomialWalkOpinion(moWalk,moWalk.getWalk().size(), true);

            TraceDFA.Accept predictedAccept = wr.isAccept(model.getAutomaton());
            ProbabilisticMachineAnalysis referenceModelAnalysis = new ProbabilisticMachineAnalysis(reference);
            WalkResult refResult = referenceModelAnalysis.walk(test,reference.getInitialState(),new ArrayList<>(),reference.getAutomaton());
            TraceDFA.Accept accept = refResult.isAccept(reference.getAutomaton());
            double probability = referenceModelAnalysis.getProbabilityOfWalk(test);
            List<String> seq = new ArrayList<String>();
            for(int i = 0; i<wr.getWalk().size(); i++){
                seq.add(test.get(i).getName());
            }

            double belief = 0;
            //if(predictedAccept == TraceDFA.Accept.ACCEPT)
            belief = so.getBelief();
            SOMResult r = new SOMResult(belief,so.getUncertainty(),predictedAccept,probability,accept,model.getStates().size(), model.getAutomaton().getTransitions().size());
            res.addResult(r);
        }
    }


}
