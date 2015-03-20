/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.network.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class Node implements Serializable, Comparable<Node>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4431969032355898744L;
	private String id;
	private Map<String,String> properties = new HashMap<String, String>();
	
	

	public Node() {
		super();
	}
	
	/**
	 * @param id
	 */
	public Node(String id) {
		super();
		this.id = id;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperty(String propertyName, String PropertyValue) {
		this.properties.put(propertyName, PropertyValue);
	}

	public String getProperty(String propertyName) {
		return this.properties.get(propertyName);
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Node arg0) {
		return arg0.getId().compareTo(id);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Node other = (Node) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	

	
	
	

}
