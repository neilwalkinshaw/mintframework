package mint.inference.gp;

import mint.tracedata.TestIO;
import mint.tracedata.types.DoubleVariableAssignment;
import mint.tracedata.types.VariableAssignment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neilwalkinshaw on 06/03/2018.
 */
public class Normaliser {

    protected List<Double> range;
    protected List<Double> min;
    protected List<Integer> doubleColumns;
    protected List<TestIO> data;

    public Normaliser(List<TestIO> data){
        this.data = data;
        this.range = new ArrayList<Double>();
        this.min = new ArrayList<Double>();
        doubleColumns = new ArrayList<Integer>();
        if(data.isEmpty())
            return;
        List<VariableAssignment<?>> vars = data.get(0).getVals();
        int count = 0;
        for(VariableAssignment<?> v : vars){
            if(v.typeString().equals(":D")) {
                doubleColumns.add(count);
            }
            count++;
        }
        for(int doubColumn : doubleColumns){
            Double min = null;
            Double max = null;
            for(TestIO test : data){
                Double val = (Double)test.getVals().get(doubColumn).getValue();
                if(min == null)
                    min = val;
                if(max == null)
                    max = val;
                if(val < min)
                    min = val;
                if(val > max)
                    max = val;
            }
            this.min.add(min);
            this.range.add(max-min);
        }
    }

    public List<TestIO> getNormalised(){
        List<TestIO> result = new ArrayList<TestIO>();
        for(TestIO element: data){
            List<VariableAssignment<?>> copied = new ArrayList<VariableAssignment<?>>();
            int count = 0;
            for(VariableAssignment<?> var : element.getVals()){
                if(doubleColumns.contains(count)){
                    DoubleVariableAssignment dvar = (DoubleVariableAssignment)var.copy();
                    double range = this.range.get(doubleColumns.indexOf(count));
                    double orig = dvar.getValue();
                    double newVal = (orig - min.get(doubleColumns.indexOf(count)))/range;
                    dvar.setValue(newVal);

                    copied.add(dvar);
                }
                else{
                    copied.add(var.copy());
                }
                count++;
            }

            TestIO copiedIO = new TestIO(element.getName(),copied);
            result.add(copiedIO);
        }
        return result;
    }
}
