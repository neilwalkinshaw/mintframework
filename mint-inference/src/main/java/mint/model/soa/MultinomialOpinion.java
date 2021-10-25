package mint.model.soa;

import java.util.*;

public class MultinomialOpinion extends SubjectiveOpinion<List<Double>>{


    /**
     * The top-level list: A list of domains that have been multiplied together to produce this domain.
     * Second-level list: List of elements corresponding to a specific domain.
     * Third-level list: Either a single String or, if product, a list of strings that have been combined together.
     */
    protected List<List<List>> domain;

    protected boolean isSimplified = false;


    public MultinomialOpinion( List<Double> belief, List<Double> apriori, List<List<List>> domain) {
        super(belief,apriori,0);
        uncertainty = 1-sum(belief);
        this.domain = domain;
    }

    /**
     * Create multinomial opinion with uninformative priors.
     * @param belief
     */
    public MultinomialOpinion(List<Double> belief, List domain) {
        super(belief,null,0);
        apriori = new ArrayList<>();
        for(int i = 0; i<belief.size(); i++){
            apriori.add(1D/belief.size());
        }
        this.domain = domain;
        this.uncertainty = 1-sum(belief);
    }

    public BinomialOpinion coarsen(String outgoing){
        List<List> dom = domain.get(domain.size()-1);
        int targetIndex = -1;
        for(int i = 0; i<dom.size(); i++){
            List seq = dom.get(i);
            if(seq.get(0).equals(outgoing)){

                targetIndex = i;
                break;
            }

        }

        if(targetIndex<0)
            targetIndex = 0; //We assume it's asking for a vacuous opinion.




        double targetBelief = belief.get(targetIndex);
        double apriori = getApriori().get(targetIndex);


        BinomialOpinion retOp = new BinomialOpinion(targetBelief,1-(uncertainty+targetBelief),uncertainty,apriori);
        return retOp;
    }

    public MultinomialOpinion multiply(MultinomialOpinion operand){



        List<List<Double>> singletonBeliefs = new ArrayList<>();
        List<Double> beliefsB = operand.getBelief();
        for(int i = 0; i< belief.size(); i++){

            List<Double> sbeliefs = new ArrayList<>();
            double currentBelief = belief.get(i);
            for(int j = 0; j<beliefsB.size(); j++){
                sbeliefs.add(currentBelief * beliefsB.get(j));
            }
            singletonBeliefs.add(i,sbeliefs);
        }

        List<Double> brows = new ArrayList<>();
        for(int i = 0; i< belief.size(); i++){
            brows.add(belief.get(i) * operand.getUncertainty());
        }

        List<Double> bcols = new ArrayList<>();
        for(int i = 0; i< operand.getBelief().size(); i++){
            bcols.add(beliefsB.get(i) * uncertainty);
        }

        List<Double> aprioriBeliefs = new ArrayList<>();
        List<Double> aprioriB = operand.getApriori();
        for(int i = 0; i< belief.size(); i++){
            double currentApriori = apriori.get(i);
            for(int j = 0; j<beliefsB.size(); j++){
                aprioriBeliefs.add(currentApriori * aprioriB.get(j));
            }
        }

        double uRows = sum(brows);
        double uCols = sum(bcols);
        double uDomain = uncertainty * operand.getUncertainty();

        double maxU = uRows + uCols + uDomain;
        double minU = uDomain;


        double minUxys = maxU;
        for(int i = 0; i< belief.size(); i++){
            Double bx = belief.get(i);
            Double ax = apriori.get(i);
            List<Double> singletons = singletonBeliefs.get(i);
            for(int j = 0; j<beliefsB.size(); j++){
                Double by = operand.getBelief().get(j);
                Double ay = operand.getApriori().get(j);
                double bxys = singletons.get(j);
                double uxy = uXY(bx,uncertainty,ax,by,operand.getUncertainty(),ay,bxys);
                if(uxy < minUxys & uxy >= minU & uxy <= maxU){
                    minUxys = uxy;
                }
            }
        }

        List<Double> productBeliefs = new ArrayList<>();
        for(int i = 0; i< belief.size(); i++){
            double bx = belief.get(i);
            double ax = apriori.get(i);
            for(int j = 0; j< operand.getBelief().size(); j++){
                double by = beliefsB.get(j);
                double ay = aprioriB.get(j);
                double bxy = (bx+ax*uncertainty)*(by+ay*operand.getUncertainty())-ax*ay*minUxys;
                productBeliefs.add(bxy);
            }
        }

        List<List<List>> prodDomains = new ArrayList<>();

        prodDomains.addAll(domain);
        List<List> thisDomain = this.domain.get(this.domain.size()-1);
        List<List<List>> otherDomains = operand.getDomain();
        List<List> otherDomain = otherDomains.get(operand.domain.size()-1);
        List<List> prodDomain = new ArrayList();

        for(int i = 0; i<thisDomain.size();i++){
            for(int j = 0; j<otherDomain.size();j++){
                List prefix = thisDomain.get(i);
                List suffix = otherDomain.get(j);
                List concat = new ArrayList();
                concat.addAll(prefix);
                concat.addAll(suffix);
                prodDomain.add(concat);
            }
        }
        prodDomains.add(prodDomain);


        return new MultinomialOpinion(productBeliefs,aprioriBeliefs,prodDomains);

    }

    public static MultinomialOpinion averagingFusion(Collection<MultinomialOpinion> opinions){
        List<MultinomialOpinion> opList = new ArrayList();
        opList.addAll(opinions);
        boolean nonZeroUncertainty = true;
        int beliefs = 0;
        for(MultinomialOpinion op : opList){
            if(op.getUncertainty()==0D)
                nonZeroUncertainty = false;
            beliefs = op.getBelief().size();
        }
        List<Double> fusedBeliefs = new ArrayList<>();
        double uncertainty=0;
        if(nonZeroUncertainty){
            for(int i = 0; i<beliefs; i++){
                double numeratorSum = 0D;
                for(int j = 0; j<opList.size(); j++){
                    MultinomialOpinion op = opList.get(j);


                    double uncertaintyProduct = 1D;
                    for(int k = 0; k<opList.size(); k++){
                        if(k==j)
                            continue;
                        MultinomialOpinion curr = opList.get(k);
                        uncertaintyProduct = uncertaintyProduct * curr.getUncertainty();
                    }
                    numeratorSum = numeratorSum + (uncertaintyProduct * op.getBelief().get(i));
                }

                double denominatorSum = getUncertaintyProduct(opList);
                fusedBeliefs.add(i,numeratorSum/denominatorSum);
            }

            double uncertaintyProduct = 1D;
            for(int k = 0; k<opList.size(); k++){
                MultinomialOpinion curr = opList.get(k);
                uncertaintyProduct = uncertaintyProduct * curr.getUncertainty();
            }

            double numerator = beliefs * uncertaintyProduct;

            double denominator = getUncertaintyProduct(opList);
            uncertainty = numerator / denominator;
        }
        else{
            double weight = 1D/opList.size();
            for(int i = 0; i<beliefs; i++) {
                double beliefSum = 0D;
                for (int j = 0; j < opList.size(); j++) {
                    MultinomialOpinion op = opList.get(j);
                    beliefSum+=weight*op.getBelief().get(i);

                }
                fusedBeliefs.add(i,beliefSum);
            }

        }


        return new MultinomialOpinion(fusedBeliefs,opList.get(0).getDomain());
    }

    protected static double getUncertaintyProduct(List<MultinomialOpinion> opList) {
        double denominatorSum = 0D;
        for(int j = 0; j<opList.size(); j++){
            //MultinomialOpinion op = opList.get(j);

            double uncertaintyProduct = 1D;
            for(int k = 0; k<opList.size(); k++){
                if(k==j)
                    continue;
                MultinomialOpinion curr = opList.get(k);
                uncertaintyProduct = uncertaintyProduct * curr.getUncertainty();
            }
            denominatorSum = denominatorSum + (uncertaintyProduct);
        }
        return denominatorSum;
    }

    double uXY(double bx, double ux,double ax, double by, double uy, double ay, double bxyS){
        return (((((bx+ax*ux)*(by+ay*uy))-bxyS)/(ax*ay)));
    }

    private double sum(List<Double> brows) {
        double result = 0D;
        for(Double b : brows){
            result+=b;
        }
        return result;
    }

    /**
     * Big challenge with multinomial opinions is the exponential growth when multiplied together, especially several
     * times in a row. If, however, you are only interested in the probabilities arising from a specific belief, you
     * can really trim down the effort by turning the belief into a 2-valued one: by retaining the belief mass
     * for the element X in question, and aggregating to another synthetic belief standing for "not X".
     * @param outgoing
     * @return
     */
    public MultinomialOpinion simplify(List outgoing){
        List<List> dom = domain.get(domain.size()-1);
        int targetIndex = -1;
        for(int i = 0; i<dom.size(); i++){
            List<List> seq = dom.get(i);
            boolean match = true;
            for(int j = 0; j< outgoing.size(); j++){
                Object from = seq.get(j);
                Object to = outgoing.get(j);
                if(!from.equals(to)){
                    match = false;
                    break;
                }
            }
            if(match){
                targetIndex = i;
                break;
            }

        }

        if(targetIndex<0)
            targetIndex = 0; //We assume it's asking for a vacuous opinion.
        double targetBelief = belief.get(targetIndex);
        double apriori = getApriori().get(targetIndex);
        double restBelief = sum(belief) - targetBelief;
        double restApriori = sum(getApriori())-apriori;
        List subject = new ArrayList<String>();
        subject.addAll(outgoing);
        List notSubject = new ArrayList<String>();
        notSubject.add("not");

        List<Double> beliefs = new ArrayList<>();
        beliefs.add(targetBelief);
        beliefs.add(restBelief);
        List<Double> simpleApriori = new ArrayList<>();
        simpleApriori.add(apriori);
        simpleApriori.add(restApriori);

        List<List<List>> newDoms = new ArrayList<>();
        newDoms.addAll(domain.subList(0,domain.size()-1));

        List<List> latest = new ArrayList<>();
        latest.add(subject);
        latest.add(notSubject);

        newDoms.add(latest);
        MultinomialOpinion simplified = new MultinomialOpinion(beliefs,simpleApriori,newDoms);
        simplified.isSimplified = true;
        assert(beliefs.size() == simpleApriori.size());
        return simplified;
    }


    /**
     * Assumes that, if simplification has happened, it has happened to all opinions in a product, not just a selection.
     * @param sequence
     * @param from
     * @param domainIndex
     * @return
     */
    public double probabilityOfEvents(List sequence, int from, int domainIndex){
        //base case
        List<List> d = domain.get(domainIndex);
        if(sequence.size()==0)
            return 0D;
        if(!d.get(0).contains(sequence.get(0)))
            return 0D;
        int element = d.indexOf(sequence.get(0));
        if(sequence.size()==1){
            return belief.get(from+element);
        }
        else {
            int sizePerElement = (int)Math.pow(d.size(),sequence.size()-1);
            //int sizePerElement = (int)Math.pow(getDomain().size(),sequence.size()-1);
            int f = element * sizePerElement;
            return probabilityOfEvents(sequence.subList(1,sequence.size()),f,domainIndex+1);
        }

    }

    public MultinomialOpinion clone(){
        return new MultinomialOpinion(belief,apriori,domain);
    }


    public List getDomain() {
        return domain;
    }

}
