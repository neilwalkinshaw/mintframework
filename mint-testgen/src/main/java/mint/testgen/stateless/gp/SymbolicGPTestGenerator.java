package mint.testgen.stateless.gp;

import com.microsoft.z3.*;
import org.apache.log4j.Logger;
import mint.inference.gp.symbolic.ExpressionBuilder;
import mint.inference.gp.symbolic.SymbExpSolver;
import mint.inference.gp.tree.Node;
import mint.tracedata.TestIO;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.VariableAssignment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Generate test cases from GP model by symbolic execution.
 *
 * Created by neilwalkinshaw on 02/06/15.
 */
public class SymbolicGPTestGenerator extends GPModelTestGenerator {

    protected SymbExpSolver solver;
    protected ExpressionBuilder builder;
    protected List<TestIO> done;


    private final static Logger LOGGER = Logger.getLogger(SymbolicGPTestGenerator.class.getName());


    public SymbolicGPTestGenerator(String name, Collection<VariableAssignment<?>> types, Node<?> tree, List<TestIO> testInputs) {
        super(name, types, tree);
        done = testInputs;
        setUp(tree);

    }

    protected void setUp(Node<?> tree){
        try {
            Context ctx = new Context();
            builder = new ExpressionBuilder(tree,ctx);
            solver = new SymbExpSolver(builder.getTargets(),ctx,builder.getVariables());
            avoid(done);
        } catch (Z3Exception e) {
            e.printStackTrace();
        }
    }

    private void avoid(List<TestIO> testInputs) {
        Context ctx = solver.getCtx();
        for(TestIO io:testInputs){
            try {
                List<BoolExpr> andList = new ArrayList<BoolExpr>();
                for (VariableAssignment<?> var : io.getVals()) {
                    ArithExpr val = null;
                    if (var.typeString().equals(":D")) {
                        DoubleVariableAssignment dva = (DoubleVariableAssignment) var;
                        Double value = dva.getValue();
                        val = ctx.MkReal(value.toString());
                    } else if (var.typeString().equals(":I")) {
                        IntegerVariableAssignment iva = (IntegerVariableAssignment) var;
                        Integer value = iva.getValue();
                        val = ctx.MkInt(value.toString());
                    } else
                        continue;
                    ArithExpr variable = builder.getVariables().get(var.getName());
                    if(variable!=null)
                        andList.add(ctx.MkEq(variable, val));
                }
                if(!andList.isEmpty()) {
                    solver.addConstraint(ctx.MkNot(ctx.MkAnd(andList.toArray(new BoolExpr[andList.size()]))));
                }
            }
            catch (Z3Exception e1) {
                e1.printStackTrace();
            }
        }
    }


    @Override
    public List<TestIO> generateTestCases(int howMany) {
        List<TestIO> testInputs = new ArrayList<TestIO>();
        Collection<Model> solutions = solver.solve(howMany);
        if(solutions.size()==0) {
            for(int i = 0; i<howMany; i++){
                testInputs.add(generateRandomTestIO());
            }
            return testInputs;
        }
        for (Model solution : solutions) {
            TestIO sol = getTestInputs(solution);
            testInputs.add(sol);

        }


        return testInputs;
    }





    protected TestIO getTestInputs(Model solution) {
        HashMap<String,VariableAssignment<?>> inputs = new HashMap<String,VariableAssignment<?>>();
        try {
            for (FuncDecl fd : solution.ConstDecls()) {
                Expr interp = solution.ConstInterp(fd);
                if (interp instanceof RatNum) {
                    RatNum rn = (RatNum) interp;
                    Double numerator = rn.Numerator().BigInteger().doubleValue();
                    Double denominator = rn.Denominator().BigInteger().doubleValue();

                    VariableAssignment assignment = findAssignment(fd.Name().toString(),types);
                    if(assignment == null) {
                        //LOGGER.debug("Couldn't find symb-var for:"+builder.getNameFor(fd));
                        continue;
                    }
                    Double val = numerator / denominator;
                    String valString = val.toString();
                    inputs.put(fd.Name().toString(),assignment.createNew(fd.Name().toString(), valString));

                } else if (interp instanceof IntNum) { //could be a string too...
                    IntNum in = (IntNum) interp;
                    try {
                        Integer val = in.Int();
                        VariableAssignment assignment = findAssignment(fd.Name().toString(),types);
                        if(assignment == null) {
                            //LOGGER.debug("Couldn't find symb-var for:"+builder.getNameFor(fd));
                            continue;
                        }
                        String valString = val.toString();
                        inputs.put(fd.Name().toString(),assignment.createNew(fd.Name().toString(), valString));
                    }
                    catch(Exception e){
                        continue;
                    }

                } else if (interp instanceof BoolExpr) {
                    BoolExpr bex = (BoolExpr) interp;

                }
            }
        } catch (Z3Exception e) {
            e.printStackTrace();
        }
        List<VariableAssignment<?>> testInputs = new ArrayList<VariableAssignment<?>>();
        for(VariableAssignment<?> va:types){
            VariableAssignment<?> inp = va.copy();
            if(inputs.get(inp.getName())==null)
                inp.setToRandom();
            else
                inp.setStringValue(inputs.get(inp.getName()).getValue().toString());
            testInputs.add(inp);
        }
        TestIO io = new TestIO(name,testInputs);
        return io;
    }

    @Override
    public List<TestIO> generateTestCases() {
        return generateTestCases(10);
    }
}
