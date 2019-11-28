/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package mint.tracedata.types;

import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

public class DoubleVariableAssignment extends NumberVariableAssignment<Double> {

	static double minDoub = Double.MIN_VALUE;
	static double maxDoub = Double.MAX_VALUE;

	private final static Logger LOGGER = Logger.getLogger(DoubleVariableAssignment.class.getName());

	public DoubleVariableAssignment(String name, Double value) {
		super(name, value, minDoub, maxDoub);
	}

	
	public DoubleVariableAssignment(String name) {
		super(name, minDoub, maxDoub);
	}

	public DoubleVariableAssignment(String name, Double min, Double max) {
		super(name, min, max);
	}

    public DoubleVariableAssignment(String name, Collection<Double> from) {
        super(name,minDoub, maxDoub, from);
    }

	@Override
	public void setStringValue(String s) {
		try{
			if(s.equals("null"))
				setNull(true);
			else {
				Double val = Double.valueOf(s);
				setToValue(val);
			}

		}
		catch (NumberFormatException nfe){
			LOGGER.warn("Failed to parse string to Double: "+s);
		}
	}

    @Override
    public boolean withinLimits() {
        if(!enforcing)
            return true;
        if(getValue() > max || getValue() < min)
            return false;
        return super.withinLimits();
    }

    @Override
	public String printableStringOfValue() {
		return Double.toString(value);
	}

	@Override
	public String typeString() {
		return ":D";
	}

	@Override
	public VariableAssignment<?> createNew(String name, String value) {
		DoubleVariableAssignment dva = new DoubleVariableAssignment(name, min, max);
        dva.setParameter(isParameter());
        if(value == null)
            setNull(true);
        else if(value.trim().equals("*"))
            setNull(true);
        else if(value.trim().equals("E")) //special error value...
            dva.setValue(Double.MIN_VALUE);
        else
            dva.setStringValue(value);
		return dva;
	}
	
	@Override
	public VariableAssignment<Double> copy() {
		DoubleVariableAssignment copied = new DoubleVariableAssignment(name,value);
        copied.setParameter(isParameter());
		copied.setMax(max);
		copied.setMin(min);
        return copied;
	}

	@Override
	protected Double generateRandom() {
		return min + (max - min) * rand.nextDouble();
	}



	@Override
    protected void setToValue(Double value) {
        super.setToValue(round(value,3));
		if(value.isInfinite() || value.isNaN())
			value = 0D;
        if(enforcing) {
            if (value > max)
                this.value = max;
            else if (value < min)
                this.value = min;
        }
    }

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		if(Double.isNaN(value) || Double.isInfinite(value))
			return 0D;
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}


}
