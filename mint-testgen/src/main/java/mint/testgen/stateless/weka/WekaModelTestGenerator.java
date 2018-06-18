package mint.testgen.stateless.weka;

import mint.testgen.stateless.TestGenerator;
import mint.tracedata.types.VariableAssignment;
import weka.classifiers.Classifier;

import java.util.Collection;

/**
 * Created by neilwalkinshaw on 26/05/15.
 */
public abstract class WekaModelTestGenerator extends TestGenerator{

    protected final Classifier classifier;

    public WekaModelTestGenerator(String name, Collection<VariableAssignment<?>> types, Classifier c){
        super(name,types);
        classifier = c;

    }


}
