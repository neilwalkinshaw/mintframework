package mint.inference.gp.tree;

public enum Datatype implements TypeCheck {
	INTEGER {
		@Override
		public boolean matches(TypeCheck c) {
			if (c.equals(INTEGER)) {
				return true;
			}
			return false;
		}
	},
	STRING {
		@Override
		public boolean matches(TypeCheck c) {
			if (c.equals(STRING)) {
				return true;
			}
			return false;
		}
	},
	DOUBLE {
		@Override
		public boolean matches(TypeCheck c) {
			if (c.equals(DOUBLE)) {
				return true;
			}
			return false;
		}
	},
	BOOLEAN {
		@Override
		public boolean matches(TypeCheck c) {
			if (c.equals(BOOLEAN)) {
				return true;
			}
			return false;
		}
	},
	LIST {
		@Override
		public boolean matches(TypeCheck c) {
			if (c.equals(LIST)) {
				return true;
			}
			return false;
		}
	},
	ANY {
		@Override
		public boolean matches(TypeCheck c) {
			return true;
		}
	};

	public static boolean typeChecks(Datatype[] t1, Datatype[] t2) {
		if (t1.length == t2.length) {
			for (int i = 0; i < t1.length; i++) {
				if (t1[i] != t2[i])
					return false;
			}
			return true;
		}
		return false;
	}

}
