package mint.evaluation;

import mint.Configuration;
import mint.evaluation.kfolds.CPResult;
import mint.evaluation.kfolds.Experiment;
import mint.evaluation.kfolds.Result;
import mint.model.LatentProbabilitiesMachine;
import mint.model.Machine;

import mint.model.walk.WalkResult;
import mint.model.walk.probabilistic.*;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

public class ConditionalProbabilitiesExperiment extends Experiment {

    private final static Logger LOGGER = Logger.getLogger(Experiment.class.getName());

    protected LatentProbabilitiesMachine reference;
    protected int prefixLimit;
    protected String numTraces;
    protected LatentDependenciesProbabilisticMachineAnalysis referenceModelAnalysis;
    protected List<String> alphabet;

    public ConditionalProbabilitiesExperiment(String label, TraceSet traces, LatentProbabilitiesMachine pdfa, int prefixLimit, int folds, int seed, String numTraces) {
        //Adaboost, tails and strategy are all irrelevant here - TODO need to adjust constructor to accommodate experiments that don't involve inference...
        super(label,new Random(seed), traces.getPos(), traces.getNeg(), folds, Configuration.Data.AdaBoost, seed, 0, true, Configuration.Strategy.exhaustive);
        this.reference = pdfa;
        this.prefixLimit = prefixLimit;
        this.numTraces=numTraces;
        alphabet = new ArrayList<>();
        alphabet.addAll(reference.getAutomaton().getAlphabet());
    }


    @Override
    public List<Result> call() {
        List<Result> results = new ArrayList<>();
        List<TraceSet> f = computeFolds(folds);

        for(int i = 0; i< folds; i++){
            TraceSet testing = f.get(i);
            TraceSet training = new TraceSet();
            for(int j = 0; j<folds;j++){
                if(j==i)
                    continue;
                training.getPos().addAll(f.get(j).getPos());
                training.getNeg().addAll(f.get(j).getNeg());
            }
            try {
                //referenceModelAnalysis = new StateInsensitiveProbabilisticMachineAnalysis(reference,prefixLimit,training);
                referenceModelAnalysis = new LatentDependenciesProbabilisticMachineAnalysis(reference);

                StateSensitiveProbabilisticMachineAnalaysis refpma = new StateSensitiveProbabilisticMachineAnalaysis(reference,prefixLimit,training);
                //StateInsensitiveProbabilisticMachineAnalysis refpma = new StateInsensitiveProbabilisticMachineAnalysis(reference,prefixLimit,training);

                Machine learn = learnModel(training);
                //StateInsensitiveProbabilisticMachineAnalysis pma = new StateInsensitiveProbabilisticMachineAnalysis(learn,prefixLimit,training);
                StateSensitiveProbabilisticMachineAnalaysis pma = new StateSensitiveProbabilisticMachineAnalaysis(learn,prefixLimit,training);

                for (List<TraceElement> tes : testing.getPos()) {
                    Result r = processSequence(i,pma, refpma, tes);
                    results.add(r);
                }
                for (List<TraceElement> tes : testing.getNeg()) {
                    Result r = processSequence(i,pma, refpma, tes);
                    results.add(r);
                }
            }
            catch(Exception e){
                LOGGER.error(e.toString());
                e.printStackTrace();
                System.exit(0);
            }
        }
        return results;
    }


    public Map<String,Double> probabilityDistribution(List<TraceElement> trace){
        WalkResult walk = referenceModelAnalysis.walk(trace);
        Integer state = walk.getTarget();
        List<String> alphabet = new ArrayList<>();
        alphabet.addAll(reference.getAutomaton().getAlphabet());
        Map<String,Double> conditionalDist = new HashMap<>();
        Collection<DefaultEdge> outgoing = reference.getAutomaton().getOutgoingTransitions(state);
        for(DefaultEdge og : outgoing){
            String label = reference.getLabel(og);
            conditionalDist.put(label,reference.getProbability(og));
        }
        Set<String> remaining = new HashSet<>();
        remaining.addAll(reference.getAutomaton().getAlphabet());
        remaining.removeAll(conditionalDist.keySet());
        for(String rem : remaining){
            conditionalDist.put(rem,0D);
        }
        return conditionalDist;
    }

    private Result processSequence(int i, ParameterisableProbabilisticMachineAnalysis pma, ParameterisableProbabilisticMachineAnalysis refpma, List<TraceElement> tes) {
        WalkResult predRes = pma.walk(tes);
        WalkResult actRes = referenceModelAnalysis.walk(tes);
        double pred_p = pma.walkProbability(tes);
        double pred_cp = pma.walkConditionalProbability(tes);
        double act_p = referenceModelAnalysis.walkProbability(tes);
        double act_cpref = referenceModelAnalysis.walkConditionalProbability(tes);
        double act_cp = refpma.walkConditionalProbability(tes);
       return new CPResult(name,Integer.parseInt(numTraces),seed,i,prefixLimit,pred_p,pred_cp,act_p,act_cpref, act_cp,predRes.isAccept(),actRes.isAccept());
    }


}
