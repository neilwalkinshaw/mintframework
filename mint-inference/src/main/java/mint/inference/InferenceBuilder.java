package mint.inference;

import mint.Configuration;
import mint.inference.efsm.AbstractMerger;
import mint.inference.efsm.EDSMDataMerger;
import mint.inference.efsm.EDSMMerger;
import mint.inference.efsm.mergingstate.RedBlueMergingState;
import mint.inference.efsm.mergingstate.SimpleMergingState;
import mint.inference.efsm.scoring.BasicScorer;
import mint.inference.efsm.scoring.RedBlueScorer;
import mint.inference.efsm.scoring.Scorer;
import mint.inference.efsm.scoring.scoreComputation.ComputeScore;
import mint.inference.efsm.scoring.scoreComputation.KTailsScorecomputer;
import mint.inference.efsm.scoring.scoreComputation.LinearScoreComputer;
import mint.inference.evo.GPConfiguration;
import mint.model.*;
import mint.model.prefixtree.EFSMPrefixTreeFactory;
import mint.model.prefixtree.FSMPrefixTreeFactory;
import mint.model.prefixtree.PrefixTreeFactory;
import mint.model.soa.BinomialOpinionMachineDecorator;
import mint.model.soa.MultinomialOpinionMachineDecorator;
import mint.tracedata.TraceSet;
import mint.tracedata.readers.TraceReader;

import java.io.IOException;

/**
 * Created by neilwalkinshaw on 11/07/2017.
 */
public class InferenceBuilder {

    protected Configuration configuration;

    BaseClassifierInference bci = null;

    public InferenceBuilder(Configuration conf){
        this.configuration = conf;
    }

    public AbstractMerger<?, ?> getInference(TraceSet posSet, TraceSet evalSet) {
        AbstractMerger<?,?> inference = null;
        if(configuration.DATA) {
            bci = new BaseClassifierInference(posSet, evalSet, configuration.ALGORITHM);
            inference = getDataMerger(posSet);
        }
        else{
            inference = getStandardMerger(posSet);
        }
        return inference;
    }

    public AbstractMerger<?, ?> getInference(TraceSet posSet) {
        AbstractMerger<?,?> inference = null;
        if(configuration.DATA){

            bci = new BaseClassifierInference(posSet, configuration.ALGORITHM);


            inference = getDataMerger(posSet);
        }
        else{

            inference = getStandardMerger(posSet);
        }
        return inference;
    }

    private AbstractMerger<?, ?> getStandardMerger(TraceSet posSet) {
        AbstractMerger<?, ?> inference;
        PrefixTreeFactory<SimpleMachine> tptg = new FSMPrefixTreeFactory(new PayloadMachine());
        if(configuration.STRATEGY == Configuration.Strategy.exhaustive){
            Machine kernel = tptg.createPrefixTree(posSet);
            if(configuration.SUBJECTIVE_OPINIONS)
                kernel = new BinomialOpinionMachineDecorator(kernel, posSet,configuration.CONFIDENCE_THRESHOLD, configuration.ONE_WEIGHT_PER_TRACE);

            SimpleMergingState<Machine> ms = new SimpleMergingState<Machine>(kernel);


            BasicScorer<SimpleMergingState<Machine>,ComputeScore> scorer = new BasicScorer<SimpleMergingState<Machine>,ComputeScore>(configuration.K, new ComputeScore());
            inference = new EDSMMerger<Machine,SimpleMergingState<Machine>>(scorer,ms);
        }
        else{

            Machine kernel = tptg.createPrefixTree(posSet);
            if(configuration.SUBJECTIVE_OPINIONS)
                kernel = new MultinomialOpinionMachineDecorator(kernel, posSet,configuration.CONFIDENCE_THRESHOLD, configuration.ONE_WEIGHT_PER_TRACE);

            RedBlueMergingState<Machine> ms = new RedBlueMergingState<Machine>(kernel);


            Scorer scorer = null;

            if(configuration.STRATEGY == Configuration.Strategy.ktails){
                KTailsScorecomputer ktailsScorer = new KTailsScorecomputer(configuration.K);
                ktailsScorer.setLimitToDecisionOnK(true);
                scorer = new BasicScorer(configuration.K, ktailsScorer);
            }
            else if(configuration.STRATEGY == Configuration.Strategy.noloops){
                scorer = new BasicScorer<SimpleMergingState<Machine>,ComputeScore>(configuration.K, new LinearScoreComputer());
            }
            else{
                scorer = new RedBlueScorer<RedBlueMergingState<WekaGuardMachineDecorator>>(configuration.K, new ComputeScore());
            }

            inference = new EDSMMerger<Machine,RedBlueMergingState<Machine>>(scorer,ms);
        }
        return inference;
    }

    private AbstractMerger<?, ?> getDataMerger(TraceSet posSet) {
        AbstractMerger<?, ?> inference;
        if(configuration.STRATEGY == Configuration.Strategy.exhaustive){
            EFSMPrefixTreeFactory tptg;
            Machine kernel = new PayloadMachine();
            if(configuration.GP)
                kernel = new GPFunctionMachineDecorator(kernel,1, new GPConfiguration(60,0.95,0.2,4,8),50);
            if(configuration.SUBJECTIVE_OPINIONS)
                kernel = new BinomialOpinionMachineDecorator(kernel, posSet,configuration.CONFIDENCE_THRESHOLD, configuration.ONE_WEIGHT_PER_TRACE);
            tptg = new EFSMPrefixTreeFactory(kernel,bci.getClassifiers(),bci.getElementsToInstances());

            SimpleMergingState<WekaGuardMachineDecorator> ms = new SimpleMergingState<WekaGuardMachineDecorator>(tptg.createPrefixTree(posSet));

            BasicScorer<SimpleMergingState<WekaGuardMachineDecorator>,ComputeScore> scorer = new BasicScorer<SimpleMergingState<WekaGuardMachineDecorator>,ComputeScore>(configuration.K, new ComputeScore());
            inference = new EDSMDataMerger(scorer,ms);
        }
        else{


            EFSMPrefixTreeFactory tptg;
            Machine kernel = new PayloadMachine();
            if(configuration.GP)
                kernel = new GPFunctionMachineDecorator(kernel,1, new GPConfiguration(200,0.8,0.1,4,7),40);
            if(configuration.SUBJECTIVE_OPINIONS)
                kernel = new BinomialOpinionMachineDecorator(kernel, posSet,configuration.CONFIDENCE_THRESHOLD, configuration.ONE_WEIGHT_PER_TRACE);
            //tptg = new EFSMPrefixTreeFactory(new DaikonMachineDecorator(kernel,configuration.MINDAIKON,true),bci.getClassifiers(),bci.getElementsToInstances());
            //else
            //  tptg = new EFSMPrefixTreeFactory(kernel,bci.getClassifiers(),bci.getElementsToInstances());

            tptg = new EFSMPrefixTreeFactory(kernel,bci.getClassifiers(),bci.getElementsToInstances());


            RedBlueMergingState<WekaGuardMachineDecorator> ms = new RedBlueMergingState<WekaGuardMachineDecorator>(tptg.createPrefixTree(posSet));
            Scorer scorer = null;
            if(configuration.STRATEGY == Configuration.Strategy.ktails){
                KTailsScorecomputer ktailsScorer = new KTailsScorecomputer(configuration.K);
                ktailsScorer.setLimitToDecisionOnK(true);
                BasicScorer bScorer = new BasicScorer(configuration.K, ktailsScorer);
                scorer = bScorer;

            }
            else if(configuration.STRATEGY == Configuration.Strategy.noloops){
                scorer =  new BasicScorer<SimpleMergingState<WekaGuardMachineDecorator>,ComputeScore>(configuration.K, new LinearScoreComputer());
            }
            else{
                scorer = new RedBlueScorer<RedBlueMergingState<WekaGuardMachineDecorator>>(configuration.K, new ComputeScore());
            }

            inference = new EDSMDataMerger<RedBlueMergingState<WekaGuardMachineDecorator>>(scorer,ms);

        }
        return inference;
    }

}
