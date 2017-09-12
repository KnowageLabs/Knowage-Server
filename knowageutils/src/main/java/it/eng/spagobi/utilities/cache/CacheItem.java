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
package it.eng.spagobi.utilities.cache;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */

public class CacheItem {

	private String name = null;
	private String signature = null;
	private String table = null;
	private BigDecimal dimension = null;
	private Date creationDate = null;
	private Date lastUsedDate = null;
	private Map<String, Object> properties = null;

	public CacheItem() {
		properties = new HashMap<String, Object>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public BigDecimal getDimension() {
		return dimension;
	}

	public void setDimension(BigDecimal dimension) {
		this.dimension = dimension;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastUsedDate() {
		return lastUsedDate;
	}

	public void setLastUsedDate(Date lastUsedDate) {
		this.lastUsedDate = lastUsedDate;
	}

	public Object getProperty(String propertyName) {
		return properties.get(propertyName);
	}

	public void setProperty(String propertyName, Object propertyValue) {
		properties.put(propertyName, propertyValue);
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
}
