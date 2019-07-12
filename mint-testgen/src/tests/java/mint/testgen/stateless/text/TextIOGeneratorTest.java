package mint.testgen.stateless.text;

import org.junit.Test;

import static org.junit.Assert.*;

public class TextIOGeneratorTest {

    @Test
    public void entropy() {
        double[] testSequence = new double[]{.25D,.25D,.2D,.15D,.15D};
        double entropy = TextIOGenerator.entropy(testSequence);
        assertEquals(entropy,2.2855,.1);
    }
}