package mint.inference.constraints;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Z3Exception;

public class TestZ3 {

	Context ctx;
	BoolExpr current;

	@Before
	public void setUp() throws Exception {
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");
		ctx = new Context(cfg);

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		ArithExpr x = (ArithExpr) ctx.mkConst(ctx.mkSymbol("x"), ctx.mkRealSort());
		ArithExpr y = (ArithExpr) ctx.mkConst(ctx.mkSymbol("y"), ctx.mkRealSort());
		addConstraint(ctx.mkGt(x, ctx.mkReal(0, 1)));
		addConstraint(ctx.mkLt(x, ctx.mkReal(20, 1)));
		addConstraint(ctx.mkGt(y, ctx.mkReal(15, 1)));
		addConstraint(ctx.mkLt(y, ctx.mkReal(5000, 1)));
		addConstraint(ctx.mkLt(y, x));
		Solver s = ctx.mkSolver();
		s.add(current);
		System.out.println(s.check());
		System.out.println(s.getModel());
		System.out.println(s.getModel().getConstInterp(x) + ", " + s.getModel().getConstInterp(y));
	}

	private void addConstraint(BoolExpr e) {
		if (current == null)
			current = e;
		else
			current = ctx.mkAnd(new BoolExpr[] { current, e });
	}
}
