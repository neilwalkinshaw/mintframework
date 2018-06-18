package mint.testgen.stateless.gp.qbc;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import mint.inference.evo.AbstractEvo;
import mint.inference.evo.Chromosome;
import mint.inference.gp.SingleOutputGP;
import mint.inference.gp.tree.Node;
import mint.tracedata.TestIO;
import mint.tracedata.types.VariableAssignment;

import java.util.*;

/**
 * Created by neilwalkinshaw on 25/01/2016.
 */
public class ClusteringQBC extends QBC {

    protected String label = "QBCC";

    public ClusteringQBC(String name, Collection<VariableAssignment<?>> types, AbstractEvo gp, List<TestIO> testInputs, QuerySelector selector) {
        super(name, types, gp, testInputs, selector);
    }

    @Override
    protected void computeCommittee(AbstractEvo gp) {
        committee = new ArrayList<Chromosome>();
        SingleOutputGP sog = (SingleOutputGP) gp;
        KMeansPlusPlusClusterer<DoublePoint> clust = new KMeansPlusPlusClusterer<DoublePoint>(committeeSize);
        Map<Node<?>, List<Double>> distances = sog.getDistances();
        if (distances.size() < committeeSize)
            super.computeCommittee(gp);
        else {
            Collection<DoublePoint> points = new HashSet<DoublePoint>();
            Map<DoublePoint, Node<?>> nodeMap = new HashMap<DoublePoint, Node<?>>();
            for (Node<?> n : distances.keySet()) {
                List<Double> scores = distances.get(n);
                double[] sArray = new double[scores.size()];
                for (int i = 0; i < scores.size(); i++) {

                    sArray[i] = scores.get(i);
                }
                if(sArray.length==sog.getEvals().size()) {
                    DoublePoint dp = new DoublePoint(sArray);
                    points.add(dp);
                    nodeMap.put(dp, n);
                }

            }
            List<CentroidCluster<DoublePoint>> clusters = clust.cluster(points);
            for (CentroidCluster<DoublePoint> c : clusters) {
                List<DoublePoint> ps = c.getPoints();
                double smallest = -1;
                DoublePoint best = null;
                for(DoublePoint dp : ps){
                    Mean mean = new Mean();
                    double m = mean.evaluate(dp.getPoint());
                    if(smallest<0 || m < smallest) {
                        smallest = m;
                        best = dp;
                    }
                }
                committee.add(nodeMap.get(best));
            }
        }
    }
}
