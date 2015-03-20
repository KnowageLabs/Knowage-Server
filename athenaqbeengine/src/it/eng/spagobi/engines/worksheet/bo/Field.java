/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.bo;

public class Field {
	String entityId = null;
	String alias = null;
	String iconCls = null;
	String nature = null;
	public Field(String entityId, String alias, String iconCls, String nature) {
		this.entityId = entityId;
		this.alias = alias;
		this.iconCls = iconCls;
		this.nature = nature;
	}
	public String getEntityId() {
		return entityId;
	}
	public String getAlias() {
		return alias;
	}
	public String getIconCls() {
		return iconCls;
	}
	public String getNature() {
		return nature;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result
				+ ((entityId == null) ? 0 : entityId.hashCode());
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
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (entityId == null) {
			if (other.entityId != null)
				return false;
		} else if (!entityId.equals(other.entityId))
			return false;
		return true;
	}
}