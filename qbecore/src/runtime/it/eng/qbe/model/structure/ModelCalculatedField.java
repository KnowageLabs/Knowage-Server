/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
