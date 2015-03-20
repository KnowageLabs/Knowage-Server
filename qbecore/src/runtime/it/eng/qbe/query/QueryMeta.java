/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.query;

import java.util.Properties;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class QueryMeta {
	
	String id;
	String name;
	String description;
	String scope;
	String owner;
	Properties properties;
	
	public QueryMeta(String name) {
		setName(name);
		properties = new Properties();
	}
	
	public QueryMeta() {
		properties = new Properties();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	
	public String getProperty(String pname) {
		return properties.getProperty(pname);
	}
	
	public void setProperty(String pname, String pvalue) {
		properties.setProperty(pname, pvalue);
	}
	
}
