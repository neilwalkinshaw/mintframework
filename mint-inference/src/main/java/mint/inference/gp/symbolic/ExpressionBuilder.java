package mint.inference.gp.symbolic;

import com.microsoft.z3.*;
import org.apache.log4j.Logger;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.nonterminals.booleans.*;
import mint.inference.gp.tree.nonterminals.doubles.*;
import mint.inference.gp.tree.nonterminals.integer.CastIntegersOperator;
import mint.inference.gp.tree.nonterminals.integer.IfThenElseIntegerOperator;
import mint.inference.gp.tree.nonterminals.integer.IntegerNonTerminal;
import mint.inference.gp.tree.nonterminals.lists.RootListNonTerminal;
import mint.inference.gp.tree.nonterminals.strings.StringNonTerminal;
import mint.inference.gp.tree.terminals.BooleanVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.DoubleVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.IntegerVariableAssignmentTerminal;
import mint.inference.gp.tree.terminals.StringVariableAssignmentTerminal;

import java.util.*;

/**
 * Builds a Z3 expression by traversing a GP tree.
 *
 * Traverses the tree in a dept-first manner. Implements the visitor
 * in a hierarchical visitor pattern implementation.
 *
 * At the exit of each node, it pushes the current constraint onto a stack. This will
 * be an arithmetic expression representing the computation in child-branches.
 *
 * Whenever it encounters a boolean condition, this is formulated as a Z3 Boolean Expression
 * and added to the additionalConstraints, which are propagated up to the root node.
 *
 * Created by neilwalkinshaw on 27/05/15.
 */
public class ExpressionBuilder implements NodeVisitor {

    private Context ctx;
    private Collection<BoolExpr> targets;
    private Map<String,ArithExpr> variables;
    private Map<String,BoolExpr> auxNodes;
    private Map<Node<?>,BoolExpr> additionalConstraints;
    private Map<Node<?>,ArithExpr> arithConstraints;

    private final static Logger LOGGER = Logger.getLogger(ExpressionBuilder.class.getName());

    public Collection<BoolExpr> getTargets(){
        return targets;
    }

    public Map<String,ArithExpr> getVariables(){
        return variables;
    }

    public ExpressionBuilder(Node<?> subject, Context ctx){
        //WriteAuxOperator.setCounter(0);
        HashMap<String, String> cfg = new HashMap<String, String>();
        auxNodes = new HashMap<String,BoolExpr>();
        cfg.put("model", "true");
        arithConstraints = new HashMap<Node<?>,ArithExpr>();
        variables = new HashMap<String,ArithExpr>();
        this.ctx = ctx;
        targets= new HashSet<BoolExpr>();
        additionalConstraints = new HashMap<Node<?>,BoolExpr>();
        try {
            subject.accept(this);
        } catch (InterruptedException e) {
            LOGGER.error("Evaluation interrupted.");
        }
        for(Node<?> child: subject.getChildren()){
            buildTargets(child);
        }
        //buildTargets(subject.getChildren().get(0));
        //assumes that subject is root node! Not necessarily the case!
    }


    /**
     * Generate every possible true-false combination of constraints associated with nodes,
     * set their conjunction as a target.
     */
    private void buildTargets(Node<?> root) {
        BoolExpr additionalConstraint = additionalConstraints.get(root);
        if(additionalConstraint!=null)
            targets.add(additionalConstraint);
    }



    protected BoolExpr findExpr(Node<?> exp){
        if(additionalConstraints.containsKey(exp)){
            return additionalConstraints.get(exp);
        }
        else{
            LOGGER.error("Could not find Expr for: "+exp.toString());
            return null;
        }
    }

    protected ArithExpr findArithExpr(Node<?> exp){
        if(arithConstraints.containsKey(exp))
            return arithConstraints.get(exp);
        else {
            LOGGER.error("Could not find ArithExpr for: "+exp.toString());
            return null;
        }
    }


    @Override
    public boolean visitEnter(EQBooleanOperator eqBooleanOperator) {
        return true;
    }

    @Override
    public boolean visitExit(EQBooleanOperator eqBooleanOperator) throws InterruptedException {
        try {
            BoolExpr ex = null;
            if(eqBooleanOperator.numVarsInTree()==0){
                ex = ctx.MkBool(eqBooleanOperator.evaluate().getValue());
            }
            else {
                ex = ctx.MkEq(findExpr(eqBooleanOperator.getChildren().get(0)), findExpr(eqBooleanOperator.getChildren().get(1)));
                ex = addAdditionalConstraints(ex, eqBooleanOperator);
            }
            additionalConstraints.put(eqBooleanOperator, ex);

        }
        catch (Z3Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }



    @Override
    public boolean visitEnter(EQArithOperator eqBooleanOperator) {
        return true;
    }

    @Override
    public boolean visitExit(EQArithOperator eqBooleanOperator) {
        try {
            BoolExpr ex = null;
            if(eqBooleanOperator.numVarsInTree()==0){
                ex = ctx.MkBool(eqBooleanOperator.evaluate().getValue());
            }
            else {
                ArithExpr exp1 = findArithExpr(eqBooleanOperator.getChildren().get(0));
                ArithExpr exp2 = findArithExpr(eqBooleanOperator.getChildren().get(1));

                ex = ctx.MkEq(exp1, exp2);
                ex = addAdditionalConstraints(ex, eqBooleanOperator);
            }
            additionalConstraints.put(eqBooleanOperator, ex);
        }
        catch (Z3Exception e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean visitEnter(AndBooleanOperator andBooleanOperator) {
        return true;
    }

    @Override
    public boolean visitExit(AndBooleanOperator andBooleanOperator) {
        try {
            BoolExpr ex = null;
            if(andBooleanOperator.numVarsInTree()==0){
                ex = ctx.MkBool(andBooleanOperator.evaluate().getValue());
            }
            else {
                BoolExpr[] ands = new BoolExpr[]{(BoolExpr) findExpr(andBooleanOperator.getChildren().get(0)),
                        (BoolExpr) findExpr(andBooleanOperator.getChildren().get(1))};
                ex = ctx.MkAnd(ands);
                //ex = addAdditionalConstraints(andExpr, andBooleanOperator);
            }
            additionalConstraints.put(andBooleanOperator, ex);
        }
        catch (Z3Exception e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean visitEnter(GTBooleanDoublesOperator gtBooleanDoublesOperator) {
        return true;
    }

    @Override
    public boolean visitExit(GTBooleanDoublesOperator gtBooleanDoublesOperator) {
        try {
            BoolExpr ex = null;
            if(gtBooleanDoublesOperator.numVarsInTree()==0){
                ex = ctx.MkBool(gtBooleanDoublesOperator.evaluate().getValue());
            }
            else {
                ex = ctx.MkGt(findArithExpr(gtBooleanDoublesOperator.getChildren().get(0)), findArithExpr(gtBooleanDoublesOperator.getChildren().get(1)));
                ex = addAdditionalConstraints(ex, gtBooleanDoublesOperator);
            }
            additionalConstraints.put(gtBooleanDoublesOperator, ex);
        }
        catch (Z3Exception e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }



    @Override
    public boolean visitEnter(LTBooleanDoublesOperator ltBooleanDoublesOperator) {
        return true;
    }

    @Override
    public boolean visitExit(LTBooleanDoublesOperator ltBooleanDoublesOperator) {
        try {
            BoolExpr ex = null;
            if(ltBooleanDoublesOperator.numVarsInTree()==0){
                ex = ctx.MkBool(ltBooleanDoublesOperator.evaluate().getValue());
            }
            else {
                ex = ctx.MkLt(findArithExpr(ltBooleanDoublesOperator.getChildren().get(0)), findArithExpr(ltBooleanDoublesOperator.getChildren().get(1)));
                ex = addAdditionalConstraints(ex, ltBooleanDoublesOperator);
            }
            additionalConstraints.put(ltBooleanDoublesOperator,ex);


        }
        catch (Z3Exception e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }


    @Override
    public boolean visitEnter(OrBooleanOperator orBooleanOperator) {
        return true;
    }

    @Override
    public boolean visitExit(OrBooleanOperator orBooleanOperator) {
        try {
            BoolExpr ex = null;
            if(orBooleanOperator.numVarsInTree()==0){
                ex = ctx.MkBool(orBooleanOperator.evaluate().getValue());
            }
            else {
                BoolExpr[] ors = new BoolExpr[]{(BoolExpr) findExpr(orBooleanOperator.getChildren().get(0)), (BoolExpr) findExpr(orBooleanOperator.getChildren().get(1))};
                ex = ctx.MkOr(ors);
                ex = addAdditionalConstraints(ex, orBooleanOperator);
            }
            additionalConstraints.put(orBooleanOperator,ex);
        }
        catch (Z3Exception e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean visitEnter(CastIntegersOperator integerNonTerminal) {
        return true;
    }

    @Override
    public boolean visitExit(CastIntegersOperator integerNonTerminal) {
        ArithExpr ex = null;
        if(integerNonTerminal.numVarsInTree()==0){
            try {
                ex = ctx.MkReal(integerNonTerminal.evaluate().getValue().toString());
            } catch (Z3Exception e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            ex = findArithExpr(integerNonTerminal.getChildren().get(0));
            checkChildren(integerNonTerminal);
        }
        arithConstraints.put(integerNonTerminal, ex);
        return true;
    }

    @Override
    public boolean visitEnter(StringNonTerminal stringNonTerminal) {
        return true;
    }

    @Override
    public boolean visitExit(StringNonTerminal stringNonTerminal) {
        return true;
    }

    @Override
    public boolean visitEnter(IfThenElseOperator ifThenElseOperator) {
        return true;
    }

    @Override
    public boolean visitExit(IfThenElseOperator ifThenElseOperator) {

        try {
            BoolExpr be = findExpr(ifThenElseOperator.getChildren().get(0));
            BoolExpr tr=null,fl=null;
            if(additionalConstraints.get(ifThenElseOperator.getChildren().get(1))!=null) {
                 tr = ctx.MkAnd(new BoolExpr[]{be, additionalConstraints.get(ifThenElseOperator.getChildren().get(1))});
            }
            else{
                tr = be;
            }
            if(additionalConstraints.get(ifThenElseOperator.getChildren().get(2))!=null) {
                 fl = ctx.MkAnd(new BoolExpr[]{not(be), additionalConstraints.get(ifThenElseOperator.getChildren().get(2))});
            }
            else{
                fl = not(be);
            }

            BoolExpr or = ctx.MkOr(new BoolExpr[]{tr, fl});
            additionalConstraints.put(ifThenElseOperator, or);

            ArithExpr exp = (ArithExpr) ctx.MkITE(be,
                    findArithExpr(ifThenElseOperator.getChildren().get(1)),
                    findArithExpr(ifThenElseOperator.getChildren().get(2)));
            arithConstraints.put(ifThenElseOperator,exp);

        } catch (Z3Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean visitEnter(DivideDoublesOperator divideDoublesOperator) {
        return true;
    }

    @Override
    public boolean visitExit(DivideDoublesOperator divideDoublesOperator) {
        ArithExpr ex = null;
        if(divideDoublesOperator.numVarsInTree()==0){
            String val = null;
            try {
                val = divideDoublesOperator.evaluate().getValue().toString();
                if(val.equals("Infinity")) {
                    Double max = Double.MAX_VALUE;
                    ex = ctx.MkReal(max.toString());
                }
                else
                    ex = ctx.MkReal(val);
            } catch (Z3Exception e) {
                LOGGER.debug("Parser error for DivideDoublesOperator, parsing: "+val);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                ex = ctx.MkDiv(findArithExpr(divideDoublesOperator.getChildren().get(0)), findArithExpr(divideDoublesOperator.getChildren().get(1)));
            } catch (Z3Exception e) {
                e.printStackTrace();
            }
            checkChildren(divideDoublesOperator);
        }
        arithConstraints.put(divideDoublesOperator, ex);

        return true;
    }


    @Override
    public boolean visitEnter(AddDoublesOperator addDoublesOperator) {
        return true;
    }

    @Override
    public boolean visitExit(AddDoublesOperator addDoublesOperator) {

        ArithExpr ex = null;
        if(addDoublesOperator.numVarsInTree()==0){
            try {
                String val = addDoublesOperator.evaluate().getValue().toString();
                if(val.equals("Infinity")) {
                    Double max = Double.MAX_VALUE;
                    ex = ctx.MkReal(max.toString());
                }
                else
                    ex = ctx.MkReal(val);
            } catch (Z3Exception e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                ex = ctx.MkAdd(new ArithExpr[]{findArithExpr(addDoublesOperator.getChildren().get(0)), findArithExpr(addDoublesOperator.getChildren().get(1))});
            } catch (Z3Exception e) {
                e.printStackTrace();
            }
            checkChildren(addDoublesOperator);
        }
        arithConstraints.put(addDoublesOperator, ex);

        return true;
    }

    @Override
    public boolean visitEnter(CastDoublesOperator castDoublesOperator) {
        return true;
    }

    @Override
    public boolean visitExit(CastDoublesOperator castDoublesOperator) {
        ArithExpr ex = null;
        if(castDoublesOperator.numVarsInTree()==0){
            try {
                ex = ctx.MkInt(castDoublesOperator.evaluate().getValue().toString());
            } catch (Z3Exception e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            ex = findArithExpr(castDoublesOperator.getChildren().get(0));
            checkChildren(castDoublesOperator);
        }
        arithConstraints.put(castDoublesOperator, ex);
        return true;
    }

    @Override
    public boolean visitEnter(CosDoublesOperator cosDoublesOperator) {
        return true;
    }

    @Override
    public boolean visitExit(CosDoublesOperator cosDoublesOperator) {
        ArithExpr ex = null;
        if(cosDoublesOperator.numVarsInTree()==0){
            try {
                String val = cosDoublesOperator.evaluate().getValue().toString();
                if(val.equals("Infinity") || val.equals("NaN")) {
                    Double max = Double.MAX_VALUE;
                    ex = ctx.MkReal(max.toString());
                }
                else
                    ex = ctx.MkReal(val);
            } catch (Z3Exception e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            ex = findArithExpr(cosDoublesOperator.getChildren().get(0));
            checkChildren(cosDoublesOperator);
        }
        arithConstraints.put(cosDoublesOperator, ex);
        return true;
    }

    @Override
    public boolean visitEnter(LogDoublesOperator cosDoublesOperator) {
        return false;
    }

    @Override
    public boolean visitExit(LogDoublesOperator cosDoublesOperator) {
        return false;
    }

    @Override
    public boolean visitEnter(IntegerNonTerminal enter) {
        return true;
    }

    @Override
    public boolean visitExit(IntegerNonTerminal exit) {

        return true;
    }

    @Override
    public boolean visitEnter(MultiplyDoublesOperator multiplyDoublesOperator) {
        return true;
    }

    @Override
    public boolean visitExit(MultiplyDoublesOperator multiplyDoublesOperator) {
        ArithExpr ex = null;
        if(multiplyDoublesOperator.numVarsInTree()==0){
            try {
                String val = multiplyDoublesOperator.evaluate().getValue().toString();
                if(val.equals("Infinity") || val.equals("NaN")) {
                    Double max = Double.MAX_VALUE;
                    ex = ctx.MkReal(max.toString());
                }
                else
                    ex = ctx.MkReal(val);
            } catch (Z3Exception e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                ex = ctx.MkMul(new ArithExpr[]{findArithExpr(multiplyDoublesOperator.getChildren().get(0)), findArithExpr(multiplyDoublesOperator.getChildren().get(1))});
            } catch (Z3Exception e) {
                e.printStackTrace();
            }
            checkChildren(multiplyDoublesOperator);
        }
        arithConstraints.put(multiplyDoublesOperator, ex);


        return true;
    }

    @Override
    public boolean visitEnter(PwrDoublesOperator pwrDoublesOperator) {
        return true;
    }

    @Override
    public boolean visitExit(PwrDoublesOperator pwrDoublesOperator) {
        ArithExpr ex = null;
        if(pwrDoublesOperator.numVarsInTree()==0){
            try {
                String val = pwrDoublesOperator.evaluate().getValue().toString();
                if(val.equals("Infinity") || val.equals("NaN")) {
                    Double max = Double.MAX_VALUE;
                    ex = ctx.MkReal(max.toString());
                }
                else
                    ex = ctx.MkReal(val);
            } catch (Z3Exception e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                ex = ctx.MkPower(findArithExpr(pwrDoublesOperator.getChildren().get(0)), findArithExpr(pwrDoublesOperator.getChildren().get(1)));
            } catch (Z3Exception e) {
                e.printStackTrace();
            }
            checkChildren(pwrDoublesOperator);
        }
        arithConstraints.put(pwrDoublesOperator, ex);

        return true;
    }

    @Override
    public boolean visitEnter(RootDouble rootDouble) {
        return true;
    }

    @Override
    public boolean visitExit(RootDouble rootDouble) {
        return true;
    }

    @Override
    public boolean visitEnter(ExpDoublesOperator expDoublesOperator) {
        return true;
    }

    @Override
    public boolean visitExit(ExpDoublesOperator expDoublesOperator) {
        //TODO - requires Z3 non-linear solver.
        return true;
    }

    @Override
    public boolean visitEnter(SubtractDoublesOperator subtractDoublesOperator) {
        return true;
    }

    @Override
    public boolean visitExit(SubtractDoublesOperator subtractDoublesOperator) {
        ArithExpr ex = null;
        if(subtractDoublesOperator.numVarsInTree()==0){
            try {
                ex = ctx.MkSub(new ArithExpr[]{findArithExpr(subtractDoublesOperator.getChildren().get(0)), findArithExpr(subtractDoublesOperator.getChildren().get(1))});
            } catch (Z3Exception e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                ex = ctx.MkMul(new ArithExpr[]{findArithExpr(subtractDoublesOperator.getChildren().get(0)), findArithExpr(subtractDoublesOperator.getChildren().get(1))});
            } catch (Z3Exception e) {
                e.printStackTrace();
            }
            checkChildren(subtractDoublesOperator);
        }
        arithConstraints.put(subtractDoublesOperator, ex);

        return true;
    }


    @Override
    public boolean visitEnter(BooleanVariableAssignmentTerminal booleanVariableAssignmentTerminal) {
        return true;
    }

    @Override
    public boolean visitExit(BooleanVariableAssignmentTerminal booleanVariableAssignmentTerminal) {
        try {

            BoolExpr eq = null;
            if(booleanVariableAssignmentTerminal.isConstant()){
                if(booleanVariableAssignmentTerminal.evaluate().getValue() == true)
                    eq = ctx.MkTrue();
                else
                    eq = ctx.MkFalse();
            }
            else {
                eq = ctx.MkBoolConst(booleanVariableAssignmentTerminal.getName());
            }
            additionalConstraints.put(booleanVariableAssignmentTerminal,eq);
        }
        catch (Z3Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void visitEnter(DoubleVariableAssignmentTerminal doubleVariableAssignmentTerminal) {

    }


    @Override
    public boolean visitExit(DoubleVariableAssignmentTerminal doubleVariableAssignmentTerminal) {
        try{
            if(doubleVariableAssignmentTerminal.isConstant()){
                RatNum num = ctx.MkReal(doubleVariableAssignmentTerminal.evaluate().getValue().toString());
                arithConstraints.put(doubleVariableAssignmentTerminal,num);
            }
            else {
                ArithExpr variable = addRealVariable(doubleVariableAssignmentTerminal.getName());
                arithConstraints.put(doubleVariableAssignmentTerminal, variable);
            }

        }
        catch(Z3Exception e){
            e.printStackTrace();
        }
        return true;
    }


    @Override
    public void visitEnter(StringVariableAssignmentTerminal stringVariableAssignmentTerminal) {
    }

    @Override
    public boolean visitExit(StringVariableAssignmentTerminal stringVariableAssignmentTerminal) {
        return true;
    }

    @Override
    public boolean visitEnter(RootBoolean rootBoolean) {
        return true;
    }

    @Override
    public boolean visitExit(RootBoolean rootBoolean) {
        return true;
    }

    @Override
    public void visitEnter(IntegerVariableAssignmentTerminal integerVariableAssignmentTerminal) {

    }

    @Override
    public boolean visitExit(IntegerVariableAssignmentTerminal integerVariableAssignmentTerminal) {
        try{
            if(integerVariableAssignmentTerminal.isConstant()){
                ArithExpr num = ctx.MkInt(integerVariableAssignmentTerminal.evaluate().getValue().toString());
                arithConstraints.put(integerVariableAssignmentTerminal,num);
            }
            else {
                ArithExpr variable = addIntVariable((integerVariableAssignmentTerminal.getName()));
                arithConstraints.put(integerVariableAssignmentTerminal, variable);
            }

        }
        catch(Z3Exception e){
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean visitEnter(EQStringOperator eqStringOperator) {
        return true;
    }

    @Override
    public boolean visitExit(EQStringOperator eqStringOperator) {
        return true;
    }

    @Override
    public boolean visitEnter(RootListNonTerminal rootListNonTerminal) {
        return false;
    }

    @Override
    public boolean visitExit(RootListNonTerminal rootListNonTerminal) {
        return false;
    }

    @Override
    public boolean visitEnter(IfThenElseIntegerOperator ifThenElseIntegerOperator) {
        return false;
    }

    @Override
    public boolean visitExit(IfThenElseIntegerOperator ifThenElseIntegerOperator) {
        return false;
    }


    private ArithExpr addRealVariable(String varName)throws Z3Exception {
        ArithExpr var = null;
        if (variables.containsKey(varName)) {
            var = variables.get(varName);
        } else {
            var = ctx.MkRealConst(ctx.MkSymbol(varName));
            variables.put(varName,var);
        }
        return var;
    }

    private ArithExpr addIntVariable(String varName)throws Z3Exception {
        ArithExpr var = null;
        if (variables.containsKey(varName)) {
            var = variables.get(varName);
        } else {
            var = ctx.MkIntConst(ctx.MkSymbol(varName));
            variables.put(varName,var);
        }
        return var;
    }

    private BoolExpr not(BoolExpr target){
        BoolExpr neg = null;
        try {
            neg = ctx.MkNot(target);

        } catch (Z3Exception e) {
            e.printStackTrace();
        }
        return neg;
    }

    private BoolExpr addAdditionalConstraints(BoolExpr eqExpr, NonTerminal eqBooleanOperator) {
        BoolExpr combined = eqExpr;
        List<BoolExpr> others = new ArrayList<BoolExpr>();
        for(Object child : eqBooleanOperator.getChildren()){
            BoolExpr constraint = additionalConstraints.get(child);
            if(constraint!=null){
                others.add(constraint);
            }
        }
        if(!others.isEmpty()){
            others.add(eqExpr);
            try {
                combined = ctx.MkAnd(others.toArray(new BoolExpr[others.size()]));
            } catch (Z3Exception e) {
                e.printStackTrace();
            }
        }
        return combined;
    }

    private void checkChildren(NonTerminal nt){
        List<BoolExpr> others = new ArrayList<BoolExpr>();
        for(Object child : nt.getChildren()){
            BoolExpr constraint = additionalConstraints.get(child);
            if(constraint!=null){
                others.add(constraint);
            }
        }
        if(!others.isEmpty()){
            try {
                BoolExpr combined = ctx.MkAnd(others.toArray(new BoolExpr[others.size()]));
                additionalConstraints.put(nt,combined);
            } catch (Z3Exception e) {
                e.printStackTrace();
            }
        }
    }
}
