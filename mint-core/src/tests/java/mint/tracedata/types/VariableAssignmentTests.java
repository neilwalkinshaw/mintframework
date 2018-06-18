package mint.tracedata.types;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.assertTrue;

public class VariableAssignmentTests {

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testRestrictedDoubleTypes() {
        Collection<Double> restriction = new HashSet<Double>();
        restriction.add(1D);
        restriction.add(2D);
		DoubleVariableAssignment dva = new DoubleVariableAssignment("test",restriction);
        dva.setToValue(1D);
        dva.setToValue(2D);
        boolean exceptionThrown = false;
        try{
            dva.setToValue(3D);
        }
        catch(AssertionError e){
            exceptionThrown = true;
        }
        assertTrue(dva.value == 2D);
	}

    @Test
    public void testRestrictedStringTypes() {
        Collection<String> restriction = new HashSet<String>();
        restriction.add("a");
        restriction.add("b");
        StringVariableAssignment dva = new StringVariableAssignment("test",restriction);
        dva.setToValue("a");
        dva.setToValue("b");
        boolean exceptionThrown = false;
        try{
            dva.setToValue("c");
        }
        catch(AssertionError e){
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

}
