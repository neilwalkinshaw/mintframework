package mint.model.soa;

import java.util.List;
import java.util.Objects;

public class BinomialOpinion extends SubjectiveOpinion<Double>{

    protected Double disbelief;

    public BinomialOpinion(double belief, double disbelief, double uncertainty, double apriori) {
        super(belief,apriori,uncertainty);
        this.disbelief = disbelief;
    }

    public BinomialOpinion(double belief, double disbelief, double uncertainty) {
        super(belief,0.5D,uncertainty);
        this.disbelief = disbelief;
    }

    public double getProjectedProbability(){
        return belief + (apriori * uncertainty);
    }

    public Double getDisbelief() {
        return disbelief;
    }

    public void multiply(BinomialOpinion sub){
        double newbelief = (belief*sub.getBelief())+((((1-apriori)*sub.getApriori()*belief*sub.getUncertainty())+(apriori*(1-sub.getApriori())*uncertainty*sub.getBelief()))/
                (1-(apriori*sub.getApriori())));
        double newDisbelief = disbelief + sub.getDisbelief()-(disbelief*sub.getDisbelief());
        double newUncertainty = (uncertainty*sub.getUncertainty())+((((1-sub.getApriori())*belief*sub.getUncertainty())+((1-apriori)*uncertainty*sub.getBelief()))/
                (1-(apriori*sub.getApriori())));
        double newApriori = apriori*sub.getApriori();
        belief = newbelief;
        disbelief = newDisbelief;
        uncertainty=newUncertainty;
        apriori=newApriori;
    }

    public static BinomialOpinion multiSourceFusion(List<BinomialOpinion> sources){
        double sm = 0;
        for(int i = 0; i<sources.size(); i++){
            BinomialOpinion bo = sources.get(i);
            double el = bo.getBelief() * (1-bo.getUncertainty());
            double pd = 1;
            for(int j = 0; j<sources.size(); j++){
                if(j==i)
                    continue;
                BinomialOpinion co = sources.get(j);
                pd = pd * co.getUncertainty();
            }
            sm = sm + (el * pd);
        }
        double dsm = 0;

        double overAllPD = 1;
        double uncertaintySum = 0;
        for(int i = 0; i<sources.size(); i++) {
            double pd = 1;
            uncertaintySum += sources.get(i).getUncertainty();
            overAllPD = overAllPD * sources.get(i).getUncertainty();
            for (int j = 0; j < sources.size(); j++) {
                if(j==i)
                    continue;
                BinomialOpinion co = sources.get(j);
                pd = pd * co.getUncertainty();

            }
            dsm = dsm + pd;
        }

        double numSources = (double)sources.size();


        double belief = (sm / (dsm-numSources* overAllPD));
        double uncertainty = ((numSources - uncertaintySum)*overAllPD)/(dsm - numSources * overAllPD);
        double disbelief = 1-(belief + uncertainty);
        return new BinomialOpinion(belief,disbelief,uncertainty);
    }

    public BinomialOpinion clone(){
        return new BinomialOpinion(belief,disbelief,uncertainty,apriori);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinomialOpinion that = (BinomialOpinion) o;
        return Double.compare(that.belief, belief) == 0 &&
                Double.compare(that.disbelief, disbelief) == 0 &&
                Double.compare(that.uncertainty, uncertainty) == 0 &&
                Double.compare(that.apriori, apriori) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(belief, disbelief, uncertainty, apriori);
    }

    public String toString(){
        return belief+","+disbelief+","+uncertainty+","+apriori;
    }
}
