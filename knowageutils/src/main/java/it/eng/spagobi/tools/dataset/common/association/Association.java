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
package it.eng.spagobi.tools.dataset.common.association;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An association is a collection of fields belonging to different datasets/documents that refer to the same thing. The association represent a one to one
 * relationship between these datasets/documents. The fields defined in an association can be used to join together the datasets/documents.
 *
 * NOTE: It's not possible to define an association that use more than one field per dataset.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class Association implements Serializable {

	private static final long serialVersionUID = -2252418966402313311L;

	private String id;
	private String description;
	private final List<Field> fields;

	public Association(String id, String description) {
		this.id = id;
		this.description = description;
		this.fields = new ArrayList<>();
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
		for (Field field : fields) {
			if (field.getLabel().equals(dataset))
				return field;
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
	 * A filed have a unique name withing the dataset/document it belongs to
	 */
	public static class Field implements Serializable {

		private static final long serialVersionUID = -1897937148214003640L;

		public static final String DATASET_TYPE = "dataset";
		public static final String DOCUMENT_TYPE = "document";

		private String label;
		private String name;
		private String type;

		public Field(String label, String name, String type) {
			setLabel(label);
			setFieldName(name);
			setType(type);
		}

		public Field(String label, String name) {
			this(label, name, "");
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getFieldName() {
			return name;
		}

		public void setFieldName(String fieldName) {
			this.name = fieldName;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			if (type == null) {
				this.type = "";
			} else {
				this.type = type;
			}
		}

		public boolean hasDatasetType() {
			return type.equals(DATASET_TYPE);
		}

		public boolean hasDocumentType() {
			return type.equals(DOCUMENT_TYPE);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((label == null) ? 0 : label.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
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
			if (label == null) {
				if (other.label != null)
					return false;
			} else if (!label.equals(other.label))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}
	}

}
