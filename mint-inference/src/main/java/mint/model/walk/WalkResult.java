package mint.model.walk;

import mint.model.dfa.TraceDFA;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;

public class WalkResult{
	
	protected Integer target;
	protected List<DefaultEdge> walk;
	protected TraceDFA.Accept accept = TraceDFA.Accept.UNDEFINED;

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
	
	public TraceDFA.Accept isAccept(){
		return accept;
	}
	
	public WalkResult(Integer target,
			List<DefaultEdge> walk, TraceDFA.Accept accept) {
		super();
		this.target = target;
		this.walk = walk;
		this.accept=accept;
	}
	
	
	
}
