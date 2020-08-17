package mint.evaluation.kfolds;

import mint.Configuration;

/**
 * Created by neilwalkinshaw on 07/05/2016.
 */
public class SimpleResult {

    protected String name, algo;
    protected int seed, tail;
    protected boolean data;
    protected double states, transitions, result;
    protected Configuration.Strategy strategy;

    public SimpleResult(String name, String algo, int seed, int tail, boolean data, double states, double transitions, Configuration.Strategy strategy, double result) {
        this.name = name;
        this.algo = algo;
        this.seed = seed;
        this.tail = tail;
        this.data = data;
        this.states = states;
        this.transitions = transitions;
        this.strategy = strategy;
        this.result = result;
    }


    @Override
    public String toString() {
        return name + "," + algo  +"," + seed +"," + tail +"," + data +"," + states +"," + transitions +"," + strategy+","+result;
    }
}
