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

package it.eng.spagobi.cache.metadata;

// Generated 21-mag-2015 12.23.29 by Hibernate Tools 3.4.0.CR1

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import java.util.Date;

/**
 * SbiCacheItem generated by hbm2java
 */
public class SbiCacheItem extends SbiHibernateModel {

	private String tableName;
	private String signature;
	private String name;
	private Long dimension;
	private Date creationDate;
	private Date lastUsedDate;
	private String properties;
	private String parameters;

	public SbiCacheItem() {
	}

	public SbiCacheItem(String tableName, String signature, String name) {
		this.tableName = tableName;
		this.signature = signature;
		this.name = name;
	}

	public SbiCacheItem(String tableName, String signature, String name, Long dimension, Date creationDate, Date lastUsedDate, String properties, String parameters) {
		this.tableName = tableName;
		this.signature = signature;
		this.name = name;
		this.dimension = dimension;
		this.creationDate = creationDate;
		this.lastUsedDate = lastUsedDate;
		this.properties = properties;
		this.parameters = parameters;
	}

	public SbiCacheItem(String signature) {
		this.signature = signature;
	}
	
	public String getTableName() {
		return this.tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getSignature() {
		return this.signature;
	}

	private void setSignature(String signature) {
		this.signature = signature;
	}

	public void changeSignature(String signature) {
		this.setSignature(signature);
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getDimension() {
		return this.dimension;
	}

	public void setDimension(Long dimension) {
		this.dimension = dimension;
	}

	public Date getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastUsedDate() {
		return this.lastUsedDate;
	}

	public void setLastUsedDate(Date lastUsedDate) {
		this.lastUsedDate = lastUsedDate;
	}

	public String getProperties() {
		return this.properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}

	public String getParameters() { return this.parameters; }

	public void setParameters(String parameters) { this.parameters = parameters; }
}
