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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.eng.spagobi.utilities.objects.Couple;

/**
 * @author Andrea Gioia
 */
public class ModelField extends AbstractModelNode implements IModelField {

	private boolean key;
	private String type;
	private int length;
	private int precision;
	private Class javaClass;

	// private String datamartName;

	protected ModelField() {
		// in order to let subclass in this package to relax constraints imposed by the public constructor
		// DataMartField(String name, DataMartEntity parent). Ex. DataMartCalculatedField
		// can be created by themself without a pre-existing parent entity.
		initProperties();
	}

	public ModelField(String name, IModelEntity parent) {
		setStructure(parent.getStructure());
		setId(getStructure().getNextId());
		setName(name);
		setParent(parent);
		initProperties();
	}

	@Override
	public String getUniqueName() {

		String parentViewName = getParent().getPropertyAsString("parentView");
		if (parentViewName != null) {
			return parentViewName + ":" + getParent().getType() + ":" + getName();
		}
		if (getParent().getParent() == null) {
			return getParent().getType() + ":" + getName();
		}
		return getParent().getUniqueName() + ":" + getName();
	}

	@Override
	public Couple getQueryName() {
		String fieldName = "";

		IModelEntity entity = getParent();
		if (entity.getParent() != null) {
			if (entity.getParent() instanceof ModelViewEntity) {
				fieldName = getName();
				return new Couple(fieldName, entity);
			}
			fieldName = toLowerCase(entity.getName());
			entity = entity.getParent();
		}
		while (entity.getParent() != null) {
			if (entity.getParent() instanceof ModelViewEntity) {
				if (!fieldName.equalsIgnoreCase(""))
					fieldName += ".";
				fieldName = fieldName + getName();
				return new Couple(fieldName, entity);
			}
			fieldName = toLowerCase(entity.getName()) + "." + fieldName;
			entity = entity.getParent();
		}
		if (!fieldName.equalsIgnoreCase(""))
			fieldName += ".";
		fieldName += getName();

		return new Couple(fieldName, null);
	}

	private String toLowerCase(String str) {
		/*
		 * String head = str.substring(0,1); String tail = str.substring(1, str.length());
		 *
		 * return head.toLowerCase() + tail;
		 */
		return str;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public void setLength(int length) {
		this.length = length;
	}

	@Override
	public int getPrecision() {
		return precision;
	}

	@Override
	public void setPrecision(int precision) {
		this.precision = precision;
	}

	@Override
	public boolean isKey() {
		return key;
	}

	@Override
	public void setKey(boolean key) {
		this.key = key;
	}

	@Override
	public String toString() {
		return getName() + "(id=" + getId() + "; parent:" + (getParent() == null ? "NULL" : getParent().getName()) + "; type=" + type + "; length=" + length
				+ "; precision=" + precision + ")";
	}

	@Override
	public IModelField clone(IModelEntity newParent) {
		IModelField field = new ModelField(name, newParent);
		field.setKey(this.key);
		field.setType(this.type);
		field.setLength(this.length);
		field.setPrecision(this.precision);
		Map<String, Object> properties2 = new HashMap<String, Object>();
		for (Iterator iterator = properties.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			String o = (String) properties.get(key);
			if (o == null) {
				properties2.put(key.substring(0), null);
			} else {
				properties2.put(key.substring(0), o.substring(0));
			}

		}
		field.setProperties(properties2);
		return field;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.qbe.model.structure.IModelNode#getParentViews()
	 */
	@Override
	public List<ModelViewEntity> getParentViews() {
		return super.getParentViews(this.getParent());
	}

	@Override
	public Class getJavaClass() {
		return javaClass;
	}

	@Override
	public void setJavaClass(Class javaClass) {
		this.javaClass = javaClass;

	}

	@Override
	public boolean isEncrypted() {
		String decryptValue = getPropertyAsString("decrypt");
		return new Boolean(decryptValue);
	}

}
