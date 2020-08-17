package mint.evaluation.kfolds;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neilwalkinshaw on 18/05/2016.
 */
public class ProbabilisticExperimentTest {

    @Test
    public void KLDistanceTestIdentical(){
        List<Double> from = new ArrayList<Double>();
        from.add(0D);
        from.add(12D);
        from.add(13D);
        from.add(1D);
        List<Double> to = new ArrayList<Double>();
        to.add(0D);
        to.add(12D);
        to.add(13D);
        to.add(1D);
        System.out.println(ProbabilisticExperiment.KLDivergencee(from,to));

        Assert.assertEquals(ProbabilisticExperiment.KLDivergencee(from,to),0D,0D);
    }

    @Test
    public void KLDistanceTestNonIdentical(){
        List<Double> from = new ArrayList<Double>();
        from.add(0D);
        from.add(1D);
        from.add(2D);
        from.add(3D);
        List<Double> to = new ArrayList<Double>();
        to.add(3D);
        to.add(2D);
        to.add(1D);
        to.add(0D);
        System.out.println(ProbabilisticExperiment.KLDivergencee(from,to));
        Assert.assertNotSame(ProbabilisticExperiment.KLDivergencee(from, to), 0D);
    }

    @Test
    public void KLDistanceTestSimilar(){
        List<Double> from = new ArrayList<Double>();
        from.add(0D);
        from.add(1D);
        from.add(2D);
        from.add(3D);
        List<Double> to = new ArrayList<Double>();
        to.add(1D);
        to.add(2D);
        to.add(3D);
        to.add(4D);
        System.out.println(ProbabilisticExperiment.KLDivergencee(from,to));
        Assert.assertNotSame(ProbabilisticExperiment.KLDivergencee(from, to), 0D);
    }

    @Test
    public void KLDistanceTestVerySimilar(){
        List<Double> from = new ArrayList<Double>();
        from.add(0D);
        from.add(1D);
        from.add(2D);
        from.add(3D);
        List<Double> to = new ArrayList<Double>();
        to.add(0D);
        to.add(1D);
        to.add(2D);
        to.add(3.5D);
        System.out.println(ProbabilisticExperiment.KLDivergencee(from,to));
        Assert.assertNotSame(ProbabilisticExperiment.KLDivergencee(from, to), 0D);
    }

}
