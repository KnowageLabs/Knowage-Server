/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.qbe.model.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Andrea Gioia
 */
public class ModelCalculatedField extends ModelField {
	
	String expression;
	boolean inLine;
	String nature;
	List<Slot> slots;
	String defaultSlotValue;


	public ModelCalculatedField(String name, String type, String expression) {
		setName(name);
		setType(type);
		setExpression(expression);
		inLine = false;
		slots = new ArrayList<Slot>();
		initProperties();
	}
	
	public ModelCalculatedField(String name, String type, String expression, boolean inLine) {
		setName(name);
		setType(type);
		setExpression(expression);
		this.inLine = inLine;
		slots = new ArrayList<Slot>();
	}
	
	public ModelCalculatedField(String name, IModelEntity parent, String type, String expression) {
		super(name, parent);
		setType(type);
		setExpression(expression);
		slots = new ArrayList<Slot>();
	}
	
	public String getNature() {
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public boolean hasSlots() {
		return slots.size() > 0;
	}
	
	public void addSlot(Slot slot) {
		slots.add(slot);
	}
	
	public void addSlots(List<Slot> slots) {
		this.slots.addAll(slots);
	}
	
	public List<Slot> getSlots() {
		return slots;
	}
	
	public String getDefaultSlotValue() {
		return defaultSlotValue;
	}

	public void setDefaultSlotValue(String defaultSlotValue) {
		this.defaultSlotValue = defaultSlotValue;
	}
	
	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}	
	
	public boolean isBoundToDataMart() {
		return getStructure() != null && getParent() != null;
	}

	public boolean isInLine() {
		return inLine;
	}

	public void setInLine(boolean inLine) {
		this.inLine = inLine;
	}
	
	public IModelField clone(IModelEntity newParent){
		IModelField field = new ModelCalculatedField(expression, newParent, getType(), expression);
		field.setProperties(properties);
		return field;
	}
	
	public static class Slot {
		String name;
		List<IMappedValuesDescriptor> mappedValues;
		
		public interface IMappedValuesDescriptor {}
		
		public static class MappedValuesRangeDescriptor implements IMappedValuesDescriptor {
			public String minValue;
			public boolean includeMinValue;
			public String maxValue;
			public boolean includeMaxValue;
			
			public MappedValuesRangeDescriptor(String minValue, String maxValue) {
				this.minValue = minValue;
				includeMinValue = true;
				this.maxValue = maxValue;
				includeMaxValue = false;
			}
			
			
			public String getMinValue() { return minValue; }
			public void setMinValue(String minValue) { this.minValue = minValue; } 
			public boolean isIncludeMinValue() { return includeMinValue; }
			public void setIncludeMinValue(boolean includeMinValue) { this.includeMinValue = includeMinValue; }
			public String getMaxValue() { return maxValue; } 
			public void setMaxValue(String maxValue) { this.maxValue = maxValue; } 
			public boolean isIncludeMaxValue() { return includeMaxValue; }
			public void setIncludeMaxValue(boolean includeMaxValue) { this.includeMaxValue = includeMaxValue; }
		}
		
		public static class MappedValuesPunctualDescriptor implements IMappedValuesDescriptor {
			public Set<String> punctualValues;
			
			public MappedValuesPunctualDescriptor() {
				punctualValues = new HashSet();
			}
			
			public void addValue(String v) { punctualValues.add(v); }
			public Set<String> getValues() { return punctualValues; }
		}
		
		
		public Slot(String value) {
			this.name = value;
			mappedValues = new ArrayList<IMappedValuesDescriptor>();
		}
		
		public void addMappedValuesDescriptors(IMappedValuesDescriptor descriptor) {
			mappedValues.add(descriptor);
		}
		
		public List<IMappedValuesDescriptor> getMappedValuesDescriptors() {
			return mappedValues;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
		
	}
	
	
}
