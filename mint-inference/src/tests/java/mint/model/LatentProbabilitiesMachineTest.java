package mint.model;

import com.google.common.collect.HashBasedTable;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class LatentProbabilitiesMachineTest {

    @Test
    public void latentDependenciesTest(){
        HashBasedTable<Integer, Integer, Double> table = HashBasedTable.create();
        table.put(1,2,2D);
        table.put(1,3,3D);
        Assert.assertEquals(3D,table.get(1,3),0.0000000001D);
        table.put(2,1,2D);
        table.put(3,1,3D);
        Assert.assertEquals(2D,table.get(2,1),0.0000000001D);
    }

}