package mint.testgen.stateless;

import mint.tracedata.TestIO;
import mint.tracedata.types.VariableAssignment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class TestGenerator {
	

	protected final Collection<VariableAssignment<?>> types;
	protected final String name;
	
	public TestGenerator(String name, Collection<VariableAssignment<?>> types){
		this.types = types;
		this.name = name;
	}
	
	/**
	 * Generate a set of test inputs limited by howMany.
	 * @param howMany
	 * @return
	 */
	public abstract List<TestIO> generateTestCases(int howMany);

	/**
	 * Generate an arbitrary number of test inputs.
	 * @return
	 */
	public abstract List<TestIO> generateTestCases();
	
	protected TestIO generateTestIO(Collection<VariableAssignment<?>> sol) {
		List<VariableAssignment<?>> params = new ArrayList<VariableAssignment<?>>();
		for(VariableAssignment<?> type : types){
			VariableAssignment<?> s = findAssignment(type.getName(),sol);
			if(s != null)
				params.add(s.createNew(s.getName(), s.getValue().toString()));
			else{
				type.setToRandom();
				VariableAssignment<?> newRandom = type.createNew(type.getName(), type.getValue().toString());
				newRandom.setToRandom();
				params.add(newRandom);
			}
		}
		return new TestIO(name,params);
	}
	
	protected TestIO generateRandomTestIO() {
		List<VariableAssignment<?>> params = new ArrayList<VariableAssignment<?>>();
		for(VariableAssignment<?> type : getParamTypes()){
			type.setToRandom();
			VariableAssignment newRandom = type.createNew(type.getName(), type.getValue().toString());

			newRandom.setValue(type.getValue());
			params.add(newRandom);
		}
		return new TestIO(name,params);
	}

	protected Collection<VariableAssignment<?>> getParamTypes() {
		return types;
	}

	protected static VariableAssignment<?> findAssignment(String name, Collection<VariableAssignment<?>> vars){
		VariableAssignment<?> found = null;
		for(VariableAssignment<?> v : vars){
			if(v.getName().equals(name)){
				found = v;
				break;
			}
		}
		return found;
	}
	
}
