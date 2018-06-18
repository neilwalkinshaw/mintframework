package mint.model.walk;

import mint.model.dfa.TraceDFA;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;

public class WalkResult{
	
	protected Integer target;
	protected List<DefaultEdge> walk;

	/**
	 * Get the destination state of the walk.
	 * @return
	 */
	public Integer getTarget() {
		return target;
	}
	
	public List<DefaultEdge> getWalk() {
		return walk;
	}
	
	public TraceDFA.Accept isAccept(TraceDFA automaton){
		if(walk == null)
			return TraceDFA.Accept.UNDEFINED;
		else
			return automaton.getAccept(target);
	}
	
	public WalkResult(Integer target,
			List<DefaultEdge> walk) {
		super();
		this.target = target;
		this.walk = walk;
	}
	
	
	
}
