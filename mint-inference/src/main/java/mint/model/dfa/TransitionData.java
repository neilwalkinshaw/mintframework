package mint.model.dfa;

public class TransitionData<T> {
	
	String label;
	T payLoad;

	public TransitionData(String label, T payLoad) {
		this.payLoad = payLoad;
		this.label = label;
	}
	
	public T getPayLoad(){
		return payLoad;
	}

	public void setPayLoad(T payload){
		this.payLoad = payload;
	}
	
	public String getLabel(){
		return label;
	}

	public String toString(){
		return label+", "+payLoad;
	}

}
