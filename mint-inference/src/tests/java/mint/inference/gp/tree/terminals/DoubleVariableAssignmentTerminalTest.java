package mint.inference.gp.tree.terminals;

import mint.inference.gp.NodeExecutor;
import mint.inference.gp.tree.Node;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.VariableAssignment;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DoubleVariableAssignmentTerminalTest {

    @Test
    public void constantNotMutable() throws InterruptedException {
        Node<?> doubleConst = new DoubleVariableAssignmentTerminal(new DoubleVariableAssignment("five",5.0D),true);
        NodeExecutor nex = new NodeExecutor(doubleConst);
        List<VariableAssignment<?>> inputs = new ArrayList<>();
        inputs.add(new DoubleVariableAssignment("five",3.0D));
        nex.execute(inputs);
        assert(doubleConst.evaluate().getValue().equals(5.0D));
    }

}