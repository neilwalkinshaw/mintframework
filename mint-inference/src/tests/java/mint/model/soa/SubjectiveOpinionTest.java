package mint.model.soa;

import org.junit.Test;

import static org.junit.Assert.*;

public class SubjectiveOpinionTest {

    @Test
    public void multiply() {
        SubjectiveOpinion from = new SubjectiveOpinion(0.15,0.5,0.34,0.5);
        SubjectiveOpinion to = new SubjectiveOpinion(0.56,0.09,0.35,0.31);
        from.multiply(to);
        assertEquals(0.17,from.getBelief(),0.01);
        assertEquals(0.55,from.getDisbelief(),0.01);
        assertEquals(0.27,from.getUncertainty(),0.01);
        assertEquals(0.16,from.getApriori(),0.01);


    }
}