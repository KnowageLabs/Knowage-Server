/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.tools.dataset.common.association;

import java.util.ArrayList;
import java.util.List;

/**
 * An association is a collection of fields belonging to different datasets 
 * that refer to the same thing. The association represent a one to one
 * relationship between these datasets. The fields defined in an association
 * can be used to join together the datasets.
 * 
 * NOTE: It's not possible to define an association that use more than one field 
 * per dataset.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class Association {
	
	String id;
	String description;
	List<Field> fields;
	
	public Association(String id, String description) {
		this.id = id;
		this.description = description;
		this.fields = new ArrayList<Field>();
	}
	
	public Association(String id, String description, List<Field> fields) {
		this.id = id;
		this.description = description;
		this.fields = fields;
	}
		
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Field> getFields() {
		return fields;
	}
	
	public Field getField(String dataset) {
		for(Field field : fields) {
			if(field.getDataSetLabel().equals(dataset)) return field;
		}
		return null;
	}
	
	public boolean containsDataset(String dataset) {
		return getField(dataset) != null;
	}
	
	public void addField(Field field) {
		this.fields.add(field);
	}
	
	public void addFields(List<Field> fields) {
		this.fields.addAll(fields);
	}
	
	/**
	 * A filed have a unique name withing the dataset it belongs to
	 */
	public static class Field {
		String dataSetLabel;
		String name;
		
		public Field(String dataSetLabel, String name) {
			setDataSetLabel(dataSetLabel);
			setFieldName(name);
		}
		
		public String getDataSetLabel() {
			return dataSetLabel;
		}

		public void setDataSetLabel(String dataSetLabel) {
			this.dataSetLabel = dataSetLabel;
		}

		public String getFieldName() {
			return name;
		}

		public void setFieldName(String fieldName) {
			this.name = fieldName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((dataSetLabel == null) ? 0 : dataSetLabel.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
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
			Field other = (Field) obj;
			if (dataSetLabel == null) {
				if (other.dataSetLabel != null)
					return false;
			} else if (!dataSetLabel.equals(other.dataSetLabel))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}

		
}
