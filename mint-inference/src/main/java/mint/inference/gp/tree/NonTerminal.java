package mint.inference.gp.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import mint.inference.evo.Chromosome;
import mint.inference.gp.Generator;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public abstract class NonTerminal<T extends VariableAssignment<?>> extends Node<T> {

	protected List<Node<?>> children;

	public NonTerminal() {
		super();
		children = new ArrayList<Node<?>>();
	}

	protected void visitChildren(NodeVisitor visitor) throws InterruptedException {
		Stack<Node<?>> childrenStack = new Stack<Node<?>>();
		for (Node<?> child : children) {
			childrenStack.push(child);

		}
		while (!childrenStack.isEmpty()) {
			childrenStack.pop().accept(visitor);
		}
	}

	/**
	 * Get the first value from vals (there must be one) and return as a terminal.
	 * Used only for simplification
	 * 
	 * @return
	 */
	protected abstract Terminal<T> getTermFromVals();

	@Override
	public List<Node<?>> getChildren() {
		return children;
	}

	public void addChild(Node<?> child) {
		children.add(child);
		child.setParent(this);
	}

	@Override
	public void mutate(Generator g, int depth) {
		// subtree-mutation
		int childPos = g.getRandom().nextInt(children.size());
		Node<?> child = children.get(childPos);
		if (child.getType().equals("boolean")) {
			child = g.generateRandomBooleanExpression(depth - 1);
		} else if (child.getType().equals("double")) {
			child = g.generateRandomDoubleExpression(depth - 1);
		} else if (child.getType().equals("integer")) {
			child = g.generateRandomIntegerExpression(depth - 1);
		} else {
			child = g.generateRandomStringExpression(depth - 1);
		}
		children.set(childPos, child);
	}

	public abstract NonTerminal<T> createInstance(Generator g, int depth);

	/**
	 * String that returns a summary of the node and its children.
	 * 
	 * @return
	 */
	protected abstract String nodeString();

	protected String childrenString() {
		String retString = "";
		for (int i = 0; i < children.size(); i++) {
			if (i > 0)
				retString += " ";
			retString += children.get(i).toString();
		}
		return retString;
	}

	@Override
	public int size() {
		return 1 + childrenSizes();
	}

	private int childrenSizes() {
		int sizes = 0;
		for (Node<?> n : children) {
			sizes += n.size();
		}
		return sizes;
	}

	protected Node<?> getChild(int x) {
		return children.get(x);
	}

	@Override
	public int numVarsInTree() {
		int vars = 0;
		for (Node<?> n : children) {
			vars += n.numVarsInTree();
		}
		return vars;
	}

	@Override
	public Set<VariableTerminal<?>> varsInTree() {
		Set<VariableTerminal<?>> vars = new HashSet<VariableTerminal<?>>();
		for (Node<?> child : this.getChildren()) {
			for (VariableTerminal<?> var : child.varsInTree()) {
				vars.add(var);
			}
		}
		return vars;
	}

	@Override
//	public String toString() {
//		return nodeString();
//	}

	public String toString() {
		if (opString() == "")
			return "(" + opString() + childrenString() + ")";
		else
			return "(" + opString() + " " + childrenString() + ")";
	}

	public abstract String opString();

	public void clearChildren() {
		children.clear();
	}

	@Override
	public Node<T> copy() {
		NonTerminal<T> copy = this.newInstance();
		for (Node<?> child : children) {
			copy.addChild(child.copy());
		}
		return copy;
	}

	protected abstract NonTerminal<T> newInstance();

	@Override
	public boolean sameSyntax(Chromosome c) {
		if (this.getClass().equals(c.getClass())) {
			if (this.getChildren().size() == ((NonTerminal<T>) c).getChildren().size()) {
				for (int i = 0; i < this.getChildren().size(); i++) {
					if (!(this.getChild(i).sameSyntax(((NonTerminal<T>) c).getChild(i))))
						return false;
				}
				return true;
			}
		}
		return false;
	}
}
