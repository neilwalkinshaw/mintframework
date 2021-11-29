package mint.model.soa;


import citcom.subjectiveLogic.BinomialOpinion;
import citcom.subjectiveLogic.MultinomialOpinion;

import citcom.subjectiveLogic.operators.binomial.BinomialMultiSourceFusion;
import citcom.subjectiveLogic.operators.binomial.BinomialMultiplication;
import citcom.subjectiveLogic.operators.multinomial.MultinomialAveragingFusion;
import citcom.subjectiveLogic.operators.multinomial.MultinomialMultiplication;
import mint.model.Machine;
import mint.model.ProbabilisticTraceMachineDecorator;
import mint.model.dfa.TraceDFA;
import mint.model.walk.WalkResult;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;


public class MultinomialOpinionMachineDecorator extends ProbabilisticTraceMachineDecorator {

    protected Map<Integer, MultinomialOpinion> soaMap;




    private final static Logger LOGGER = Logger.getLogger(MultinomialOpinionMachineDecorator.class.getName());


    public MultinomialOpinionMachineDecorator(Machine decorated, TraceSet traces, double confidenceThreshold) {
        super(decorated, traces, confidenceThreshold);
        soaMap=new HashMap<>();
    }




    private List<List<TraceElement>> allTraces(){
        List<List<TraceElement>> allTrc = new ArrayList<>();
        allTrc.addAll(traces.getNeg());
        allTrc.addAll(traces.getPos());
        return allTrc;
    }

    public MultinomialOpinion getOpinion(Integer state){
        return soaMap.get(state);
    }

    @Override
    public void postProcess() {
        component.postProcess();

        Iterator<Integer> stateIt = component.getAutomaton().getStates().iterator();
        List<String> alphabet = new ArrayList<>();
        alphabet.addAll(getAutomaton().getAlphabet());
        while(stateIt.hasNext()){
            Integer current = stateIt.next();
            Map<String,Double> alphaToWeight = new HashMap<>();
            for(String a : alphabet){
                Collection<DefaultEdge> outgoing = getAutomaton().getOutgoingTransitions(current,a);
                double total = 0D;
                for(DefaultEdge de : outgoing){
                    total += getAutomaton().getTransitionData(de).getPayLoad().size();
                }
                //if(total>0)
                    alphaToWeight.put(a,total);
            }


            double traceCount = 0D;
            List<List<TraceElement>> allTrc = allTraces();


            for(List<TraceElement> trace: allTrc){
                WalkResult result= ma.walk(trace,getInitialState(),new ArrayList<>(),getAutomaton());
                //if(result.getWalk().contains(current))
                //   traceCount++;
                for(DefaultEdge de : result.getWalk()){
                    if(getAutomaton().getTransitionSource(de)==current.intValue()) {
                        traceCount++;
                        //break;
                    }
                }

            }

            double uncertainty = 1-(Math.min(1,traceCount/confidenceThreshold));

            List<Double> beliefDistribution = normalise(alphaToWeight);
            for(int i = 0; i<beliefDistribution.size();i++){
                if(uncertainty<1)
                    beliefDistribution.set(i,beliefDistribution.get(i)*(1-uncertainty));
                else
                    beliefDistribution.set(i,0D);
            }

            List<List<List>> domain = new ArrayList<>();
            List<List> outgoing = new ArrayList<>();
            for(String alpha : alphaToWeight.keySet()){
                //if(alphaToWeight.get(alpha)>0D) {
                    List<String> dom = new ArrayList<>();
                    dom.add(alpha);
                    outgoing.add(dom);
                //}
            }
            domain.add(outgoing);
            MultinomialOpinion mo = new MultinomialOpinion(beliefDistribution,domain);
            soaMap.put(current,mo);
        }
    }

    private List<Double> normalise(Map<String, Double> alphaToWeight) {
        List<Double> normalised = new ArrayList<>();
        for(String a : alphaToWeight.keySet()){
            normalised.add(alphaToWeight.get(a));
        }
        double sum = sum(normalised);
        for(int i = 0; i<normalised.size(); i++){
            normalised.set(i,normalised.get(i)/sum);
        }
        return normalised;
    }

    private double sum(List<Double> normalised) {
        double retVal = 0D;
        for(Double d : normalised){
            retVal+=d;
        }
        return retVal;
    }


    /**
     * Return a binomial opinion for a walk, by multiplying the binomial opinions on the transitions of the walk
     * together. If angelic=true, for a path that does not exist in the machine it will simply walk upto the
     * accepted prefix and return the binomial opinion at that point. If angelic=false, it will return a
     * default reject Binomial opinion (0,0,1).
     * @param walk
     * @param originalLength
     * @param angelic
     * @return
     */
    public BinomialOpinion binomialWalkOpinion(WalkResult walk, int originalLength, boolean angelic){
        BinomialOpinion so = null;

        List<DefaultEdge> path = new ArrayList<>();
        path.addAll(walk.getWalk());
        for(DefaultEdge de : path){

            if(so == null) {
                Integer source = getAutomaton().getTransitionSource(de);
                String label = getAutomaton().getTransitionData(de).getLabel();
                if (!label.isEmpty()) {

                    MultinomialOpinion sourceOp = soaMap.get(source);
                    so = sourceOp.coarsen(label);
                }
                else break;
            }
            else {
                Integer target = getAutomaton().getTransitionTarget(de);
                String label = getAutomaton().getTransitionData(de).getLabel();
                if (!label.isEmpty()) {

                    BinomialOpinion targetOp = soaMap.get(target).coarsen(label);
                    BinomialMultiplication multi = new BinomialMultiplication();
                    so = multi.apply(so,targetOp);
                }
                else break;
            }
        }
        if(so == null) //we were given an empty path to start with
            so = new BinomialOpinion(0,0,1);
        else if(walk.isAccept(component.getAutomaton())!= TraceDFA.Accept.ACCEPT){
            if(!angelic){
                BinomialMultiplication multi = new BinomialMultiplication();
                so = multi.apply(so,new BinomialOpinion(0,0.5,0.5));
            }
        }

        return so;
    }

    public MultinomialOpinion walkOpinion(WalkResult walk, int originalLength){
        MultinomialOpinion so = null;
        if(walk.getWalk()==null){ //trace rejected by inferred machine
            so = getRejectionOpinion();
        }
        else if(walk.getWalk().size() == 0)
            so = soaMap.get(getInitialState());

        List<Integer> lt = new ArrayList<>();
        List labList = new ArrayList();
        List<DefaultEdge> path = new ArrayList<>();
        path.addAll(walk.getWalk());
        if(walk.isAccept(getAutomaton())==TraceDFA.Accept.REJECT){
            path.remove(path.size()-1);
        }
        for(DefaultEdge de : path){
            Integer source = getAutomaton().getTransitionSource(de);
            lt.add(source);
            if(so==null) {
                so = soaMap.get(getInitialState());
            }
            //else{
                String label = getAutomaton().getTransitionData(de).getLabel();
                if(!label.isEmpty()) {

                    labList.add(label);
                    MultinomialOpinion simplified = so.simplify(labList);
                    if(simplified != null)
                        so = simplified;
                }
                MultinomialMultiplication multi = new MultinomialMultiplication();
                so = multi.apply(so,soaMap.get(source));
           // }
        }
        //for(int i = path.size(); i<originalLength;i++){
            if(so == null)
                so = getRejectionOpinion();
            //so =  so.simplify(labList);

        //    so = so.multiply(getRejectionOpinion());
        //}
        if(!so.isSimplified()) {
            MultinomialOpinion simplified = so.simplify(labList);
            if (simplified != null)
                so = simplified;
        }
        return so;
    }

    public MultinomialOpinion fusedOpinion(WalkResult walk, int originalLength){
        MultinomialOpinion so = null;
        if(walk.getWalk()==null){ //trace rejected by inferred machine
            so = getRejectionOpinion();
        }
        else if(walk.getWalk().size() == 0)
            so = soaMap.get(getInitialState());

        List<DefaultEdge> path = new ArrayList<>();
        path.addAll(walk.getWalk());

        List<MultinomialOpinion> opinions = new ArrayList<>();

        for(DefaultEdge de : path){
            Integer source = getAutomaton().getTransitionSource(de);
            if(so==null) {
                so = soaMap.get(getInitialState());
            }
            else{
                so = soaMap.get(source);
            }
            opinions.add(so);
        }
        for(int i = 0; i< originalLength - path.size(); i++){
            MultinomialOpinion mo= new MultinomialOpinion(so.getBelief(),so.getApriori(),so.getDomain());
            MultinomialMultiplication multi = new MultinomialMultiplication();
            mo = multi.apply(mo,getRejectionOpinion());
            opinions.add(mo);
        }
        MultinomialAveragingFusion maf = new MultinomialAveragingFusion();
        so = maf.apply(opinions.toArray(new MultinomialOpinion[opinions.size()]));

        return so;
    }

    public BinomialOpinion binomialFusedWalkOpinion(WalkResult walk, int originalLength){
        BinomialOpinion so = null;

        List<DefaultEdge> path = new ArrayList<>();
        path.addAll(walk.getWalk());
        List<BinomialOpinion> toFuse = new ArrayList<>();
        for(DefaultEdge de : path){

            if(so == null) {
                Integer source = getAutomaton().getTransitionSource(de);
                String label = getAutomaton().getTransitionData(de).getLabel();
                if (!label.isEmpty()) {

                    MultinomialOpinion sourceOp = soaMap.get(source);
                    so = sourceOp.coarsen(label);
                }
            }
            else {
                Integer target = getAutomaton().getTransitionTarget(de);
                String label = getAutomaton().getTransitionData(de).getLabel();
                if (!label.isEmpty()) {

                    BinomialOpinion targetOp = new BinomialOpinion(0,1,0);

                    if(getAutomaton().getAccept(target) == TraceDFA.Accept.ACCEPT) {
                        targetOp = soaMap.get(target).coarsen(label);
                    }
                    toFuse.add(targetOp);
                }
                else{
                    toFuse.add(new BinomialOpinion(0,1,0));
                }

            }
        }
        if(so == null) //we were given an empty path to start with
           return new BinomialOpinion(0,1,0);
        else if(path.size()<originalLength){
            int difference = originalLength - path.size();
            for(int i = 0; i<difference; i++){
                toFuse.add(new BinomialOpinion(0,1,0));
            }
        }
        BinomialMultiSourceFusion bmsf = new BinomialMultiSourceFusion();
        BinomialOpinion fused = bmsf.apply(toFuse.toArray(new BinomialOpinion[toFuse.size()]));

        return fused;
    }

    /**
     * Currently gives a vacuous opinion as a rejection.
     * @return
     */
    public MultinomialOpinion getRejectionOpinion() {
        List<Double> zeroBelief = new ArrayList<>();
        List<Double> prior = new ArrayList<>();

        double alphaSize = getAutomaton().getAlphabet().size();
        for(int i = 0; i<alphaSize; i++){
            zeroBelief.add(1/alphaSize);
        }
        for(int i = 0; i<alphaSize; i++){
            prior.add(1/alphaSize);
        }
        List<List<List>> domain = new ArrayList<>();
        List<List> alphabet = new ArrayList<>();
        for(String alpha : getAutomaton().getAlphabet()){
            List alph = new ArrayList();
            alph.add(alpha);
            alphabet.add(alph);
        }
        domain.add(alphabet);
        return new MultinomialOpinion(zeroBelief,prior,domain);
    }

}
