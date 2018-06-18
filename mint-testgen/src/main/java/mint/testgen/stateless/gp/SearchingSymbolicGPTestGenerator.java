package mint.testgen.stateless.gp;

import com.microsoft.z3.Model;
import mint.inference.gp.tree.Node;
import mint.tracedata.TestIO;
import mint.tracedata.types.VariableAssignment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

/**
 *
 */

public class SearchingSymbolicGPTestGenerator extends SymbolicGPTestGenerator {


    protected Node<?> gp;

    public SearchingSymbolicGPTestGenerator(String name, Collection<VariableAssignment<?>> types, Node<?> tree, List<TestIO> testInputs) {
        super(name, types, tree, testInputs);
        this.gp = tree;
    }

    @Override
    public List<TestIO> generateTestCases(int howMany) {
        List<TestIO> testInputs = new ArrayList<TestIO>();
        Collection<Model> solutions = solver.solve(howMany);

        Stack<Node<?>> workList = new Stack<Node<?>>();
        workList.addAll(gp.getChildren());
        if(solutions.size()<howMany) {

            while(!workList.isEmpty()){
                Node<?> child = workList.pop();
                setUp(child);
                if(builder.getTargets().isEmpty())
                    break;
                solutions.addAll( solver.solve(howMany));
                if(solutions.isEmpty()){
                    workList.addAll(child.getChildren());
                }
                else if(workList.isEmpty())
                    break;
            }

        }
        for (Model solution : solutions) {
            TestIO sol = getTestInputs(solution);
            testInputs.add(sol);

        }


        return testInputs;
    }

}