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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import mint.Configuration;

/**
 * Store variable assignments of a given type. Instances of this type might
 * either represent specific variables, or types / variable ranges, which might
 * be placeholders.
 *
 * Values can be read from String values and are parsed according to sub-class
 * specific routines. Universally, A `*' represents a wild-card, in which case
 * the internal value is set to null.
 *
 * Variables are by default set *not* to be parameter variables. This can be set
 * however.
 *
 */
public abstract class VariableAssignment<T> {

	protected String name;
	protected int id;
	protected T value;
	protected Collection<T> from;
	private boolean isNull, parameter, restricted;
	static Random rand = new Random(Configuration.getInstance().SEED);
	protected static int idcounter = 0;
	private final static Logger LOGGER = Logger.getLogger(VariableAssignment.class.getName());

	public abstract List<T> getValues();

	public abstract void addValue(T v);

	/**
	 * Create a parameter with a set value
	 * 
	 * @param name
	 * @param value
	 */
	public VariableAssignment(String name, T value) {
		this.name = name;
		this.id = idcounter++;
		if (value != null)
			this.isNull = false;
		else
			this.isNull = true;
		this.from = new HashSet<T>();
		this.parameter = false;
		this.restricted = false;
		setValue(value);
	}

	/**
	 * Create a parameter with a set value
	 * 
	 * @param name
	 * @param value
	 * @param add   - whether or not to add the given value to the value set
	 */
	public VariableAssignment(String name, T value, boolean add) {
		if (add)
			addValue(value);
		this.name = name;
		this.id = idcounter++;
		if (value != null)
			this.isNull = false;
		else
			this.isNull = true;
		this.from = new HashSet<T>();
		this.parameter = false;
		this.restricted = false;
		setValue(value);
	}

	/**
	 * Create a null parameter (useful for specifying types only).
	 * 
	 * @param name
	 */
	public VariableAssignment(String name) {
		this.name = name;
		this.id = idcounter++;
		this.isNull = true;
		this.from = new HashSet<T>();
		this.parameter = false;
		this.restricted = false;
	}

	/**
	 * Create a null parameter with a fixed range of values from which to select.
	 * 
	 * @param name
	 * @param values
	 */
	public VariableAssignment(String name, Collection<T> values) {
		this.name = name;
		this.id = idcounter++;
		this.isNull = true;
		this.from = values;
		this.parameter = false;
		this.restricted = true;
	}

	/**
	 * Set whether or not this variable represents a parameter (i.e. given as
	 * input).
	 * 
	 * @param param
	 */
	public void setParameter(boolean param) {
		this.parameter = param;
	}

	/**
	 * Is this variable a parameter value (i.e. given as input)? Or is it computed
	 * as part of the execution?
	 */
	public boolean isParameter() {
		return parameter;
	}

	/**
	 * Is this variable restricted to a particular set of values?
	 * 
	 * @return
	 */
	protected boolean isRestricted() {
		return restricted;
	}

	/**
	 * Set variable to a random value. If set of values is restricted, it will do so
	 * by selecting a random set value. Otherwise the random generation will be
	 * delegated to an appropriate subclass via the generateRandom abstract
	 * function.
	 */
	public void setToRandom() {
		if (isRestricted()) {
			List<T> fromList = new ArrayList<T>();
			fromList.addAll(from);
			setValue(fromList.get(rand.nextInt(fromList.size())));
		} else
			setValue(generateRandom());

	}

	public abstract VariableAssignment<?> createNew(String name, String value);

	/*
	 * Getters & setters.
	 */
	public int getID() {
		return id;
	}

	public void setRange(Collection<T> range) {
		this.from = range;
		if (from == null)
			return;
		if (!from.isEmpty())
			this.restricted = true;
	}

	public String getName() {
		return name;
	}

	public T getValue() {
		if (isNull)
			return null;
		return value;
	}

	public void setValue(T value) {
		if (value instanceof String) {
			String s = (String) value;
			if (s.trim().equals("*"))
				setNull(true);
			else {
				setStringValue(s);
				setNull(false);
			}
		} else if (value != null) {
			setToValue(value);

		}
	}

	protected void setToValue(T value) {
		if (isRestricted()) {
//            assert(from.contains(value));
			if (!from.contains(value)) {
				LOGGER.error("Variable " + getName() + " set to value that does not belong to restricted set.");
				return;
			}
		}
		this.value = value;
		setNull(false);
	}

	public void setNull(boolean isNull) {
		this.isNull = isNull;
	}

	public boolean isNull() {
		return isNull;
	}

	public abstract void setStringValue(String s);

	@Override
	public String toString() {
		String retString = name;
		if (value != null)
			retString += "=" + printableStringOfValue();
		return retString;
	}

	public abstract String printableStringOfValue();

	public abstract String typeString();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + (isNull ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariableAssignment<?> other = (VariableAssignment<?>) obj;
		if (id != other.id)
			return false;
		if (isNull != other.isNull)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		else if (!parameter == other.parameter)
			return false;
		return true;
	}

	public abstract VariableAssignment<T> copy();

	protected abstract T generateRandom();

	public boolean withinLimits() {
		return true;
	}

	/**
	 * Records that this type has been assigned the given value.
	 *
	 * IMPORTANT SIDE-EFFECT: Will affect subsequent calls to setRandom, as these
	 * will be chosen from the pool of given values.
	 *
	 * @param value
	 */
	public void recordValue(T value) {
		from.add(value);
	}

	public Collection<T> getPriors() {
		return from;
	}

	public void setPriors(Collection<T> from) {
		this.from = from;
	}
}
