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
public class Edge implements Serializable, Comparable<Edge>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7921048815814152256L;
	private String id;
	private Node sourceNode;
	private Node targetNode;
	private Map<String,String> properties = new HashMap<String, String>();


	
	/**
	 * @param id
	 * @param srcNode
	 * @param destNode
	 */
	public Edge(String id, Node srcNode, Node destNode) {
		super();
		this.id = id;
		this.sourceNode = srcNode;
		this.targetNode = destNode;
	}

	public Edge() {
		super();

	}
	
	public String getId() {
		return id;
	}
	
	public Node getSourceNode() {
		return sourceNode;
	}

	public Node getTargetNode() {
		return targetNode;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setSourceNode(Node sourceNode) {
		this.sourceNode = sourceNode;
	}

	public void setTargetNode(Node targetNode) {
		this.targetNode = targetNode;
	}

	public int compareTo(Edge arg0) {
		return arg0.getId().compareTo(id);
	}

	public void setProperty(String propertyName, String PropertyValue) {
		this.properties.put(propertyName, PropertyValue);
	}
	
	public String getProperty(String propertyName) {
		return this.properties.get(propertyName);
	}
	

	public Map<String, String> getProperties() {
		return properties;
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
		Edge other = (Edge) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	

	
	
}
