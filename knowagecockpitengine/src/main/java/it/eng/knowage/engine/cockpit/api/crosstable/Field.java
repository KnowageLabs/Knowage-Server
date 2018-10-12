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
package it.eng.knowage.engine.cockpit.api.crosstable;

import org.json.JSONObject;

public class Field {
	String entityId = null;
	String alias = null;
	String sortingId = null;
	String iconCls = null;
	String nature = null;
	JSONObject config = null;

	public Field(String entityId, String alias, String sortingId, String iconCls, String nature, JSONObject config) {
		this.entityId = entityId;
		this.alias = alias;
		this.sortingId = sortingId;
		this.iconCls = iconCls;
		this.nature = nature;
		this.config = config;
	}

	public String getSortingId() {
		return sortingId;
	}

	public void setSortingId(String sortingId) {
		this.sortingId = sortingId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
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

	public JSONObject getConfig() {
		return config;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
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