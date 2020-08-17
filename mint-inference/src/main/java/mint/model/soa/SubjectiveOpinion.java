package mint.model.soa;

import java.util.Objects;

public class SubjectiveOpinion {

    protected double belief, disbelief, uncertainty, apriori;

    public SubjectiveOpinion(double belief, double disbelief, double uncertainty, double apriori) {
        this.belief = belief;
        this.disbelief = disbelief;
        this.uncertainty = uncertainty;
        this.apriori = apriori;
    }

    public SubjectiveOpinion(double belief, double disbelief, double uncertainty) {
        this.belief = belief;
        this.disbelief = disbelief;
        this.uncertainty = uncertainty;
        this.apriori = 0.5D;
    }

    public double getBelief() {
        return belief;
    }

    public double getDisbelief() {
        return disbelief;
    }

    public double getUncertainty() {
        return uncertainty;
    }

    public double getApriori() {
        return apriori;
    }

    public void multiply(SubjectiveOpinion sub){
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

    public SubjectiveOpinion clone(){
        return new SubjectiveOpinion(belief,disbelief,uncertainty,apriori);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectiveOpinion that = (SubjectiveOpinion) o;
        return Double.compare(that.belief, belief) == 0 &&
                Double.compare(that.disbelief, disbelief) == 0 &&
                Double.compare(that.uncertainty, uncertainty) == 0 &&
                Double.compare(that.apriori, apriori) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(belief, disbelief, uncertainty, apriori);
    }
}
