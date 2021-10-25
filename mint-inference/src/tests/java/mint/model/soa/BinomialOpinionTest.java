package mint.model.soa;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BinomialOpinionTest {

    @Test
    public void multiply() {
        BinomialOpinion from = new BinomialOpinion(0.15,0.5,0.34,0.5);
        BinomialOpinion to = new BinomialOpinion(0.56,0.09,0.35,0.31);
        from.multiply(to);
        assertEquals(0.17,from.getBelief(),0.01);
        assertEquals(0.55,from.getDisbelief(),0.01);
        assertEquals(0.27,from.getUncertainty(),0.01);
        assertEquals(0.16,from.getApriori(),0.01);


    }

    @Test
    public void multiply2() {
        BinomialOpinion from = new BinomialOpinion(0.15,0.51,0.34,0.5);
        BinomialOpinion to = new BinomialOpinion(0.49,0.25,0.29,1);
        from.multiply(to);
        System.out.println(from);


    }

    @Test
    public void multiplyExample() {
        BinomialOpinion openA = new BinomialOpinion(0.3,0.15,0.55,0.5);
        BinomialOpinion editC = new BinomialOpinion(0.45,0.3,0.25,0.5);
        BinomialOpinion closeD = new BinomialOpinion(0.15,0.6,0.25,0.5);
        BinomialOpinion exitA = new BinomialOpinion(0.15,0,0.85,0.5);

        openA.multiply(editC);
        openA.multiply(closeD);
        openA.multiply(exitA);

        System.out.println(openA);


    }

    @Test
    public void multiSourceFusion() {

        List<BinomialOpinion> sources = new ArrayList<>();
        BinomialOpinion b1 = new BinomialOpinion(0.1,0.3,0.6);
        BinomialOpinion b2 = new BinomialOpinion(0.4,0.2,0.4);
        BinomialOpinion b3 = new BinomialOpinion(0.7,0.1,0.2);

        sources.add(b1);
        sources.add(b2);
        sources.add(b3);

        BinomialOpinion outcome = BinomialOpinion.multiSourceFusion(sources);

        System.out.println(outcome);

    }
}