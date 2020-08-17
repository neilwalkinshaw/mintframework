package mint.evaluation.kfolds;

import mint.Configuration;
import mint.tracedata.TraceElement;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StrategicKFoldsEvaluator extends KFoldsEvaluator {

    private final static Logger LOGGER = Logger.getLogger(StrategicKFoldsEvaluator.class.getName());


    protected Configuration.Strategy strategy = Configuration.Strategy.redblue;

    public StrategicKFoldsEvaluator(String name, Collection<List<TraceElement>> trace, Collection<List<TraceElement>> negTrace, int seed, int tail) {
        super(name, trace, negTrace, seed, tail);
    }

    public StrategicKFoldsEvaluator(String name, Collection<List<TraceElement>> trace, Collection<List<TraceElement>> negTrace, int seed, int tail, Configuration.Strategy strategy) {
        super(name, trace, negTrace, seed, tail);
        this.strategy = strategy;
    }



    public void kfolds(int folds, boolean data){
        LOGGER.info("Running K-Folds experiments for k="+tail);
        if(folds>trace.size()){
            LOGGER.error("Incorrect number of folds specified.");
        }
        List<List> results = new ArrayList<List>();
        Configuration.Data[] algos = new Configuration.Data[]{Configuration.Data.J48,
                Configuration.Data.AdaBoost, Configuration.Data.JRIP, Configuration.Data.NaiveBayes};
        if(data){

            for(int i = 0; i<algos.length;i++){
                Experiment a = generateExperiment(folds, algos, i, true,strategy);
                results.add(a.call());

            }
        }
        else{

            Experiment a = generateExperiment(folds, algos, 0, false,strategy);
            results.add(a.call());

        }
        //Experiment nodata = generateExperiment(folds, algos, 0, false);
        //results.add(nodata.call());


        for(int i = 0; i< results.size();i++){
            List<Object> outcomes = results.get(i);
            output(outcomes);
        }
        LOGGER.info("Completed K-Folds experiments for k="+tail);
    }
}
