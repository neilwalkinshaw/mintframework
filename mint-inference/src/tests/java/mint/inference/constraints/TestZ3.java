package mint.inference.constraints;

import com.microsoft.z3.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

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
	public void test() throws Z3Exception {
		ArithExpr x = (ArithExpr) ctx.MkConst(ctx.MkSymbol("x"),ctx.MkRealSort());
		ArithExpr y = (ArithExpr) ctx.MkConst(ctx.MkSymbol("y"),ctx.MkRealSort());	
		addConstraint(ctx.MkGt(x, (ArithExpr)ctx.MkReal(0,1)));
		addConstraint(ctx.MkLt(x, (ArithExpr)ctx.MkReal(20,1)));
		addConstraint(ctx.MkGt(y, (ArithExpr)ctx.MkReal(15,1)));
		addConstraint(ctx.MkLt(y, (ArithExpr)ctx.MkReal(5000,1)));
		addConstraint(ctx.MkLt(y, x));
		Solver s = ctx.MkSolver();
		s.Assert(current);
		System.out.println(s.Check());
		System.out.println(s.Model());
		System.out.println(s.Model().ConstInterp(x) + ", "+s.Model().ConstInterp(y));
	}
	
	private void addConstraint(BoolExpr e) throws Z3Exception{
		if(current == null)
			current = e;
		else
			current = ctx.MkAnd(new BoolExpr[]{current,e});
	}
}
