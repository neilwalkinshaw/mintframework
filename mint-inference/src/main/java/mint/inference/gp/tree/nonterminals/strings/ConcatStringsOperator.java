package mint.inference.gp.tree.nonterminals.strings;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.SeqExpr;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.types.StringVariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public class ConcatStringsOperator extends StringNonTerminal {

	public ConcatStringsOperator() {
	}

	protected ConcatStringsOperator(Node<StringVariableAssignment> a, Node<StringVariableAssignment> b) {
		super();
		addChild(a);
		addChild(b);
	}

	@Override
	public StringVariableAssignment evaluate() throws InterruptedException {
		checkInterrupted();
		StringVariableAssignment svar = new StringVariableAssignment("result",
				(getChild(0).evaluate().getValue().toString().concat(getChild(1).evaluate().getValue().toString())));
		vals.add(svar);
		return svar;
	}

	@Override
	public NonTerminal<StringVariableAssignment> createInstance(Generator g, int depth) {
		return new ConcatStringsOperator(g.generateRandomStringExpression(depth),
				g.generateRandomStringExpression(depth));
	}

	@Override
	public String nodeString() {
		return "Concat(" + childrenString() + ")";
	}

	@Override
	public String opString() {
		return "str.++";
	}

	@Override
	public Expr toZ3(Context ctx) {
		List<SeqExpr> args = new ArrayList<SeqExpr>();
		for (Node<?> child : children) {
			args.add((SeqExpr) child.toZ3(ctx));
		}

		return ctx.mkConcat((SeqExpr[]) args.toArray());
	}

	@Override
	protected NonTerminal<StringVariableAssignment> newInstance() {
		return new ConcatStringsOperator();
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.STRING, Datatype.STRING, Datatype.STRING };
	}

}
